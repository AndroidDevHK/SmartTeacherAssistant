package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Add_View_Semester_Activity.SemesterModel;
import DisplayInstituteTeachersForLoginActivity.InstituteTeacherModel;
import Display_Institute_Teachers_To_Assign_Him_Course_Activity.TeacherModel;
import View_Class_Courses_Activity.CourseModel;
import View_Classes_For_Repeaters_Selection_Activity.RepeaterClassModel;

public class UserInstituteModel {

    private static final String USER_SHARED_PREFS = "USER_SHARED_PREFS";
    private static final String KEY_INSTITUTE_ID = "institute_id";
    private static final String KEY_SEMESTER_ID = "semester_id";
    private static final String KEY_CLASS_NAME = "class_name";
    private static final String KEY_SEMESTER_NAME = "semester_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_CLASS_ID = "class_id";
    private static final String KEY_COURSE_ID = "course_id";
    private static final String KEY_SOLO_USER_ID = "solo_user_id";
    private static final String KEY_IS_SOLO_USER = "is_solo_user";
    private static final String KEY_COURSE_LIST = "course_list";
    private static final String KEY_TEACHER_LIST = "teacher_list";
    private static final String KEY_SEMESTERS = "semesters";
    private static final String KEY_REPEATER_CLASS_LIST = "repeater_class_list";
    private static final String KEY_INSTITUTE_TEACHER_LIST = "institute_teacher_list";
    private static final String KEY_ADMIN_NAME = "admin_name";
    private static final String KEY_CAMPUS_NAME = "campus_name";

    private static UserInstituteModel instance;
    private Context context;

    private String instituteId;
    private String semesterId;
    private String className;
    private String semesterName;
    private String username;
    private String classId;
    private String courseId;
    private String soloUserId;
    private boolean isSoloUser;
    private List<CourseModel> courseList;
    private List<TeacherModel> teacherList;
    private List<SemesterModel> semesters;
    private List<RepeaterClassModel> repeaterClassList;
    private List<InstituteTeacherModel> instituteTeacherList;
    private String adminName;  // Admin name field
    private String campusName;

    private UserInstituteModel(Context context) {
        this.context = context.getApplicationContext();
        loadUserData();
    }
    public String getCampusName() {
        if (campusName == null || campusName.isEmpty()) {
            loadCampusNameFromPrefs();
        }
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
        saveCampusNameToPrefs(campusName);
    }

    private void saveCampusNameToPrefs(String campusName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CAMPUS_NAME, campusName);
        editor.apply();
    }

    private void loadCampusNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        campusName = sharedPreferences.getString(KEY_CAMPUS_NAME, "");
    }
    public static synchronized UserInstituteModel getInstance(Context context) {
        if (instance == null) {
            instance = new UserInstituteModel(context);
        }
        return instance;
    }
    public String getAdminName() {
        if (adminName == null || adminName.isEmpty()) {
            loadAdminNameFromPrefs();
        }
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
        saveAdminNameToPrefs(adminName);
    }
    private void saveAdminNameToPrefs(String adminName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ADMIN_NAME, adminName);
        editor.apply();
    }

    private void loadAdminNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        adminName = sharedPreferences.getString(KEY_ADMIN_NAME, "");
    }
    public String getInstituteId() {
        if (instituteId == null || instituteId.isEmpty()) {
            loadInstituteIdFromPrefs();
        }
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
        saveInstituteIdToPrefs(instituteId);
    }

    public String getSemesterId() {
        if (semesterId == null || semesterId.isEmpty()) {
            loadSemesterIdFromPrefs();
        }
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
        saveSemesterIdToPrefs(semesterId);
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

    public String getSemesterName() {
        if (semesterName == null || semesterName.isEmpty()) {
            loadSemesterNameFromPrefs();
        }
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
        saveSemesterNameToPrefs(semesterName);
    }

    public String getUsername() {
        if (username == null || username.isEmpty()) {
            loadUsernameFromPrefs();
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        saveUsernameToPrefs(username);
    }

    public String getClassId() {
        if (classId == null || classId.isEmpty()) {
            loadClassIdFromPrefs();
        }
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
        saveClassIdToPrefs(classId);
    }

    public String getCourseId() {
        if (courseId == null || courseId.isEmpty()) {
            loadCourseIdFromPrefs();
        }
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
        saveCourseIdToPrefs(courseId);
    }

    public String getSoloUserId() {
        if (soloUserId == null || soloUserId.isEmpty()) {
            loadSoloUserIdFromPrefs();
        }
        return soloUserId;
    }

    public void setSoloUserId(String soloUserId) {
        this.soloUserId = soloUserId;
        saveSoloUserIdToPrefs(soloUserId);
    }

    public boolean isSoloUser() {
        return isSoloUser;
    }

    public void setSoloUser(boolean soloUser) {
        isSoloUser = soloUser;
        saveIsSoloUserToPrefs(soloUser);
    }

    public List<CourseModel> getCourseList() {
        if (courseList == null) {
            loadCourseListFromPrefs();
        }
        return courseList;
    }

    public void setCourseList(List<CourseModel> courseList) {
        this.courseList = courseList;
        saveCourseListToPrefs(courseList);
    }

    public List<TeacherModel> getTeacherList() {
        if (teacherList == null) {
            loadTeacherListFromPrefs();
        }
        return teacherList;
    }

    public void setTeacherList(List<TeacherModel> teacherList) {
        this.teacherList = teacherList;
        saveTeacherListToPrefs(teacherList);
    }

    public List<SemesterModel> getSemesters() {
        if (semesters == null) {
            loadSemestersFromPrefs();
        }
        return semesters;
    }

    public void setSemesters(List<SemesterModel> semesters) {
        this.semesters = semesters;
        saveSemestersToPrefs(semesters);
    }

    public List<RepeaterClassModel> getRepeaterClassList() {
        if (repeaterClassList == null) {
            loadRepeaterClassListFromPrefs();
        }
        return repeaterClassList;
    }

    public void setRepeaterClassList(List<RepeaterClassModel> repeaterClassList) {
        this.repeaterClassList = repeaterClassList;
        saveRepeaterClassListToPrefs(repeaterClassList);
    }

    public List<InstituteTeacherModel> getInstituteTeacherList() {
        if (instituteTeacherList == null) {
            loadInstituteTeacherListFromPrefs();
        }
        return instituteTeacherList;
    }

    public void setInstituteTeacherList(List<InstituteTeacherModel> instituteTeacherList) {
        this.instituteTeacherList = instituteTeacherList;
        saveInstituteTeacherListToPrefs(instituteTeacherList);
    }

    private void saveInstituteIdToPrefs(String instituteId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_INSTITUTE_ID, instituteId);
        editor.apply();
    }

    private void loadInstituteIdFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        instituteId = sharedPreferences.getString(KEY_INSTITUTE_ID, "");
    }

    private void saveSemesterIdToPrefs(String semesterId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SEMESTER_ID, semesterId);
        editor.apply();
    }

    private void loadSemesterIdFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        semesterId = sharedPreferences.getString(KEY_SEMESTER_ID, "");
    }

    private void saveClassNameToPrefs(String className) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CLASS_NAME, className);
        editor.apply();
    }

    private void loadClassNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        className = sharedPreferences.getString(KEY_CLASS_NAME, "");
    }

    private void saveSemesterNameToPrefs(String semesterName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SEMESTER_NAME, semesterName);
        editor.apply();
    }

    private void loadSemesterNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        semesterName = sharedPreferences.getString(KEY_SEMESTER_NAME, "");
    }

    private void saveUsernameToPrefs(String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    private void loadUsernameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(KEY_USERNAME, "");
    }

    private void saveClassIdToPrefs(String classId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CLASS_ID, classId);
        editor.apply();
    }

    private void loadClassIdFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        classId = sharedPreferences.getString(KEY_CLASS_ID, "");
    }

    private void saveCourseIdToPrefs(String courseId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COURSE_ID, courseId);
        editor.apply();
    }

    private void loadCourseIdFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        courseId = sharedPreferences.getString(KEY_COURSE_ID, "");
    }

    private void saveSoloUserIdToPrefs(String soloUserId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SOLO_USER_ID, soloUserId);
        editor.apply();
    }

    private void loadSoloUserIdFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        soloUserId = sharedPreferences.getString(KEY_SOLO_USER_ID, "");
    }

    private void saveIsSoloUserToPrefs(boolean isSoloUser) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_SOLO_USER, isSoloUser);
        editor.apply();
    }

    private void loadIsSoloUserFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        isSoloUser = sharedPreferences.getBoolean(KEY_IS_SOLO_USER, false);
    }

    private void saveCourseListToPrefs(List<CourseModel> courseList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String courseListJson = gson.toJson(courseList);
        editor.putString(KEY_COURSE_LIST, courseListJson);
        editor.apply();
    }

    private void loadCourseListFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String courseListJson = sharedPreferences.getString(KEY_COURSE_LIST, "");
        Type type = new TypeToken<List<CourseModel>>() {}.getType();
        courseList = gson.fromJson(courseListJson, type);
        if (courseList == null) {
            courseList = new ArrayList<>();
        }
    }

    private void saveTeacherListToPrefs(List<TeacherModel> teacherList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String teacherListJson = gson.toJson(teacherList);
        editor.putString(KEY_TEACHER_LIST, teacherListJson);
        editor.apply();
    }

    private void loadTeacherListFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String teacherListJson = sharedPreferences.getString(KEY_TEACHER_LIST, "");
        Type type = new TypeToken<List<TeacherModel>>() {}.getType();
        teacherList = gson.fromJson(teacherListJson, type);
        if (teacherList == null) {
            teacherList = new ArrayList<>();
        }
    }

    private void saveSemestersToPrefs(List<SemesterModel> semesters) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String semestersJson = gson.toJson(semesters);
        editor.putString(KEY_SEMESTERS, semestersJson);
        editor.apply();
    }

    private void loadSemestersFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String semestersJson = sharedPreferences.getString(KEY_SEMESTERS, "");
        Type type = new TypeToken<List<SemesterModel>>() {}.getType();
        semesters = gson.fromJson(semestersJson, type);
        if (semesters == null) {
            semesters = new ArrayList<>();
        }
    }

    private void saveRepeaterClassListToPrefs(List<RepeaterClassModel> repeaterClassList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String repeaterClassListJson = gson.toJson(repeaterClassList);
        editor.putString(KEY_REPEATER_CLASS_LIST, repeaterClassListJson);
        editor.apply();
    }

    private void loadRepeaterClassListFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String repeaterClassListJson = sharedPreferences.getString(KEY_REPEATER_CLASS_LIST, "");
        Type type = new TypeToken<List<RepeaterClassModel>>() {}.getType();
        repeaterClassList = gson.fromJson(repeaterClassListJson, type);
        if (repeaterClassList == null) {
            repeaterClassList = new ArrayList<>();
        }
    }

    private void saveInstituteTeacherListToPrefs(List<InstituteTeacherModel> instituteTeacherList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String instituteTeacherListJson = gson.toJson(instituteTeacherList);
        editor.putString(KEY_INSTITUTE_TEACHER_LIST, instituteTeacherListJson);
        editor.apply();
    }

    private void loadInstituteTeacherListFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String instituteTeacherListJson = sharedPreferences.getString(KEY_INSTITUTE_TEACHER_LIST, "");
        Type type = new TypeToken<List<InstituteTeacherModel>>() {}.getType();
        instituteTeacherList = gson.fromJson(instituteTeacherListJson, type);
        if (instituteTeacherList == null) {
            instituteTeacherList = new ArrayList<>();
        }
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);

        instituteId = sharedPreferences.getString(KEY_INSTITUTE_ID, "");
        semesterId = sharedPreferences.getString(KEY_SEMESTER_ID, "");
        className = sharedPreferences.getString(KEY_CLASS_NAME, "");
        semesterName = sharedPreferences.getString(KEY_SEMESTER_NAME, "");
        username = sharedPreferences.getString(KEY_USERNAME, "");
        classId = sharedPreferences.getString(KEY_CLASS_ID, "");
        courseId = sharedPreferences.getString(KEY_COURSE_ID, "");
        soloUserId = sharedPreferences.getString(KEY_SOLO_USER_ID, "");
        isSoloUser = sharedPreferences.getBoolean(KEY_IS_SOLO_USER, false);
        adminName = sharedPreferences.getString(KEY_ADMIN_NAME, "");  // Loading Admin Name
        campusName = sharedPreferences.getString(KEY_CAMPUS_NAME, "");

        Gson gson = new Gson();

        String courseListJson = sharedPreferences.getString(KEY_COURSE_LIST, "");
        Type courseListType = new TypeToken<ArrayList<CourseModel>>() {}.getType();
        courseList = gson.fromJson(courseListJson, courseListType);
        if (courseList == null) {
            courseList = new ArrayList<>();
        }

        String teacherListJson = sharedPreferences.getString(KEY_TEACHER_LIST, "");
        Type teacherListType = new TypeToken<ArrayList<TeacherModel>>() {}.getType();
        teacherList = gson.fromJson(teacherListJson, teacherListType);
        if (teacherList == null) {
            teacherList = new ArrayList<>();
        }

        String semestersJson = sharedPreferences.getString(KEY_SEMESTERS, "");
        Type semestersType = new TypeToken<ArrayList<SemesterModel>>() {}.getType();
        semesters = gson.fromJson(semestersJson, semestersType);
        if (semesters == null) {
            semesters = new ArrayList<>();
        }

        String repeaterClassListJson = sharedPreferences.getString(KEY_REPEATER_CLASS_LIST, "");
        Type repeaterClassListType = new TypeToken<ArrayList<RepeaterClassModel>>() {}.getType();
        repeaterClassList = gson.fromJson(repeaterClassListJson, repeaterClassListType);
        if (repeaterClassList == null) {
            repeaterClassList = new ArrayList<>();
        }

        String instituteTeacherListJson = sharedPreferences.getString(KEY_INSTITUTE_TEACHER_LIST, "");
        Type instituteTeacherListType = new TypeToken<ArrayList<InstituteTeacherModel>>() {}.getType();
        instituteTeacherList = gson.fromJson(instituteTeacherListJson, instituteTeacherListType);
        if (instituteTeacherList == null) {
            instituteTeacherList = new ArrayList<>();
        }
    }
}

