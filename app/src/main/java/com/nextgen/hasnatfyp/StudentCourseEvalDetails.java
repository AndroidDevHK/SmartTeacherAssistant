package com.nextgen.hasnatfyp;

public class StudentCourseEvalDetails {
    private String evalName;
    private String evalTMarks;
    private double evalObtMarks;

    public StudentCourseEvalDetails() {
        // Default constructor required for Firestore
    }

    public StudentCourseEvalDetails(String evalName, String evalTMarks, double evalObtMarks) {
        this.evalName = evalName;
        this.evalTMarks = evalTMarks;
        this.evalObtMarks = evalObtMarks;
    }

    public String getEvalName() {
        return evalName;
    }

    public void setEvalName(String evalName) {
        this.evalName = evalName;
    }

    public String getEvalTMarks() {
        return evalTMarks;
    }

    public void setEvalTMarks(String evalTMarks) {
        this.evalTMarks = evalTMarks;
    }

    public double getEvalObtMarks() {
        return evalObtMarks;
    }

    public void setEvalObtMarks(double evalObtMarks) {
        this.evalObtMarks = evalObtMarks;
    }
}

