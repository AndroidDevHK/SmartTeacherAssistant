package Display_Teacher_Semester_Classes_Acitivity;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.Gson;
import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class TeacherClassModel implements Parcelable {
    private String classId;
    private String className;
    private String courseId;
    private String courseName;
    private List<StudentModel> regularCourseStudents;
    private int regularStudentCount;
    private int courseRepeatersStudentsCount;
    private List<StudentModel> courseRepeatersStudents;

    public TeacherClassModel(String classId, String className, String courseId, String courseName,
                             List<StudentModel> regularCourseStudents, int regularStudentCount,
                             int courseRepeatersStudentsCount, List<StudentModel> courseRepeatersStudents) {
        this.classId = classId;
        this.className = className;
        this.courseId = courseId;
        this.courseName = courseName;
        this.regularCourseStudents = regularCourseStudents;
        this.regularStudentCount = regularStudentCount;
        this.courseRepeatersStudentsCount = courseRepeatersStudentsCount;
        this.courseRepeatersStudents = courseRepeatersStudents;
    }

    protected TeacherClassModel(Parcel in) {
        classId = in.readString();
        className = in.readString();
        courseId = in.readString();
        courseName = in.readString();
        regularCourseStudents = in.createTypedArrayList(StudentModel.CREATOR);
        regularStudentCount = in.readInt();
        courseRepeatersStudentsCount = in.readInt();
        courseRepeatersStudents = in.createTypedArrayList(StudentModel.CREATOR);
    }

    public static final Creator<TeacherClassModel> CREATOR = new Creator<TeacherClassModel>() {
        @Override
        public TeacherClassModel createFromParcel(Parcel in) {
            return new TeacherClassModel(in);
        }

        @Override
        public TeacherClassModel[] newArray(int size) {
            return new TeacherClassModel[size];
        }
    };

    // Method to convert object to JSON string
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // Static method to create object from JSON string
    public static TeacherClassModel fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, TeacherClassModel.class);
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public List<StudentModel> getRegularCourseStudents() {
        return regularCourseStudents;
    }

    public void setRegularCourseStudents(List<StudentModel> regularCourseStudents) {
        this.regularCourseStudents = regularCourseStudents;
    }

    public int getRegularStudentCount() {
        return regularStudentCount;
    }

    public void setRegularStudentCount(int regularStudentCount) {
        this.regularStudentCount = regularStudentCount;
    }

    public int getCourseRepeatersStudentsCount() {
        return courseRepeatersStudentsCount;
    }

    public void setCourseRepeatersStudentsCount(int courseRepeatersStudentsCount) {
        this.courseRepeatersStudentsCount = courseRepeatersStudentsCount;
    }

    public List<StudentModel> getCourseRepeatersStudents() {
        return courseRepeatersStudents;
    }

    public void setCourseRepeatersStudents(List<StudentModel> courseRepeatersStudents) {
        this.courseRepeatersStudents = courseRepeatersStudents;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(classId);
        dest.writeString(className);
        dest.writeString(courseId);
        dest.writeString(courseName);
        dest.writeTypedList(regularCourseStudents);
        dest.writeInt(regularStudentCount);
        dest.writeInt(courseRepeatersStudentsCount);
        dest.writeTypedList(courseRepeatersStudents);
    }
}
