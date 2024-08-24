package OfflineEvluationManagement;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class OfflineEvaluationModel implements Parcelable {
    private List<StudentEvalMarksOffline> studentsList;
    private String evaluationTMarks;
    private String courseId;
    private String courseName;
    private boolean areRepeaters;
    private String evaluationName;
    private String evaluationDate; // New field for evaluation date

    public OfflineEvaluationModel(List<StudentEvalMarksOffline> studentsList, String evaluationTMarks, String courseId, String courseName, boolean areRepeaters, String evaluationName, String evaluationDate) {
        this.studentsList = studentsList;
        this.evaluationTMarks = evaluationTMarks;
        this.courseId = courseId;
        this.courseName = courseName;
        this.areRepeaters = areRepeaters;
        this.evaluationName = evaluationName;
        this.evaluationDate = evaluationDate;
    }

    public List<StudentEvalMarksOffline> getStudentsList() {
        return studentsList;
    }

    public void setStudentsList(List<StudentEvalMarksOffline> studentsList) {
        this.studentsList = studentsList;
    }

    public String getEvaluationTMarks() {
        return evaluationTMarks;
    }

    public void setEvaluationTMarks(String evaluationTMarks) {
        this.evaluationTMarks = evaluationTMarks;
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

    public boolean isAreRepeaters() {
        return areRepeaters;
    }

    public void setAreRepeaters(boolean areRepeaters) {
        this.areRepeaters = areRepeaters;
    }

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
    }

    public String getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(String evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    protected OfflineEvaluationModel(Parcel in) {
        studentsList = new ArrayList<>();
        in.readList(studentsList, StudentEvalMarksOffline.class.getClassLoader());
        evaluationTMarks = in.readString();
        courseId = in.readString();
        courseName = in.readString();
        areRepeaters = in.readByte() != 0;
        evaluationName = in.readString();
        evaluationDate = in.readString(); // Read the evaluationDate field from Parcel
    }

    public static final Creator<OfflineEvaluationModel> CREATOR = new Creator<OfflineEvaluationModel>() {
        @Override
        public OfflineEvaluationModel createFromParcel(Parcel in) {
            return new OfflineEvaluationModel(in);
        }

        @Override
        public OfflineEvaluationModel[] newArray(int size) {
            return new OfflineEvaluationModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(studentsList);
        dest.writeString(evaluationTMarks);
        dest.writeString(courseId);
        dest.writeString(courseName);
        dest.writeByte((byte) (areRepeaters ? 1 : 0));
        dest.writeString(evaluationName);
        dest.writeString(evaluationDate); // Write the evaluationDate field to Parcel
    }
}
