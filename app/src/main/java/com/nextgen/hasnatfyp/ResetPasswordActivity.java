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

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        emailEditText = findViewById(R.id.emailEditText);
        mAuth = FirebaseAuth.getInstance();
    }

    public void sendResetEmail(View view) {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialogHelper.showProgressDialog(this, "Sending Reset Code...");
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset code sent to your email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send reset code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
