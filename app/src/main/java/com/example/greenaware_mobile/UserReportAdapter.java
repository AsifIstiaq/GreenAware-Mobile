package com.example.greenaware_mobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserReportAdapter extends RecyclerView.Adapter<UserReportAdapter.ReportViewHolder> {

    private Context context;
    private List<ReportModel> reportList;

    public UserReportAdapter(Context context, List<ReportModel> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportModel report = reportList.get(position);

        holder.tvCategory.setText(report.getCategory());
        holder.tvLocation.setText(report.getLocation());
        holder.tvStatus.setText(report.getStatus());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailsActivity.class);
            intent.putExtra("REPORT_ID", report.getReportId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvLocation, tvStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCardCategory);
            tvLocation = itemView.findViewById(R.id.tvCardLocation);
            tvStatus = itemView.findViewById(R.id.tvCardStatus);
        }
    }
}
