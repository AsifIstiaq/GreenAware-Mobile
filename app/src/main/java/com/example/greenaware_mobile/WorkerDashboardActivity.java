package com.example.greenaware_mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeWorker, tvWorkerFullName, tvWorkerUsername, tvWorkerEmail, tvWorkerPhone, tvSpecialization;
    private EditText editWorkLocation, editWorkProgress;
    private Button btnUploadPhoto, btnSubmitProgress, btnWorkerLogout;
    private ImageView imgPreview;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        tvWelcomeWorker = findViewById(R.id.tvWelcomeWorker);
        tvWorkerFullName = findViewById(R.id.tvWorkerFullName);
        tvWorkerUsername = findViewById(R.id.tvWorkerUsername);
        tvWorkerEmail = findViewById(R.id.tvWorkerEmail);
        tvWorkerPhone = findViewById(R.id.tvWorkerPhone);
        tvSpecialization = findViewById(R.id.tvSpecialization);
        editWorkLocation = findViewById(R.id.editWorkLocation);
        editWorkProgress = findViewById(R.id.editWorkProgress);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnSubmitProgress = findViewById(R.id.btnSubmitProgress);
        btnWorkerLogout = findViewById(R.id.btnWorkerLogout);
        imgPreview = findViewById(R.id.imgPreview);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        String username = getIntent().getStringExtra("username");
        if (username == null) username = "";

        loadWorkerData(username);

        btnUploadPhoto.setOnClickListener(v -> openImagePicker());
        String finalUsername = username;
        btnSubmitProgress.setOnClickListener(v -> submitProgress(finalUsername));
        btnWorkerLogout.setOnClickListener(v -> {
            startActivity(new Intent(WorkerDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadWorkerData(String username) {
        db.collection("workers")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    if (documents.isEmpty()) {
                        Toast.makeText(this, "Worker data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = documents.get(0);

                    String fullName = doc.getString("name");
                    String email = doc.getString("email");
                    String phone = doc.getString("phone");
                    String specialization = doc.getString("specialization");

                    tvWelcomeWorker.setText("Welcome, " + fullName);
                    tvWorkerFullName.setText(fullName);
                    tvWorkerUsername.setText(username);
                    tvWorkerEmail.setText(email);
                    tvWorkerPhone.setText(phone);
                    tvSpecialization.setText(specialization);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load worker data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgPreview.setImageURI(selectedImageUri);
            imgPreview.setVisibility(View.VISIBLE);
        }
    }

    private void submitProgress(String username) {
        String location = editWorkLocation.getText().toString().trim();
        String progressText = editWorkProgress.getText().toString().trim();

        if(location.isEmpty() || progressText.isEmpty()){
            Toast.makeText(this, "Please fill work location and progress details", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("worker_username", username);
        progressData.put("location", location);
        progressData.put("progress_text", progressText);
        progressData.put("timestamp", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Instant.now().toString() : "");

        if(selectedImageUri != null){
            String fileName = "worker_progress_images/" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                progressData.put("photo_url", uri.toString());
                                saveProgress(progressData);
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed: "+e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            progressData.put("photo_url", "");
            saveProgress(progressData);
        }
    }

    private void saveProgress(Map<String,Object> progressData){
        db.collection("worker_progress")
                .add(progressData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,"Progress submitted successfully", Toast.LENGTH_SHORT).show();
                    editWorkLocation.setText("");
                    editWorkProgress.setText("");
                    selectedImageUri = null;
                    imgPreview.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this,"Failed to submit progress: "+e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
