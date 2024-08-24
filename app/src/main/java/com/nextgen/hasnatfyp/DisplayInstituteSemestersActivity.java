package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import Display_Institute_Teachers_To_Assign_Him_Course_Activity.TeacherModel;

public class DisplayInstituteSemestersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InstituteSemestersAdapter adapter;
    private List<InstituteSemesterModel> semesterList;
    private TextView noResultTextView;
    private FirebaseFirestore db;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_institute_semesters);

        recyclerView = findViewById(R.id.recycler_view_teacher_semesters);
        noResultTextView = findViewById(R.id.noResultTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        semesterList = new ArrayList<>();
        adapter = new InstituteSemestersAdapter(semesterList,this); // Initialize adapter with semesterList
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        fetchSemesters();
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
       searchView = findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSemesters(newText);
                return true;
            }
        });
    }

    private void filterSemesters(String newText) {
        List<InstituteSemesterModel> filteredList = new ArrayList<>();

        if (newText.isEmpty()) {
            // If the search query is empty, show all teachers
            filteredList.addAll(semesterList);
        } else {
            // Convert the search query to lowercase for case-insensitive search
            String query = newText.toLowerCase().trim();

            for (InstituteSemesterModel teacher : semesterList) {
                // Check if the teacher's name or username contains the search query
                if (teacher.getSemesterName().toLowerCase().contains(query)) {
                    filteredList.add(teacher);
                }
            }
        }

        adapter.setSemesterList(filteredList);
        if (filteredList.isEmpty()) {
            noResultTextView.setVisibility(View.VISIBLE); // Show the TextView
        } else {
            noResultTextView.setVisibility(View.GONE); // Hide the TextView
        }
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Display Term/Semesters", true);
    }
    @SuppressLint("SetTextI18n")
    private void fetchSemesters() {
        ProgressDialogHelper.showProgressDialog(this,"Fetching data...");
        String instituteID = "";
        if(!UserInstituteModel.getInstance(this).isSoloUser())
        {instituteID = UserInstituteModel.getInstance(this).getInstituteId();}
        else
        {Toast.makeText(this, "Error : 402", Toast.LENGTH_SHORT).show();
        return;}


        db.collection("InstituteSemesters")
                .whereEqualTo("InstituteID", instituteID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        int totalSemesters = querySnapshot.size();
                        int fetchedSemesters = 0;

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String semesterID = document.getString("SemesterID");
                            String semesterName = document.getString("semesterName");
                            InstituteSemesterModel semester = new InstituteSemesterModel(semesterID, semesterName);
                            semesterList.add(semester);
                            fetchedSemesters++;
                        }

                        // Update UI based on fetched data
                        if (totalSemesters == 0) {
                            searchView.setVisibility(View.GONE);
                            noResultTextView.setText("No Term/Semester Available to show");
                            noResultTextView.setVisibility(View.VISIBLE);
                            ProgressDialogHelper.dismissProgressDialog();
                        } else {
                            ProgressDialogHelper.dismissProgressDialog();
                            noResultTextView.setVisibility(View.GONE);
                        }

                        // Notify adapter if all semesters are fetched
                        if (fetchedSemesters == totalSemesters) {
                            ProgressDialogHelper.dismissProgressDialog();
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        ProgressDialogHelper.dismissProgressDialog();
                        Log.d("DisplayInstituteSemesters", "Error getting documents: ", task.getException());
                        noResultTextView.setVisibility(View.VISIBLE);
                    }
                });
    }
}
