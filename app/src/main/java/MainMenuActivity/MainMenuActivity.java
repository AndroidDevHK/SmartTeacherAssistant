package MainMenuActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nextgen.hasnatfyp.AdminDashboardActivity;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.TeacherDashboardActivity;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.Random;

import DisplayInstituteTeachersForLoginActivity.DisplayInstituteTeachersListActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        if (TeacherInstanceModel.getInstance(this).isOfflineMode()) {
            showOfflineModeDialog();
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
