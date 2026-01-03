package com.example.greenaware_mobile;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "WorkerDashboard";

    RecyclerView recyclerView;
    WorkerActionAdapter adapter;
    List<ActionModel> actionList;

    String workerId;

    FirebaseFirestore db;
    private Map<String, String> lastStatusMap = new HashMap<>();

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
        deadlineNotification(workerId);

        loadAssignments();
    }

    private void deadlineNotification(String workerId) {
        db.collection("actions")
                .whereEqualTo("worker_id", workerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            checkDeadlineAndNotify(doc);
                        }
                    }
                });

        db.collection("actions")
                .whereEqualTo("worker_id", workerId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore error: " + e.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            checkDeadlineAndNotify(doc);
                        }
                    }
                });
    }

    private void checkDeadlineAndNotify(DocumentSnapshot doc) {
        String actionId = doc.getId();
        String status = doc.getString("status");
        String deadline = doc.getString("deadline");
        String location = doc.getString("location");
        String actionNote = doc.getString("action_note");

        if (status == null || (!status.equals("PENDING") && !status.equals("IN_PROGRESS"))) return;

        String lastStatus = lastStatusMap.get(actionId);

        if (DeadlineUtils.isDeadlineTomorrow(deadline) && !status.equals(lastStatus)) {

            String message = "Assignment: " + actionNote +
                    "\nLocation: " + location +
                    "\nDeadline: " + deadline;

            NotificationHelper.showNotification(
                    getApplicationContext(),
                    "Work Deadline Alert",
                    message
            );
        }

        lastStatusMap.put(actionId, status);
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

