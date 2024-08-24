package Display_Course_Evaluations_List_Activity;

import android.os.Parcel;
import android.os.Parcelable;

import com.nextgen.hasnatfyp.CourseEvaluationDetailsModel;

import java.util.List;

public class CourseEvaluationInfoModel implements Parcelable {
    private String evaluationName;
    private double evaluationTotalMarks; // Change type to double
    private String date;
    private List<CourseEvaluationDetailsModel> evaluationInfoList;
    private String evalId; // Add EvalId attribute

    public CourseEvaluationInfoModel(String evaluationName, double evaluationTotalMarks, String date, List<CourseEvaluationDetailsModel> evaluationInfoList, String evalId) {
        this.evaluationName = evaluationName;
        this.evaluationTotalMarks = evaluationTotalMarks;
        this.date = date;
        this.evaluationInfoList = evaluationInfoList;
        this.evalId = evalId; // Initialize EvalId
    }

    protected CourseEvaluationInfoModel(Parcel in) {
        evaluationName = in.readString();
        evaluationTotalMarks = in.readDouble(); // Read double
        date = in.readString();
        evaluationInfoList = in.createTypedArrayList(CourseEvaluationDetailsModel.CREATOR);
        evalId = in.readString(); // Read EvalId
    }

    public static final Creator<CourseEvaluationInfoModel> CREATOR = new Creator<CourseEvaluationInfoModel>() {
        @Override
        public CourseEvaluationInfoModel createFromParcel(Parcel in) {
            return new CourseEvaluationInfoModel(in);
        }

        @Override
        public CourseEvaluationInfoModel[] newArray(int size) {
            return new CourseEvaluationInfoModel[size];
        }
    };

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
    }

    public double getEvaluationTotalMarks() {
        return evaluationTotalMarks;
    }

    public void setEvaluationTotalMarks(double evaluationTotalMarks) {
        this.evaluationTotalMarks = evaluationTotalMarks;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CourseEvaluationDetailsModel> getEvaluationInfoList() {
        return evaluationInfoList;
    }

    public void setEvaluationInfoList(List<CourseEvaluationDetailsModel> evaluationInfoList) {
        this.evaluationInfoList = evaluationInfoList;
    }

    public String getEvalId() {
        return evalId;
    }

    public void setEvalId(String evalId) {
        this.evalId = evalId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(evaluationName);
        dest.writeDouble(evaluationTotalMarks); // Write double
        dest.writeString(date);
        dest.writeTypedList(evaluationInfoList);
        dest.writeString(evalId); // Write EvalId
    }
}
