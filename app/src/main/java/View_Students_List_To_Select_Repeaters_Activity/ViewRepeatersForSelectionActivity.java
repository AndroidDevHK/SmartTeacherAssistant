package View_Students_List_To_Select_Repeaters_Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import View_Class_Students_Activity.StudentModel;

public class ViewRepeatersForSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RepeaterSelectionAdapter adapter;
    private Button saveChangesButton;
    private String RepeaterclassId;
    private List<StudentModel> existingRepeaters;
    private List<StudentModel> initialStudentList;
    private ProgressDialog progressDialog;
    private TextView noResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_repeaters_for_selection);
        showProgressDialog("Fetching..");
        RepeaterclassId = getIntent().getStringExtra("classId");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noResultText = findViewById(R.id.noResultTextView);

        // Initialize and set up adapter with an empty list initially
        adapter = new RepeaterSelectionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Retrieve all students and set their IsActive status
        retrieveAllStudentsAndSetIsActive();

        saveChangesButton = findViewById(R.id.saveChangesButton);
        saveChangesButton.setOnClickListener(v -> {
            List<StudentModel> selectedStudents = adapter.getSelectedStudents();
            List<StudentModel> currentRepeaterStatuses = adapter.getAllStudentsWithCurrentStatus();

            if (!isNoChangeMade(currentRepeaterStatuses)) {
                if (!selectedStudents.isEmpty() || !existingRepeaters.isEmpty()) {
                    showProgressDialog("Saving...");
                    List<StudentModel> removedRepeaters = new ArrayList<>();
                    for (StudentModel existingRepeater : existingRepeaters) {
                        if (!selectedStudents.contains(existingRepeater)) {
                            removedRepeaters.add(existingRepeater);
                        }
                    }

                    if (!removedRepeaters.isEmpty()) {
                        showConfirmationDialog(removedRepeaters, selectedStudents);
                    } else {
                        saveSelectedStudents(selectedStudents);
                    }
                }
            } else {
                Toast.makeText(ViewRepeatersForSelectionActivity.this, "No changes to save.", Toast.LENGTH_SHORT).show();
            }
        });

        SearchView searchView = findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStudents(newText);
                return true;
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Select Repeaters", true);
    }
    private void filterStudents(String query) {
        List<StudentModel> filteredList = new ArrayList<>();
        for (StudentModel student : initialStudentList) {
            if (student.getRollNo().toLowerCase().contains(query.toLowerCase()) ||
                    student.getStudentName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(student);
            }
        }
        adapter.setStudentList(filteredList);
        if (filteredList.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE); // Show the TextView
        } else {
            noResultText.setVisibility(View.GONE); // Hide the TextView
        }
    }
    private boolean isNoChangeMade(List<StudentModel> currentStudentList) {
        if (initialStudentList.size() != currentStudentList.size()) {
            return false;
        }
        for (int i = 0; i < initialStudentList.size(); i++) {
            if (initialStudentList.get(i).isActive() != currentStudentList.get(i).isActive()) {
                return false;
            }
        }
        return true;
    }

    private void retrieveAllStudentsAndSetIsActive() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String courseId = UserInstituteModel.getInstance(this).getCourseId();
        String classId = UserInstituteModel.getInstance(this).getClassId();

        CollectionReference repeatersRef = db.collection("ClassRepeaters")
                .document(courseId + "_" + classId)
                .collection("RepeaterClasses")
                .document(RepeaterclassId)
                .collection("RepeaterStudents");

        CollectionReference classStudentsRef = db.collection("Classes")
                .document(RepeaterclassId)
                .collection("ClassStudents");

        classStudentsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<StudentModel> allStudentsList = new ArrayList<>();

                for (QueryDocumentSnapshot studentDoc : task.getResult()) {
                    String rollNo = studentDoc.getString("RollNo");
                    String name = studentDoc.getString("StudentName");

                    StudentModel student = new StudentModel();
                    student.setRollNo(rollNo);
                    student.setStudentName(name);

                    allStudentsList.add(student);
                }

                checkStudentsInRepeatersSubcollection(allStudentsList, repeatersRef);
            }
        });
    }

    private void checkStudentsInRepeatersSubcollection(List<StudentModel> allStudentsList, CollectionReference repeatersRef) {
        repeatersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> repeaterRollNumbers = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String rollNo = document.getId(); // Use document ID as the repeater roll number
                    boolean isActive = true; // Default to true if the roll number exists
                    repeaterRollNumbers.add(rollNo);

                    // Find the corresponding student and update IsActive status
                    for (StudentModel student : allStudentsList) {
                        if (student.getRollNo().equals(rollNo)) {
                            student.setActive(isActive);
                            break;
                        }
                    }
                }

                // Set IsActive to false for students whose roll numbers are not found in the repeaters list
                for (StudentModel student : allStudentsList) {
                    if (!repeaterRollNumbers.contains(student.getRollNo())) {
                        student.setActive(false);
                    }
                }

                Collections.sort(allStudentsList, (s1, s2) -> s1.getRollNo().compareToIgnoreCase(s2.getRollNo()));

                adapter.setStudentList(allStudentsList);
                existingRepeaters = adapter.getActiveStudents();
                initialStudentList = adapter.getAllStudentsWithCurrentStatus();
                hideProgressDialog();
            }
        });
    }


    private void showConfirmationDialog(List<StudentModel> removedRepeaters, List<StudentModel> selectedStudents) {
        hideProgressDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");

        // Constructing the message with repeater roll numbers
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("You have unchecked the following existing repeaters:\n\n");
        for (StudentModel repeater : removedRepeaters) {
            messageBuilder.append(repeater.getRollNo()).append("\n");
        }
        messageBuilder.append("\nAre you sure you want to remove them?");

        builder.setMessage(messageBuilder.toString());

        builder.setPositiveButton("Yes", (dialog, which) -> {
            showProgressDialog("Processing..Please Wait..");
            removeExistingRepeaters(removedRepeaters, selectedStudents);
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void removeExistingRepeaters(List<StudentModel> removedRepeaters, List<StudentModel> selectedStudents) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String courseId = UserInstituteModel.getInstance(this).getCourseId();
        String classId = UserInstituteModel.getInstance(this).getClassId();
        CollectionReference repeatersRef = db.collection("ClassRepeaters")
                .document(courseId + "_" + classId)
                .collection("RepeaterClasses")
                .document(RepeaterclassId)
                .collection("RepeaterStudents");

        // Reference to the CoursesStudents collection
        CollectionReference coursesStudentsRef = db.collection("CoursesStudents");

        // Using AtomicInteger to track the number of repeaters to delete
        AtomicInteger deletedCount = new AtomicInteger(0);
        int totalToDelete = removedRepeaters.size();

        // Callback listener for completion of repeater deletions
        OnSuccessListener<Void> deleteSuccessListener = aVoid -> {
            int count = deletedCount.incrementAndGet();
            if (count == totalToDelete) {
                if (!selectedStudents.isEmpty()) {
                    saveSelectedStudents(selectedStudents);
                } else {
                    deleteRepeaterClass(courseId, classId, RepeaterclassId);
                }
            }
        };

        for (StudentModel repeater : removedRepeaters) {
            // Delete repeater from RepeaterStudents collection
            repeatersRef.document(repeater.getRollNo())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove repeater from CoursesStudents collection
                        coursesStudentsRef.whereEqualTo("CourseID", courseId)
                                .whereEqualTo("StudentRollNo", repeater.getRollNo())
                                .whereEqualTo("isRepeater", true)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        documentSnapshot.getReference().delete();
                                    }
                                    deleteSuccessListener.onSuccess(null); // Note: All repeaters deleted
                                })
                                .addOnFailureListener(e -> {
                                    // Dismiss the progress dialog if there's an error in deleting repeaters from CoursesStudents
                                    hideProgressDialog();
                                    Toast.makeText(ViewRepeatersForSelectionActivity.this, "Failed to remove existing repeaters from CoursesStudents.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Dismiss the progress dialog if there's an error in deleting repeaters from RepeaterStudents
                        hideProgressDialog();
                        Toast.makeText(ViewRepeatersForSelectionActivity.this, "Failed to remove existing repeaters from RepeaterStudents.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void deleteRepeaterClass(String courseId, String classId, String repeaterClassId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ClassRepeaters")
                .document(courseId + "_" + classId)
                .collection("RepeaterClasses")
                .document(repeaterClassId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ViewRepeatersForSelectionActivity.this, "All Repeaters Removed Successfully!", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure to delete repeater class document
                    Toast.makeText(ViewRepeatersForSelectionActivity.this, "Failed to delete repeater class", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSelectedStudents(@NonNull List<StudentModel> selectedStudents) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String courseId = UserInstituteModel.getInstance(this).getCourseId();
        String classId = UserInstituteModel.getInstance(this).getClassId();
        CollectionReference repeatersRef = db.collection("ClassRepeaters")
                .document(courseId + "_" + classId)
                .collection("RepeaterClasses")
                .document(RepeaterclassId)
                .collection("RepeaterStudents");

        // Reference to the CoursesStudents collection
        CollectionReference coursesStudentsRef = db.collection("CoursesStudents");

        // Using AtomicInteger to track successful save operations
        AtomicInteger savedCount = new AtomicInteger(0);

        for (StudentModel student : selectedStudents) {
            repeatersRef.document(student.getRollNo())
                    .set(student)
                    .addOnSuccessListener(aVoid -> {
                        Map<String, Object> courseStudentData = new HashMap<>();
                        courseStudentData.put("ClassID", RepeaterclassId);
                        courseStudentData.put("CourseID", courseId);
                        courseStudentData.put("CourseClassID", UserInstituteModel.getInstance(this).getClassId());
                        courseStudentData.put("StudentRollNo", student.getRollNo());
                        courseStudentData.put("SemesterID", UserInstituteModel.getInstance(this).getSemesterId()); // Replace "your_semester_id" with actual semester ID
                        courseStudentData.put("isRepeater", true);
                        courseStudentData.put("IsEnrolled", true);

                        coursesStudentsRef
                                .add(courseStudentData)
                                .addOnSuccessListener(documentReference -> {
                                    int count = savedCount.incrementAndGet();
                                    if (count == selectedStudents.size()) {
                                        hideProgressDialog();
                                        Toast.makeText(ViewRepeatersForSelectionActivity.this, "Save Changes Successfully!", Toast.LENGTH_SHORT).show();
                                        storeRepeaterClass(courseId, classId, RepeaterclassId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    hideProgressDialog();
                                    Toast.makeText(ViewRepeatersForSelectionActivity.this, "Failed to save selected students to CoursesStudents.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        hideProgressDialog();
                        Toast.makeText(ViewRepeatersForSelectionActivity.this, "Failed to save selected students to RepeaterStudents.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    private void storeRepeaterClass(String courseId, String classId, String repeaterClassId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the repeaterClasses collection
        CollectionReference repeaterClassesRef = db.collection("RepeaterClasses");

        // Query to check if the document already exists
        Query query = repeaterClassesRef.whereEqualTo("courseId", courseId)
                .whereEqualTo("classId", classId)
                .whereEqualTo("repeaterClassId", repeaterClassId);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Document with the same data does not exist, proceed with insertion
                        Map<String, Object> data = new HashMap<>();
                        data.put("courseId", courseId);
                        data.put("classId", classId);
                        data.put("repeaterClassId", repeaterClassId);

                        repeaterClassesRef
                                .add(data)
                                .addOnSuccessListener(documentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to store repeater class", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    // Log an error message if the query fails
                });

        finish();
    }


}
