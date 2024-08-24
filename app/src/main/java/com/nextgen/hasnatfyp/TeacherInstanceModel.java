package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Display_Teacher_Semester_Classes_Acitivity.TeacherClassModel;

public class TeacherInstanceModel {

    private static final String TEACHER_SHARED_PREFS = "TEACHER_SHARED_PREFS";
    private static final String KEY_SEMESTER_NAME = "semester_name";
    private static final String KEY_SEMESTER_START_DATE = "semester_start_date";
    private static final String KEY_SEMESTER_END_DATE = "semester_end_date";
    private static final String KEY_OFFLINE_MODE = "offline_mode";
    private static final String KEY_COURSE_NAME = "course_name";
    private static final String KEY_CLASS_NAME = "class_name";
    private static final String KEY_ARE_REPEATERS = "are_repeaters";
    private static final String KEY_TEACHER_USERNAME = "teacher_username";
    private static final String KEY_TEACHER_NAME = "teacher_name";
    private static final String KEY_TEACHER_CLASSES_LIST = "teacher_classes_list";
    private static final String KEY_INSTITUTE_NAME = "institute_name";
    private static final String KEY_PAST_ATTENDANCE_PERMISSION = "past_attendance_permission";

    private static TeacherInstanceModel instance;
    private Context context;

    private String semesterName;
    private String semesterStartDate;
    private String semesterEndDate;
    private boolean isOfflineMode;
    private String courseName;
    private String className;
    private boolean areRepeaters;
    private String teacherUsername;
    private String teacherName;
    private List<TeacherClassModel> teacherClassesList;
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_QUALIFICATION = "qualification";
    private String department;
    private String qualification;
    private String instituteName;
    private boolean pastAttendancePermission;

    private TeacherInstanceModel(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized TeacherInstanceModel getInstance(Context context) {
        if (instance == null) {
            instance = new TeacherInstanceModel(context);
        }
        return instance;
    }

    public String getSemesterName() {
        if (semesterName == null || semesterName.isEmpty()) {
            loadSemesterNameFromPrefs();
        }
        return semesterName;
    }
    public boolean isPastAttendancePermission() {
        loadPastAttendancePermissionFromPrefs();
        return pastAttendancePermission;
    }
    public void setPastAttendancePermission(boolean pastAttendancePermission) {
        this.pastAttendancePermission = pastAttendancePermission;
        savePastAttendancePermissionToPrefs(pastAttendancePermission);
    }
    private void savePastAttendancePermissionToPrefs(boolean pastAttendancePermission) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PAST_ATTENDANCE_PERMISSION, pastAttendancePermission);
        editor.apply();
    }
    private void loadPastAttendancePermissionFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        pastAttendancePermission = sharedPreferences.getBoolean(KEY_PAST_ATTENDANCE_PERMISSION, false);
    }


    public String getInstituteName() {
        if (instituteName == null || instituteName.isEmpty()) {
            loadInstituteNameFromPrefs();
        }
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
        saveInstituteNameToPrefs(instituteName);
    }
    private void saveInstituteNameToPrefs(String instituteName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_INSTITUTE_NAME, instituteName);
        editor.apply();
    }

    private void loadInstituteNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        instituteName = sharedPreferences.getString(KEY_INSTITUTE_NAME, "");
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
        saveSemesterNameToPrefs(semesterName);
    }
    public String getDepartment() {
        if (department == null || department.isEmpty()) {
            loadDepartmentFromPrefs();
        }
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        saveDepartmentToPrefs(department);
    }

    public String getQualification() {
        if (qualification == null || qualification.isEmpty()) {
            loadQualificationFromPrefs();
        }
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
        saveQualificationToPrefs(qualification);
    }

    public String getSemesterStartDate() {
        if (semesterStartDate == null || semesterStartDate.isEmpty()) {
            loadSemesterStartDateFromPrefs();
        }
        return semesterStartDate;
    }
    private void saveDepartmentToPrefs(String department) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DEPARTMENT, department);
        editor.apply();
    }

    private void loadDepartmentFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        department = sharedPreferences.getString(KEY_DEPARTMENT, "");
    }

    private void saveQualificationToPrefs(String qualification) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_QUALIFICATION, qualification);
        editor.apply();
    }

    private void loadQualificationFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        qualification = sharedPreferences.getString(KEY_QUALIFICATION, "");
    }

    public void setSemesterStartDate(String semesterStartDate) {
        this.semesterStartDate = semesterStartDate;
        saveSemesterStartDateToPrefs(semesterStartDate);
    }

    public String getSemesterEndDate() {
        if (semesterEndDate == null || semesterEndDate.isEmpty()) {
            loadSemesterEndDateFromPrefs();
        }
        return semesterEndDate;
    }

    public void setSemesterEndDate(String semesterEndDate) {
        this.semesterEndDate = semesterEndDate;
        saveSemesterEndDateToPrefs(semesterEndDate);
    }

    public boolean isOfflineMode() {
        return isOfflineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        isOfflineMode = offlineMode;
        saveOfflineModeToPrefs(offlineMode);
    }

    public String getCourseName() {
        if (courseName == null || courseName.isEmpty()) {
            loadCourseNameFromPrefs();
        }
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
        saveCourseNameToPrefs(courseName);
    }

    public String getClassName() {
        if (className == null || className.isEmpty()) {
            loadClassNameFromPrefs();
        }
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
        saveClassNameToPrefs(className);
    }

    public boolean isAreRepeaters() {
        return areRepeaters;
    }

    public void setAreRepeaters(boolean areRepeaters) {
        this.areRepeaters = areRepeaters;
        saveAreRepeatersToPrefs(areRepeaters);
    }

    public String getTeacherUsername() {
        if (teacherUsername == null || teacherUsername.isEmpty()) {
            loadTeacherUsernameFromPrefs();
        }
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
        saveTeacherUsernameToPrefs(teacherUsername);
    }

    public String getTeacherName() {
        if (teacherName == null || teacherName.isEmpty()) {
            loadTeacherNameFromPrefs();
        }
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
        saveTeacherNameToPrefs(teacherName);
    }

    public List<TeacherClassModel> getTeacherClassesList() {
        if (teacherClassesList == null) {
            loadTeacherClassesListFromPrefs();
        }
        return teacherClassesList;
    }

    public void setTeacherClassesList(List<TeacherClassModel> teacherClassesList) {
        this.teacherClassesList = teacherClassesList;
        saveTeacherClassesListToPrefs(teacherClassesList);
    }

    private void saveSemesterNameToPrefs(String semesterName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SEMESTER_NAME, semesterName);
        editor.apply();
    }

    private void loadSemesterNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        semesterName = sharedPreferences.getString(KEY_SEMESTER_NAME, "");
    }

    private void saveSemesterStartDateToPrefs(String semesterStartDate) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SEMESTER_START_DATE, semesterStartDate);
        editor.apply();
    }

    private void loadSemesterStartDateFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        semesterStartDate = sharedPreferences.getString(KEY_SEMESTER_START_DATE, "");
    }

    private void saveSemesterEndDateToPrefs(String semesterEndDate) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SEMESTER_END_DATE, semesterEndDate);
        editor.apply();
    }

    private void loadSemesterEndDateFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        semesterEndDate = sharedPreferences.getString(KEY_SEMESTER_END_DATE, "");
    }

    private void saveOfflineModeToPrefs(boolean offlineMode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_OFFLINE_MODE, offlineMode);
        editor.apply();
    }

    private void loadOfflineModeFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        isOfflineMode = sharedPreferences.getBoolean(KEY_OFFLINE_MODE, false);
    }

    private void saveCourseNameToPrefs(String courseName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COURSE_NAME, courseName);
        editor.apply();
    }

    private void loadCourseNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        courseName = sharedPreferences.getString(KEY_COURSE_NAME, "");
    }

    private void saveClassNameToPrefs(String className) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CLASS_NAME, className);
        editor.apply();
    }

    private void loadClassNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        className = sharedPreferences.getString(KEY_CLASS_NAME, "");
    }

    private void saveAreRepeatersToPrefs(boolean areRepeaters) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ARE_REPEATERS, areRepeaters);
        editor.apply();
    }

    private void loadAreRepeatersFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        areRepeaters = sharedPreferences.getBoolean(KEY_ARE_REPEATERS, false);
    }

    private void saveTeacherUsernameToPrefs(String teacherUsername) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TEACHER_USERNAME, teacherUsername);
        editor.apply();
    }

    private void loadTeacherUsernameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        teacherUsername = sharedPreferences.getString(KEY_TEACHER_USERNAME, "");
    }

    private void saveTeacherNameToPrefs(String teacherName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TEACHER_NAME, teacherName);
        editor.apply();
    }

    private void loadTeacherNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        teacherName = sharedPreferences.getString(KEY_TEACHER_NAME, "");
    }

    private void saveTeacherClassesListToPrefs(List<TeacherClassModel> teacherClassesList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String teacherClassesListJson = gson.toJson(teacherClassesList);
        editor.putString(KEY_TEACHER_CLASSES_LIST, teacherClassesListJson);
        editor.apply();
    }

    private void loadTeacherClassesListFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String teacherClassesListJson = sharedPreferences.getString(KEY_TEACHER_CLASSES_LIST, "");
        Type type = new TypeToken<List<TeacherClassModel>>() {
        }.getType();
        teacherClassesList = gson.fromJson(teacherClassesListJson, type);
        if (teacherClassesList == null) {
            teacherClassesList = new ArrayList<>();
        }
    }

    public void clearSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TEACHER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        semesterName = null;
        semesterStartDate = null;
        semesterEndDate = null;
        isOfflineMode = false;
        courseName = null;
        className = null;
        areRepeaters = false;
        teacherUsername = null;
        teacherName = null;
        department = null;
        qualification = null;
        instituteName = null;
        teacherClassesList = null;
        pastAttendancePermission = false;


    }
}