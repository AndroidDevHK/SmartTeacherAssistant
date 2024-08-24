package com.nextgen.hasnatfyp;

import android.os.Parcel;
import android.os.Parcelable;

public class StudentEvaluationModel implements Parcelable {
    private String evalName;
    private String evalObtMarks;
    private String evalTMarks;

    public StudentEvaluationModel(String evalName, String evalObtMarks, String evalTMarks) {
        this.evalName = evalName;
        this.evalObtMarks = evalObtMarks;
        this.evalTMarks = evalTMarks;
    }

    protected StudentEvaluationModel(Parcel in) {
        evalName = in.readString();
        evalObtMarks = in.readString();
        evalTMarks = in.readString();
    }

    public static final Creator<StudentEvaluationModel> CREATOR = new Creator<StudentEvaluationModel>() {
        @Override
        public StudentEvaluationModel createFromParcel(Parcel in) {
            return new StudentEvaluationModel(in);
        }

        @Override
        public StudentEvaluationModel[] newArray(int size) {
            return new StudentEvaluationModel[size];
        }
    };


    public String getEvalName() {
        return evalName;
    }

    public void setEvalName(String evalName) {
        this.evalName = evalName;
    }

    public String getEvalObtMarks() {
        return evalObtMarks;
    }

    public void setEvalObtMarks(String evalObtMarks) {
        this.evalObtMarks = evalObtMarks;
    }

    public String getEvalTMarks() {
        return evalTMarks;
    }

    public void setEvalTMarks(String evalTMarks) {
        this.evalTMarks = evalTMarks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(evalName);
        dest.writeString(evalObtMarks);
        dest.writeString(evalTMarks);
    }
}
