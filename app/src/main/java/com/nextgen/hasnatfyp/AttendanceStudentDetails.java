package com.nextgen.hasnatfyp;


import android.os.Parcel;
import android.os.Parcelable;

public class AttendanceStudentDetails implements Parcelable {
    private String studentName;
    private String studentRollNo;
    private String attendanceStatus;

    public AttendanceStudentDetails(String studentName, String studentRollNo, String attendanceStatus) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.attendanceStatus = attendanceStatus;
    }

    protected AttendanceStudentDetails(Parcel in) {
        studentName = in.readString();
        studentRollNo = in.readString();
        attendanceStatus = in.readString();
    }

    public static final Creator<AttendanceStudentDetails> CREATOR = new Creator<AttendanceStudentDetails>() {
        @Override
        public AttendanceStudentDetails createFromParcel(Parcel in) {
            return new AttendanceStudentDetails(in);
        }

        @Override
        public AttendanceStudentDetails[] newArray(int size) {
            return new AttendanceStudentDetails[size];
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

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studentName);
        dest.writeString(studentRollNo);
        dest.writeString(attendanceStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

