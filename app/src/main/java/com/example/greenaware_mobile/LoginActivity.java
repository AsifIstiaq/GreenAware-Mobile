package com.example.greenaware_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnUserLogin, btnWorkerLogin;
    private TextView tvRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnUserLogin = findViewById(R.id.btnUserLogin);
        btnWorkerLogin = findViewById(R.id.btnWorkerLogin);
        tvRegister = findViewById(R.id.tvRegister);

        db = FirebaseFirestore.getInstance();

        btnUserLogin.setOnClickListener(v -> loginUser());
        btnWorkerLogin.setOnClickListener(v -> loginWorker());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            toast("Enter username and password");
            return;
        }

        String hashedPassword = hashPassword(password);

        db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password_hash", hashedPassword)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        toast("Invalid user credentials");
                        return;
                    }

                    Intent intent = new Intent(this, UserDashboardActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> toast(e.getMessage()));
    }

    private void loginWorker() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            toast("Enter username and password");
            return;
        }

        db.collection("workers")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        toast("Invalid worker credentials");
                        return;
                    }

                    DocumentSnapshot doc = snapshot.getDocuments().get(0);

                    String workerId = doc.getString("id");
                    String name = doc.getString("name");
                    String phone = doc.getString("phone");
                    String email = doc.getString("email");

                    int workerIdHash = stringHashCode(workerId);
                    String workerIdStr = String.valueOf(workerIdHash);

                    WorkerSession.getInstance()
                            .setWorker(workerIdStr, name, phone, email);

                    Intent intent = new Intent(this, WorkerDashboardActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> toast(e.getMessage()));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int stringHashCode(String s) {
        int h = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }
}
