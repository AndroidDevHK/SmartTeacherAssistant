package com.nextgen.hasnatfyp;

import java.util.List;

public class AuthStudentCAttendanceModel {
    private String courseName;
    private List<SCattendanceModel> attendanceList;

    // Constructor
    public AuthStudentCAttendanceModel(String courseName, List<SCattendanceModel> attendanceList) {
        this.courseName = courseName;
        this.attendanceList = attendanceList;
    }

    // Getter and Setter for CourseName
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Getter and Setter for AttendanceList
    public List<SCattendanceModel> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<SCattendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @Override
    public String toString() {
        return "AuthStudentCAttendanceModel{" +
                "courseName='" + courseName + '\'' +
                ", attendanceList=" + attendanceList +
                '}';
    }
}
