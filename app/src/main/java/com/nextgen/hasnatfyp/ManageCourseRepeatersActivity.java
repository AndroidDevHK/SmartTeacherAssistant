package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import Display_Course_Repeaters_Activity.DisplayCourseRepeaters;
import Display_Course_Repeaters_Activity.ViewClassCourseRepeaters;
import Display_Course_Students_Evaluation_Activity.DisplayCourseStudentsEvaluationActivity;
import View_Classes_For_Repeaters_Selection_Activity.SelectClassForRepeatersAcitvity;

public class ManageCourseRepeatersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_course_repeaters);

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        setCardDetails();
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Manage Repeaters", true);
    }

    @SuppressLint("SetTextI18n")
    private void setCardDetails() {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(this);
        if (userInstituteModel != null) {
            String className = userInstituteModel.getClassName();
            String courseName = getIntent().getStringExtra("courseName");


            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);
            classNameTextView.setText(className);
            courseNameTextView.setText(courseName);
        }
    }
    public void OpenAddCourseRepeatersActivity(View view) {
        Intent intent = new Intent(ManageCourseRepeatersActivity.this, SelectClassForRepeatersAcitvity.class);
        startActivity(intent);
    }

    public void OpenViewCourseRepeatersActivity(View view) {
        Intent intent = new Intent(ManageCourseRepeatersActivity.this, ViewClassCourseRepeaters.class);
        startActivity(intent);
    }
}