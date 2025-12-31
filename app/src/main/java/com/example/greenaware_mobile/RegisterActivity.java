package com.example.greenaware_mobile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editFullName, editUsername, editEmail, editPhone, editPassword;
    private RadioGroup radioRole;
    private RadioButton radioUser, radioWorker;
    private Spinner spinnerSpecialization;
    private Button btnRegister;

    private FirebaseFirestore db;

    private final String[] SPECIALIZATIONS = {
            "Waste Management",
            "Water Treatment",
            "Air Quality",
            "Landscaping",
            "Tree Planting",
            "Recycling",
            "Sanitation",
            "General Maintenance"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        editFullName = findViewById(R.id.editFullName);
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);

        radioRole = findViewById(R.id.radioRole);
        radioUser = findViewById(R.id.radioUser);
        radioWorker = findViewById(R.id.radioWorker);

        spinnerSpecialization = findViewById(R.id.spinnerSpecialization);
        btnRegister = findViewById(R.id.btnRegister);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, SPECIALIZATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialization.setAdapter(adapter);

        spinnerSpecialization.setVisibility(View.GONE);

        radioRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWorker) {
                spinnerSpecialization.setVisibility(View.VISIBLE);
            } else {
                spinnerSpecialization.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> {
            if (radioWorker.isChecked()) {
                registerWorker();
            } else {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String fullName = editFullName.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String passwordHash = HashUtils.sha256(password);
        String id = db.collection("users").document().getId();

        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("full_name", fullName);
        user.put("username", username);
        user.put("email", email);
        user.put("phone", phone);
        user.put("password_hash", passwordHash);
        user.put("role", "USER");
        user.put("status", "ACTIVE");
        user.put("specialization", "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            user.put("created_at", Instant.now().toString());
        }

        db.collection("users")
                .document(id)
                .set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void registerWorker() {
        String fullName = editFullName.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String specialization = spinnerSpecialization.getSelectedItem().toString();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || specialization.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = db.collection("workers").document().getId();

        Map<String, Object> worker = new HashMap<>();
        worker.put("id", id);
        worker.put("name", fullName);
        worker.put("username", username);
        worker.put("email", email);
        worker.put("phone", phone);
        worker.put("password", password);
        worker.put("status", "AVAILABLE");
        worker.put("specialization", specialization);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            worker.put("created_at", Instant.now().toString());
        }

        db.collection("workers")
                .document(id)
                .set(worker)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RegisterActivity.this, "Worker registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
