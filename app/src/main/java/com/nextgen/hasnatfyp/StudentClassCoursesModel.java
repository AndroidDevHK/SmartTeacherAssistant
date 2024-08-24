package com.nextgen.hasnatfyp;

import java.util.List;

public class StudentClassCoursesModel {
    private String classID;
    private String className;
    private List<StudentCourseModel> activeCoursesList;

    // Constructor
    public StudentClassCoursesModel(String classID, String className, List<StudentCourseModel> activeCoursesList) {
        this.classID = classID;
        this.className = className;
        this.activeCoursesList = activeCoursesList;
    }

    // Getters and Setters
    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<StudentCourseModel> getActiveCoursesList() {
        return activeCoursesList;
    }

    public void setActiveCoursesList(List<StudentCourseModel> activeCoursesList) {
        this.activeCoursesList = activeCoursesList;
    }
}
