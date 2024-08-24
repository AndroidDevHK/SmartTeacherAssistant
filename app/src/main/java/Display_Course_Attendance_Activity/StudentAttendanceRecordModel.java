package Display_Course_Attendance_Activity;

import java.util.List;

public class StudentAttendanceRecordModel {
    private String studentRollNo;
    private String name; // New field for student name
    private List<StudentAttendanceModel> attendanceList;

    public StudentAttendanceRecordModel() {
        // Required empty constructor for Firestore
    }

    public StudentAttendanceRecordModel(String studentRollNo, String name, List<StudentAttendanceModel> attendanceList) {
        this.studentRollNo = studentRollNo;
        this.name = name;
        this.attendanceList = attendanceList;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StudentAttendanceModel> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<StudentAttendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

}
