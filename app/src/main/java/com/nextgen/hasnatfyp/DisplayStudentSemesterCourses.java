package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DisplayStudentSemesterCourses extends AppCompatActivity {

    private String userId;
    private String rollNo;
    private FirebaseFirestore db;
    private List<StudentCourseModel> activeCoursesList;
    private RecyclerView recyclerView;
    private StudentSemesterCoursesAdapter adapter;
    private int coursesToFetch; // Track number of courses to fetch
    private int coursesFetched; // Track number of courses fetched

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_semester_courses);

        // Get userId and rollNo from intent extras

        userId = StudentSessionInfo.getInstance(this).getStudentID();
        rollNo= StudentSessionInfo.getInstance(this).getStudentRollNo();
        setStudentInfo(StudentSessionInfo.getInstance(this).getStudentName(),StudentSessionInfo.getInstance(this).getStudentRollNo());

            // Initialize Firestore
            db = FirebaseFirestore.getInstance();

            // Initialize activeCoursesList
            activeCoursesList = new ArrayList<>();

            // Initialize RecyclerView
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new StudentSemesterCoursesAdapter(this, activeCoursesList,rollNo);
            recyclerView.setAdapter(adapter);

            // Retrieve student courses based on rollNo
            retrieveStudentCourses(rollNo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }
    private void setStudentInfo(String studentName, String rollNo) {
        TextView textViewStudentName = findViewById(R.id.textViewStudentName);
        TextView textViewRollNo = findViewById(R.id.textViewRollNo);
        TextView ClassNameTextView = findViewById(R.id.ClassNameTextView);

        textViewStudentName.setText(studentName);
        textViewRollNo.setText(rollNo);
        ClassNameTextView.setText(StudentSessionInfo.getInstance(this).getClassName());
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View my Classes", true);
    }
    private void showNoCourses() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Courses Found");
        builder.setMessage("It appears that no courses have been added for this student yet.");

        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Set the dialog to not cancelable
        dialog.show();
    }


    private void retrieveStudentCourses(String rollNo) {
ProgressDialogHelper.showProgressDialog(this,"Loading Courses...");
        CollectionReference coursesRef = db.collection("CoursesStudents");

        // Query to get courses for the given rollNo
        coursesRef.whereEqualTo("StudentRollNo", rollNo)
                .whereEqualTo("IsEnrolled", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activeCoursesList.clear(); // Clear existing list

                    coursesToFetch = queryDocumentSnapshots.size(); // Number of courses to fetch
                    if (coursesToFetch == 0) {
                        showNoCourses();
                        ProgressDialogHelper.dismissProgressDialog();
                        return;
                    }
                    coursesFetched = 0; // Reset fetched courses counter

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Extract classID and courseID from each document
                        String classID = documentSnapshot.getString("ClassID");
                        String courseID = documentSnapshot.getString("CourseID");

                        // Retrieve courses from 'ClassCourses' subcollection
                        retrieveClassCourses(classID, courseID);
                    }
                })
                .addOnFailureListener(e -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    Log.e("Firestore", "Error retrieving student courses: " + e.getMessage());
                });
    }

    private void retrieveClassCourses(String classID, String courseID) {
        // Reference to the 'ClassCourses' subcollection
        CollectionReference classCoursesRef = db.collection("ClassCourses").document(classID)
                .collection("ClassCoursesSubcollection");

        // Query to get courses for the given classID
        classCoursesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Extract course details
                        String courseIdFromFirestore = documentSnapshot.getId();
                        if (courseID.equals(courseIdFromFirestore)) {
                            String courseName = documentSnapshot.getString("CourseName");
                            // Assuming you have a model class StudentCourseModel
                            StudentCourseModel course = new StudentCourseModel(courseID, courseName);
                            activeCoursesList.add(course);
                        }
                    }

                    // Increment the fetched courses counter
                    coursesFetched++;

                    // Check if all courses have been fetched
                    if (coursesFetched == coursesToFetch) {
                        Collections.sort(activeCoursesList, Comparator.comparing(StudentCourseModel::getCourseName));
                        ProgressDialogHelper.dismissProgressDialog();
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("Firestore", "Error retrieving class courses: " + e.getMessage());
                });
    }
}
