package Display_Course_Evaluations_List_Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.CourseEvaluationDetailsModel;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DisplayCourseEvaluationListActivity extends AppCompatActivity {

    private static final String TAG = "DisplayCourseEvaluationListActivity";

    private RecyclerView recyclerView;
    private CourseEvaluationListAdapter adapter;
    private List<CourseEvaluationInfoModel> courseEvaluationList;

    private FirebaseFirestore db;
    private String courseID;
    private boolean areRepeaters;
    int totalEvaluations;
    ActivityManager activityManager;
    TextView totalEvalCountTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_course_evaluation_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseEvaluationList = new ArrayList<>();
        totalEvalCountTextView = findViewById(R.id.TotalEvalCountTxtView);

        activityManager = (ActivityManager) getApplication();
        activityManager.addActivityForKill(this);
        db = FirebaseFirestore.getInstance();
        if (getIntent().hasExtra("CourseID")) {
            areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
            courseID = getIntent().getStringExtra("CourseID");
            fetchCourseEvaluations(courseID);
            ProgressDialogHelper.showProgressDialog(this, "Fetching Evaluation Details...");
            setClassAndCourseName();
        } else {
            Log.e(TAG, "No Course ID found in intent");
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Evaluation Details", true);
    }

    @SuppressLint("SetTextI18n")
    private void setClassAndCourseName() {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);

            classNameTextView.setText(className);
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }

    private void fetchCourseEvaluations(String courseID) {
        db.collection("CourseEvaluationsInfo")
                .whereEqualTo("CourseID", courseID)
                .whereEqualTo("AreRepeaters", areRepeaters)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalEvaluations = queryDocumentSnapshots.size();
                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoEvaluationsDialog();
                    } else {
                        totalEvalCountTextView.setText(String.valueOf(totalEvaluations));
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String evalID = document.getString("EvalID");
                            String date = document.getString("CreatedWhen");
                            fetchEvaluationDetails(evalID, date);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching course evaluations: " + e.getMessage());
                    ProgressDialogHelper.dismissProgressDialog();
                });
    }

    private void fetchEvaluationDetails(String evalID, String date) {
        db.collection("Evaluations")
                .document(evalID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String evalName = documentSnapshot.getString("EvalName");
                    double evalTMarks = Double.parseDouble(documentSnapshot.getString("EvalTMarks"));

                    retrieveStudentsEvaluation(evalID, evalName, evalTMarks, date);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching evaluation details: " + e.getMessage());
                });
    }


    private void retrieveStudentsEvaluation(String evalID, String evalName, double evalTMarks, String date) {
        List<CourseEvaluationDetailsModel> localEvaluationDetailsList = new ArrayList<>();

        db.collection("CourseStudentsEvaluation")
                .whereEqualTo("EvalID", evalID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int taskCount = queryDocumentSnapshots.size();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String studentRollNo = document.getString("StudentRollNo");
                        double obtainedMarks = document.getDouble("StudentObtMarks");
                        String classID = document.getString("ClassID");

                        retrieveStudentNames(evalID, evalName, evalTMarks, date, studentRollNo, obtainedMarks, classID, localEvaluationDetailsList, taskCount);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving students evaluation: " + e.getMessage());
                });
    }
    private String getShortenedName(String fullName) {
        String shortname = fullName.replaceAll("\\b(?i)(Muhammad|Mohammad|Mohd|Mohammed)\\b", "M.");
        return shortname;
    }
    private void retrieveStudentNames(String evalID, String evalName, double evalTMarks, String date, String studentRollNo, double obtainedMarks, String classID, List<CourseEvaluationDetailsModel> localEvaluationDetailsList, int taskCount) {
        db.collection("Classes").document(classID)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", studentRollNo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String studentName = getShortenedName(document.getString("StudentName"));
                        CourseEvaluationDetailsModel evaluationDetails = new CourseEvaluationDetailsModel(studentName, studentRollNo, obtainedMarks);
                        localEvaluationDetailsList.add(evaluationDetails);
                    }

                    // If this is the last student, add all evaluation details to the main list
                    if (localEvaluationDetailsList.size() == taskCount) {
                        Collections.sort(localEvaluationDetailsList, Comparator.comparing(CourseEvaluationDetailsModel::getStudentRollNo));

                        courseEvaluationList.add(new CourseEvaluationInfoModel(evalName, evalTMarks, date, localEvaluationDetailsList, evalID));
                        adapter = new CourseEvaluationListAdapter(courseEvaluationList, areRepeaters,totalEvalCountTextView,totalEvaluations);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student names: " + e.getMessage());
                });
    }

    private void showNoEvaluationsDialog() {
        ProgressDialogHelper.dismissProgressDialog();
        new AlertDialog.Builder(this)
                .setTitle("No Evaluations Found")
                .setMessage("No evaluations available for this course.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
