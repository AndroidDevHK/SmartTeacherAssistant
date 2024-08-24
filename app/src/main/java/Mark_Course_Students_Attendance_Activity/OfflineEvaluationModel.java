package Mark_Course_Students_Attendance_Activity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class OfflineEvaluationModel implements Parcelable {
    private List<StudentModel> studentsList;
    private String attendanceDate;
    private String courseId;
    private String courseName;
    private boolean areRepeaters;

    public OfflineEvaluationModel(List<StudentModel> studentsList, String attendanceDate, String courseId, String courseName, boolean areRepeaters) {
        this.studentsList = studentsList;
        this.attendanceDate = attendanceDate;
        this.courseId = courseId;
        this.courseName = courseName;
        this.areRepeaters = areRepeaters;
    }

    protected OfflineEvaluationModel(Parcel in) {
        studentsList = in.createTypedArrayList(StudentModel.CREATOR);
        attendanceDate = in.readString();
        courseId = in.readString();
        courseName = in.readString();
        areRepeaters = in.readByte() != 0;
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
        dest.writeTypedList(studentsList);
        dest.writeString(attendanceDate);
        dest.writeString(courseId);
        dest.writeString(courseName);
        dest.writeByte((byte) (areRepeaters ? 1 : 0));
    }

    // Getters and Setters
    public List<StudentModel> getStudentsList() {
        return studentsList;
    }

    public void setStudentsList(List<StudentModel> studentsList) {
        this.studentsList = studentsList;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
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
}
