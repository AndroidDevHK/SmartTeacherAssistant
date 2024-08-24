package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import Add_View_Semester_Activity.ManageSemesterActivity;
import Display_Teacher_Semesters_Activity.DisplayTeacherSemesterActivity;

public class DisplaySoloUserDashboardActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private LinearLayout welcomeLayout, cardLayoutTopA, cardLayoutTopB;
    private TextView welcomeText;
    private ImageView logoutIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_solo_user_dashboard);

        initializeViews();
        applyFadeInAnimation();
        UserGreetingManager.updateWelcomeText(this, welcomeText);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
    }

    private void initializeViews() {

        logoImageView = findViewById(R.id.imglogo);
        welcomeLayout = findViewById(R.id.welcomeLayout);
        cardLayoutTopA = findViewById(R.id.cardLayoutTopA);
        cardLayoutTopB = findViewById(R.id.cardLayoutTopB);
        welcomeText = findViewById(R.id.welcomeText);
        logoutIcon = findViewById(R.id.logoutIcon);
    }

    private void applyFadeInAnimation() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.customToolbar).startAnimation(fadeInAnimation);
        logoImageView.startAnimation(fadeInAnimation);
        welcomeLayout.startAnimation(fadeInAnimation);
        cardLayoutTopA.startAnimation(fadeInAnimation);
        cardLayoutTopB.startAnimation(fadeInAnimation);
    }
    public void onLogoutClicked(View view) {
        LogoutManager.confirmAndLogout(this,"USER_SHARED_PREFS");
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Admin Dashboard", true);
    }

    public void OpenManageSemesterActivity(View view) {
        Intent intent = new Intent(this, ManageSemesterActivity.class);
        startActivity(intent);
    }

    public void OpenViewTeacherClassesActivity(View view) {
        Intent intent = new Intent(this, DisplayTeacherSemesterActivity.class);
        startActivity(intent);
    }

    public void OpenOfflineMarkedAttendances(View view) {
        Intent intent = new Intent(this, DisplayOfflineMarkedAttendanceListActivity.class);
        startActivity(intent);
    }

    public void OpenOfflineAddedEvaluation(View view) {
        Intent intent = new Intent(this, DisplayOfflineAddedEvaluationListActivity.class);
        startActivity(intent);
    }
}