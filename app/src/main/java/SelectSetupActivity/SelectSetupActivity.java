package SelectSetupActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nextgen.hasnatfyp.DisplaySoloUserDashboardActivity;
import com.nextgen.hasnatfyp.MainMenuActivity;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.StudentLoginTestActivity;

public class SelectSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_setup);
    }

    public void openInstituteMainMenuDashboardActivity(View view) {
        Intent intent = new Intent(this, StudentLoginTestActivity.class);
        startActivity(intent);
    }

    public void ViewSoloUserDashboardActivity(View view) {
        Intent intent = new Intent(this, DisplaySoloUserDashboardActivity.class);
        startActivity(intent);
    }
}