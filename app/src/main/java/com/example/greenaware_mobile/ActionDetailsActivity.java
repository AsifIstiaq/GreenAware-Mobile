package com.example.greenaware_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActionDetailsActivity extends AppCompatActivity {

    private TextView txtLocation, txtDescription, txtDeadline, txtStatus,
            txtWorkerName, txtCreatedAt, txtCompletedDate, txtResolution;

    private Button btnSubmitProgress;

    private FirebaseFirestore db;
    private String actionId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_details);

        txtLocation = findViewById(R.id.txtLocation);
        txtDescription = findViewById(R.id.txtDescription);
        txtDeadline = findViewById(R.id.txtDeadline);
        txtStatus = findViewById(R.id.txtStatus);
        txtWorkerName = findViewById(R.id.txtWorkerName);
        txtCreatedAt = findViewById(R.id.txtCreatedAt);
        txtCompletedDate = findViewById(R.id.txtCompletedDate);
        txtResolution = findViewById(R.id.txtResolution);
        btnSubmitProgress = findViewById(R.id.btnSubmitProgress);

        db = FirebaseFirestore.getInstance();

        actionId = getIntent().getStringExtra("ACTION_ID");

        if (actionId == null) {
            Toast.makeText(this, "Invalid action", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadActionDetails();

        btnSubmitProgress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmitProgressActivity.class);
            intent.putExtra("ACTION_ID", actionId);
            startActivity(intent);
        });
    }

    private void loadActionDetails() {

        db.collection("actions")
                .document(actionId)
                .get()
                .addOnSuccessListener(this::bindData)
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void bindData(DocumentSnapshot doc) {

        if (!doc.exists()) {
            Toast.makeText(this, "Action not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtLocation.setText(doc.getString("location"));
        txtDescription.setText(doc.getString("action_note"));
        txtDeadline.setText(doc.getString("deadline"));
        txtStatus.setText(doc.getString("status"));
        txtWorkerName.setText(doc.getString("worker_name"));
        txtCreatedAt.setText(doc.getString("created_at"));

        String completedDate = doc.getString("completed_date");
        txtCompletedDate.setText(
                completedDate == null ? "Not completed yet" : completedDate
        );

        txtResolution.setText(doc.getString("resolution_details"));
    }
}
