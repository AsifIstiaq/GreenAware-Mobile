package com.example.greenaware_mobile;

import android.app.DatePickerDialog;
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
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Calendar;
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

public class AddReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;
    private static final int TAKE_IMAGE = 102;

    private Spinner spinnerCategory, spinnerSeverity;
    private EditText editLocation, editDateObserved, editDescription, editName, editEmail;
    private ImageView imgPreview;
    private Button btnChooseImage, btnTakePhoto, btnSubmitReport;

    private Uri imageUri;
    private Uri cameraImageUri;

    private FirebaseFirestore db;

    private final String CLOUD_NAME = "dabmwyr02";
    private final String UPLOAD_PRESET = "mobile_upload";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSeverity = findViewById(R.id.spinnerSeverity);
        editLocation = findViewById(R.id.editLocation);
        editDateObserved = findViewById(R.id.editDateObserved);
        editDescription = findViewById(R.id.editDescription);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);

        db = FirebaseFirestore.getInstance();

        setupSpinners();
        setupUserInfo();
        setupDatePicker();

        btnChooseImage.setOnClickListener(v -> chooseImage());
        btnTakePhoto.setOnClickListener(v -> takeImage());
        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void setupSpinners() {
        String[] categories = {"Air pollution", "Noise pollution", "Water pollution"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories));

        String[] severities = {"LOW", "MEDIUM", "HIGH"};
        spinnerSeverity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, severities));
    }

    private void setupUserInfo() {
        editName.setText(UserSession.getInstance().getName());
        editEmail.setText(UserSession.getInstance().getEmail());
    }

    private void setupDatePicker() {
        editDateObserved.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) ->
                            editDateObserved.setText((month + 1) + "/" + dayOfMonth + "/" + year),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void takeImage() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File imageFile = File.createTempFile("camera_", ".jpg", getCacheDir());
                cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(intent, TAKE_IMAGE);
            } catch (Exception e) {
                toast("Camera error: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                imageUri = data.getData();
                imgPreview.setImageURI(imageUri);
            } else if (requestCode == TAKE_IMAGE) {
                imageUri = cameraImageUri;
                imgPreview.setImageURI(imageUri);
            }
        }
    }

    private void submitReport() {
        if (imageUri == null) {
            toast("Please select an image");
            return;
        }

        String category = spinnerCategory.getSelectedItem().toString();
        String location = editLocation.getText().toString().trim();
        String dateObserved = editDateObserved.getText().toString().trim();
        String severity = spinnerSeverity.getSelectedItem().toString();
        String description = editDescription.getText().toString().trim();

        if(category.isEmpty() || location.isEmpty() || dateObserved.isEmpty() || severity.isEmpty() || description.isEmpty()) {
            toast("Fill all fields");
            return;
        }

        uploadToCloudinary(imageUri, category, location, dateObserved, severity, description);
    }

    private void uploadToCloudinary(Uri imageUri, String category, String location,
                                    String dateObserved, String severity, String description) {
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
                    JSONObject json = null;
                    try {
                        json = new JSONObject(resp);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    String imageUrl = null;
                    try {
                        imageUrl = json.getString("secure_url");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    String finalImageUrl = imageUrl;
                    runOnUiThread(() ->
                            saveReport(finalImageUrl, category, location, dateObserved, severity, description));
                }
            });

        } catch (Exception e) {
            toast(e.getMessage());
        }
    }

    private void saveReport(String imageUrl,
                            String category,
                            String location,
                            String dateObserved,
                            String severity,
                            String description) {

        UserSession session = UserSession.getInstance();

        Map<String, Object> report = new HashMap<>();

        DocumentReference reportRef = db.collection("reports").document();
        String reportId = reportRef.getId();

        report.put("id", reportId);
        report.put("user_id", session.getUserId());
        report.put("reporter_name", session.getName());
        report.put("category_name", category);
        report.put("incident_type", null);
        report.put("location", location);
        report.put("date_reported", dateObserved);
        report.put("severity", severity);
        report.put("description", description);
        report.put("image_path", imageUrl);
        report.put("final_photo_path", null);
        report.put("status", "PENDING");
        report.put("created_at", LocalDateTime.now().toString());

        db.collection("reports")
                .add(report)
                .addOnSuccessListener(docRef -> {
                    db.collection("reports")
                            .document(docRef.getId())
                            .update("id", docRef.getId());

                    toast("Report submitted successfully");
                    finish();
                })
                .addOnFailureListener(e -> toast(e.getMessage()));
    }


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
