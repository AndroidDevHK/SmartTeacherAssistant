package OfflineEvluationManagement;
import android.os.Parcel;
import android.os.Parcelable;

public class StudentEvalMarksOffline implements Parcelable {
    private String studentName;
    private String rollNo;
    private double obtainedMarks;
    private String classId;

    // Constructor
    public StudentEvalMarksOffline(String studentName, String rollNo, double obtainedMarks, String classId) {
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.obtainedMarks = obtainedMarks;
        this.classId = classId;
    }

    // Parcelable implementation
    protected StudentEvalMarksOffline(Parcel in) {
        studentName = in.readString();
        rollNo = in.readString();
        obtainedMarks = in.readDouble();
        classId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studentName);
        dest.writeString(rollNo);
        dest.writeDouble(obtainedMarks);
        dest.writeString(classId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StudentEvalMarksOffline> CREATOR = new Creator<StudentEvalMarksOffline>() {
        @Override
        public StudentEvalMarksOffline createFromParcel(Parcel in) {
            return new StudentEvalMarksOffline(in);
        }

        @Override
        public StudentEvalMarksOffline[] newArray(int size) {
            return new StudentEvalMarksOffline[size];
        }
    };

    // Getters and setters
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public double getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(double obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }
}
