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
import org.json.JSONObject;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SubmitProgressActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;

    private Spinner spinnerStatus;
    private EditText editDescription;
    private ImageView imgPreview;
    private Button btnChooseImage, btnSubmit;

    private Uri imageUri;
    private String actionId;

    private FirebaseFirestore db;

    private final String CLOUD_NAME = "dabmwyr02";
    private final String UPLOAD_PRESET = "mobile_upload";

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

        uploadToCloudinary(imageUri, description, status);
    }

    private void uploadToCloudinary(Uri imageUri, String description, String status) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> toast("Upload failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> toast("Upload failed: " + response.message()));
                        return;
                    }

                    String resp = response.body().string();
                    try {
                        JSONObject json = new JSONObject(resp);
                        String imageUrl = json.getString("secure_url"); // direct URL

                        runOnUiThread(() -> saveProgress(imageUrl, status, description));

                    } catch (Exception e) {
                        runOnUiThread(() -> toast("Upload parse error: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            toast(e.getMessage());
        }
    }

    private void saveProgress(String photoUrl, String status, String description) {

        WorkerSession session = WorkerSession.getInstance();

        Map<String, Object> progress = new HashMap<>();
        progress.put("action_id", actionId);
        progress.put("description", description);
        progress.put("status", status);
        progress.put("photo_path", photoUrl);
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
