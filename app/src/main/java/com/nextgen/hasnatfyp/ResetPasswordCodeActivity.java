package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordCodeActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private FirebaseAuth mAuth;
    private String oobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_code);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        oobCode = intent.getStringExtra("oobCode");

        if (oobCode == null) {
            Toast.makeText(this, "Invalid reset code", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void resetPassword(View view) {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialogHelper.showProgressDialog(this, "Resetting Password...");
        mAuth.confirmPasswordReset(oobCode, newPassword)
                .addOnCompleteListener(task -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
