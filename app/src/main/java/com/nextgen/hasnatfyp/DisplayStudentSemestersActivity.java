package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayStudentSemestersActivity extends AppCompatActivity {

    private static final String TAG = "DisplayTeacherSemester";

    private RecyclerView recyclerView;
    private StudentSemestersAdapter adapter;
    private List<StudentSemestersModel> studentSemestersList;
    private ProgressDialog progressDialog;
    private SearchView searchView;
    private TextView noResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_semesters);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentSemestersList = new ArrayList<>();
        adapter = new StudentSemestersAdapter(this, studentSemestersList);
        recyclerView.setAdapter(adapter);

        noResultTextView = findViewById(R.id.noResultTextView);
        searchView = findViewById(R.id.simpleSearchView);
        setupSearchView();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);

        // Assuming the student roll number is "6122" and institute ID is "1"
        String instituteID = "1";
        String rollNo = "6122";
        retrieveSemesterIDAndClassID(instituteID, rollNo);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String query) {
        List<StudentSemestersModel> filteredList = new ArrayList<>();
        for (StudentSemestersModel model : studentSemestersList) {
            if (model.getSemesterName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(model);
            }
        }

        if (filteredList.isEmpty()) {
            noResultTextView.setVisibility(View.VISIBLE);
        } else {
            noResultTextView.setVisibility(View.GONE);
        }

        adapter.filterList(filteredList);
    }

    private void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Semesters / Years");
    }

    private void retrieveSemesterIDAndClassID(String instituteID, String rollNo) {
        progressDialog.show();
        AtomicBoolean hasActiveSemester = new AtomicBoolean(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference studentSemestersRef = db.collection("StudentSemestersDetails");

        studentSemestersRef.whereEqualTo("StudentRollNo", rollNo)
                .whereEqualTo("InstituteID", instituteID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalCount = task.getResult().size();
                        AtomicInteger count = new AtomicInteger(0);
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documents) {
                            String semesterID = document.getString("SemesterID");
                            String classID = document.getString("ClassID");
                            if (semesterID != null) {
                                retrieveSemesterNameAndStatus(semesterID, totalCount, count, hasActiveSemester, classID);
                            }
                        }

                        if (totalCount > 3) {
                            searchView.setVisibility(View.VISIBLE);
                        } else {
                            searchView.setVisibility(View.GONE);
                        }

                        if (totalCount == 0) {
                            progressDialog.dismiss();
                            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                            searchView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(TAG, "Error retrieving semester IDs: ", task.getException());
                        progressDialog.dismiss();
                        findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                    }
                });
    }

    private void retrieveSemesterNameAndStatus(String semesterID, int totalCount, AtomicInteger count, AtomicBoolean hasActiveSemester, String classID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference semesterRef = db.collection("Semesters");

        semesterRef.document(semesterID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            boolean isActive = document.getBoolean("isActive");
                            String semesterName = document.getString("semesterName");

                            if (isActive && semesterName != null) {
                                retrieveClassCoursesCount(semesterID, semesterName, totalCount, count, classID);
                                hasActiveSemester.set(true);
                            } else {
                                count.incrementAndGet();
                                if (count.get() == totalCount) {
                                    progressDialog.dismiss();
                                    if (!hasActiveSemester.get()) {
                                        searchView.setVisibility(View.GONE);
                                        findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Semester document not found for ID: " + semesterID);
                            count.incrementAndGet();
                            if (count.get() == totalCount) {
                                progressDialog.dismiss();
                                if (!hasActiveSemester.get()) {
                                    findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Error retrieving semester document: ", task.getException());
                        progressDialog.dismiss();
                        if (!hasActiveSemester.get()) {
                            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void retrieveClassCoursesCount(String semesterID, String semesterName, int totalCount, AtomicInteger count, String classID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference classCoursesRef = db.collection("ClassCourses");

        classCoursesRef.document(classID)
                .collection("ClassCoursesSubcollection")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> activeCoursesNames = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Boolean isCourseActive = document.getBoolean("IsCourseActive");
                        if (isCourseActive != null && isCourseActive) {
                            activeCoursesNames.add(document.getString("CourseName"));
                        }
                    }
                    StudentSemestersModel model = new StudentSemestersModel(semesterID, semesterName, activeCoursesNames.size(), classID);
                    studentSemestersList.add(model);

                    count.incrementAndGet();
                    if (count.get() == totalCount) {
                        progressDialog.dismiss();
                        adapter.notifyDataSetChanged();
                        if (studentSemestersList.isEmpty()) {
                            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving class courses: ", e);
                    count.incrementAndGet();
                    if (count.get() == totalCount) {
                        progressDialog.dismiss();
                    }
                });
    }
}
