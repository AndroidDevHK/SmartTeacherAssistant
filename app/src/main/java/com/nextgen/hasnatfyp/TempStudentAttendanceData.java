package com.nextgen.hasnatfyp;


import java.util.List;

public class TempStudentAttendanceData {
    private String studentName;
    private String rollNo;
    private List<String> attendanceIds;

    public TempStudentAttendanceData() {

    }

    public TempStudentAttendanceData(String studentName, String rollNo, List<String> attendanceIds) {
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.attendanceIds = attendanceIds;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public List<String> getAttendanceIds() {
        return attendanceIds;
    }

    public void setAttendanceIds(List<String> attendanceIds) {
        this.attendanceIds = attendanceIds;
    }
}

