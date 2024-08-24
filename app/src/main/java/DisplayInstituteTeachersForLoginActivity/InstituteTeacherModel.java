package DisplayInstituteTeachersForLoginActivity;

public class InstituteTeacherModel {
    private String teacherUsername;
    private String teacherName;
    private String qualification;
    private String department;
    private boolean pastAPermission;
    private boolean accountStatus;

    public InstituteTeacherModel(String teacherUsername, String teacherName, String qualification, String department, boolean pastAPermission, boolean accountStatus) {
        this.teacherUsername = teacherUsername;
        this.teacherName = teacherName;
        this.qualification = qualification;
        this.department = department;
        this.pastAPermission = pastAPermission;
        this.accountStatus = accountStatus;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getQualification() {
        return qualification;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isPastAPermission() {
        return pastAPermission;
    }

    public boolean isAccountStatus() {
        return accountStatus;
    }
}
