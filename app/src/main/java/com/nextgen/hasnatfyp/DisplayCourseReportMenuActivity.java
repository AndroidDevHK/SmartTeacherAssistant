package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import Display_Complete_Course_Att_Eval_data_Activity.DisplayCompleteCourseStudentsDetailsActivity;
import Display_Course_Attendance_Activity.DisplayClassCourseAttendanceActivity;
import Display_Course_Students_Evaluation_Activity.DisplayCourseStudentsEvaluationActivity;

public class DisplayCourseReportMenuActivity extends AppCompatActivity {

    private boolean areRepeaters;
    private String courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_course_report_menu);

        if (getIntent().hasExtra("CourseID")) {
            areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
            courseID = getIntent().getStringExtra("CourseID");
            setClassAndCourseName();
        } else {
            finish();
        }

        setupButtonListeners();
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Course Reports", true);
    }
    @SuppressLint("SetTextI18n")
    private void setClassAndCourseName() {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);

            classNameTextView.setText(className);
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }

    private void setupButtonListeners() {
        CardView attendanceReportButton = findViewById(R.id.view_attendance_card);
        CardView evaluationReportButton = findViewById(R.id.view_evaluation_card);
        CardView completeReportButton = findViewById(R.id.view_complete_report);

        attendanceReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayCourseReportMenuActivity.this, DisplayClassCourseAttendanceActivity.class);
            intent.putExtra("CourseID", courseID);
            intent.putExtra("AreRepeaters", areRepeaters);
            startActivity(intent);
        });

        evaluationReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayCourseReportMenuActivity.this, DisplayCourseStudentsEvaluationActivity.class);
            intent.putExtra("CourseID", courseID);
            intent.putExtra("AreRepeaters", areRepeaters);
            startActivity(intent);
        });

        completeReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayCourseReportMenuActivity.this, DisplayCompleteCourseStudentsDetailsActivity.class);
            intent.putExtra("CourseID", courseID);
            intent.putExtra("AreRepeaters", areRepeaters);
            startActivity(intent);
        });
    }
}
