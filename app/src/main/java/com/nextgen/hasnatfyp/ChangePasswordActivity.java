package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.EmailEditText);
        oldPasswordEditText = findViewById(R.id.OldPasswordEditText);
        newPasswordEditText = findViewById(R.id.NewPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.ConfirmNewPasswordEditText);

        // Populate the logged-in user's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());
            emailEditText.setEnabled(false); // Make the email field non-editable
        }

        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Change Password", true);
    }
    public void ChangePassword(View view) {
        String email = emailEditText.getText().toString().trim();
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() <= 6) {
            Toast.makeText(this, "New password must be greater than 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialogHelper.showProgressDialog(this,"Changing Password");
        // Re-authenticate the user with the old password
        mAuth.signInWithEmailAndPassword(email, oldPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check user role before changing the password
                            checkUserRoleAndChangePassword(user, newPassword);
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed. Please check your old password", Toast.LENGTH_SHORT).show();
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void checkUserRoleAndChangePassword(FirebaseUser user, String newPassword) {
        db.collection("UserCollection").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String userType = document.getString("userType");

                        if (userType != null) {
                            if (userType.equals("Teacher")) {
                                // Update the password in Firebase Auth and UserCollection for Teacher
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                updateUserCollectionPassword(user.getUid(), newPassword);
                                            } else {
                                                ProgressDialogHelper.dismissProgressDialog();
                                                Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else if (userType.equals("InstituteAdmin") || userType.equals("Individual")) {
                                // Update the password in Firebase Auth only for Admin
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                ProgressDialogHelper.dismissProgressDialog();
                                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                ProgressDialogHelper.dismissProgressDialog();
                                                Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                ProgressDialogHelper.dismissProgressDialog();
                                Toast.makeText(this, "Unauthorized user role", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ProgressDialogHelper.dismissProgressDialog();
                            Toast.makeText(this, "Failed to retrieve user role", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ProgressDialogHelper.dismissProgressDialog();
                        Toast.makeText(this, "Failed to retrieve user role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserCollectionPassword(String uid, String newPassword) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPassword);

        db.collection("UserCollection").document(uid)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ProgressDialogHelper.dismissProgressDialog();
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        ProgressDialogHelper.dismissProgressDialog();
                        Toast.makeText(this, "Failed to update password in UserCollection", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
