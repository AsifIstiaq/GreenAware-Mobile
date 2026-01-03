package com.example.greenaware_mobile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class ReportDetailsActivity extends AppCompatActivity {

    private TextView tvCategory, tvLocation, tvDateReported, tvSeverity,
            tvDescription, tvStatus;
    private ImageView imgUserPhoto, imgFinalPhoto;

    private FirebaseFirestore db;
    private ListenerRegistration reportListener;
    private String currentStatus = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        tvCategory = findViewById(R.id.tvCategory);
        tvLocation = findViewById(R.id.tvLocation);
        tvDateReported = findViewById(R.id.tvDateReported);
        tvSeverity = findViewById(R.id.tvSeverity);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);

        imgUserPhoto = findViewById(R.id.imgUserPhoto);
        imgFinalPhoto = findViewById(R.id.imgFinalPhoto);

        db = FirebaseFirestore.getInstance();

        loadReport();
    }

    private void loadReport() {
        String reportId = getIntent().getStringExtra("REPORT_ID");

        if (reportId == null) {
            Toast.makeText(this, "Report ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("reports")
                .document(reportId)
                .get()
                .addOnSuccessListener(this::displayReport)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });

        DocumentReference reportRef = db.collection("reports").document(reportId);

        reportListener = reportRef.addSnapshotListener((doc, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (doc != null && doc.exists()) {
                displayReport(doc);
                checkStatusChange(doc.getString("status"), doc.getString("location"), doc.getString("date_reported"));
            }
        });

    }

    private void checkStatusChange(String newStatus, String location, String dateReported) {
        if (newStatus == null || newStatus.equals(currentStatus)) return;

        String message = "";

        if (currentStatus.equals("PENDING") && newStatus.equals("IN_PROGRESS")) {
            message = "Your report at " + location + " reported on " + dateReported + " is now in progress.";
        } else if (currentStatus.equals("IN_PROGRESS") && newStatus.equals("RESOLVED")) {
            message = "Your report at " + location + " reported on " + dateReported + " has been resolved.";
        }

        if (!message.isEmpty()) {
            NotificationHelper.showNotification(
                    getApplicationContext(),
                    "Report Update",
                    message
            );
        }

        currentStatus = newStatus;
    }

    private void displayReport(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCategory.setText(doc.getString("category_name"));
        tvLocation.setText(doc.getString("location"));
        tvDateReported.setText(doc.getString("date_reported"));
        tvSeverity.setText(doc.getString("severity"));
        tvDescription.setText(doc.getString("description"));
        tvStatus.setText(doc.getString("status"));

        String userPhoto = doc.getString("image_path");
        if (userPhoto != null && !userPhoto.isEmpty()) {
            Glide.with(this)
                    .load(userPhoto)
                    .into(imgUserPhoto);
        }

        String finalPhoto = doc.getString("final_photo_path");
        if (finalPhoto != null && !finalPhoto.isEmpty()) {
            Glide.with(this)
                    .load(finalPhoto)
                    .into(imgFinalPhoto);
        } else {
            imgFinalPhoto.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportListener != null) reportListener.remove();
    }
}
