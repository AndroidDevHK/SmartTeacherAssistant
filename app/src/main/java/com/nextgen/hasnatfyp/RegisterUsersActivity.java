package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterUsersActivity extends AppCompatActivity {

    private EditText teacherNameEditText;
    private EditText EmailEditText;
    private EditText teacherQualificationEditText;
    private EditText teacherDepartmentEditText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_users);

        initializeComponents();
    }

    private void initializeComponents() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        teacherNameEditText = findViewById(R.id.teacherNameEditText);
        EmailEditText = findViewById(R.id.EmailEditText);
        teacherQualificationEditText = findViewById(R.id.teacherQualificationEditText);
        teacherDepartmentEditText = findViewById(R.id.teacherDepartmentEditText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering teacher...");
        progressDialog.setCancelable(false);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Register Staff", true);
    }

    public void registerTeacher(View view) {
        if (NetworkUtils.isInternetConnected(this)) {
            if (validateFields()) {
                String username = EmailEditText.getText().toString().trim();
                checkIfUserExists(username);
            }
        } else {
            Toast.makeText(this, "Please Connect Internet to Continue..", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateFields() {
        String name = teacherNameEditText.getText().toString().trim();
        String username = EmailEditText.getText().toString().trim();
        String qualification = teacherQualificationEditText.getText().toString().trim();
        String department = teacherDepartmentEditText.getText().toString().trim();

        if (name.isEmpty()) {
            teacherNameEditText.setError("Teacher name is required");
            teacherNameEditText.requestFocus();
            return false;
        }
        if (username.isEmpty()) {
            EmailEditText.setError("Email is required");
            EmailEditText.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            EmailEditText.setError("Please enter a valid email");
            EmailEditText.requestFocus();
            return false;
        }
        if (qualification.isEmpty()) {
            teacherQualificationEditText.setError("Qualification is required");
            teacherQualificationEditText.requestFocus();
            return false;
        }
        if (department.isEmpty()) {
            teacherDepartmentEditText.setError("Department is required");
            teacherDepartmentEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void checkIfUserExists(String username) {
            ProgressDialogHelper.showProgressDialog(this,"Validating....");
            db.collection("UserCollection")
                .whereEqualTo("UserEmail", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            progressDialog.dismiss();
                            Toast.makeText(this, "User already exists. Please use another email.", Toast.LENGTH_SHORT).show();
                            ProgressDialogHelper.dismissProgressDialog();
                        } else {
                            ProgressDialogHelper.dismissProgressDialog();
                            showConfirmationDialog();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error checking username", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    private void showConfirmationDialog() {
        String name = teacherNameEditText.getText().toString().trim();
        String username = EmailEditText.getText().toString().trim();
        String qualification = teacherQualificationEditText.getText().toString().trim();
        String department = teacherDepartmentEditText.getText().toString().trim();

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_register_teacher_dialog);

        // Set the background of the dialog window to be transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView nameTextView = dialog.findViewById(R.id.nameTextView);
        TextView emailTextView = dialog.findViewById(R.id.emailTextView);
        TextView qualificationTextView = dialog.findViewById(R.id.qualificationTextView);
        TextView departmentTextView = dialog.findViewById(R.id.departmentTextView);
        TextView passwordTextView = dialog.findViewById(R.id.passwordTextView);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        MaterialButton okButton = dialog.findViewById(R.id.okButton);

        nameTextView.setText("Name: " + name);
        emailTextView.setText("Email: " + username);
        qualificationTextView.setText("Qualification: " + qualification);
        departmentTextView.setText("Department: " + department);
        passwordTextView.setText("Default PWD: " + username); // Default password is the email

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            registerNewUser();
        });

        dialog.show();

        dialog.findViewById(R.id.dialog_root_layout).post(() -> {
            int width = dialog.findViewById(R.id.dialog_root_layout).getWidth();
            dialog.getWindow().setLayout(width + 100, ViewGroup.LayoutParams.WRAP_CONTENT); // Add some padding if needed
        });
    }



    private void registerNewUser() {
        ProgressDialogHelper.showProgressDialog(this,"Registering Teacher....");

        String name = teacherNameEditText.getText().toString().trim();
        String username = EmailEditText.getText().toString().trim();
        String password = username; // Default password is the email
        String qualification = teacherQualificationEditText.getText().toString().trim();
        String department = teacherDepartmentEditText.getText().toString().trim();
        String email = username;

        createUserWithEmail(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String hashedPassword = password;

                            mAuth.signOut();
                            mAuth.signInWithEmailAndPassword(UserCredentialsManager.getEmail(this), UserCredentialsManager.getPassword(this))
                                    .addOnCompleteListener(signInTask -> {
                                        if (signInTask.isSuccessful()) {
                                            Task<Void> userTask = insertUserDataInUserCollection(uid, username, hashedPassword, UserInstituteModel.getInstance(this).getInstituteId(), "Teacher");
                                            Task<Void> teacherTask = insertUserDataInTeachersCollection(uid, name, username, qualification, department);
                                            Task<Void> instituteTeacherTask = insertUserDataInInstituteTeachersCollection(uid, name, username, UserInstituteModel.getInstance(this).getInstituteId());

                                            Tasks.whenAllComplete(userTask, teacherTask, instituteTeacherTask)
                                                    .addOnCompleteListener(allTasks -> {
                                                        if (allTasks.isSuccessful()) {
                                                            ProgressDialogHelper.dismissProgressDialog();
                                                            Toast.makeText(this, "Teacher Registered successfully", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        } else {
                                                            Toast.makeText(this, "Error occurred while inserting data", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            ProgressDialogHelper.dismissProgressDialog();
                                            Toast.makeText(this, "Error logging back in as admin", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            ProgressDialogHelper.dismissProgressDialog();
                            Toast.makeText(this, "Error creating user", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error creating user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<AuthResult> createUserWithEmail(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    private Task<Void> insertUserDataInUserCollection(String uid, String username, String password, String instituteId, String userType) {
        Map<String, Object> userData = createUserMap(username, password, instituteId, userType);
        return db.collection("UserCollection").document(uid).set(userData);
    }

    private Task<Void> insertUserDataInTeachersCollection(String uid, String name, String username, String qualification, String department) {
        Map<String, Object> teacherData = createTeacherMap(name, username, qualification, department);
        return db.collection("Teachers").document(uid).set(teacherData);
    }

    private Task<Void> insertUserDataInInstituteTeachersCollection(String uid, String name, String username, String instituteId) {
        Map<String, Object> instituteTeachersData = createInstituteTeachersMap(name, username, instituteId);
        return db.collection("InstituteTeachers").document(uid).set(instituteTeachersData);
    }

    private Map<String, Object> createUserMap(String username, String password, String instituteId, String userType) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("UserEmail", username);
        userData.put("password", password);
        userData.put("InstituteID", instituteId);
        userData.put("userType", userType);
        return userData;
    }

    private Map<String, Object> createTeacherMap(String name, String username, String qualification, String department) {
        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("TeacherName", name);
        teacherData.put("Username", username);
        teacherData.put("AccountStatus", true);
        teacherData.put("PastAPermission", true);
        teacherData.put("Qualification", qualification);
        teacherData.put("Department", department);
        return teacherData;
    }

    private Map<String, Object> createInstituteTeachersMap(String name, String username, String instituteId) {
        Map<String, Object> instituteTeachersData = new HashMap<>();
        instituteTeachersData.put("TeacherName", name);
        instituteTeachersData.put("Username", username);
        instituteTeachersData.put("InstituteID", instituteId);
        return instituteTeachersData;
    }
}
