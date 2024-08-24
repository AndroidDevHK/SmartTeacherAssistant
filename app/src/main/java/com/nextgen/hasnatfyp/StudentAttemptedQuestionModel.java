package com.nextgen.hasnatfyp;

import android.os.Parcel;
import android.os.Parcelable;

public class StudentAttemptedQuestionModel implements Parcelable {
    private String questionId;
    private String selectedOption;

    public StudentAttemptedQuestionModel(String questionId, String selectedOption) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
    }

    protected StudentAttemptedQuestionModel(Parcel in) {
        questionId = in.readString();
        selectedOption = in.readString();
    }

    public static final Creator<StudentAttemptedQuestionModel> CREATOR = new Creator<StudentAttemptedQuestionModel>() {
        @Override
        public StudentAttemptedQuestionModel createFromParcel(Parcel in) {
            return new StudentAttemptedQuestionModel(in);
        }

        @Override
        public StudentAttemptedQuestionModel[] newArray(int size) {
            return new StudentAttemptedQuestionModel[size];
        }
    };

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    @Override
    public String toString() {
        return "StudentAttemptedQuestionModel{" +
                "questionId='" + questionId + '\'' +
                ", selectedOption='" + selectedOption + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(questionId);
        dest.writeString(selectedOption);
    }
}
