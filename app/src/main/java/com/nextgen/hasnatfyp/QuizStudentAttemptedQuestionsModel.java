package com.nextgen.hasnatfyp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class QuizStudentAttemptedQuestionsModel implements Parcelable {
    private String studentRollNo;
    private Map<String, String> mapQuestionsAttempted;
    private String studentName; // New field

    public QuizStudentAttemptedQuestionsModel() {
        // Default constructor required for Firestore
    }

    public QuizStudentAttemptedQuestionsModel(String studentRollNo, Map<String, String> mapQuestionsAttempted) {
        this.studentRollNo = studentRollNo;
        this.mapQuestionsAttempted = mapQuestionsAttempted;
    }

    protected QuizStudentAttemptedQuestionsModel(Parcel in) {
        studentRollNo = in.readString();
        int size = in.readInt();
        mapQuestionsAttempted = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            mapQuestionsAttempted.put(key, value);
        }
        studentName = in.readString(); // Read the studentName
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studentRollNo);
        dest.writeInt(mapQuestionsAttempted.size());
        for (Map.Entry<String, String> entry : mapQuestionsAttempted.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeString(studentName); // Write the studentName
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QuizStudentAttemptedQuestionsModel> CREATOR = new Creator<QuizStudentAttemptedQuestionsModel>() {
        @Override
        public QuizStudentAttemptedQuestionsModel createFromParcel(Parcel in) {
            return new QuizStudentAttemptedQuestionsModel(in);
        }

        @Override
        public QuizStudentAttemptedQuestionsModel[] newArray(int size) {
            return new QuizStudentAttemptedQuestionsModel[size];
        }
    };

    // Getters and setters
    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public Map<String, String> getMapQuestionsAttempted() {
        return mapQuestionsAttempted;
    }

    public void setMapQuestionsAttempted(Map<String, String> mapQuestionsAttempted) {
        this.mapQuestionsAttempted = mapQuestionsAttempted;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        return "QuizStudentAttemptedQuestionsModel{" +
                "studentRollNo='" + studentRollNo + '\'' +
                ", mapQuestionsAttempted=" + mapQuestionsAttempted +
                ", studentName='" + studentName + '\'' +
                '}';
    }
}
