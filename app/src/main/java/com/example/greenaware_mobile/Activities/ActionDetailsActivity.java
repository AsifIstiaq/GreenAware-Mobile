package com.example.greenaware_mobile.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.greenaware_mobile.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActionDetailsActivity extends AppCompatActivity {

    private TextView txtLocation, txtDescription, txtDeadline, txtStatus;

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
        String status = doc.getString("status");
        txtStatus.setText(status);

        switch (status) {
            case "PENDING":
                txtStatus.setTextColor(Color.parseColor("#FFA726"));
                break;
            case "IN_PROGRESS":
                txtStatus.setTextColor(Color.parseColor("#29B6F6"));
                break;
            case "RESOLVED":
                txtStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            default:
                txtStatus.setTextColor(Color.parseColor("#388E3C"));
                break;
        }
    }
}
