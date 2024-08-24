package com.nextgen.hasnatfyp;

import java.util.List;

public class StudentSemestersModel {
    private String semesterID;
    private String semesterName;
    private int numberOfCourses;
    private String classID; // Added attribute

    // Constructor
    public StudentSemestersModel(String semesterID, String semesterName, int numberOfCourses, String classID) {
        this.semesterID = semesterID;
        this.semesterName = semesterName;
        this.numberOfCourses = numberOfCourses;
        this.classID = classID;
    }

    // Getters and Setters
    public String getSemesterID() {
        return semesterID;
    }

    public void setSemesterID(String semesterID) {
        this.semesterID = semesterID;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public int getNumberOfCourses() {
        return numberOfCourses;
    }

    public void setNumberOfCourses(int numberOfCourses) {
        this.numberOfCourses = numberOfCourses;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }
}
