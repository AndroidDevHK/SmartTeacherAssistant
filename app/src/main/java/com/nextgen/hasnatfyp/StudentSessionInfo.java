package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.SharedPreferences;

public class StudentSessionInfo {
    private static final String STUDENT_SHARED_PREFS = "STUDENT_SHARED_PREFS";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_CLASS_ID = "class_id";
    private static final String KEY_INSTITUTE_ID = "institute_id";
    private static final String KEY_SEMESTER_ID = "semester_id";
    private static final String KEY_STUDENT_ROLL_NO = "student_roll_no";
    private static final String KEY_STUDENT_NAME = "student_name";
    private static final String KEY_COURSE_NAME = "course_name";
    private static final String KEY_CLASS_NAME = "class_name"; // New field for class name

    private static StudentSessionInfo instance;
    private Context context;

    private String studentID;
    private String classID;
    private String instituteID;
    private String semesterID;
    private String studentRollNo;
    private String studentName;
    private String courseName;
    private String className; // New field for class name

    // Private constructor to ensure Singleton pattern
    private StudentSessionInfo(Context context) {
        this.context = context.getApplicationContext();
    }

    // Singleton instance accessor
    public static synchronized StudentSessionInfo getInstance(Context context) {
        if (instance == null) {
            instance = new StudentSessionInfo(context);
        }
        return instance;
    }

    // Getter and setter methods with SharedPreferences integration
    public String getStudentID() {
        if (studentID == null) {
            loadStudentIDFromPrefs();
        }
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
        saveToPrefs(KEY_STUDENT_ID, studentID);
    }

    public String getClassID() {
        if (classID == null) {
            loadClassIDFromPrefs();
        }
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
        saveToPrefs(KEY_CLASS_ID, classID);
    }

    public String getInstituteID() {
        if (instituteID == null) {
            loadInstituteIDFromPrefs();
        }
        return instituteID;
    }

    public void setInstituteID(String instituteID) {
        this.instituteID = instituteID;
        saveToPrefs(KEY_INSTITUTE_ID, instituteID);
    }

    public String getSemesterID() {
        if (semesterID == null) {
            loadSemesterIDFromPrefs();
        }
        return semesterID;
    }

    public void setSemesterID(String semesterID) {
        this.semesterID = semesterID;
        saveToPrefs(KEY_SEMESTER_ID, semesterID);
    }

    public String getStudentRollNo() {
        if (studentRollNo == null) {
            loadStudentRollNoFromPrefs();
        }
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
        saveToPrefs(KEY_STUDENT_ROLL_NO, studentRollNo);
    }

    public String getStudentName() {
        if (studentName == null) {
            loadStudentNameFromPrefs();
        }
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
        saveToPrefs(KEY_STUDENT_NAME, studentName);
    }

    public String getCourseName() {
        if (courseName == null) {
            loadCourseNameFromPrefs();
        }
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
        saveToPrefs(KEY_COURSE_NAME, courseName);
    }

    public String getClassName() {
        if (className == null) {
            loadClassNameFromPrefs();
        }
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
        saveToPrefs(KEY_CLASS_NAME, className);
    }

    // Helper methods for SharedPreferences
    private void saveToPrefs(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void loadStudentIDFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        studentID = sharedPreferences.getString(KEY_STUDENT_ID, null);
    }

    private void loadClassIDFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        classID = sharedPreferences.getString(KEY_CLASS_ID, null);
    }

    private void loadInstituteIDFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        instituteID = sharedPreferences.getString(KEY_INSTITUTE_ID, null);
    }

    private void loadSemesterIDFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        semesterID = sharedPreferences.getString(KEY_SEMESTER_ID, null);
    }

    private void loadStudentRollNoFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        studentRollNo = sharedPreferences.getString(KEY_STUDENT_ROLL_NO, null);
    }

    private void loadStudentNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        studentName = sharedPreferences.getString(KEY_STUDENT_NAME, null);
    }

    private void loadCourseNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        courseName = sharedPreferences.getString(KEY_COURSE_NAME, null);
    }

    private void loadClassNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(STUDENT_SHARED_PREFS, Context.MODE_PRIVATE);
        className = sharedPreferences.getString(KEY_CLASS_NAME, null);
    }
}
