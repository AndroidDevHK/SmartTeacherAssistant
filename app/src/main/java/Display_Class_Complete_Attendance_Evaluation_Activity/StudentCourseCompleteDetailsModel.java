package Display_Class_Complete_Attendance_Evaluation_Activity;

import com.nextgen.hasnatfyp.StudentEvaluationModel;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StudentCourseCompleteDetailsModel implements Parcelable {
    private String courseId;
    private List<StudentEvaluationModel> studentEvalList;
    private String allEvaluationTotal;
    private String allEvaluationObtainedMarks;
    private String percentage;
    private int totalCount;
    private boolean isRepeater;
    private int presents;
    private int absents;
    private int leaves;
    private String firstDate;
    private String lastDate;
    private float presentPercentage;
    private String courseName;

    public StudentCourseCompleteDetailsModel(String courseId, List<StudentEvaluationModel> studentEvalList, String allEvaluationTotal, String allEvaluationObtainedMarks, String percentage, int totalCount, boolean isRepeater, int presents, int absents, int leaves, String firstDate, String lastDate, float presentPercentage, String courseName) {
        this.courseId = courseId;
        this.studentEvalList = studentEvalList;
        this.allEvaluationTotal = allEvaluationTotal;
        this.allEvaluationObtainedMarks = allEvaluationObtainedMarks;
        this.percentage = percentage;
        this.totalCount = totalCount;
        this.isRepeater = isRepeater;
        this.presents = presents;
        this.absents = absents;
        this.leaves = leaves;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.presentPercentage = presentPercentage;
        this.courseName = courseName;
    }

    protected StudentCourseCompleteDetailsModel(Parcel in) {
        courseId = in.readString();
        studentEvalList = in.createTypedArrayList(StudentEvaluationModel.CREATOR);
        allEvaluationTotal = in.readString();
        allEvaluationObtainedMarks = in.readString();
        percentage = in.readString();
        totalCount = in.readInt();
        isRepeater = in.readByte() != 0;
        presents = in.readInt();
        absents = in.readInt();
        leaves = in.readInt();
        firstDate = in.readString();
        lastDate = in.readString();
        presentPercentage = in.readFloat();
        courseName = in.readString();
    }

    public static final Creator<StudentCourseCompleteDetailsModel> CREATOR = new Creator<StudentCourseCompleteDetailsModel>() {
        @Override
        public StudentCourseCompleteDetailsModel createFromParcel(Parcel in) {
            return new StudentCourseCompleteDetailsModel(in);
        }

        @Override
        public StudentCourseCompleteDetailsModel[] newArray(int size) {
            return new StudentCourseCompleteDetailsModel[size];
        }
    };

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public List<StudentEvaluationModel> getStudentEvalList() {
        return studentEvalList;
    }

    public void setStudentEvalList(List<StudentEvaluationModel> studentEvalList) {
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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isRepeater() {
        return isRepeater;
    }

    public void setRepeater(boolean repeater) {
        isRepeater = repeater;
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

    public String getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(String firstDate) {
        this.firstDate = firstDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public float getPresentPercentage() {
        return presentPercentage;
    }

    public void setPresentPercentage(float presentPercentage) {
        this.presentPercentage = presentPercentage;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourses(List<StudentEvaluationModel> evaluationDetailsList) {
        this.studentEvalList = evaluationDetailsList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseId);
        dest.writeTypedList(studentEvalList);
        dest.writeString(allEvaluationTotal);
        dest.writeString(allEvaluationObtainedMarks);
        dest.writeString(percentage);
        dest.writeInt(totalCount);
        dest.writeByte((byte) (isRepeater ? 1 : 0));
        dest.writeInt(presents);
        dest.writeInt(absents);
        dest.writeInt(leaves);
        dest.writeString(firstDate);
        dest.writeString(lastDate);
        dest.writeFloat(presentPercentage);
        dest.writeString(courseName);
    }
}
