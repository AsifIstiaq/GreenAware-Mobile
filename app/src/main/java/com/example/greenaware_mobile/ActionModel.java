package com.example.greenaware_mobile;

public class ActionModel {

    private String id;
    private String action_note;
    private String location;
    private String deadline;
    private String status;
    private String worker_id;
    private String worker_name;
    private String created_at;
    private String completed_date;
    private String resolution_details;

    public ActionModel() {}

    // Getters
    public String getId() { return id; }
    public String getAction_note() { return action_note; }
    public String getLocation() { return location; }
    public String getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public String getWorker_id() { return worker_id; }
    public String getWorker_name() { return worker_name; }
    public String getCreated_at() { return created_at; }
    public String getCompleted_date() { return completed_date; }
    public String getResolution_details() { return resolution_details; }

    public void setId(String id) { this.id = id; }
    public void setAction_note(String action_note) { this.action_note = action_note; }
    public void setLocation(String location) { this.location = location; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setStatus(String status) { this.status = status; }
    public void setWorker_id(String worker_id) { this.worker_id = worker_id; }
    public void setWorker_name(String worker_name) { this.worker_name = worker_name; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setCompleted_date(String completed_date) { this.completed_date = completed_date; }
    public void setResolution_details(String resolution_details) { this.resolution_details = resolution_details; }
}
