package View_Semester_Classes_Activity;

import android.os.Parcel;
import android.os.Parcelable;

public class ClassModel implements Parcelable {
    private String classId;
    private String className;
    private int numberOfStudents;
    private int coursesCount; // New field for courses count

    public ClassModel() {
        // Default constructor required for Firestore
    }

    public ClassModel(String classId, String className, int numberOfStudents, int coursesCount) {
        this.classId = classId;
        this.className = className;
        this.numberOfStudents = numberOfStudents;
        this.coursesCount = coursesCount; // Initialize courses count
    }

    // Parcelable implementation
    protected ClassModel(Parcel in) {
        classId = in.readString();
        className = in.readString();
        numberOfStudents = in.readInt();
        coursesCount = in.readInt(); // Read courses count from Parcel
    }

    public static final Creator<ClassModel> CREATOR = new Creator<ClassModel>() {
        @Override
        public ClassModel createFromParcel(Parcel in) {
            return new ClassModel(in);
        }

        @Override
        public ClassModel[] newArray(int size) {
            return new ClassModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(classId);
        dest.writeString(className);
        dest.writeInt(numberOfStudents);
        dest.writeInt(coursesCount); // Write courses count to Parcel
    }

    // Getters and setters
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

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public int getCoursesCount() {
        return coursesCount;
    }

    public void setCoursesCount(int coursesCount) {
        this.coursesCount = coursesCount;
    }
}
