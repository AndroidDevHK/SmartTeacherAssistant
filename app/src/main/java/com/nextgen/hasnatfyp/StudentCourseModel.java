package com.nextgen.hasnatfyp;


public class StudentCourseModel {
    private String courseID;
    private String courseName;

    // Constructor
    public StudentCourseModel(String courseID, String courseName) {
        this.courseID = courseID;
        this.courseName = courseName;
    }

    // Getters and Setters
    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
