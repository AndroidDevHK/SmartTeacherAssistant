package com.nextgen.hasnatfyp;

import java.util.List;

import Display_Complete_Course_Att_Eval_data_Activity.CourseStudentDetailsModel;

public class StudentCoursesModel {
    private String StudentName;
    private String StudentRollNo;
    private List<CourseStudentDetailsModel> courseDetailsList;

    // Constructor
    public StudentCoursesModel(String studentName, String studentRollNo, List<CourseStudentDetailsModel> courseDetailsList) {
        StudentName = studentName;
        StudentRollNo = studentRollNo;
        this.courseDetailsList = courseDetailsList;
    }

    // Getters and setters
    public String getStudentName() {
        return StudentName;
    }

    public void setStudentName(String studentName) {
        StudentName = studentName;
    }

    public String getStudentRollNo() {
        return StudentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        StudentRollNo = studentRollNo;
    }

    public List<CourseStudentDetailsModel> getCourseDetailsList() {
        return courseDetailsList;
    }

    public void setCourseDetailsList(List<CourseStudentDetailsModel> courseDetailsList) {
        this.courseDetailsList = courseDetailsList;
    }
}
