package Display_Institute_Teachers_To_Assign_Him_Course_Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import View_Class_Courses_Activity.ManageCoursesActivity;

public class DisplayInstituteTeachersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private FirebaseFirestore db;
    private String instituteId;
    private String courseID;
    List<TeacherModel> teacherList =  new ArrayList<>();
    private AtomicInteger fetchCounter;
    private TextView noResultText;
    private ProgressDialog progressDialog;

    ActivityManager activityManager;
    private String courseName;
    private String teacherUserName;
    private String teacherFullName;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_institute_teachers);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        noResultText = findViewById(R.id.noResultText);

        Intent intent = getIntent();
        courseName = intent.getStringExtra("courseName");
        teacherUserName = intent.getStringExtra("teacherUserName");
        teacherFullName = intent.getStringExtra("teacherFullName");
        TextView courseTextView = findViewById(R.id.CourseTextView);
        TextView currentTeacherTextView = findViewById(R.id.CourseTeacherTextView);
        courseTextView.setText("Course: " + courseName);
        if (teacherUserName != null && !teacherUserName.isEmpty()) {
            currentTeacherTextView.setText("Current Teacher: " + teacherFullName + " (" + teacherUserName + ")");
        } else {
            currentTeacherTextView.setText("Current Teacher: Not Assigned");
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        activityManager = (ActivityManager) getApplication();
        activityManager.addActivityForKill(this);
        // Retrieve institute ID from UserInstituteModel
        instituteId = UserInstituteModel.getInstance(this).getInstituteId();
        courseID = UserInstituteModel.getInstance(this).getCourseId();

        recyclerView = findViewById(R.id.recycler_view_teachers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherAdapter(this,teacherList,activityManager,courseName,teacherUserName);
        recyclerView.setAdapter(adapter);
        fetchCounter = new AtomicInteger(0);
        showLoadingDialog("Fetching teachers...");
        if (UserInstituteModel.getInstance(this).getTeacherList().isEmpty()) {
            retrieveTeachers();
        } else
        {
            List<TeacherModel> teacherlisttemp = UserInstituteModel.getInstance(this).getTeacherList();
            teacherList.clear();
            teacherList.addAll(teacherlisttemp);
            adapter.setTeacherList(teacherList);
            dismissLoadingDialog();
        }
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
        SetupToolbar.setup(this, toolbar, "Select Teacher", true);
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
        List<TeacherModel> filteredList = new ArrayList<>();

        if (newText.isEmpty()) {
            // If the search query is empty, show all teachers
            filteredList.addAll(teacherList);
        } else {
            // Convert the search query to lowercase for case-insensitive search
            String query = newText.toLowerCase().trim();

            for (TeacherModel teacher : teacherList) {
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
                        List<TeacherModel> teacherList = UserInstituteModel.getInstance(this).getTeacherList();
                        teacherList.clear();
                        fetchCounter.set(task.getResult().size());
                        if(task.getResult().size()==0)
                        {
                            Toast.makeText(this,"There are currently no teachers registered.",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        for (DocumentSnapshot document : task.getResult()) {
                            String teacherUsername = document.getString("Username");
                            // Retrieve additional teacher details from the Teachers collection
                            retrieveTeacherDetails(teacherUsername);
                        }
                    } else {
                        Toast.makeText(DisplayInstituteTeachersActivity.this, "Failed to retrieve teachers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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

                        // Create a TeacherModel object and add it to the list
                        TeacherModel teacherModel = new TeacherModel(teacherUsername, teacherName, qualification, department);
                        UserInstituteModel.getInstance(this).getTeacherList().add(teacherModel);

                        if (fetchCounter.decrementAndGet() == 0) {
                            // Copy all elements from tempTeacherList to teacherList
                            teacherList.clear();
                            teacherList.addAll(UserInstituteModel.getInstance(this).getTeacherList());
                            dismissLoadingDialog();
                            // Notify adapter
                            adapter.setTeacherList(teacherList);
                        }
                    } else {
                        Toast.makeText(DisplayInstituteTeachersActivity.this, "Failed to retrieve teacher details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
