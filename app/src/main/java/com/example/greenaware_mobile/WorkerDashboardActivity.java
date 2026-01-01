package com.example.greenaware_mobile;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "WorkerDashboard";

    RecyclerView recyclerView;
    WorkerActionAdapter adapter;
    List<ActionModel> actionList;

    String workerId;

    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        workerId = WorkerSession.getInstance().getWorkerId();
        Log.d(TAG, "Worker ID: " + workerId);

        recyclerView = findViewById(R.id.recyclerAssignments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        actionList = new ArrayList<>();
        adapter = new WorkerActionAdapter(this, actionList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAssignments();
    }

    private void loadAssignments() {
        db.collection("actions")
                .whereEqualTo("worker_id", workerId)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Log.e(TAG, "Firestore error: " + error.getMessage());
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        Log.d(TAG, "No assignments found for workerId: " + workerId);
                        actionList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    actionList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        ActionModel action = doc.toObject(ActionModel.class);
                        actionList.add(action);
                        Log.d(TAG, "Loaded assignment: " + action.getAction_note() + " | Status: " + action.getStatus());
                    }

                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Total assignments loaded: " + actionList.size());
                });
    }
}

