package com.example.greenaware_mobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ReportDetailsActivity extends AppCompatActivity {

    private TextView tvCategory, tvLocation, tvDateReported, tvSeverity,
            tvDescription, tvStatus;
    private ImageView imgUserPhoto, imgFinalPhoto;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

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
}
