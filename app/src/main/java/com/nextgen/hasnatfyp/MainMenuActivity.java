package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

import DisplayInstituteTeachersForLoginActivity.DisplayInstituteTeachersListActivity;

public class MainMenuActivity extends AppCompatActivity {
    static final String PREF_NAME = "MySharedPrefs";
    static final String KEY_INSTITUTE_ID = "institute_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(this);
        userInstituteModel.setSoloUser(false);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Check if institute ID exists in SharedPreferences
        String instituteId = sharedPreferences.getString(KEY_INSTITUTE_ID, null);

        if (instituteId != null) {
            userInstituteModel.setInstituteId(instituteId);
        } else {
            // Generate and set a new institute ID
            String newInstituteId = getOrCreateInstituteId(this);
            userInstituteModel.setInstituteId(newInstituteId);
        }
        if (TeacherInstanceModel.getInstance(this).isOfflineMode()) {
            showOfflineModeDialog();
        }

    }

    public static String generateAndSaveInstituteId(Context context) {
        final String PREF_NAME = "MySharedPrefs";
        final String KEY_INSTITUTE_ID = "institute_id";

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int randomId = new Random().nextInt(9000) + 1000; // Range: 1000 - 9999

        String instituteId = String.format("%04d", randomId);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_INSTITUTE_ID, instituteId);

        editor.apply();

        return instituteId;
    }

    public static String getOrCreateInstituteId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(KEY_INSTITUTE_ID)) {
            return generateAndSaveInstituteId(context);
        } else {
            String existingId = sharedPreferences.getString(KEY_INSTITUTE_ID, null);
            return existingId;
        }
    }
    private void showOfflineModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connectivity")
                .setMessage("There is no internet connectivity. Do you want to proceed with offline mode?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    TeacherInstanceModel.getInstance(this).setOfflineMode(false);
                    finish(); // Finish the activity if user chooses not to proceed with offline mode
                })
                .setCancelable(false)
                .show();
    }

    public void ViewAdminPanel(View view) {
        if (NetworkUtils.isInternetConnected(this))
        {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this,"Please Connect Internet to Continue..",Toast.LENGTH_LONG).show();
        }

    }

    public void ViewTeacherPanel(View view) {
        Intent intent = new Intent(this, TeacherDashboardActivity.class);
        startActivity(intent);
    }

    public void onLogoutClick(View view) {
        Toast.makeText(this,"No User Logged In Yet",Toast.LENGTH_LONG).show();
    }


    public void OpenInstituteTeachersActivity(View view) {
        Intent intent = new Intent(this, DisplayInstituteTeachersListActivity.class);
        startActivity(intent);
    }
}
