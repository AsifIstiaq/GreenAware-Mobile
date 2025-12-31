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

import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvFullName, tvUsername, tvEmail, tvPhone;
    private Button btnLogout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvFullName = findViewById(R.id.tvFullName);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        btnLogout = findViewById(R.id.btnLogout);

        db = FirebaseFirestore.getInstance();

        String username = getIntent().getStringExtra("username");
        if (username == null) username = "";

        loadUserData(username);

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserData(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    if (documents.isEmpty()) {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = documents.get(0);

                    String fullName = doc.getString("full_name");
                    String email = doc.getString("email");
                    String phone = doc.getString("phone");

                    tvWelcome.setText("Welcome, " + fullName);
                    tvFullName.setText(fullName);
                    tvUsername.setText(username);
                    tvEmail.setText(email);
                    tvPhone.setText(phone);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
