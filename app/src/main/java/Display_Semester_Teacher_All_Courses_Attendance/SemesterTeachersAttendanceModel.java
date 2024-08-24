package Display_Semester_Teacher_All_Courses_Attendance;


import Display_Teacher_Courses_Attendance_Activity.TeacherCourseAttendanceModel;

import java.util.ArrayList;
import java.util.List;

public class SemesterTeachersAttendanceModel {
    private String teacherUserName;
    private String teacherName;
    private List<TeacherCourseAttendanceModel> teacherAttendance;

    public SemesterTeachersAttendanceModel(String teacherUserName, String teacherName) {
        this.teacherUserName = teacherUserName;
        this.teacherName = teacherName;
    }

    public String getTeacherUserName() {
        return teacherUserName;
    }

    public void setTeacherUserName(String teacherUserName) {
        this.teacherUserName = teacherUserName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<TeacherCourseAttendanceModel> getTeacherAttendance() {
        return teacherAttendance;
    }

    public void setTeacherAttendance(List<TeacherCourseAttendanceModel> teacherAttendance) {
        this.teacherAttendance = teacherAttendance;
    }

    public void addCourseAttendance(TeacherCourseAttendanceModel courseAttendance) {
        if (teacherAttendance == null) {
            teacherAttendance = new ArrayList<>();
        }
        teacherAttendance.add(courseAttendance);


    }
}
