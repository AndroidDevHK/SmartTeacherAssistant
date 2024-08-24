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

import Add_View_Semester_Activity.ManageSemesterActivity;
import DisplayInstituteTeachersForLoginActivity.DisplayInstituteTeachersListActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView welcomeText,instituteNameText;
    private ImageView logoImageView;
    private LinearLayout welcomeLayout, cardLayoutTop, cardLayoutTop2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar); // Set the toolbar as the activity's app bar
        SetupToolbar(toolbar);

        initializeViews();
        applyFadeInAnimation();
        UserGreetingManager.updateWelcomeText(this, welcomeText);

        if (!UserInstituteModel.getInstance(this).isSoloUser()) {
            UserGreetingManager.updateInstituteName(this, instituteNameText);
        }



    }

    private void initializeViews() {
        instituteNameText = findViewById(R.id.instituteNameText);
        welcomeText = findViewById(R.id.welcomeText);
        logoImageView = findViewById(R.id.imglogo);
        welcomeLayout = findViewById(R.id.welcomeLayout);
        cardLayoutTop = findViewById(R.id.cardLayoutTop);
        cardLayoutTop2 = findViewById(R.id.cardLayoutTop2);
    }

    private void applyFadeInAnimation() {
        // Load fade-in animation
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animation to views
        findViewById(R.id.customToolbar).startAnimation(fadeInAnimation);
        logoImageView.startAnimation(fadeInAnimation);
        welcomeLayout.startAnimation(fadeInAnimation);
        cardLayoutTop.startAnimation(fadeInAnimation);
        cardLayoutTop2.startAnimation(fadeInAnimation);
        instituteNameText.startAnimation(fadeInAnimation);
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
            // Handle the reset password action
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
        LogoutManager.confirmAndLogout(this, "USER_SHARED_PREFS");
    }

    public void OpenRegisterStaffActivity(View view) {
        if (NetworkUtils.isInternetConnected(this)) {
            Intent intent = new Intent(this, RegisterUsersActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please Connect Internet to Continue..", Toast.LENGTH_LONG).show();
        }
    }

    public void OpenManageSemesterActivity(View view) {
        if (NetworkUtils.isInternetConnected(this)) {
            Intent intent = new Intent(this, ManageSemesterActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please Connect Internet to Continue..", Toast.LENGTH_LONG).show();
        }
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Admin Dashboard", false);
    }

    public void ViewTeachersAttendanceActivity(View view) {
        if (NetworkUtils.isInternetConnected(this)) {
            Intent intent = new Intent(this, DisplayInstituteSemestersActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please Connect Internet to Continue..", Toast.LENGTH_LONG).show();
        }
    }

    public void DisplayTeachersListActivity(View view) {
        if (NetworkUtils.isInternetConnected(this)) {
            Intent intent = new Intent(this, DisplayInstituteTeachersListActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please Connect Internet to Continue..", Toast.LENGTH_LONG).show();
        }
    }
}
