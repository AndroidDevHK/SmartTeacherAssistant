package Display_Course_Repeaters_Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;
import java.util.ArrayList;
import java.util.List;
import View_Class_Students_Activity.StudentModel;

public class ViewClassCourseRepeaters extends AppCompatActivity {

    private static final String TAG = "ViewClassCourseRepeaters";

    private FirebaseFirestore db;
    private List<RepeaterClassStudentsModel> repeaterClass;
    private RecyclerView recyclerViewRepeaters;
    private DisplayCourseRepeaters adapter;

    private int successfulRetrievals;
    private int totalRetrievals;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_class_course_repeaters);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching data...");
        progressDialog.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        repeaterClass = new ArrayList<>();
        recyclerViewRepeaters = findViewById(R.id.recyclerViewRepeaters);
        recyclerViewRepeaters.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DisplayCourseRepeaters(repeaterClass);
        recyclerViewRepeaters.setAdapter(adapter);

        showProgressDialog(); // Show progress dialog

        successfulRetrievals = 0;
        totalRetrievals = 0;

        // Retrieve courseId and classId
        String classId = UserInstituteModel.getInstance(this).getClassId();
        String courseId = UserInstituteModel.getInstance(this).getCourseId();

        retrieveRepeatersClassID(courseId, classId);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Repeaters", true);
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showNoRepeatersDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Repeaters Found")
                .setMessage("There are no repeaters for this course.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void updateProgress() {
        if (totalRetrievals > 0) {
            int progressPercentage = (successfulRetrievals * 100) / totalRetrievals;
            progressDialog.setMessage("Fetching data... (" + progressPercentage + "%)");
        }
    }

    private void retrieveRepeatersClassID(String courseId, String classId) {
        db.collection("RepeaterClasses")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("classId", classId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String repeaterClassId = document.getString("repeaterClassId");
                            if (repeaterClassId != null) {
                                totalRetrievals++; // Increment the total retrievals counter
                                retrieveRepeaterStudents(courseId, classId, repeaterClassId);
                            }
                        }
                        // If no repeater class IDs were found, show the no repeaters dialog
                        if (totalRetrievals == 0) {
                            dismissProgressDialog();
                            showNoRepeatersDialog();
                        } else {
                            // Update the progress initially
                            updateProgress();
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void retrieveRepeaterStudents(String courseId, String classId, String repeaterClassId) {
        CollectionReference repeaterStudentsRef = db.collection("ClassRepeaters")
                .document(courseId + "_" + classId)
                .collection("RepeaterClasses")
                .document(repeaterClassId)
                .collection("RepeaterStudents");

        repeaterStudentsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<StudentModel> repeaterStudents = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String rollNo = documentSnapshot.getId();
                        Log.e(TAG, "SRoll: " + rollNo);
                        fetchStudentDetailsFromClassCollection(repeaterClassId, rollNo, (name, isActive) -> {
                            if (isActive) {
                                repeaterStudents.add(new StudentModel(name, rollNo, true, "P", repeaterClassId));
                                Log.e(TAG, "Roll No : " + rollNo);
                            }
                        });
                    }
                    retrieveClassName(classId, repeaterClassId, new ClassNameCallback() {
                        @Override
                        public void onClassNameRetrieved(String className) {
                            RepeaterClassStudentsModel repeaterClassModel = new RepeaterClassStudentsModel(repeaterClassId, className, repeaterStudents);
                            repeaterClass.add(repeaterClassModel);

                            // Increment the successful retrievals counter
                            successfulRetrievals++;

                            // Update progress
                            updateProgress();

                            // Check if all expected retrievals are completed
                            if (successfulRetrievals == totalRetrievals) {
                                dismissProgressDialog();
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error retrieving class name: ", e);
                            Toast.makeText(ViewClassCourseRepeaters.this, "Failed to retrieve class name", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving repeater students: ", e);
                    Toast.makeText(ViewClassCourseRepeaters.this, "Failed to retrieve repeater students", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchStudentDetailsFromClassCollection(String classId, String rollNo, StudentDetailsCallback callback) {
        db.collection("Classes")
                .document(classId)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", rollNo) // Query to find the document with matching roll number
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        String name = documentSnapshot.getString("StudentName");
                        boolean isActive = documentSnapshot.getBoolean("IsActive");
                        if (name != null && isActive) {
                            callback.onStudentDetailsFetched(name, isActive);
                            return; // Stop the loop after finding the matching student
                        }
                    }
                    // If no matching student is found, invoke the callback with null values
                    callback.onStudentDetailsFetched(null, false);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching student details: ", e));
    }

    private void retrieveClassName(String classId, String repeaterClassId, ClassNameCallback callback) {
        db.collection("Classes").document(repeaterClassId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String className = documentSnapshot.getString("ClassName");
                        callback.onClassNameRetrieved(className);
                    } else {
                        callback.onFailure(new Exception("Document does not exist"));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public interface StudentDetailsCallback {
        void onStudentDetailsFetched(String name, boolean isActive);
    }

    interface ClassNameCallback {
        void onClassNameRetrieved(String className);
        void onFailure(Exception e);
    }
}
