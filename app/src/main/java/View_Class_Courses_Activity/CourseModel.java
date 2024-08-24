package View_Class_Courses_Activity;
public class CourseModel {
    private String courseId;
    private String courseName;
    private int creditHours;
    private boolean isCourseActive;
    private String courseTeacher;
    private String courseTeacherFullName;

    public CourseModel(String courseId, String courseName, int creditHours, boolean isCourseActive, String courseTeacher, String courseTeacherFullName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.isCourseActive = isCourseActive;
        this.courseTeacher = courseTeacher;
        this.courseTeacherFullName = courseTeacherFullName;
    }

    public CourseModel(CourseModel course) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.isCourseActive = isCourseActive;
        this.courseTeacher = courseTeacher;
        this.courseTeacherFullName = courseTeacherFullName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public boolean isCourseActive() {
        return isCourseActive;
    }

    public void setCourseActive(boolean courseActive) {
        isCourseActive = courseActive;
    }

    public String getCourseTeacher() {
        return courseTeacher;
    }

    public void setCourseTeacher(String courseTeacher) {
        this.courseTeacher = courseTeacher;
    }

    public String getCourseTeacherFullName() {
        return courseTeacherFullName;
    }

    public void setCourseTeacherFullName(String courseTeacherFullName) {
        this.courseTeacherFullName = courseTeacherFullName;
    }

    public void updateCourseInfo(String courseTeacher, String courseTeacherFullName) {
        this.courseTeacher = courseTeacher;
        this.courseTeacherFullName = courseTeacherFullName;
    }
}
