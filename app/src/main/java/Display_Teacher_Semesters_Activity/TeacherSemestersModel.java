package Display_Teacher_Semesters_Activity;

public class TeacherSemestersModel {
    private String semesterID;
    private String semesterName;
    private String startDate;
    private String endDate;
    private int numberOfClasses;

    // Constructor
    public TeacherSemestersModel(String semesterID, String semesterName, String startDate, String endDate, int numberOfClasses) {
        this.semesterID = semesterID;
        this.semesterName = semesterName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfClasses = numberOfClasses;
    }

    // Getter and Setter methods for semesterID, semesterName, startDate, endDate, and numberOfClasses
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

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(int numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }
}
