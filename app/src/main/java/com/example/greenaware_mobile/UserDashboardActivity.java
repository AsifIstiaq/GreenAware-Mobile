package com.example.greenaware_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddReport;
    private UserReportAdapter adapter;
    private List<ReportModel> reportList;

    private FirebaseFirestore db;
    private Map<String, String> lastStatusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        recyclerView = findViewById(R.id.recyclerReports);
        btnAddReport = findViewById(R.id.btnAddReport);

        db = FirebaseFirestore.getInstance();
        String userId = UserSession.getInstance().getUserId();

        db.collection("reports")
                .whereEqualTo("user_id", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots == null) return;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String reportId = doc.getId();
                        String newStatus = doc.getString("status");
                        String lastStatus = lastStatusMap.get(reportId);

                        String location = doc.getString("location");
                        String dateReported = doc.getString("date_reported");

                        if (lastStatus != null && !lastStatus.equals(newStatus)) {
                            String message = "";

                            if (lastStatus.equals("PENDING") && newStatus.equals("IN_PROGRESS")) {
                                message = "Your report at " + location + " reported on " + dateReported + " is now in progress.";
                            } else if (lastStatus.equals("IN_PROGRESS") && newStatus.equals("RESOLVED")) {
                                message = "Your report at " + location + " reported on " + dateReported + " has been resolved.";
                            }

                            if (!message.isEmpty()) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        "Report Update",
                                        message
                                );
                            }
                        }

                        lastStatusMap.put(reportId, newStatus);
                    }
                });

        reportList = new ArrayList<>();

        adapter = new UserReportAdapter(this, reportList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAddReport.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, AddReportActivity.class));
        });

        fetchReports();
    }

    private void fetchReports() {
        String userId = UserSession.getInstance().getUserId();

        db.collection("reports")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(this::onReportsFetched)
                .addOnFailureListener(e -> toast("Failed to load reports: " + e.getMessage()));
    }

    private void onReportsFetched(QuerySnapshot snapshots) {
        reportList.clear();

        for (DocumentSnapshot doc : snapshots) {

            String docId = doc.getId();

            String category = doc.contains("category_name")
                    ? doc.getString("category_name")
                    : "Unknown";

            String location = doc.contains("location")
                    ? doc.getString("location")
                    : "Unknown";

            String status = doc.contains("status")
                    ? doc.getString("status")
                    : "PENDING";

            ReportModel model = new ReportModel(
                    docId,
                    category,
                    location,
                    status
            );

            reportList.add(model);
        }

        adapter.notifyDataSetChanged();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchReports();
    }
}
