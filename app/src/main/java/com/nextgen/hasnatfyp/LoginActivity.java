package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText userIDTextView, passwordTextView;
    private Spinner loginTypeSpinner;
    private MaterialButton loginBtn;


    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final String ERROR_EMPTY_FIELD = "Please fill in all fields.";
    private static final String ERROR_INVALID_CREDENTIALS = "Invalid credentials. Please try again.";
    private static final String FAILURE_LOGIN = "Failed to login.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupSpinner();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        applyAnimations();

    }
    private void applyAnimations() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        findViewById(R.id.logoImageView).startAnimation(fadeInAnimation);
        findViewById(R.id.titleTextView).startAnimation(fadeInAnimation);
        findViewById(R.id.cardView).startAnimation(fadeInAnimation);
    }
    private void initializeViews() {
        userIDTextView = findViewById(R.id.userIDTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        loginTypeSpinner = findViewById(R.id.spinnerSignupTypes);
        loginBtn = findViewById(R.id.LoginBtn);

        loginBtn.setOnClickListener(v -> login());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.login_role_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loginTypeSpinner.setAdapter(adapter);

    }

    private void login() {
        String userID = userIDTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(password)) {
            showToast(ERROR_EMPTY_FIELD);
            return;
        }

        String selectedRole = loginTypeSpinner.getSelectedItem().toString();
        ProgressDialogHelper.showProgressDialog(this,"Authenticating...");
        if (selectedRole.equals("Admin/Teacher")) {
            loginAdminOrTeacher(userID,password);
        } else {
            loginStudent(userID,password);
        }
    }

    private void loginAdminOrTeacher(String userID, String password) {
        mAuth.signInWithEmailAndPassword(userID, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        } else {
                            showToast(FAILURE_LOGIN);
                            ProgressDialogHelper.dismissProgressDialog();
                        }
                    } else {
                        showToast(ERROR_INVALID_CREDENTIALS);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void checkUserRole(String uid) {
        db.collection("UserCollection").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String userType = document.getString("userType");
                        String InstituteID = document.getString("InstituteID");

                        if (userType != null) {
                            if (userType.equals("InstituteAdmin") || userType.equals("Individual")) {
                                handleAdminLogin(uid,userType,InstituteID);
                            } else if (userType.equals("Teacher")) {
                                handleTeacherLogin(uid,InstituteID);
                            } else {
                                showToast(ERROR_INVALID_CREDENTIALS);
                                ProgressDialogHelper.dismissProgressDialog();
                            }
                        } else {
                            showToast(ERROR_INVALID_CREDENTIALS);
                            ProgressDialogHelper.dismissProgressDialog();
                        }
                    } else {
                        showToast(FAILURE_LOGIN);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void handleAdminLogin(String uid, String userType, String instituteID) {
        db.collection("AdminCollection").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String adminName = document.getString("UserFullName");

                        // Fetch institute name from the separate collection
                        db.collection("InstitutesName").document(instituteID).get()
                                .addOnCompleteListener(instituteTask -> {
                                    if (instituteTask.isSuccessful() && instituteTask.getResult() != null) {
                                        String instituteName = instituteTask.getResult().getString("InstituteName");

                                        if (adminName != null && instituteName != null) {
                                            if (userType.equals("InstituteAdmin")) {
                                                UserCredentialsManager.saveCredentials(this, userIDTextView.getText().toString().trim(), passwordTextView.getText().toString().trim());
                                                NavigateToInsituteAdminDashboard(uid, userType, instituteID, instituteName, adminName);
                                            } else {
                                                NavigateToSoloUserDashboard(uid, userType, instituteID, instituteName, adminName);
                                            }
                                        } else {
                                            showToast(ERROR_INVALID_CREDENTIALS);
                                            ProgressDialogHelper.dismissProgressDialog();
                                        }
                                    } else {
                                        showToast(FAILURE_LOGIN);
                                        ProgressDialogHelper.dismissProgressDialog();
                                    }
                                });
                    } else {
                        showToast(FAILURE_LOGIN);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void NavigateToSoloUserDashboard(String uid, String userType, String instituteID, String instituteName, String adminName) {
        UserInstituteModel userInstanceModel = UserInstituteModel.getInstance(this);
        userInstanceModel.setInstituteId(instituteID);
        userInstanceModel.setSoloUser(true);
        userInstanceModel.setAdminName(adminName);
        userInstanceModel.setCampusName(instituteName);
        userInstanceModel.setUsername(userIDTextView.getText().toString().trim());
        ProgressDialogHelper.dismissProgressDialog();
        Intent intent = new Intent(this,DisplaySoloUserDashboardActivity.class);
        startActivity(intent);
        finish();

    }


    private void NavigateToInsituteAdminDashboard(String uid, String userType, String instituteID, String instituteName, String adminName) {
        UserInstituteModel userInstanceModel = UserInstituteModel.getInstance(this);
        userInstanceModel.setInstituteId(instituteID);
        userInstanceModel.setSoloUser(false);
        userInstanceModel.setAdminName(adminName);
        userInstanceModel.setCampusName(instituteName);
        userInstanceModel.setUsername(userIDTextView.getText().toString().trim());
        ProgressDialogHelper.dismissProgressDialog();
        Intent intent = new Intent(this,AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleTeacherLogin(String uid, String instituteID) {
        db.collection("Teachers").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String teacherName = document.getString("TeacherName");
                        String department = document.getString("Department");
                        String qualification = document.getString("Qualification");
                        String username = document.getString("Username");
                        Boolean accountStatus = document.getBoolean("AccountStatus");
                        Boolean pastAttendancePermission = document.getBoolean("PastAPermission"); // Read PastAttendancePermission

                        if (accountStatus != null && !accountStatus) {
                            // AccountStatus is false, log out and navigate to login with an error
                            mAuth.signOut();
                            showToast("Your account is deactivated. Please contact your admin.");
                            ProgressDialogHelper.dismissProgressDialog();
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (teacherName != null && department != null && qualification != null && username != null) {
                            // Fetch the institute name
                            fetchInstituteName(uid, instituteID, teacherName, department, qualification, username,pastAttendancePermission);
                        } else {
                            showToast(ERROR_INVALID_CREDENTIALS);
                            ProgressDialogHelper.dismissProgressDialog();
                        }
                    } else {
                        showToast(FAILURE_LOGIN);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }


    private void fetchInstituteName(String uid, String instituteID, String teacherName, String department, String qualification, String username, Boolean pastAttendancePermission) {
        db.collection("InstitutesName").document(instituteID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String instituteName = task.getResult().getString("InstituteName");
                        if (instituteName != null) {
                            NavigateToTeacherDashboard(uid, instituteID, teacherName, department, qualification, username, instituteName,pastAttendancePermission);
                        } else {
                            showToast(ERROR_INVALID_CREDENTIALS);
                        }
                    } else {
                        showToast(FAILURE_LOGIN);
                    }
                });
    }

    private void NavigateToTeacherDashboard(String uid, String instituteID, String teacherName, String department, String qualification, String username, String instituteName, Boolean pastAttendancePermission) {
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        teacherInstanceModel.setTeacherName(teacherName);
        teacherInstanceModel.setTeacherUsername(username);
        teacherInstanceModel.setDepartment(department);
        teacherInstanceModel.setQualification(qualification);
        teacherInstanceModel.setInstituteName(instituteName);
        teacherInstanceModel.setPastAttendancePermission(pastAttendancePermission != null && pastAttendancePermission); // Set PastAttendancePermission

        Intent intent = new Intent(this, TeacherDashboardActivity.class);
        startActivity(intent);
        ProgressDialogHelper.dismissProgressDialog();
        finish();
    }


    private void loginStudent(String userID, String password) {
        db.collection("StudentSemestersDetails")
                .whereEqualTo("UserID", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String studentRollNo = document.getString("StudentRollNo");
                        String classID = document.getString("ClassID");
                        String Password = document.getString("HashedPassword");
                        String instituteID = document.getString("InstituteID");
                        String semesterID = document.getString("SemesterID");

                        if (validateLogin(password, Password)) {
                            // Fetch the student name and class name from the subcollection
                            db.collection("Classes").document(classID)
                                    .get()
                                    .addOnCompleteListener(classTask -> {
                                        if (classTask.isSuccessful() && classTask.getResult() != null) {
                                            String className = classTask.getResult().getString("ClassName");
                                            db.collection("Classes").document(classID)
                                                    .collection("ClassStudents")
                                                    .whereEqualTo("RollNo", studentRollNo)
                                                    .get()
                                                    .addOnCompleteListener(studentTask -> {
                                                        if (studentTask.isSuccessful() && !studentTask.getResult().isEmpty()) {
                                                            String studentName = studentTask.getResult().getDocuments().get(0).getString("StudentName");
                                                            saveStudentLoginData(userID, "Student");
                                                            navigateToStudentDashboard(userID, classID, className, instituteID, semesterID, studentRollNo, studentName);
                                                        } else {
                                                            ProgressDialogHelper.dismissProgressDialog();
                                                            showToast("Failed to retrieve student name.");
                                                        }
                                                    });
                                        } else {
                                            ProgressDialogHelper.dismissProgressDialog();

                                            showToast("Failed to retrieve class information.");
                                        }
                                    });
                        } else {
                            ProgressDialogHelper.dismissProgressDialog();
                            showToast(ERROR_INVALID_CREDENTIALS);
                        }
                    } else {
                        showToast(ERROR_INVALID_CREDENTIALS);
                        ProgressDialogHelper.dismissProgressDialog();

                    }
                });
    }

    private void navigateToStudentDashboard(String studentID, String classID, String className, String instituteID, String semesterID, String studentRollNo, String studentName) {
        StudentSessionInfo session = StudentSessionInfo.getInstance(getApplicationContext());
        session.setStudentID(studentID);
        session.setClassID(classID);
        session.setInstituteID(instituteID);
        session.setSemesterID(semesterID);
        session.setStudentRollNo(studentRollNo);
        session.setStudentName(studentName);
        session.setClassName(className);
        ProgressDialogHelper.dismissProgressDialog();

        Intent intent = new Intent(this, StudentDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveStudentLoginData(String userID, String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("Current_USER_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userID", userID);
        editor.putString("userRole", role);
        editor.apply();
    }
    private boolean validateLogin(String plainPassword, String hashedPassword) {
        return plainPassword.equals(hashedPassword);
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void OpenSignUpActivity(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);

    }

    public void OpenResetPasswordActivity(View view) {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }
}
