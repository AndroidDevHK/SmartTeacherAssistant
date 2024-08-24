package Display_Course_Attendance_Activity;


public class StudentAttendanceModel {
    private String date;
    private String attendanceStatus;

    public StudentAttendanceModel() {
        // Required empty constructor for Firestore
    }

    public StudentAttendanceModel(String date, String attendanceStatus) {
        this.date = date;
        this.attendanceStatus = attendanceStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}
