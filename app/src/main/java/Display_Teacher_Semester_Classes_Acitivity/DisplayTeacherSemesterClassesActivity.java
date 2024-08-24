package Display_Teacher_Semester_Classes_Acitivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nextgen.hasnatfyp.DataStorageHelperTeacherClasses;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.SimpleLoadingDialog;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Course_Repeaters_Activity.ViewClassCourseRepeaters;
import View_Class_Students_Activity.StudentModel;

public class DisplayTeacherSemesterClassesActivity extends AppCompatActivity {

    private static final String TAG = "DisplayTeacherClasses";

    private List<TeacherClassModel> teacherClassesList;
    private TeacherClassesAdapter adapter;

    private SearchView searchView;
    private TextView noResultTextView;
    private List<TeacherClassModel> filteredTeacherClassesList;
    private RecyclerView recyclerView;
    private SimpleLoadingDialog loadingDialog;
    String TeacherUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_teacher_classes);
        loadingDialog = new SimpleLoadingDialog(this, "Loading Classes..");
        loadingDialog.show();
        if(UserInstituteModel.getInstance(this).isSoloUser()) {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
            Log.e(TAG, TeacherUserName);

        }
        else
        {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();

        }

        teacherClassesList = new ArrayList<>();
        filteredTeacherClassesList = new ArrayList<>();
        adapter = new TeacherClassesAdapter(teacherClassesList);

        recyclerView = findViewById(R.id.recycler_view_teacher_classes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.simpleSearchView);
        noResultTextView = findViewById(R.id.noResultTextView);

        String semesterID = getIntent().getStringExtra("semesterID");

        setupSearchView();
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);

        if (isNetworkAvailable()) {
            retrieveCourseAndClassID(semesterID, TeacherUserName);
        } else {
            Toast.makeText(this, "No internet connection. Showing recently fetched data.", Toast.LENGTH_SHORT).show();
            teacherClassesList = DataStorageHelperTeacherClasses.readTeacherClassesListLocally(DisplayTeacherSemesterClassesActivity.this);
            adapter.setTeacherClassesList(teacherClassesList);
            adapter.notifyDataSetChanged();
            loadingDialog.dismiss();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "My Classes - " + TeacherInstanceModel.getInstance(this).getSemesterName(), true);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTeacherClasses(newText);
                return true;
            }
        });
    }

    private void filterTeacherClasses(@NonNull String query) {
        filteredTeacherClassesList.clear();
        if (query.isEmpty()) {
            filteredTeacherClassesList.addAll(teacherClassesList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (TeacherClassModel teacherClass : teacherClassesList) {
                String className = teacherClass.getClassName().toLowerCase();
                String courseName = teacherClass.getCourseName().toLowerCase();
                if (className.contains(lowerCaseQuery) || courseName.contains(lowerCaseQuery)) {
                    filteredTeacherClassesList.add(teacherClass);
                }
            }
        }

        adapter.setTeacherClassesList(filteredTeacherClassesList);
        if (filteredTeacherClassesList.isEmpty()) {
            noResultTextView.setVisibility(TextView.VISIBLE);
        } else {
            noResultTextView.setVisibility(TextView.GONE);
        }

    }


    private void retrieveCourseAndClassID(String semesterID, String teacherUsername) {
        Log.d(TAG, "Retrieving course and class ID for semesterID: " + semesterID + ", teacherUsername: " + teacherUsername);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("TeacherCourses")
                .whereEqualTo("SemesterID", semesterID)
                .whereEqualTo("TeacherUsername", teacherUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicInteger totalClasses = new AtomicInteger(task.getResult().size()); // Keep track of total classes
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String courseID = document.getString("CourseID");
                            String classID = document.getString("ClassID");
                            if (courseID != null && classID != null) {
                                Log.d(TAG, "Retrieved courseID: " + courseID + ", classID: " + classID);
                                // Retrieve class name and set it in TeacherClassModel
                                retrieveClassName(classID, new ClassNameCallback() {
                                    @Override
                                    public void onClassNameRetrieved(String className) {
                                        Log.d(TAG, "Retrieved className: " + className);
                                        // Retrieve regular and repeater students
                                        retrieveStudents(courseID, classID, new StudentsCallback() {
                                            @Override
                                            public void onStudentsRetrieved(List<StudentModel> regularStudents, List<StudentModel> repeaterStudents) {
                                                // Retrieve course name
                                                retrieveCourseName(classID, courseID, new CourseNameCallback() {
                                                    @Override
                                                    public void onCourseNameRetrieved(String courseName) {
                                                        Log.d(TAG, "Retrieved courseName: " + courseName);
                                                        TeacherClassModel teacherClassModel = new TeacherClassModel(
                                                                classID, className, courseID, courseName,
                                                                regularStudents, regularStudents.size(),
                                                                repeaterStudents.size(), repeaterStudents
                                                        );

                                                        teacherClassesList.add(teacherClassModel);
                                                        if (totalClasses.decrementAndGet() == 0) {
                                                            DataStorageHelperTeacherClasses.storeTeacherClassesListLocally(DisplayTeacherSemesterClassesActivity.this, teacherClassesList);
                                                            adapter.notifyDataSetChanged();
                                                            loadingDialog.dismiss();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        Log.e(TAG, "Error retrieving course name: ", e);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e(TAG, "Error retrieving students: ", e);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "Error retrieving class name: ", e);
                                    }
                                });
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void retrieveStudents(String courseId, String classID, StudentsCallback callback) {
        List<StudentModel> regularStudents = new ArrayList<>();
        List<StudentModel> repeaterStudents = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CoursesStudents")
                .whereEqualTo("CourseID", courseId)
                .whereEqualTo("IsEnrolled", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> studentRollNumbers = new ArrayList<>();
                        Map<String, Boolean> rollNoToRepeaterMap = new HashMap<>();
                        Map<String, String> rollNoToClassIdMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentRollNo = document.getString("StudentRollNo");
                            boolean isRepeater = document.getBoolean("isRepeater");
                            String studentClassID = document.getString("ClassID");

                            if (studentRollNo != null) {
                                studentRollNumbers.add(studentRollNo);
                                rollNoToClassIdMap.put(studentRollNo, studentClassID);
                                rollNoToRepeaterMap.put(studentRollNo, isRepeater);
                            }
                        }

                        AtomicInteger studentCounter = new AtomicInteger(0);

                        for (String rollNo : studentRollNumbers) {
                            String studentClassID = rollNoToClassIdMap.get(rollNo);
                            db.collection("Classes")
                                    .document(studentClassID)
                                    .collection("ClassStudents")
                                    .whereEqualTo("RollNo", rollNo)
                                    .whereEqualTo("IsActive", true) // Only query active students
                                    .get()
                                    .addOnCompleteListener(studentTask -> {
                                        if (studentTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot studentDocument : studentTask.getResult()) {
                                                String studentName = getShortenedName(studentDocument.getString("StudentName"));
                                                String studentRollNo = studentDocument.getString("RollNo");
                                                boolean isRepeater = rollNoToRepeaterMap.getOrDefault(studentRollNo, false);

                                                if (isRepeater) {
                                                    repeaterStudents.add(new StudentModel(studentName, studentRollNo, true, "P", studentClassID));
                                                } else {
                                                    regularStudents.add(new StudentModel(studentName, studentRollNo, true, "P", classID));
                                                }
                                            }

                                            int count = studentCounter.incrementAndGet();

                                            if (count == studentRollNumbers.size()) {
                                                // Sorting roll numbers using custom comparator
                                                Collections.sort(regularStudents, new RollNoComparator());
                                                Collections.sort(repeaterStudents, new RollNoComparator());

                                                callback.onStudentsRetrieved(regularStudents, repeaterStudents);
                                            }
                                        } else {
                                            callback.onFailure(studentTask.getException());
                                        }
                                    });
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public interface StudentsCallback {
        void onStudentsRetrieved(List<StudentModel> regularStudents, List<StudentModel> repeaterStudents);

        void onFailure(Exception e);
    }
    private String getShortenedName(String fullName) {
        String shortname = fullName.replaceAll("\\b(?i)(Muhammad|Mohammad|Mohd|Mohammed)\\b", "M.");
        return shortname;
    }
    private void retrieveClassName(String classID, ClassNameCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the Classes collection to get the class name
        db.collection("Classes")
                .document(classID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String className = task.getResult().getString("ClassName");
                            if (className != null) {
                                // Pass the class name to the callback
                                callback.onClassNameRetrieved(className);
                            } else {
                                callback.onFailure(new Exception("Class name is null"));
                            }
                        } else {
                            callback.onFailure(new Exception("Document does not exist"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    private void retrieveCourseName(String classID, String courseID, CourseNameCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the ClassCoursesSubcollection to get the course name
        db.collection("ClassCourses")
                .document(classID)
                .collection("ClassCoursesSubcollection")
                .document(courseID)  // Assuming courseID is the document ID in ClassCoursesSubcollection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String courseName = task.getResult().getString("CourseName");
                            if (courseName != null) {
                                // Pass the course name to the callback
                                callback.onCourseNameRetrieved(courseName);
                                return;
                            }
                        }
                        callback.onFailure(new Exception("Course name not found"));
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public interface ClassNameCallback {
        void onClassNameRetrieved(String className);

        void onFailure(Exception e);
    }

    public interface CourseNameCallback {
        void onCourseNameRetrieved(String courseName);

        void onFailure(Exception e);
    }

    class RollNoComparator implements Comparator<StudentModel> {
        @Override
        public int compare(StudentModel s1, StudentModel s2) {
            String rollNo1 = s1.getRollNo();
            String rollNo2 = s2.getRollNo();

            if (isNumeric(rollNo1) && isNumeric(rollNo2)) {
                return Integer.compare(Integer.parseInt(rollNo1), Integer.parseInt(rollNo2));
            }
            return rollNo1.compareTo(rollNo2);
        }

        private boolean isNumeric(String str) {
            return str != null && str.matches("\\d+");
        }
    }
}
