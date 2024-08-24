package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DisplayStudentCourseEvaluationActivity extends AppCompatActivity {

    private static final String TAG = "DisplayStudenteval";
    private FirebaseFirestore db;
    private List<SCEvaluationModel> evaluationList;
    private String studentRollNo;
    private String courseId;

    private RecyclerView recyclerView;

    private TextView classNameTextView;
    private TextView courseNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_course_evaluation);

        db = FirebaseFirestore.getInstance();
        evaluationList = new ArrayList<>();
        getExtrasFromIntent();

        classNameTextView = findViewById(R.id.classNameTextView);
        courseNameTextView = findViewById(R.id.courseNameTextView);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setupToolbar(toolbar);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch evaluations data
        fetchAttendanceList();
        setCourseAndClassName();
    }
    public void setCourseAndClassName() {
        classNameTextView.setText(StudentSessionInfo.getInstance(this).getClassName());
        courseNameTextView.setText(StudentSessionInfo.getInstance(this).getCourseName());
    }
    private void setupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Course Evaluations", true);

    }

    private void getExtrasFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            studentRollNo = intent.getStringExtra("ROLL_NO");
            courseId = intent.getStringExtra("COURSE_ID");
        }
    }

    private void fetchAttendanceList() {
        ProgressDialogHelper.showProgressDialog(this,"Fetching Data...");
        db.collection("StudentCourseEvaluationList")
                .whereEqualTo("StudentRollNo", studentRollNo)
                .whereEqualTo("CourseID", courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            ProgressDialogHelper.dismissProgressDialog();
                            showNoEvaluationDialog();
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
                                List<String> EvaluationIDs = (List<String>) document.get("EvaluationIDs");
                                if (EvaluationIDs != null) {
                                    int size = EvaluationIDs.size();
                                    fetchEvaluationDetails(EvaluationIDs, size);
                                }
                            }
                        }
                    } else {
                        ProgressDialogHelper.dismissProgressDialog();
                        showNoEvaluationDialog();
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }


    private void fetchEvaluationDetails(List<String> EvaluationIDs, int size) {
        for (String EvaluationID : EvaluationIDs) {
            db.collection("CourseStudentsEvaluation")
                    .whereEqualTo("EvalID", EvaluationID)
                    .whereEqualTo("StudentRollNo", studentRollNo)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String evalId = document.getString("EvalID");
                                double obtainedMarks = document.getDouble("StudentObtMarks");
                                String obtainedMarksStr;
                                if (obtainedMarks == (long) obtainedMarks) {
                                    obtainedMarksStr = String.format("%d", (long) obtainedMarks); // Cast to long and format as integer
                                } else {
                                    obtainedMarksStr = String.format("%s", obtainedMarks); // Keep as decimal if not a whole number
                                }
                                fetchEval(evalId, obtainedMarksStr, size);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }

    private void fetchEval(String evalId, String obtainedMarksStr, int size) {
        db.collection("Evaluations")
                .document(evalId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String evalName = document.getString("EvalName");
                        String totalMarks = document.getString("EvalTMarks");
                        SCEvaluationModel model = new SCEvaluationModel(evalId,evalName,totalMarks,obtainedMarksStr);
                        evaluationList.add(model);


                        // Check if all attendance documents are fetched
                        if (evaluationList.size() == size) {
                            ProgressDialogHelper.dismissProgressDialog();
                            populateEvaluationsUI();
                        }
                    } else {
                        Log.w(TAG, "Error getting document.", task.getException());
                    }
                });
    }
    private void showNoEvaluationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Evaluation Records Found");
        builder.setMessage("There are no Evaluation records available for this course.");

        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Set the dialog to not cancelable
        dialog.show();
    }

    private void populateEvaluationsUI() {
        // Initialize RecyclerView adapter
        SCEvaluationAdapter adapter = new SCEvaluationAdapter(this, evaluationList);
        recyclerView.setAdapter(adapter);
    }
}
