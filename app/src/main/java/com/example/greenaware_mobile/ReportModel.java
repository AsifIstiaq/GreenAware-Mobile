package com.example.greenaware_mobile;

public class ReportModel {

    private String reportId;
    private String category;
    private String location;
    private String status;

    public ReportModel(String reportId, String category, String location, String status) {
        this.reportId = reportId;
        this.category = category;
        this.location = location;
        this.status = status;
    }

    public String getReportId() { return reportId; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
}