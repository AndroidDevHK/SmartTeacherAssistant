package com.nextgen.hasnatfyp;

public class StudentQuizResults {
    private String studentRollNo;
    private String classID; // Added classID
    private String totalMarks;
    private float obtainedMarks;
    private boolean isRepeater;

    public StudentQuizResults(String studentRollNo, String classID, String totalMarks, float obtainedMarks, boolean isRepeater) {
        this.studentRollNo = studentRollNo;
        this.classID = classID;
        this.totalMarks = totalMarks;
        this.obtainedMarks = obtainedMarks;
        this.isRepeater = isRepeater;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public float getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(float obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public boolean isRepeater() {
        return isRepeater;
    }

    public void setRepeater(boolean repeater) {
        isRepeater = repeater;
    }

    @Override
    public String toString() {
        return "StudentQuizResults{" +
                "studentRollNo='" + studentRollNo + '\'' +
                ", classID='" + classID + '\'' +
                ", totalMarks='" + totalMarks + '\'' +
                ", obtainedMarks=" + obtainedMarks +
                ", isRepeater=" + isRepeater +
                '}';
    }
}
