package Add_Class_Courses_Activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.PreferenceManager;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import View_Semester_Classes_Activity.ManageClassesActivity;

public class AddClassCoursesManuallyActivity extends AppCompatActivity {

    private LinearLayout coursesListLayout;
    private EditText numberOfCoursesEditText;
    private FirebaseFirestore db;
    private String classId;
    private List<String> existingCourses;
    private CardView CourseDetailsCard;
    private CardView addClassCardView;
    private ProgressDialog progressDialog;

    List<String> savedCourseIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_courses_manually);

        // Get class ID from intent
        classId = getIntent().getStringExtra("classId");
        CourseDetailsCard = findViewById(R.id.CourseDetailsCard);
        addClassCardView = findViewById(R.id.addClassCardView);
        coursesListLayout = findViewById(R.id.CoursesListLayout);
        numberOfCoursesEditText = findViewById(R.id.numberOfStudentsEditText);
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);


    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Class - " + UserInstituteModel.getInstance(this).getClassName(), true);
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    public void onNextButtonClick(View view) {
        String numberOfCoursesStr = numberOfCoursesEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(numberOfCoursesStr)) {
            int numberOfCourses = Integer.parseInt(numberOfCoursesStr);
            displayCourseFields(numberOfCourses);
        } else {
            Toast.makeText(this, "Please enter the number of courses", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayCourseFields(int numberOfCourses) {
        coursesListLayout.removeAllViews();
        addClassCardView.setVisibility(View.GONE);
        CourseDetailsCard.setVisibility(View.VISIBLE);
        for (int i = 0; i < numberOfCourses; i++) {
            View courseView = getLayoutInflater().inflate(R.layout.course_layout, null);
            coursesListLayout.addView(courseView);
        }
    }

    public void onSaveButtonClick(View view) {
        // Set to store unique course names
        Set<String> courseNamesSet = new HashSet<>();
        existingCourses = new ArrayList<>();

        int childCount = coursesListLayout.getChildCount();
        final int[] checkedCoursesCount = {0}; // Counter to keep track of the number of courses checked for existence

        for (int i = 0; i < childCount; i++) {
            View childView = coursesListLayout.getChildAt(i);
            EditText courseNameEditText = childView.findViewById(R.id.CourseNameEditText);
            EditText crHoursEditText = childView.findViewById(R.id.CrHoursEditText);

            String courseName = courseNameEditText.getText().toString().trim();
            String crHours = "3";

            //    String crHours = crHoursEditText.getText().toString().trim();

            // Validate credit hours
/*
if (!isValidCreditHours(crHours)) {
    crHoursEditText.setError("Credit hours must be between 1 and 6");
    dismissProgressDialog();
    return;
}
*/

            if (!TextUtils.isEmpty(courseName)) {
                // Add the course name to the set
                if (!isUniqueCourseName(courseName, courseNamesSet)) {
                    courseNameEditText.setError("Course name must be unique");
                    dismissProgressDialog();
                    return;

                }
            } else {
                Toast.makeText(this, "Please fill in all fields for each course", Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
                return;
            }

            // Check existence of the course
            checkCourseExistence(courseName, exists -> {
                checkedCoursesCount[0]++;

                // If the course exists, add it to the existingCourses list
                if (exists) {
                    existingCourses.add(courseName);
                }

                // If all courses have been checked, proceed to save or show error
                if (checkedCoursesCount[0] == childCount) {
                    if (!existingCourses.isEmpty()) {
                        // Some courses already exist
                        StringBuilder existingCourseNames = new StringBuilder();
                        for (String existingCourse : existingCourses) {
                            existingCourseNames.append(existingCourse).append(", ");
                        }
                        existingCourseNames.delete(existingCourseNames.length() - 2, existingCourseNames.length()); // Remove trailing comma and space
                        Toast.makeText(AddClassCoursesManuallyActivity.this, "Courses '" + existingCourseNames.toString() + "' already exist", Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                    } else {
                        dismissProgressDialog();
                        saveAllCoursesToFirestore();
                    }
                }
            });
        }
    }

    private boolean isUniqueCourseName(String courseName, Set<String> courseNamesSet) {
        // Convert set to lowercase and check for case-sensitive uniqueness
        return courseNamesSet.add(courseName.toLowerCase());
    }

    private boolean isValidCreditHours(String crHours) {
        if (TextUtils.isEmpty(crHours)) {
            return false;
        }
        try {
            int creditHours = Integer.parseInt(crHours);
            return creditHours >= 1 && creditHours <= 6;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    interface OnCourseExistenceCheckedListener {
        void onCourseExistenceChecked(boolean exists);
    }

    private void checkCourseExistence(String courseName, OnCourseExistenceCheckedListener listener) {
        showProgressDialog("Validating Course Information...");

        db.collection("ClassCourses")
                .document(classId)
                .collection("ClassCoursesSubcollection")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean courseExists = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentCourseName = document.getString("CourseName");
                        if (documentCourseName != null && documentCourseName.equalsIgnoreCase(courseName)) {
                            // Course name matches (case-insensitive comparison)
                            courseExists = true;
                            break;
                        }
                    }
                    listener.onCourseExistenceChecked(courseExists);
                })
                .addOnFailureListener(e -> {
                    // Error occurred while checking course existence
                    Toast.makeText(this, "Error checking course existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onCourseExistenceChecked(false);
                });
    }

    private void saveAllCoursesToFirestore() {
        // Build the confirmation message
        StringBuilder confirmationMessage = new StringBuilder();
        confirmationMessage.append("Confirm saving the following courses:\n\n");

        int childCount = coursesListLayout.getChildCount();
        AtomicInteger coursesSavedCounter = new AtomicInteger(0);

        for (int i = 0; i < childCount; i++) {
            View childView = coursesListLayout.getChildAt(i);
            EditText courseNameEditText = childView.findViewById(R.id.CourseNameEditText);
            EditText crHoursEditText = childView.findViewById(R.id.CrHoursEditText);

            String courseName = courseNameEditText.getText().toString().trim();
            //String crHours = crHoursEditText.getText().toString().trim();
            String crHours = "3";

            confirmationMessage.append(i + 1).append(": ").append(courseName).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Save")
                .setMessage(confirmationMessage.toString())
                .setPositiveButton("Save", (dialog, which) -> {
                    // User confirmed, proceed with saving courses
                    showProgressDialog("Saving Courses for Class...");

                    for (int i = 0; i < childCount; i++) {
                        View childView = coursesListLayout.getChildAt(i);
                        EditText courseNameEditText = childView.findViewById(R.id.CourseNameEditText);
                        EditText crHoursEditText = childView.findViewById(R.id.CrHoursEditText);

                        String courseName = courseNameEditText.getText().toString().trim();
                        String crHours = "3";

                        Map<String, Object> course = new HashMap<>();
                        course.put("CourseName", courseName);
                        course.put("CreditHours", crHours);
                        course.put("IsCourseActive", true);

                        // Save each course as a separate document with auto-generated ID
                        saveCourseToFirestore(course, coursesSavedCounter, childCount);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dismissProgressDialog();
                })
                .show();
    }

    private void saveCourseToFirestore(Map<String, Object> course, AtomicInteger counter, int totalCourses) {
        db.collection("ClassCourses")
                .document(classId)
                .collection("ClassCoursesSubcollection")
                .add(course)
                .addOnSuccessListener(documentReference -> {

                    savedCourseIds.add(documentReference.getId());
                    int count = counter.incrementAndGet();
                    if (count == totalCourses) {
                        saveStudentsForCourses();}
                })
                .addOnFailureListener(e -> {
                });
    }

    private void saveStudentsForCourses() {
        String semesterID = UserInstituteModel.getInstance(this).getSemesterId();
        String classID = classId;
        AtomicInteger processedCoursesCount = new AtomicInteger(0);
        for (String courseID : savedCourseIds) {
            getStudentsFromClass(classID, semesterID, courseID, processedCoursesCount);
        }
    }

    private void getStudentsFromClass(String classID, String semesterID, String courseID, AtomicInteger processedCoursesCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Classes").document(classID)
                .collection("ClassStudents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String rollNo = document.getString("RollNo");

                        saveStudentForCourse(courseID, semesterID, classID, rollNo, processedCoursesCount);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddClassCoursesManuallyActivity.this,
                            "Error getting students from class: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveStudentForCourse(String courseID, String semesterID, String classID, String rollNo, AtomicInteger processedCoursesCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new document in CoursesStudents collection
        db.collection("CoursesStudents")
                .add(new HashMap<String, Object>() {{
                    put("CourseID", courseID);
                    put("SemesterID", semesterID);
                    put("ClassID", classID);
                    put("StudentRollNo", rollNo);
                    put("IsEnrolled", true);
                    put("isRepeater", false);
                }})
                .addOnSuccessListener(documentReference -> {
                    // Student data saved successfully
                    Log.d(TAG, "Student data saved for course: " + courseID);
                    // Check if all courses have been processed
                    if (processedCoursesCount.incrementAndGet() == savedCourseIds.size()) {

                        if (UserInstituteModel.getInstance(this).isSoloUser()) {
                            String soloUserID;
                            String semID = UserInstituteModel.getInstance(this).getSemesterId();
                            soloUserID = UserInstituteModel.getInstance(this).getInstituteId();

                            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                            AtomicInteger counter = new AtomicInteger(savedCourseIds.size());

                            // Check if the semester for the solo user is already added
                            db1.collection("TeacherSemesters")
                                    .whereEqualTo("SemesterID", semID)
                                    .whereEqualTo("TeacherUserName", soloUserID)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().isEmpty()) {
                                                // Semester not added, add it
                                                Map<String, Object> semesterData = new HashMap<>();
                                                semesterData.put("SemesterID", semID);
                                                semesterData.put("TeacherUserName", soloUserID);

                                                db1.collection("TeacherSemesters")
                                                        .add(semesterData)
                                                        .addOnSuccessListener(documentReference1 -> {
                                                            // Semester added, now assign courses to solo user
                                                            assignCoursesToTeacher(soloUserID);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Error adding semester
                                                            Toast.makeText(AddClassCoursesManuallyActivity.this, "Error adding semester: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                // Semester already added, directly assign courses to solo user
                                                assignCoursesToTeacher(soloUserID);
                                            }
                                        } else {
                                            // Error checking semester existence
                                            Toast.makeText(AddClassCoursesManuallyActivity.this, "Error checking semester existence: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            handleCompletion();
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while saving student data
                    Log.e(TAG, "Error saving student data for course " + courseID + ": " + e.getMessage());
                });
    }

    private void assignCoursesToTeacher(String teacherID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the semester ID
        String semesterID = UserInstituteModel.getInstance(this).getSemesterId();

        // Create a WriteBatch instance
        WriteBatch batch = db.batch();

        // Counter to keep track of the number of courses processed
        AtomicInteger processedCoursesCount = new AtomicInteger(0);

        // Iterate through the list of saved course IDs
        for (String courseID : savedCourseIds) {
            // Check if the course is already assigned to the teacher
            db.collection("TeacherCourses")
                    .whereEqualTo("CourseID", courseID)
                    .whereEqualTo("TeacherUsername", teacherID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Course is not assigned to the teacher, assign it
                                Map<String, Object> teacherCourseData = new HashMap<>();
                                teacherCourseData.put("CourseID", courseID);
                                teacherCourseData.put("TeacherUsername", teacherID);
                                teacherCourseData.put("SemesterID", semesterID);
                                teacherCourseData.put("ClassID", classId);

                                // Add the write operation to the batch
                                batch.set(db.collection("TeacherCourses").document(), teacherCourseData);

                                // Increment the counter
                                int count = processedCoursesCount.incrementAndGet();
                                // Check if all courses have been processed
                                if (count == savedCourseIds.size()) {
                                    // Commit the batch write
                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                // All courses assigned successfully
                                                handleCompletion();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Error committing batch
                                                Log.e(TAG, "Error committing batch write: " + e.getMessage());
                                            });
                                }
                            } else {
                                // Course is already assigned to the teacher
                                // Increment the counter even if course is already assigned
                                int count = processedCoursesCount.incrementAndGet();
                                // Check if all courses have been processed
                                if (count == savedCourseIds.size()) {
                                    // Commit the batch write
                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                // All courses assigned successfully
                                                handleCompletion();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Error committing batch
                                                Log.e(TAG, "Error committing batch write: " + e.getMessage());
                                            });
                                }
                            }
                        } else {
                            // Error checking course assignment
                            Log.e(TAG, "Error checking course assignment for course " + courseID + " and teacher " + teacherID + ": " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void handleCompletion() {
        ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
        Intent intent = new Intent(AddClassCoursesManuallyActivity.this, ManageClassesActivity.class);
        startActivity(intent);
        dismissProgressDialog();
        Toast.makeText(AddClassCoursesManuallyActivity.this, "All courses saved successfully", Toast.LENGTH_SHORT).show();

    }

}
