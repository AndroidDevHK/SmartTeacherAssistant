package Display_Institute_Teachers_To_Assign_Him_Course_Activity;


public class TeacherModel {
    private String teacherUsername;
    private String teacherName;
    private String qualification;
    private String department;

    public TeacherModel(String teacherUsername, String teacherName, String qualification, String department) {
        this.teacherUsername = teacherUsername;
        this.teacherName = teacherName;
        this.qualification = qualification;
        this.department = department;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
