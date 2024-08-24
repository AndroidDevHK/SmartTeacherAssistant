package com.nextgen.hasnatfyp;

public class RepeaterIndicator {
    private String repeaterName;
    private String rollNo;

    public RepeaterIndicator() {
        // Default constructor required for Firebase
    }

    public RepeaterIndicator(String repeaterName, String rollNo) {
        this.repeaterName = repeaterName;
        this.rollNo = rollNo;
    }

    public String getRepeaterName() {
        return repeaterName;
    }

    public void setRepeaterName(String repeaterName) {
        this.repeaterName = repeaterName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }
}
