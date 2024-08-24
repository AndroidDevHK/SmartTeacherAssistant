package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class ResetCodeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String oobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_code);

        mAuth = FirebaseAuth.getInstance();

        // Extract the reset code from the incoming intent
        oobCode = getIntent().getData().getQueryParameter("oobCode");

        // Verify the reset code
        verifyResetCode(oobCode);
    }

    private void verifyResetCode(String code) {
        mAuth.verifyPasswordResetCode(code)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, ResetPasswordCodeActivity.class);
                        intent.putExtra("oobCode", oobCode);
                        startActivity(intent);
                        finish();  // Close ResetCodeActivity

                    } else {
                        Toast.makeText(this, "Invalid or expired reset code", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
