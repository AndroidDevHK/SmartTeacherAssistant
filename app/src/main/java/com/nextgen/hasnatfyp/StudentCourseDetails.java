package com.nextgen.hasnatfyp;



import java.util.List;

public class StudentCourseDetails {
    private String courseId;
    private List<StudentCourseEvalDetails> evaluationDetails;
    private boolean isRepeater;
    private String info1;
    private String info2;
    private int presents;
    private int absents;
    private int leaves;
    private int totalCount;
    private float presentPercentage;

    public StudentCourseDetails(String courseId, List<StudentCourseEvalDetails> evaluationDetails, boolean isRepeater, String info1, String info2, int presents, int absents, int leaves, int totalCount, float presentPercentage) {
        this.courseId = courseId;
        this.evaluationDetails = evaluationDetails;
        this.isRepeater = isRepeater;
        this.info1 = info1;
        this.info2 = info2;
        this.presents = presents;
        this.absents = absents;
        this.leaves = leaves;
        this.totalCount = totalCount;
        this.presentPercentage = presentPercentage;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public List<StudentCourseEvalDetails> getEvaluationDetails() {
        return evaluationDetails;
    }

    public void setEvaluationDetails(List<StudentCourseEvalDetails> evaluationDetails) {
        this.evaluationDetails = evaluationDetails;
    }

    public boolean isRepeater() {
        return isRepeater;
    }

    public void setRepeater(boolean repeater) {
        isRepeater = repeater;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public float getPresentPercentage() {
        return presentPercentage;
    }

    public void setPresentPercentage(float presentPercentage) {
        this.presentPercentage = presentPercentage;
    }
}
