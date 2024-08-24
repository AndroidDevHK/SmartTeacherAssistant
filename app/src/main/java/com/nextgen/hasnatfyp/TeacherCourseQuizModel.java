package com.nextgen.hasnatfyp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TeacherCourseQuizModel implements Parcelable {
    private String courseId;
    private String courseName;  // New field for course name
    private String quizId;
    private String availableWhen;
    private String quizDuration;
    private String questionWeightage;
    private List<String> studentRollNumbers;
    private List<QuizStudentAttemptedQuestionsModel> studentResponses;
    private List<QuizModel> quizQuestions;
    private boolean isSubmitted;  // Field to indicate if the quiz has been submitted

    // Constructor
    public TeacherCourseQuizModel(String courseId, String courseName, String quizId, String availableWhen,
                                  String quizDuration, String questionWeightage, List<String> studentRollNumbers,
                                  List<QuizStudentAttemptedQuestionsModel> studentResponses, List<QuizModel> quizQuestions) {
        this.courseId = courseId;
        this.courseName = courseName;  // Initialize courseName
        this.quizId = quizId;
        this.availableWhen = availableWhen;
        this.quizDuration = quizDuration;
        this.questionWeightage = questionWeightage;
        this.studentRollNumbers = studentRollNumbers;
        this.studentResponses = studentResponses;
        this.quizQuestions = quizQuestions;
        this.isSubmitted = false; // Initialize as false by default
    }

    // Parcelable implementation
    protected TeacherCourseQuizModel(Parcel in) {
        courseId = in.readString();
        courseName = in.readString();  // Read courseName from parcel
        quizId = in.readString();
        availableWhen = in.readString();
        quizDuration = in.readString();
        questionWeightage = in.readString();
        studentRollNumbers = in.createStringArrayList();
        studentResponses = new ArrayList<>();
        in.readTypedList(studentResponses, QuizStudentAttemptedQuestionsModel.CREATOR);
        quizQuestions = new ArrayList<>();
        in.readTypedList(quizQuestions, QuizModel.CREATOR);
        isSubmitted = in.readByte() != 0;  // isSubmitted is true if byte != 0
    }

    public static final Creator<TeacherCourseQuizModel> CREATOR = new Creator<TeacherCourseQuizModel>() {
        @Override
        public TeacherCourseQuizModel createFromParcel(Parcel in) {
            return new TeacherCourseQuizModel(in);
        }

        @Override
        public TeacherCourseQuizModel[] newArray(int size) {
            return new TeacherCourseQuizModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseId);
        dest.writeString(courseName);  // Write courseName to parcel
        dest.writeString(quizId);
        dest.writeString(availableWhen);
        dest.writeString(quizDuration);
        dest.writeString(questionWeightage);
        dest.writeStringList(studentRollNumbers);
        dest.writeTypedList(studentResponses);
        dest.writeTypedList(quizQuestions);
        dest.writeByte((byte) (isSubmitted ? 1 : 0));  // Write isSubmitted as byte, where 1 represents true
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {  // New getter for courseName
        return courseName;
    }

    public void setCourseName(String courseName) {  // New setter for courseName
        this.courseName = courseName;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getAvailableWhen() {
        return availableWhen;
    }

    public void setAvailableWhen(String availableWhen) {
        this.availableWhen = availableWhen;
    }

    public String getQuizDuration() {
        return quizDuration;
    }

    public void setQuizDuration(String quizDuration) {
        this.quizDuration = quizDuration;
    }

    public String getQuestionWeightage() {
        return questionWeightage;
    }

    public void setQuestionWeightage(String questionWeightage) {
        this.questionWeightage = questionWeightage;
    }

    public List<String> getStudentRollNumbers() {
        return studentRollNumbers;
    }

    public void setStudentRollNumbers(List<String> studentRollNumbers) {
        this.studentRollNumbers = studentRollNumbers;
    }

    public List<QuizStudentAttemptedQuestionsModel> getStudentResponses() {
        return studentResponses;
    }

    public void setStudentResponses(List<QuizStudentAttemptedQuestionsModel> studentResponses) {
        this.studentResponses = studentResponses;
    }

    public List<QuizModel> getQuizQuestions() {
        return quizQuestions;
    }

    public void setQuizQuestions(List<QuizModel> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }
}
