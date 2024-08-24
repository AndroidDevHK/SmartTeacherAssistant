package Add_View_Semester_Activity;

public class SemesterModel {
    private String semesterID;
    private String semesterName;
    private boolean isActive;
    private String startDate;
    private String endDate;
    private int classCount;

    // Default constructor (required for Firestore)
    public SemesterModel() {
        // Default constructor required for calls to DataSnapshot.getValue(SemesterModel.class)
    }

    public SemesterModel(String semesterID, String semesterName, boolean isActive, String startDate, String endDate) {
        this.semesterID = semesterID;
        this.semesterName = semesterName;
        this.isActive = isActive;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters

    public String getSemesterID() {
        return semesterID;
    }

    public void setSemesterID(String semesterID) {
        this.semesterID = semesterID;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getClassCount() {
        return classCount;
    }

    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }
}
