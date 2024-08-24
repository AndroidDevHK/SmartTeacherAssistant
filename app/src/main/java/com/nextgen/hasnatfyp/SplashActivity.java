package com.nextgen.hasnatfyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkCurrentUser();



    }

    private void showOfflineModeDialogSoloUser(Class<DisplaySoloUserDashboardActivity> displaySoloUserDashboardActivityClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connectivity")
                .setMessage("No internet connection detected. Do you want to proceed in offline mode?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TeacherInstanceModel.getInstance(SplashActivity.this).setOfflineMode(true);
                    showToast("Offline Mode Activated!");
                    Intent intent = new Intent(this, displaySoloUserDashboardActivityClass);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Close the app
                    finish();
                })
                .setCancelable(false); // Prevents dismissing the dialog by clicking outside
        builder.show();
    }
    private void showOfflineModeDialogTeacher(Class<TeacherDashboardActivity> teacherDashboardActivityClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connectivity")
                .setMessage("No internet connection detected. Do you want to proceed in offline mode?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TeacherInstanceModel.getInstance(SplashActivity.this).setOfflineMode(true);
                    showToast("Offline Mode Activated!");
                    Intent intent = new Intent(this, teacherDashboardActivityClass);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Close the app
                    finish();
                })
                .setCancelable(false); // Prevents dismissing the dialog by clicking outside
        builder.show();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            checkUserRole(uid);
        } else {
            checkStudentPrefs();
        }
    }

    private void checkStudentPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences("Current_USER_prefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", null);
        String userRole = sharedPreferences.getString("userRole", null);

        if (userID != null && userRole != null) {
            if (userRole.equals("Student")) {
                navigateToStudentDashboard(userID);
            } else {
                navigateToLogin();
            }
        } else {
            navigateToLogin();
        }
    }

    private void checkUserRole(String uid) {
        db.collection("UserCollection").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String userType = document.getString("userType");
                        String instituteID = document.getString("InstituteID");

                        if (userType != null) {
                            if (userType.equals("InstituteAdmin") || userType.equals("Individual")) {
                                handleAdminLogin(uid, userType, instituteID);
                            } else if (userType.equals("Teacher")) {
                                handleTeacherLogin(uid, instituteID);
                            } else {
                                navigateToLogin();
                            }
                        } else {
                            navigateToLogin();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching user role", task.getException());
                        navigateToLogin();
                    }
                });
    }

    private void handleAdminLogin(String uid, String userType, String instituteID) {
        db.collection("AdminCollection").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String adminName = document.getString("UserFullName");

                        if (adminName != null) {
                            // Fetch InstituteName
                            db.collection("InstitutesName").document(instituteID).get()
                                    .addOnCompleteListener(instituteTask -> {
                                        if (instituteTask.isSuccessful() && instituteTask.getResult() != null) {
                                            String instituteName = instituteTask.getResult().getString("InstituteName");
                                            if (userType.equals("InstituteAdmin")) {
                                                navigateToInstituteAdminDashboard(uid, userType, instituteID, instituteName, adminName);
                                            } else {
                                                navigateToSoloUserDashboard(uid, userType, instituteID, instituteName, adminName);
                                            }
                                        } else {
                                            Log.e("Firestore", "Error fetching institute data", instituteTask.getException());
                                            navigateToLogin();
                                        }
                                    });
                        } else {
                            navigateToLogin();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching admin data", task.getException());
                        navigateToLogin();
                    }
                });
    }

    private void navigateToSoloUserDashboard(String uid, String userType, String instituteID, String instituteName, String adminName) {
        UserInstituteModel userInstanceModel = UserInstituteModel.getInstance(this);
        userInstanceModel.setInstituteId(instituteID);
        userInstanceModel.setSoloUser(true);
        userInstanceModel.setAdminName(adminName);
        userInstanceModel.setCampusName(instituteName);
        if (!NetworkUtils.isInternetConnected(this) && userInstanceModel.isSoloUser()) {
            showOfflineModeDialogSoloUser(DisplaySoloUserDashboardActivity.class);
        }
        else {
            Intent intent = new Intent(this, DisplaySoloUserDashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void navigateToInstituteAdminDashboard(String uid, String userType, String instituteID, String instituteName, String adminName) {
        UserInstituteModel userInstanceModel = UserInstituteModel.getInstance(this);
        userInstanceModel.setInstituteId(instituteID);
        userInstanceModel.setSoloUser(false);
        userInstanceModel.setAdminName(adminName);
        userInstanceModel.setCampusName(instituteName);
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }
    private void navigateToLoginWithError(String errorMessage) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("error", errorMessage);
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
                            navigateToLoginWithError("Your account is deactivated. Please contact your admin.");
                        } else if (teacherName != null && department != null && qualification != null && username != null) {
                            // Fetch institute name from the Institutes collection
                            db.collection("InstitutesName").document(instituteID).get()
                                    .addOnCompleteListener(instituteTask -> {
                                        if (instituteTask.isSuccessful() && instituteTask.getResult() != null) {
                                            String instituteName = instituteTask.getResult().getString("InstituteName");
                                            if (instituteName != null) {
                                                navigateToTeacherDashboard(uid, instituteID, teacherName, department, qualification, username, instituteName, pastAttendancePermission);
                                            } else {
                                                navigateToLogin();
                                            }
                                        } else {
                                            Log.e("Firestore", "Error fetching institute name", instituteTask.getException());
                                            navigateToLogin();
                                        }
                                    });
                        } else {
                            navigateToLogin();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching teacher data", task.getException());
                        navigateToLogin();
                    }
                });
    }

    private void navigateToTeacherDashboard(String uid, String instituteID, String teacherName, String department, String qualification, String username, String instituteName, Boolean pastAttendancePermission) {
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        teacherInstanceModel.setTeacherName(teacherName);
        teacherInstanceModel.setTeacherUsername(username);
        teacherInstanceModel.setDepartment(department);
        teacherInstanceModel.setQualification(qualification);
        teacherInstanceModel.setInstituteName(instituteName);
        teacherInstanceModel.setPastAttendancePermission(pastAttendancePermission != null && pastAttendancePermission); // Set PastAttendancePermission
        if (!NetworkUtils.isInternetConnected(this)) {
            showOfflineModeDialogTeacher(TeacherDashboardActivity.class);
        }
        else {
            Intent intent = new Intent(this, TeacherDashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void navigateToStudentDashboard(String userID) {
        db.collection("StudentSemestersDetails")
                .whereEqualTo("UserID", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String studentRollNo = document.getString("StudentRollNo");
                        String classID = document.getString("ClassID");
                        String instituteID = document.getString("InstituteID");
                        String semesterID = document.getString("SemesterID");

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
                                                        navigateToStudentDashboardActivity(userID, classID, instituteID, semesterID, studentRollNo, studentName, className);
                                                    } else {
                                                        navigateToLogin();
                                                    }
                                                });
                                    } else {
                                        navigateToLogin();
                                    }
                                });
                    } else {
                        navigateToLogin();
                    }
                });
    }

    private void navigateToStudentDashboardActivity(String userID, String classID, String instituteID, String semesterID, String studentRollNo, String studentName, String className) {
        StudentSessionInfo session = StudentSessionInfo.getInstance(getApplicationContext());
        session.setStudentID(userID);
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

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
