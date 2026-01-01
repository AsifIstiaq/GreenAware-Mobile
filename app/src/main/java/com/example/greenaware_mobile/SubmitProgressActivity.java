package com.example.greenaware_mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SubmitProgressActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;

    private Spinner spinnerStatus;
    private EditText editDescription;
    private ImageView imgPreview;
    private Button btnChooseImage, btnSubmit;

    private Uri imageUri;
    private String actionId;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_progress);

        spinnerStatus = findViewById(R.id.spinnerStatus);
        editDescription = findViewById(R.id.editDescription);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSubmit = findViewById(R.id.btnSubmitProgress);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        actionId = getIntent().getStringExtra("ACTION_ID");

        setupSpinner();

        btnChooseImage.setOnClickListener(v -> chooseImage());
        btnSubmit.setOnClickListener(v -> submitProgress());
    }

    private void setupSpinner() {
        String[] statuses = {"PENDING", "IN_PROGRESS", "COMPLETED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgPreview.setImageURI(imageUri);
        }
    }

    private void submitProgress() {

        if (imageUri == null) {
            toast("Please select an image");
            return;
        }

        String description = editDescription.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (description.isEmpty()) {
            toast("Description required");
            return;
        }

        String fileName = "progress_photos/" + System.currentTimeMillis() + ".jpg";
        StorageReference imgRef = storageRef.child(fileName);

        imgRef.putFile(imageUri)
                .addOnSuccessListener(task ->
                        imgRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveProgress(uri.toString(), status, description)))
                .addOnFailureListener(e -> toast(e.getMessage()));
    }

    private void saveProgress(String photoPath, String status, String description) {

        WorkerSession session = WorkerSession.getInstance();

        Map<String, Object> progress = new HashMap<>();
        progress.put("action_id", actionId);
        progress.put("description", description);
        progress.put("status", status);
        progress.put("photo_path", photoPath);
        progress.put("worker_id", session.getWorkerId());
        progress.put("worker_name", session.getName());
        progress.put("worker_phone", session.getPhone());
        progress.put("submitted_at", LocalDateTime.now().toString());

        db.collection("work_progress")
                .add(progress)
                .addOnSuccessListener(doc -> updateActionStatus(status))
                .addOnFailureListener(e -> toast(e.getMessage()));
    }

    private void updateActionStatus(String status) {

        Map<String, Object> update = new HashMap<>();
        update.put("status", status);

        if ("COMPLETED".equals(status)) {
            update.put("completed_date", LocalDateTime.now().toString());
        }

        db.collection("actions")
                .document(actionId)
                .update(update)
                .addOnSuccessListener(v -> {
                    toast("Progress submitted successfully");
                    finish();
                })
                .addOnFailureListener(e -> toast(e.getMessage()));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
