package com.nextgen.hasnatfyp;

public class RepeaterClass {
    private String rollNo;
    private String repeaterClassID;

    public RepeaterClass(String rollNo, String repeaterClassID) {
        this.rollNo = rollNo;
        this.repeaterClassID = repeaterClassID;
    }

    // Getters
    public String getRollNo() {
        return rollNo;
    }

    public String getRepeaterClassID() {
        return repeaterClassID;
    }

    // Setters
    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public void setRepeaterClassID(String repeaterClassID) {
        this.repeaterClassID = repeaterClassID;
    }
}
