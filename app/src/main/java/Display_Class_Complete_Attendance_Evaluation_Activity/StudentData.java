package Display_Class_Complete_Attendance_Evaluation_Activity;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.List;

public class StudentData implements Parcelable {
    private String name;
    private String rollNo;
    private List<StudentCourseCompleteDetailsModel> courses;

    public StudentData(String name, String rollNo, List<StudentCourseCompleteDetailsModel> courses) {
        this.name = name;
        this.rollNo = rollNo;
        Collections.sort(courses, (course1, course2) -> course1.getCourseId().compareToIgnoreCase(course2.getCourseId()));
        this.courses = courses;
    }

    protected StudentData(Parcel in) {
        name = in.readString();
        rollNo = in.readString();
        courses = in.createTypedArrayList(StudentCourseCompleteDetailsModel.CREATOR);
    }

    public static final Creator<StudentData> CREATOR = new Creator<StudentData>() {
        @Override
        public StudentData createFromParcel(Parcel in) {
            return new StudentData(in);
        }

        @Override
        public StudentData[] newArray(int size) {
            return new StudentData[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public List<StudentCourseCompleteDetailsModel> getCourses() {
        return courses;
    }

    public void setCourses(List<StudentCourseCompleteDetailsModel> courses) {
        this.courses = courses;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(rollNo);
        dest.writeTypedList(courses);
    }
}
