package View_Class_Students_Activity;

import android.os.Parcel;
import android.os.Parcelable;

public class StudentModel implements Parcelable {
    private String studentName;
    private String rollNo;
    private boolean isActive;
    private String attendanceStatus;
    private String classID;
    private String studentUserID;

    public StudentModel() {
    }

    public StudentModel(String studentName, String rollNo, boolean isActive, String attendanceStatus, String classID) {
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.isActive = isActive;
        this.attendanceStatus = attendanceStatus;
        this.classID = classID;
    }

    protected StudentModel(Parcel in) {
        studentName = in.readString();
        rollNo = in.readString();
        isActive = in.readByte() != 0;
        attendanceStatus = in.readString();
        classID = in.readString(); // Read class ID from parcel
        studentUserID = in.readString(); // Read studentUserID from parcel
    }

    public static final Creator<StudentModel> CREATOR = new Creator<StudentModel>() {
        @Override
        public StudentModel createFromParcel(Parcel in) {
            return new StudentModel(in);
        }

        @Override
        public StudentModel[] newArray(int size) {
            return new StudentModel[size];
        }
    };

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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getStudentUserID() { // Getter for studentUserID
        return studentUserID;
    }

    public void setStudentUserID(String studentUserID) { // Setter for studentUserID
        this.studentUserID = studentUserID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studentName);
        dest.writeString(rollNo);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(attendanceStatus);
        dest.writeString(classID); // Write class ID to parcel
        dest.writeString(studentUserID); // Write studentUserID to parcel
    }
}
