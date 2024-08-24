package Display_Course_Attendance_List_Activity;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.nextgen.hasnatfyp.AttendanceStudentDetails;

public class AttendanceInfoModel implements Parcelable {
    private String attendanceID;
    private String attendanceDate;
    private List<AttendanceStudentDetails> studentList;

    public AttendanceInfoModel(String attendanceID, String attendanceDate, List<AttendanceStudentDetails> studentList) {
        this.attendanceID = attendanceID;
        this.attendanceDate = attendanceDate;
        this.studentList = studentList;
    }

    protected AttendanceInfoModel(Parcel in) {
        attendanceID = in.readString();
        attendanceDate = in.readString();
        studentList = in.createTypedArrayList(AttendanceStudentDetails.CREATOR);
    }

    public static final Creator<AttendanceInfoModel> CREATOR = new Creator<AttendanceInfoModel>() {
        @Override
        public AttendanceInfoModel createFromParcel(Parcel in) {
            return new AttendanceInfoModel(in);
        }

        @Override
        public AttendanceInfoModel[] newArray(int size) {
            return new AttendanceInfoModel[size];
        }
    };

    public String getAttendanceID() {
        return attendanceID;
    }

    public void setAttendanceID(String attendanceID) {
        this.attendanceID = attendanceID;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<AttendanceStudentDetails> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<AttendanceStudentDetails> studentList) {
        this.studentList = studentList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(attendanceID);
        dest.writeString(attendanceDate);
        dest.writeTypedList(studentList);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
