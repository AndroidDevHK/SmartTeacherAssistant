package Display_Complete_Course_Att_Eval_data_Activity;

import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;

import java.util.List;

import Display_Course_Attendance_Activity.StudentAttendanceModel;

public class CourseStudentDetailsModel {

    private String studentName;
    private String studentRollNo;
    private List<StudentEvaluationDetailsModel> evaluationDetailsList;
    private String totalMarks;
    private String obtainedMarks;
    private String percentage;
    private List<StudentAttendanceModel> attendanceList;
    private int totalCount;
    private int presents;
    private int absents;
    private int leaves;
    private float presentPercentage;

    public CourseStudentDetailsModel() {
    }

    public CourseStudentDetailsModel(String studentName, String studentRollNo, List<StudentEvaluationDetailsModel> evaluationDetailsList, String totalMarks, String obtainedMarks, String percentage, List<StudentAttendanceModel> attendanceList, int totalCount, int presents, int absents, int leaves, float presentPercentage) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.evaluationDetailsList = evaluationDetailsList;
        this.totalMarks = totalMarks;
        this.obtainedMarks = obtainedMarks;
        this.percentage = percentage;
        this.attendanceList = attendanceList;
        this.totalCount = totalCount;
        this.presents = presents;
        this.absents = absents;
        this.leaves = leaves;
        this.presentPercentage = presentPercentage;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public List<StudentEvaluationDetailsModel> getEvaluationDetailsList() {
        return evaluationDetailsList;
    }

    public void setEvaluationDetailsList(List<StudentEvaluationDetailsModel> evaluationDetailsList) {
        this.evaluationDetailsList = evaluationDetailsList;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(String obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public List<StudentAttendanceModel> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<StudentAttendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPresents() {
        return presents;
    }

    public void setPresents(int presents) {
        this.presents = presents;
    }

    public int getAbsents() {
        return absents;
    }

    public void setAbsents(int absents) {
        this.absents = absents;
    }

    public int getLeaves() {
        return leaves;
    }

    public void setLeaves(int leaves) {
        this.leaves = leaves;
    }

    public float getPresentPercentage() {
        return presentPercentage;
    }

    public void setPresentPercentage(float presentPercentage) {
        this.presentPercentage = presentPercentage;
    }
}
