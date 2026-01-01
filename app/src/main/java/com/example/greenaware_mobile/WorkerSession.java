package com.example.greenaware_mobile;
public class WorkerSession {

    private static WorkerSession instance;

    private String workerId;
    private String name;
    private String phone;
    private String email;

    private WorkerSession() {}

    public static WorkerSession getInstance() {
        if (instance == null) {
            instance = new WorkerSession();
        }
        return instance;
    }

    public void setWorker(String workerId, String name, String phone, String email) {
        this.workerId = workerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
