package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SelectSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_setup);
    }

    public void openInstituteMainMenuDashboardActivity(View view) {
        Intent intent = new Intent(this, DisplayTeacherCourseQuizzesActivity.class);
        startActivity(intent);
    }

    public void ViewSoloUserDashboardActivity(View view) {
        Intent intent = new Intent(this, DisplaySoloUserDashboardActivity.class);
        startActivity(intent);
    }
}