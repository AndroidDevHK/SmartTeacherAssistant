package com.nextgen.hasnatfyp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class UserGreetingManager {

    @SuppressLint("SetTextI18n")
    public static void updateWelcomeText(Context context, TextView welcomeText) {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context);
        String adminName = userInstituteModel.getAdminName();
        welcomeText.setText("Welcome, " + adminName);
    }
    @SuppressLint("SetTextI18n")
    public static void updateWelcomeTextTeacher(Context context, TextView welcomeText, TextView campusName) {
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(context);
        String TeacherName = teacherInstanceModel.getTeacherName();
        String CampusName = teacherInstanceModel.getInstituteName();
        campusName.setText(CampusName);
        welcomeText.setText("Welcome, " + TeacherName);
    }

    @SuppressLint("SetTextI18n")
    public static void updateWelcomeTextStudent(StudentDashboardActivity studentDashboardActivity, TextView welcomeText) {
        welcomeText.setText("Welcome, " + StudentSessionInfo.getInstance(studentDashboardActivity).getStudentName());

    }

    public static void updateInstituteName(AdminDashboardActivity adminDashboardActivity, TextView welcomeText) {
        welcomeText.setVisibility(View.VISIBLE);
        welcomeText.setText(UserInstituteModel.getInstance(adminDashboardActivity).getCampusName());

    }
}
