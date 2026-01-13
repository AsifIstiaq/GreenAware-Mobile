package com.example.greenaware_mobile.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenaware_mobile.Helpers.NotificationHelper;
import com.example.greenaware_mobile.R;
import com.example.greenaware_mobile.Models.ReportModel;
import com.example.greenaware_mobile.Adapters.UserReportAdapter;
import com.example.greenaware_mobile.Session.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddReport, btnLogout;
    private UserReportAdapter adapter;
    private List<ReportModel> reportList;

    private TextView tvTotalReports, tvPending, tvInProgress, tvResolved;

    private FirebaseFirestore db;
    private Map<String, String> lastStatusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        recyclerView = findViewById(R.id.recyclerReports);
        btnAddReport = findViewById(R.id.btnAddReport);
        btnLogout = findViewById(R.id.btnLogout);

        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvPending = findViewById(R.id.tvPending);
        tvInProgress = findViewById(R.id.tvInProgress);
        tvResolved = findViewById(R.id.tvResolved);

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

        btnLogout.setOnClickListener(v -> {
            UserSession.getInstance().clearSession();
            startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
            finish();
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

        int pending = 0, inProgress = 0, resolved = 0;

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

            if ("PENDING".equals(status)) pending++;
            else if ("IN_PROGRESS".equals(status)) inProgress++;
            else if ("RESOLVED".equals(status)) resolved++;

            ReportModel model = new ReportModel(
                    docId,
                    category,
                    location,
                    status
            );

            reportList.add(model);
        }

        tvTotalReports.setText(String.valueOf(reportList.size()));
        tvPending.setText(String.valueOf(pending));
        tvInProgress.setText(String.valueOf(inProgress));
        tvResolved.setText(String.valueOf(resolved));

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
