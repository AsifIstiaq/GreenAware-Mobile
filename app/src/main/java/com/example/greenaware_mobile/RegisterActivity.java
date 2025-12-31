package com.example.greenaware_mobile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editFullName, editUsername, editEmail, editPhone, editPassword, editSpecialization;
    private RadioGroup radioRole;
    private RadioButton radioUser, radioWorker;
    private Button btnRegister;

    private FirebaseFirestore db;

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
        editSpecialization = findViewById(R.id.editSpecialization);
        radioRole = findViewById(R.id.radioRole);
        radioUser = findViewById(R.id.radioUser);
        radioWorker = findViewById(R.id.radioWorker);
        btnRegister = findViewById(R.id.btnRegister);

        radioRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWorker) {
                editSpecialization.setVisibility(View.VISIBLE);
            } else {
                editSpecialization.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = editFullName.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String role = radioWorker.isChecked() ? "WORKER" : "USER";
        String specialization = role.equals("WORKER") ? editSpecialization.getText().toString().trim() : "";

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() ||
                (role.equals("WORKER") && specialization.isEmpty())) {
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
        user.put("role", role);
        user.put("status", role.equals("USER") ? "ACTIVE" : "AVAILABLE");
        user.put("specialization", specialization);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            user.put("created_at", Instant.now().toString());
        }

        db.collection("users")
                .document(id)
                .set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
