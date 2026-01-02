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
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddReport;
    private UserReportAdapter adapter;
    private List<ReportModel> reportList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        recyclerView = findViewById(R.id.recyclerReports);
        btnAddReport = findViewById(R.id.btnAddReport);

        db = FirebaseFirestore.getInstance();
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
