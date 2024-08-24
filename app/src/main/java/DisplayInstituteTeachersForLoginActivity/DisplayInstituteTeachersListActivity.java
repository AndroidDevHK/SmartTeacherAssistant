package DisplayInstituteTeachersForLoginActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayInstituteTeachersListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InstituteTeacherAdapter adapter;
    private FirebaseFirestore db;
    private String instituteId;
    private String courseID;
    List<InstituteTeacherModel> teacherList =  new ArrayList<>();
    private AtomicInteger fetchCounter;
    private TextView noResultText;
    private ProgressDialog progressDialog;

    ActivityManager activityManager;
    private String courseName;
    private CardView courseCard;
    TextView TeachersCount;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_institute_teachers_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        courseCard = findViewById(R.id.textCard);
        noResultText = findViewById(R.id.noResultText);
        teacherList =  new ArrayList<>();
        Intent intent = getIntent();
        courseName = intent.getStringExtra("courseName");
        TeachersCount = findViewById(R.id.TeachersCount);


        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        activityManager = (ActivityManager) getApplication();
        activityManager.addActivityForKill(this);
        // Retrieve institute ID from UserInstituteModel
        instituteId = UserInstituteModel.getInstance(this).getInstituteId();
        courseID = UserInstituteModel.getInstance(this).getCourseId();

        recyclerView = findViewById(R.id.recycler_view_teachers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InstituteTeacherAdapter(this,teacherList,activityManager,courseName);
        recyclerView.setAdapter(adapter);
        fetchCounter = new AtomicInteger(0);
        showLoadingDialog("Fetching teachers...");
            retrieveTeachers();

        SearchView searchView = findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTeacher(newText);
                return true;
            }
        });
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Manage Teachers Permissions", true);
    }

    private void showLoadingDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void filterTeacher(String newText) {
        List<InstituteTeacherModel> filteredList = new ArrayList<>();

        if (newText.isEmpty()) {
            // If the search query is empty, show all teachers
            filteredList.addAll(teacherList);
        } else {
            // Convert the search query to lowercase for case-insensitive search
            String query = newText.toLowerCase().trim();

            for (InstituteTeacherModel teacher : teacherList) {
                // Check if the teacher's name or username contains the search query
                if (teacher.getTeacherName().toLowerCase().contains(query) ||
                        teacher.getTeacherUsername().toLowerCase().contains(query)) {
                    // If yes, add the teacher to the filtered list
                    filteredList.add(teacher);
                }
            }
        }

        adapter.setTeacherList(filteredList);
        if (filteredList.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE); // Show the TextView
        } else {
            noResultText.setVisibility(View.GONE); // Hide the TextView
        }
    }

    private void retrieveTeachers() {
        // Query InstituteTeachers collection for teachers associated with the institute
        db.collection("InstituteTeachers")
                .whereEqualTo("InstituteID", instituteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchCounter.set(task.getResult().size());
                        if (task.getResult().size() == 0) {
                            // Show an AlertDialog with error message and option to go to admin panel
                            AlertDialog.Builder builder = new AlertDialog.Builder(DisplayInstituteTeachersListActivity.this);
                            builder.setTitle("Error");
                            builder.setMessage("There are currently no teachers registered.\nGo To \nAdmin Panel - > Register Staff \nto Register Teacher");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            });
                            builder.setCancelable(false);
                            builder.show();
                            return;
                        }
                        for (DocumentSnapshot document : task.getResult()) {
                            String teacherUsername = document.getString("Username");
                            // Retrieve additional teacher details from the Teachers collection
                            retrieveTeacherDetails(teacherUsername);
                        }
                    } else {
                        // Show a toast indicating failure to retrieve teachers
                        Toast.makeText(DisplayInstituteTeachersListActivity.this, "Failed to retrieve teachers", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @SuppressLint("SetTextI18n")
    private void retrieveTeacherDetails(String teacherUsername) {
        // Query Teachers collection to get teacher details
        db.collection("Teachers")
                .whereEqualTo("Username", teacherUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String teacherName = doc.getString("TeacherName");
                        String qualification = doc.getString("Qualification");
                        String department = doc.getString("Department");
                        boolean pastAPermission = Boolean.TRUE.equals(doc.getBoolean("PastAPermission"));
                        boolean accountStatus = Boolean.TRUE.equals(doc.getBoolean("AccountStatus"));

                        InstituteTeacherModel teacherModel = new InstituteTeacherModel(teacherUsername, teacherName, qualification, department, pastAPermission, accountStatus);
                        teacherList.add(teacherModel);

                        if (fetchCounter.decrementAndGet() == 0) {
                            TeachersCount.setText("Total Teachers : " + teacherList.size());
                            adapter.setTeacherList(teacherList);
                            dismissLoadingDialog();
                        }
                    } else {
                        Toast.makeText(DisplayInstituteTeachersListActivity.this, "Failed to retrieve teacher details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
