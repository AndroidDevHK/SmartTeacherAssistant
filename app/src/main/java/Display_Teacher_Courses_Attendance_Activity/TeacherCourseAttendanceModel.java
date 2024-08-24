package Display_Teacher_Courses_Attendance_Activity;


import java.util.List;

public class TeacherCourseAttendanceModel {
    private int classesTaken;
    private String className;
    private String courseName;
    private int creditHours;
    private String firstAttendanceDate;
    private String lastAttendanceDate;
    private List<String> teacherAttendance; // New field
    private int expectedClasses; // New field
    private double percentage; // New field

    public TeacherCourseAttendanceModel(int classesTaken, String className, String courseName, int creditHours, String firstAttendanceDate, String lastAttendanceDate, List<String> teacherAttendance, int expectedClasses, double percentage) {
        this.classesTaken = classesTaken;
        this.className = className;
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.firstAttendanceDate = firstAttendanceDate;
        this.lastAttendanceDate = lastAttendanceDate;
        this.teacherAttendance = teacherAttendance;
        this.expectedClasses = expectedClasses;
        this.percentage = percentage;
    }

    public int getClassesTaken() {
        return classesTaken;
    }

    public void setClassesTaken(int classesTaken) {
        this.classesTaken = classesTaken;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public String getFirstAttendanceDate() {
        return firstAttendanceDate;
    }

    public void setFirstAttendanceDate(String firstAttendanceDate) {
        this.firstAttendanceDate = firstAttendanceDate;
    }

    public String getLastAttendanceDate() {
        return lastAttendanceDate;
    }

    public void setLastAttendanceDate(String lastAttendanceDate) {
        this.lastAttendanceDate = lastAttendanceDate;
    }

    public List<String> getTeacherAttendance() {
        return teacherAttendance;
    }

    public void setTeacherAttendance(List<String> teacherAttendance) {
        this.teacherAttendance = teacherAttendance;
    }

    public int getExpectedClasses() {
        return expectedClasses;
    }

    public void setExpectedClasses(int expectedClasses) {
        this.expectedClasses = expectedClasses;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double calculatePercentage() {
        if (expectedClasses > 0) {
            return ((double) classesTaken / expectedClasses) * 100;
        } else {
            return 0.0;
        }
    }
}
