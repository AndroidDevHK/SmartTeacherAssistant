package com.nextgen.hasnatfyp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StudentQuizModel implements Parcelable {
    private String quizId;
    private String courseName; // Use courseName along with courseId
    private String courseId;  // Added courseId field
    private String availableWhen;
    private String quizDuration;
    private String questionWeightage;
    private List<QuizModel> quizModels;
    private List<StudentAttemptedQuestionModel> attemptedQuestions;

    public StudentQuizModel(String quizId, String courseName, String availableWhen, String quizDuration, String questionWeightage, List<QuizModel> quizModels, List<StudentAttemptedQuestionModel> attemptedQuestions) {
        this.quizId = quizId;
        this.courseName = courseName; // Initialize courseName
        this.availableWhen = availableWhen;
        this.quizDuration = quizDuration;
        this.questionWeightage = questionWeightage;
        this.quizModels = quizModels;
        this.attemptedQuestions = attemptedQuestions;
    }

    protected StudentQuizModel(Parcel in) {
        quizId = in.readString();
        courseName = in.readString();
        courseId = in.readString();  // Read courseId from parcel
        availableWhen = in.readString();
        quizDuration = in.readString();
        questionWeightage = in.readString();
        quizModels = in.createTypedArrayList(QuizModel.CREATOR);
        attemptedQuestions = in.createTypedArrayList(StudentAttemptedQuestionModel.CREATOR);
    }

    public static final Creator<StudentQuizModel> CREATOR = new Creator<StudentQuizModel>() {
        @Override
        public StudentQuizModel createFromParcel(Parcel in) {
            return new StudentQuizModel(in);
        }

        @Override
        public StudentQuizModel[] newArray(int size) {
            return new StudentQuizModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(quizId);
        parcel.writeString(courseName);
        parcel.writeString(courseId);
        parcel.writeString(availableWhen);
        parcel.writeString(quizDuration);
        parcel.writeString(questionWeightage);
        parcel.writeTypedList(quizModels);
        parcel.writeTypedList(attemptedQuestions);
    }


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getAvailableWhen() {
        return availableWhen;
    }

    public String getQuizDuration() {
        return quizDuration;
    }

    public String getQuestionWeightage() {
        return questionWeightage;
    }

    public List<QuizModel> getQuizModels() {
        return quizModels;
    }

    public void setQuizModels(List<QuizModel> quizModels) {
        this.quizModels = quizModels;
    }

    public List<StudentAttemptedQuestionModel> getAttemptedQuestions() {
        return attemptedQuestions;
    }

    public void setAttemptedQuestions(List<StudentAttemptedQuestionModel> attemptedQuestions) {
        this.attemptedQuestions = attemptedQuestions;
    }

    @Override
    public String toString() {
        return "StudentQuizModel{" +
                "quizId='" + quizId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseId='" + courseId + '\'' +
                ", availableWhen='" + availableWhen + '\'' +
                ", quizDuration='" + quizDuration + '\'' +
                ", questionWeightage='" + questionWeightage + '\'' +
                ", quizModels=" + quizModels +
                ", attemptedQuestions=" + attemptedQuestions +
                '}';
    }
}
