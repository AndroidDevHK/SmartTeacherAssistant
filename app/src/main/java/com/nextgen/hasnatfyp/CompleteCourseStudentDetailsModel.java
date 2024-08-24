package com.nextgen.hasnatfyp;

import java.util.List;

import Display_Course_Attendance_Activity.StudentAttendanceModel;

public class CompleteCourseStudentDetailsModel {
    private String CourseName;
    private List<StudentEvaluationDetailsModel> studentEvalList;
    private String allEvaluationTotal;
    private String allEvaluationObtainedMarks;
    private String percentage;
    private List<StudentAttendanceModel> attendanceList;
    private int totalCount;
    private int presents;
    private int absents;
    private int leaves;
    private float presentPercentage;

    public CompleteCourseStudentDetailsModel(String courseName, List<StudentEvaluationDetailsModel> studentEvalList, String allEvaluationTotal, String allEvaluationObtainedMarks, String percentage, List<StudentAttendanceModel> attendanceList, int totalCount, int presents, int absents, int leaves, float presentPercentage) {
        CourseName = courseName;
        this.studentEvalList = studentEvalList;
        this.allEvaluationTotal = allEvaluationTotal;
        this.allEvaluationObtainedMarks = allEvaluationObtainedMarks;
        this.percentage = percentage;
        this.attendanceList = attendanceList;
        this.totalCount = totalCount;
        this.presents = presents;
        this.absents = absents;
        this.leaves = leaves;
        this.presentPercentage = presentPercentage;
    }

    // Getter and Setter methods

    public String getCourseName() {
        return CourseName;
    }

    public void setCourseName(String courseName) {
        CourseName = courseName;
    }

    public List<StudentEvaluationDetailsModel> getStudentEvalList() {
        return studentEvalList;
    }

    public void setStudentEvalList(List<StudentEvaluationDetailsModel> studentEvalList) {
        this.studentEvalList = studentEvalList;
    }

    public String getAllEvaluationTotal() {
        return allEvaluationTotal;
    }

    public void setAllEvaluationTotal(String allEvaluationTotal) {
        this.allEvaluationTotal = allEvaluationTotal;
    }

    public String getAllEvaluationObtainedMarks() {
        return allEvaluationObtainedMarks;
    }

    public void setAllEvaluationObtainedMarks(String allEvaluationObtainedMarks) {
        this.allEvaluationObtainedMarks = allEvaluationObtainedMarks;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public List<StudentAttendanceModel> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<StudentAttendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPresents() {
        return presents;
    }

    public void setPresents(int presents) {
        this.presents = presents;
    }

    public int getAbsents() {
        return absents;
    }

    public void setAbsents(int absents) {
        this.absents = absents;
    }

    public int getLeaves() {
        return leaves;
    }

    public void setLeaves(int leaves) {
        this.leaves = leaves;
    }

    public float getPresentPercentage() {
        return presentPercentage;
    }

    public void setPresentPercentage(float presentPercentage) {
        this.presentPercentage = presentPercentage;
    }
}
