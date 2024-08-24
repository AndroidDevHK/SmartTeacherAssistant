package Display_Course_Students_Evaluation_Activity;

import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;

import java.util.List;

import Display_Course_Attendance_Activity.StudentAttendanceModel;

public class CourseStudentEvaluationListModel {
    private String studentName;
    private String studentRollNo;
    private List<StudentEvaluationDetailsModel> studentEvalList;
    private String allEvaluationTotal;
    private String allEvaluationObtainedMarks;
    private String percentage;

    private List<StudentAttendanceModel> attendanceList;


    public CourseStudentEvaluationListModel() {
    }

    public CourseStudentEvaluationListModel(String studentName, String studentRollNo, List<StudentEvaluationDetailsModel> studentEvalList, String allEvaluationTotal, String allEvaluationObtainedMarks, String percentage) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.studentEvalList = studentEvalList;
        this.allEvaluationTotal = allEvaluationTotal;
        this.allEvaluationObtainedMarks = allEvaluationObtainedMarks;
        this.percentage = percentage;
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

    public List<StudentEvaluationDetailsModel> getStudentEvalList() {
        return studentEvalList;
    }

    public void setStudentEvalList(List<StudentEvaluationDetailsModel> studentEvalList) {
        this.studentEvalList = studentEvalList;
    }

    public String getAllEvaluationTotal() {
        return allEvaluationTotal;
    }

    public void setAllEvaluationTotal(String allEvaluationTotal) {
        this.allEvaluationTotal = allEvaluationTotal;
    }

    public String getAllEvaluationObtainedMarks() {
        return allEvaluationObtainedMarks;
    }

    public void setAllEvaluationObtainedMarks(String allEvaluationObtainedMarks) {
        this.allEvaluationObtainedMarks = allEvaluationObtainedMarks;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
}
