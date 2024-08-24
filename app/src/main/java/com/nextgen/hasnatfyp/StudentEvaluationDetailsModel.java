package com.nextgen.hasnatfyp;

public class StudentEvaluationDetailsModel {
    private String evaluationName;
    private String obtainedMarks;
    private String totalMarks;

    public StudentEvaluationDetailsModel() {
    }

    public StudentEvaluationDetailsModel(String evaluationName, String obtainedMarks, String totalMarks) {
        this.evaluationName = evaluationName;
        this.obtainedMarks = obtainedMarks;
        this.totalMarks = totalMarks;
    }

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
    }

    public String getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(String obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }
}
