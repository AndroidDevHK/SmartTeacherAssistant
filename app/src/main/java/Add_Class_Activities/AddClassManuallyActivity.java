package Add_Class_Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.lambdapioneer.argon2kt.Argon2Kt;
import com.lambdapioneer.argon2kt.Argon2KtResult;
import com.lambdapioneer.argon2kt.Argon2Mode;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Add_View_Semester_Activity.ManageSemesterActivity;
import Add_View_Semester_Activity.SemesterViewModel;

public class AddClassManuallyActivity extends AppCompatActivity {

    private EditText classNameEditText;
    private EditText numberOfStudentsEditText;
    private String semesterId;
    private FirebaseFirestore db;
    private LinearLayout studentListLayout;
    private String SemesterName;
    private SemesterViewModel semesterViewModel;
    ActivityManager activityManager;
    private LinearLayout numOfStdLayout;

    private ProgressDialog progressDialog;
    private Argon2Kt argon2Kt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_manually);

        argon2Kt = new Argon2Kt();

        numOfStdLayout = findViewById(R.id.numofStdLayout);

        classNameEditText = findViewById(R.id.edit_text_class_name);
        numberOfStudentsEditText = findViewById(R.id.numberOfStudentsEditText);
        studentListLayout = findViewById(R.id.studentListLayout);
        db = FirebaseFirestore.getInstance();
        activityManager = (ActivityManager) getApplication();
        activityManager.addActivity(this);
        getSemesterData();

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        semesterViewModel = new ViewModelProvider(this).get(SemesterViewModel.class);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Add Class - " + SemesterName, true);
    }

    private void hideNumOfStdLayout() {
        numOfStdLayout.setVisibility(View.GONE);
    }

    private void getSemesterData() {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(this);
        SemesterName = userInstituteModel.getSemesterName();
        semesterId = userInstituteModel.getSemesterId();
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void onNextButtonClick(View view) {
        String numberOfStudentsStr = numberOfStudentsEditText.getText().toString().trim();

        if (numberOfStudentsStr.isEmpty()) {
            numberOfStudentsEditText.setError("Number of students cannot be empty");
            return;
        }

        int numberOfStudents = Integer.parseInt(numberOfStudentsStr);

        // Check if the number of students is greater than zero
        if (numberOfStudents <= 0) {
            numberOfStudentsEditText.setError("Number of students must be greater than zero");
            return;
        }

        // Show number of students layout
        showNumberOfStudentsLayout(numberOfStudents);

        // Disable the next button to prevent multiple clicks
        findViewById(R.id.nextButton).setEnabled(false);
    }

    private void checkClassNameExists(String className) {
        showProgressDialog("Checking Class Existence...");
        db.collection("SemesterClasses")
                .whereEqualTo("SemesterID", semesterId)
                .whereEqualTo("ClassName", className)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Class name already exists in the semester
                        Toast.makeText(AddClassManuallyActivity.this,
                                "Class name already exists in " + UserInstituteModel.getInstance(this).getSemesterName(),
                                Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                    } else {
                        dismissProgressDialog();
                        fetchLatestUserIDAndSaveClass(className); // Fetch UserID and save class
                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while checking
                    Toast.makeText(AddClassManuallyActivity.this,
                            "Error checking class name: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showNumberOfStudentsLayout(int numberOfStudents) {
        // Clear any previous views
        studentListLayout.removeAllViews();

        // Inflate student input fields based on the number of students
        for (int i = 0; i < numberOfStudents; i++) {
            View studentItemView = getLayoutInflater().inflate(R.layout.student_layout, null);
            studentListLayout.addView(studentItemView);
        }
        hideNumOfStdLayout();
        findViewById(R.id.classDetailsCard).setVisibility(View.VISIBLE);
        findViewById(R.id.saveButton).setOnClickListener(v -> {
            String className = classNameEditText.getText().toString().trim().toUpperCase();
            if (className.isEmpty()) {
                classNameEditText.setError("Class name cannot be empty");
            } else {
                checkClassNameExists(className);
            }
        });
    }

    private void fetchLatestUserIDAndSaveClass(String className) {
        db.collection("StudentSemestersDetails")
                .orderBy("UserID", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int latestUserIdNumber = -1; // Start from -1 so the first increment results in 0
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String latestUserId = queryDocumentSnapshots.getDocuments().get(0).getString("UserID");
                        latestUserIdNumber = Integer.parseInt(latestUserId.substring(1));
                    }

                    showConfirmationDialog(className, latestUserIdNumber);
                })
                .addOnFailureListener(e -> {
                    onFirestoreError(e);
                    dismissProgressDialog();
                });
    }

    private void showConfirmationDialog(String className, int latestUserIdNumber) {
        List<String> studentNames = new ArrayList<>();
        List<String> rollNumbers = new ArrayList<>();
        Set<String> uniqueRollNumbers = new HashSet<>(); // To check uniqueness of roll numbers
        StringBuilder message = new StringBuilder("Class Name: " + className + "\n\nStudents:\n");

        for (int i = 0; i < studentListLayout.getChildCount(); i++) {
            View studentItemView = studentListLayout.getChildAt(i);
            EditText studentNameEditText = studentItemView.findViewById(R.id.StdNameEditText);
            EditText rollNoEditText = studentItemView.findViewById(R.id.RollNoEditText);
            String studentName = studentNameEditText.getText().toString().trim();
            String rollNo = rollNoEditText.getText().toString().trim();
            if (!studentName.isEmpty() && !rollNo.isEmpty()) {
                if (uniqueRollNumbers.contains(rollNo)) {
                    // Roll number is not unique, show error and return
                    Toast.makeText(AddClassManuallyActivity.this,
                            "Roll number must be unique",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                studentNames.add(studentName);
                rollNumbers.add(rollNo);
                uniqueRollNumbers.add(rollNo); // Add roll number to the set to ensure uniqueness
                message.append(studentName).append(" — ").append(rollNo).append("\n");
            } else {
                // If any field is empty, show a toast and return
                Toast.makeText(AddClassManuallyActivity.this,
                        "Please enter both student name and roll number",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Class Details")
                .setMessage(message.toString())
                .setPositiveButton("Yes", (dialog, which) -> saveClassDetails(className, studentNames, rollNumbers, latestUserIdNumber))
                .setNegativeButton("No", null)
                .show();
    }

    private void saveClassDetails(String className, List<String> studentNames, List<String> rollNumbers, int latestUserIdNumber) {
        showProgressDialog("Saving Class Details...");

        WriteBatch batch = db.batch();

        // Prepare class data
        Map<String, Object> classData = new HashMap<>();
        classData.put("ClassName", className);

        // Create a reference for the new class document
        DocumentReference classRef = db.collection("Classes").document();

        // Add class data to batch
        batch.set(classRef, classData);

        String instituteId = UserInstituteModel.getInstance(this).getInstituteId();

        // Prepare student details
        for (int i = 0; i < studentNames.size(); i++) {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("StudentName", studentNames.get(i));
            studentData.put("RollNo", i < rollNumbers.size() ? rollNumbers.get(i) : "N/A");
            studentData.put("IsActive", true); // Set student status as active by default

            // Create a reference for each student document
            DocumentReference studentRef = classRef.collection("ClassStudents").document();

            // Add student data to batch
            batch.set(studentRef, studentData);

            // Generate new UserID and hashed password
            int newUserIdNumber = ++latestUserIdNumber;
            String newUserId = generateUserId(newUserIdNumber);

            // Prepare student semester details
            String studentDocumentId = UUID.randomUUID().toString();

            Map<String, Object> studentSemesterData = new HashMap<>();
            studentSemesterData.put("StudentRollNo", rollNumbers.get(i));
            studentSemesterData.put("SemesterID", semesterId);
            studentSemesterData.put("InstituteID", instituteId);
            studentSemesterData.put("ClassID", classRef.getId());
            studentSemesterData.put("UserID", newUserId);
            studentSemesterData.put("HashedPassword", newUserId); // Add hashed password

            // Create a reference for each student semester document
            DocumentReference studentSemesterRef = db.collection("StudentSemestersDetails").document(studentDocumentId);

            // Add student semester data to batch
            batch.set(studentSemesterRef, studentSemesterData);
        }

        // Prepare semester class data
        Map<String, Object> semesterClassData = new HashMap<>();
        semesterClassData.put("ClassID", classRef.getId());
        semesterClassData.put("SemesterID", semesterId);
        semesterClassData.put("ClassName", className);

        // Create a reference for the new semester class document
        DocumentReference semesterClassRef = db.collection("SemesterClasses").document();

        // Add semester class data to batch
        batch.set(semesterClassRef, semesterClassData);

        // Commit the batch write
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(AddClassManuallyActivity.this,
                    "Class Added Successfully!",
                    Toast.LENGTH_SHORT).show();
            dismissProgressDialog();
            activityManager.finishActivitiesExceptMainMenuActivity();
            Intent intent = new Intent(AddClassManuallyActivity.this, ManageSemesterActivity.class);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Toast.makeText(AddClassManuallyActivity.this,
                    "Error saving class details: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            dismissProgressDialog();
        });
    }
    @SuppressLint("DefaultLocale")
    private String generateUserId(int userIdNumber) {
        return String.format("S%04d", userIdNumber);
    }
    private void onFirestoreError(Exception e) {
        Toast.makeText(this, "Error saving class details to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
