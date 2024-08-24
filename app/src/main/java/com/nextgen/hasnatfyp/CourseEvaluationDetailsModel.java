package com.nextgen.hasnatfyp;
import android.os.Parcel;
import android.os.Parcelable;

public class CourseEvaluationDetailsModel implements Parcelable {
    private String studentName;
    private String studentRollNo;
    private double obtainedMarks;

    public CourseEvaluationDetailsModel(String studentName, String studentRollNo, double obtainedMarks) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.obtainedMarks = obtainedMarks;
    }

    protected CourseEvaluationDetailsModel(Parcel in) {
        studentName = in.readString();
        studentRollNo = in.readString();
        obtainedMarks = in.readDouble();
    }

    public static final Creator<CourseEvaluationDetailsModel> CREATOR = new Creator<CourseEvaluationDetailsModel>() {
        @Override
        public CourseEvaluationDetailsModel createFromParcel(Parcel in) {
            return new CourseEvaluationDetailsModel(in);
        }

        @Override
        public CourseEvaluationDetailsModel[] newArray(int size) {
            return new CourseEvaluationDetailsModel[size];
        }
    };

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

    public double getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(double obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studentName);
        dest.writeString(studentRollNo);
        dest.writeDouble(obtainedMarks);
    }
}
