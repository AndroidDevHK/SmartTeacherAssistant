package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import Display_Teacher_Semesters_Activity.DisplayTeacherSemesterActivity;

public class TeacherDashboardActivity extends AppCompatActivity {
    private TextView welcomeText,campusName;
    private ImageView logoImageView;
    private LinearLayout welcomeLayout, cardLayoutTop,cardLayoutTopB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar); // Set the toolbar as the activity's app bar
        SetupToolbar(toolbar);

        initializeViews();
        applyFadeInAnimation();
        UserGreetingManager.updateWelcomeTextTeacher(this, welcomeText,campusName);
    }

    private void initializeViews() {
        campusName = findViewById(R.id.campusName);
        welcomeText = findViewById(R.id.welcomeText);
        logoImageView = findViewById(R.id.imglogo);
        welcomeLayout = findViewById(R.id.welcomeLayout);
        cardLayoutTop = findViewById(R.id.cardLayoutTop);
        cardLayoutTopB = findViewById(R.id.cardLayoutTopB);
    }

    private void applyFadeInAnimation() {
        // Load fade-in animation
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animation to views
        findViewById(R.id.customToolbar).startAnimation(fadeInAnimation);
        logoImageView.startAnimation(fadeInAnimation);
        welcomeLayout.startAnimation(fadeInAnimation);
        cardLayoutTop.startAnimation(fadeInAnimation);
        cardLayoutTopB.startAnimation(fadeInAnimation);
        campusName.startAnimation(fadeInAnimation);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_change_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reset_password) {
            resetPassword();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


        private void resetPassword() {
            if (NetworkUtils.isInternetConnected(this)) {
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please Connect Internet to Continue..", Toast.LENGTH_LONG).show();
            }

        }


    public void onLogoutClicked(View view) {
        LogoutManager.confirmAndLogout(this, "TEACHER_SHARED_PREFS");
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Teacher Dashboard", false);
    }

    public void ViewTeacherClasses(View view) {
        Intent intent = new Intent(this, DisplayTeacherSemesterActivity.class);
        startActivity(intent);
    }

    public void ViewOfflineMarkedAttendance(View view) {
        Intent intent = new Intent(this, DisplayOfflineMarkedAttendanceListActivity.class);
        startActivity(intent);
    }

    public void ViewQuizzesResults(View view) {
        Intent intent = new Intent(this, DisplayTeacherCourseQuizzesActivity.class);
        startActivity(intent);
    }

    public void ViewOfflineMarkedEvaluations(View view) {
        Intent intent = new Intent(this, DisplayOfflineAddedEvaluationListActivity.class);
        startActivity(intent);
    }
}
