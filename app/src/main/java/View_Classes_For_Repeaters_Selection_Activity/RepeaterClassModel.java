package View_Classes_For_Repeaters_Selection_Activity;
import android.os.Parcel;
import android.os.Parcelable;

public class RepeaterClassModel implements Parcelable {
    private String repeaterClassID;
    private String className;
    private int studentCount;

    public RepeaterClassModel() {
    }

    public RepeaterClassModel(String repeaterClassID, String className, int studentCount) {
        this.repeaterClassID = repeaterClassID;
        this.className = className;
        this.studentCount = studentCount;
    }

    protected RepeaterClassModel(Parcel in) {
        repeaterClassID = in.readString();
        className = in.readString();
        studentCount = in.readInt();
    }

    public static final Creator<RepeaterClassModel> CREATOR = new Creator<RepeaterClassModel>() {
        @Override
        public RepeaterClassModel createFromParcel(Parcel in) {
            return new RepeaterClassModel(in);
        }

        @Override
        public RepeaterClassModel[] newArray(int size) {
            return new RepeaterClassModel[size];
        }
    };

    public String getRepeaterClassID() {
        return repeaterClassID;
    }

    public void setRepeaterClassID(String repeaterClassID) {
        this.repeaterClassID = repeaterClassID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(repeaterClassID);
        dest.writeString(className);
        dest.writeInt(studentCount);
    }
}

