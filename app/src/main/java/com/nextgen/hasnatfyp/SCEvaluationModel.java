package com.nextgen.hasnatfyp;

public class SCEvaluationModel {
    private String evalId;
    private String evalName;
    private String totalMarks;
    private String obtainedMarks; // Changed to String

    public SCEvaluationModel(String evalId, String evalName, String totalMarks, String obtainedMarks) {
        this.evalId = evalId;
        this.evalName = evalName;
        this.totalMarks = totalMarks;
        this.obtainedMarks = obtainedMarks;
    }

    public String getEvalId() {
        return evalId;
    }

    public String getEvalName() {
        return evalName;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public String getObtainedMarks() {
        return obtainedMarks;
    }
}

