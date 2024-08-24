package com.nextgen.hasnatfyp;

public class RepeatersListModel {
    private String studentName;
    private String rollNo;
    private boolean isRepeater;

    public RepeatersListModel() {
    }

    public RepeatersListModel(String studentName, String rollNo, boolean isRepeater) {
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.isRepeater = isRepeater;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public boolean isRepeater() {
        return isRepeater;
    }

    public void setRepeater(boolean repeater) {
        isRepeater = repeater;
    }
}
