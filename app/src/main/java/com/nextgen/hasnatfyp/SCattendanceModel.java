package com.nextgen.hasnatfyp;

public class SCattendanceModel {
    private String date;
    private String status;

    // Constructor
    public SCattendanceModel(String date, String status) {
        this.date = date;
        this.status = status;
    }

    // Getter and Setter for Date
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Getter and Setter for Status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SCattendanceModel{" +
                "date='" + date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
