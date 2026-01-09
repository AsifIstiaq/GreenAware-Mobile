package com.example.greenaware_mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkerActionAdapter extends RecyclerView.Adapter<WorkerActionAdapter.ViewHolder> {

    Context context;
    List<ActionModel> list;

    public WorkerActionAdapter(Context context, List<ActionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ActionModel action = list.get(position);

        holder.txtLocation.setText(action.getLocation());
        holder.txtDescription.setText(action.getAction_note());
        holder.txtDeadline.setText("Deadline: " + action.getDeadline());
        String status = action.getStatus();
        holder.txtStatus.setText(status);

        switch (status) {
            case "PENDING":
                holder.txtStatus.setTextColor(Color.parseColor("#FFA726"));
                break;
            case "IN_PROGRESS":
                holder.txtStatus.setTextColor(Color.parseColor("#29B6F6"));
                break;
            case "RESOLVED":
                holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            default:
                holder.txtStatus.setTextColor(Color.parseColor("#388E3C"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActionDetailsActivity.class);
            intent.putExtra("ACTION_ID", action.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtLocation, txtDescription, txtDeadline, txtStatus;

        ViewHolder(View itemView) {
            super(itemView);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDeadline = itemView.findViewById(R.id.txtDeadline);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}

