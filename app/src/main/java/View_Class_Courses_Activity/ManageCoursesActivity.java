package View_Class_Courses_Activity;
import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Institute_Teachers_To_Assign_Him_Course_Activity.TeacherModel;

public class ManageCoursesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private FirebaseFirestore db;
    private String classId;
    ActivityManager activityManager;
    private AtomicInteger fetchCounter;
    private ProgressDialog progressDialog;

    private List<CourseModel> courses = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);



        recyclerView = findViewById(R.id.recycler_view_courses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        activityManager = (ActivityManager) getApplication();
        activityManager.addActivityForKill(this);
        db = FirebaseFirestore.getInstance();

        // Get class ID from UserInstituteModel
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(this);
        classId = userInstituteModel.getClassId();
        adapter = new CourseAdapter(this,activityManager);
        recyclerView.setAdapter(adapter);
        showLoadingDialog("Loading courses...");


        if (UserInstituteModel.getInstance(this).getCourseList().isEmpty()) {
            fetchCounter = new AtomicInteger();
            fetchCounter.set(0);
            retrieveCourseDetails();

        } else {
            List<CourseModel> courseList = UserInstituteModel.getInstance(this).getCourseList();
            courses.clear();
            courses.addAll(courseList);
            adapter.setCourseList(courses);
            dismissLoadingDialog();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);

        SearchView searchView = findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCourses(newText);
                return true;
            }
        });
    }

    private void filterCourses(String query) {
        List<CourseModel> filteredList = new ArrayList<>();
        for (CourseModel course : courses) {
            if (course.getCourseName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(course);
            }
        }
        adapter.setCourseList(filteredList);

        // Update visibility of noResultText
        if (filteredList.isEmpty()) {
            findViewById(R.id.noResultText).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.noResultText).setVisibility(View.GONE);
        }
    }


    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, UserInstituteModel.getInstance(this).getClassName() + " - Courses", true);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Call default behavior (navigate back)
        cleanupData(); // Call your data cleanup method
    }

    private void cleanupData() {
        UserInstituteModel.getInstance(this).getCourseList().clear();
        UserInstituteModel.getInstance(this).getTeacherList().clear();
        UserInstituteModel.getInstance(this).getRepeaterClassList().clear();
    }

    private void retrieveCourseDetails() {
        db.collection("ClassCourses")
                .document(classId)
                .collection("ClassCoursesSubcollection")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CourseModel> courses = new ArrayList<>();
                        fetchCounter.set(task.getResult().size());
                        for (DocumentSnapshot document : task.getResult()) {
                            String courseId = document.getId();
                            String courseName = document.getString("CourseName");
                            int creditHours = Integer.parseInt(document.getString("CreditHours"));
                            boolean isCourseActive = document.getBoolean("IsCourseActive");

                            CourseModel courseModel = new CourseModel(courseId, courseName, creditHours, isCourseActive, "", "");

                            courses.add(courseModel);
                            checkTeacherAssignment(courseModel);
                        }
                        // Sort courses alphabetically by name
                        Collections.sort(courses, (c1, c2) -> c1.getCourseName().compareToIgnoreCase(c2.getCourseName()));
                        UserInstituteModel.getInstance(this).getCourseList().addAll(courses);
                    } else {
                        Toast.makeText(ManageCoursesActivity.this, "Failed to retrieve courses", Toast.LENGTH_SHORT).show();
                        dismissLoadingDialog();
                    }
                });
    }

    private void checkTeacherAssignment(CourseModel courseModel) {
        db.collection("TeacherCourses")
                .whereEqualTo("CourseID", courseModel.getCourseId())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            String teacherUsername = task.getResult().getDocuments().get(0).getString("TeacherUsername");
                            courseModel.setCourseTeacher(teacherUsername);
                            retrieveTeacherFullName(teacherUsername, courseModel);
                        } else {
                            courseModel.setCourseTeacher("");
                            checkFetchComplete();
                        }
                    } else {
                        Toast.makeText(ManageCoursesActivity.this, "Failed to check teacher assignment", Toast.LENGTH_SHORT).show();
                        dismissLoadingDialog();
                    }
                });
    }

    private void retrieveTeacherFullName(String teacherUsername, CourseModel courseModel) {
        db.collection("Teachers")
                .whereEqualTo("Username", teacherUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String fullName = document.getString("TeacherName");
                            if (fullName != null && !fullName.isEmpty()) {
                                courseModel.setCourseTeacherFullName(fullName);
                            }
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                    checkFetchComplete();
                });
    }


    private void checkFetchComplete() {
        if (fetchCounter.decrementAndGet() == 0) {
            courses = new ArrayList<>(UserInstituteModel.getInstance(this).getCourseList());
            adapter.setCourseList(courses); // Update the adapter with the fetched data
            Log.d("CourseAdapter", "Size of courses list: " + courses.size());
            dismissLoadingDialog();
        }
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

    public void refreshData() {
        if (UserInstituteModel.getInstance(this).getCourseList().isEmpty()) {
            fetchCounter = new AtomicInteger();
            fetchCounter.set(0);
            showLoadingDialog("Loading courses...");
            retrieveCourseDetails();
        } else {
            List<CourseModel> courseList = UserInstituteModel.getInstance(this).getCourseList();
            courses.clear();
            courses.addAll(courseList);
            adapter.setCourseList(courses);
            dismissLoadingDialog();
        }
    }

}
