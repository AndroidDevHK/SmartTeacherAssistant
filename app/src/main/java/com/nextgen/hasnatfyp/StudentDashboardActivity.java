package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class StudentDashboardActivity extends AppCompatActivity {
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard2);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        welcomeText = findViewById(R.id.welcomeText);
        UserGreetingManager.updateWelcomeTextStudent(this, welcomeText);
    }
    public void onLogoutClicked(View view) {
        LogoutManager.confirmAndLogout(this,"STUDENT_SHARED_PREFS");
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Student Dashboard Dashboard", true);
    }

    public void ViewAvailableQuizzes(View view) {
        Intent intent = new Intent(this, AvailableQuizzesActivity.class);
        startActivity(intent);
    }

    public void ViewStudentClasses(View view) {
        Intent intent = new Intent(this, DisplayStudentSemesterCourses.class);
        startActivity(intent);
    }

    public void ViewQuizResults(View view) {
        Intent intent = new Intent(this, ViewStudentQuizResultsActivity.class);
        startActivity(intent);
    }

}