package Display_Course_Students_Evaluation_Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Course_Attendance_Activity.ClassCourseAttendanceAdapter;
import Display_Course_Attendance_Activity.DataStorageHelperCourseAttendance;
import Display_Course_Attendance_Activity.StudentAttendanceRecordModel;
import Report_Making_Files.ExcelReportGeneratorCourseEvaluations;

public class DisplayCourseStudentsEvaluationActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";
    private RecyclerView recyclerView;
    private StudentsCourseEvaluationAdapter adapter;
    private String courseID;
    private List<CourseStudentEvaluationListModel> studentEvalList;
    private boolean areRepeaters;
    private AtomicInteger studentsProcessed = new AtomicInteger(0);
    private int totalStudents;
    private TextView noResultText;
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_course_students_evaluation);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentEvalList = new ArrayList<>();
        noResultText = findViewById(R.id.noResultText);
        searchView = findViewById(R.id.simpleSearchView);
        if (getIntent().hasExtra("CourseID")) {
            areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
            courseID = getIntent().getStringExtra("CourseID");
            if (NetworkUtils.isInternetConnected(this)) {
                ProgressDialogHelper.showProgressDialog(DisplayCourseStudentsEvaluationActivity.this, "Loading data...");
                retrieveAttendanceData();
            } else {
                handleOfflineData();
            }
        } else {
            Log.e(TAG, "No Course ID found in intent");
            finish();
        }
        setupSearchFunctionality();

        setupToolbar();
        setupFirstLogoClickListener();
        Button generateReportButton = findViewById(R.id.button_generate_report);
        generateReportButton.setOnClickListener(v -> {
            ExcelReportGeneratorCourseEvaluations.generateEvaluationReport(studentEvalList, this, areRepeaters);
        });

        setClassAndCourseName();
    }

    private void handleOfflineData() {
        List<CourseStudentEvaluationListModel> retrievedStudentEvalList = DataStorageHelperCourseStudentEvaluation.readCourseStudentEvaluationLocally(
                this,
                courseID,
                areRepeaters
        );

        if (retrievedStudentEvalList.isEmpty()) {
            Toast.makeText(this, "No Course Evaluation Data to show. Please connect to the internet to load it.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "No Internet Connection. Showing previously fetched data.", Toast.LENGTH_LONG).show();
            studentEvalList.addAll(retrievedStudentEvalList);
            adapter = new StudentsCourseEvaluationAdapter(retrievedStudentEvalList,this);
            recyclerView.setAdapter(adapter);
        }
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

    private void filterStudentList(String query) {
        List<CourseStudentEvaluationListModel> filteredList = new ArrayList<>();
        for (CourseStudentEvaluationListModel record : studentEvalList) {
            if (record.getStudentName().toLowerCase().contains(query.toLowerCase())
                    || record.getStudentRollNo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(record);
            }
        }
        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE);
        } else {
            noResultText.setVisibility(View.GONE);
        }
    }


    private void setupSearchFunctionality() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStudentList(newText);
                return true;
            }
        });
    }

    private void setupFirstLogoClickListener() {
        ImageView logo1 = findViewById(R.id.logo1);
        logo1.setOnClickListener(v -> {
            Uri excelUri = ExcelReportGeneratorCourseEvaluations.generateEvaluationReport(studentEvalList, this,areRepeaters);

            if (excelUri != null) {
                // Show Excel options
                showExcelOptions(excelUri);
            } else {
                Toast.makeText(DisplayCourseStudentsEvaluationActivity.this, "Failed to generate Excel report", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("View Evaluation Report");

    }
    private void showExcelOptions(Uri excelUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayCourseStudentsEvaluationActivity.this);
        builder.setTitle("Excel Options")
                .setMessage("Choose an option:")
                .setPositiveButton("View Excel", (dialog, which) -> {
                    viewExcel(excelUri);
                })
                .setNegativeButton("Share Excel", (dialog, which) -> {
                    shareExcel(excelUri);
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void viewExcel(Uri excelUri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(excelUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(viewIntent);
    }

    private void shareExcel(Uri excelUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        shareIntent.putExtra(Intent.EXTRA_STREAM, excelUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Excel using"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back button press
        onBackPressed();
        return true;
    }


    private void retrieveAttendanceData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CoursesStudents")
                .whereEqualTo("CourseID", courseID)
                .whereEqualTo("isRepeater", areRepeaters)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalStudents = queryDocumentSnapshots.size();
                    if (totalStudents==0) {
                        Toast.makeText(this, "No students available to show for evaluation.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    for (DocumentSnapshot studentDoc : queryDocumentSnapshots.getDocuments()) {
                        String rollNo = studentDoc.getString("StudentRollNo");
                        String ClassID = studentDoc.getString("ClassID");

                        retrieveStudentEvaluations(db, rollNo,ClassID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student attendance records", e);
                    Toast.makeText(DisplayCourseStudentsEvaluationActivity.this, "Error retrieving student attendance records", Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveStudentEvaluations(FirebaseFirestore db, String rollNo, String classID) {
        db.collection("StudentCourseEvaluationList")
                .whereEqualTo("CourseID", courseID)
                .whereEqualTo("StudentRollNo", rollNo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> EvaluationIDs = (List<String>) documentSnapshot.get("EvaluationIDs");
                        if (EvaluationIDs != null && !EvaluationIDs.isEmpty()) {
                            retrieveAttendanceDetails(db, rollNo, EvaluationIDs);
                        } else {
                            List<StudentEvaluationDetailsModel> emptyEvalList = new ArrayList<>();
                            retrieveStudentName(db, rollNo, emptyEvalList, classID);
                        }
                    } else {
                        List<StudentEvaluationDetailsModel> emptyEvalList = new ArrayList<>();
                        retrieveStudentName(db, rollNo, emptyEvalList, classID);
                    }
                })
                .addOnFailureListener(e -> {
                    List<StudentEvaluationDetailsModel> emptyEvalList = new ArrayList<>();
                    retrieveStudentName(db, rollNo, emptyEvalList, classID);
                });
    }


    private void retrieveAttendanceDetails(FirebaseFirestore db, String rollNo, List<String> EvaluationIDs) {
        Map<String, StudentEvaluationDetailsModel> evaluationMap = new HashMap<>();

        for (String EvaluationID : EvaluationIDs) {
            db.collection("CourseStudentsEvaluation")
                    .document(rollNo + "_" + EvaluationID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double obtainedMarks = documentSnapshot.getDouble("StudentObtMarks");
                            String ClassID = documentSnapshot.getString("ClassID");

                            String obtainedMarksString;
                            if (obtainedMarks != null) {
                                obtainedMarksString = obtainedMarks.toString();
                                if (obtainedMarksString.endsWith(".0")) {
                                    obtainedMarksString = obtainedMarksString.substring(0, obtainedMarksString.length() - 2);
                                }
                            } else {
                                obtainedMarksString = "0";
                            }

                            String finalObtainedMarksString = obtainedMarksString;
                            db.collection("Evaluations")
                                    .document(EvaluationID)
                                    .get()
                                    .addOnSuccessListener(attendanceDoc -> {
                                        if (attendanceDoc.exists()) {
                                            String EvalName = attendanceDoc.getString("EvalName");
                                            String EvalTMarks = attendanceDoc.getString("EvalTMarks");

                                            StudentEvaluationDetailsModel attendanceModel = new StudentEvaluationDetailsModel(EvalName, finalObtainedMarksString, EvalTMarks);
                                            evaluationMap.put(EvaluationID, attendanceModel);

                                            if (evaluationMap.size() == EvaluationIDs.size()) {
                                                List<StudentEvaluationDetailsModel> attendanceList = new ArrayList<>();
                                                for (String id : EvaluationIDs) {
                                                    attendanceList.add(evaluationMap.get(id));
                                                }
                                                retrieveStudentName(db, rollNo, attendanceList, ClassID);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error retrieving attendance data for EvaluationID: " + EvaluationID, e);
                                        Toast.makeText(DisplayCourseStudentsEvaluationActivity.this, "Error retrieving attendance data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error retrieving attendance details for EvaluationID: " + EvaluationID, e);
                        Toast.makeText(DisplayCourseStudentsEvaluationActivity.this, "Error retrieving attendance details", Toast.LENGTH_SHORT).show();
                    });
        }
    }



    private void retrieveStudentName(FirebaseFirestore db, String rollNo, List<StudentEvaluationDetailsModel> evalList, String classID) {
        if (evalList.isEmpty()) {
            // If the evaluation list is empty, set default values
            processStudentData(db, rollNo, evalList, classID, "0", "0", "");
        } else {
            float allEvalTotal = 0.0f;
            float allEvalObtainedMarks = 0.0f;

            // Calculate total and obtained marks from evaluation list
            for (StudentEvaluationDetailsModel eval : evalList) {
                float totalMarks = Float.parseFloat(eval.getTotalMarks());
                float obtainedMarks = Float.parseFloat(eval.getObtainedMarks());
                allEvalTotal += totalMarks;
                allEvalObtainedMarks += obtainedMarks;
            }

            // Convert total and obtained marks to string
            String finalAllEvalTotal = removeDecimalIfNotNecessary(allEvalTotal);
            String finalAllEvalObtainedMarks = removeDecimalIfNotNecessary(allEvalObtainedMarks);

            processStudentData(db, rollNo, evalList, classID, finalAllEvalTotal, finalAllEvalObtainedMarks, calculatePercentage(finalAllEvalTotal, finalAllEvalObtainedMarks));
        }
    }

    private void processStudentData(FirebaseFirestore db, String rollNo, List<StudentEvaluationDetailsModel> evalList, String classID, String finalAllEvalTotal, String finalAllEvalObtainedMarks, String percentageString) {
        db.collection("Classes")
                .document(classID)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", rollNo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot studentDoc = querySnapshot.getDocuments().get(0); // Assuming there's only one document with the given roll number
                        String name = studentDoc.getString("StudentName");

                        // Create the model with the data
                        CourseStudentEvaluationListModel recordModel = new CourseStudentEvaluationListModel(name, rollNo, evalList, finalAllEvalTotal, finalAllEvalObtainedMarks, percentageString);
                        studentEvalList.add(recordModel);

                        if (studentsProcessed.incrementAndGet() == totalStudents) {
                            updateRecyclerView();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student name for roll number: " + rollNo, e);
                    Toast.makeText(DisplayCourseStudentsEvaluationActivity.this, "Error retrieving student name", Toast.LENGTH_SHORT).show();
                });
    }

    private String calculatePercentage(String totalMarks, String obtainedMarks) {
        float totalMarksFloat = Float.parseFloat(totalMarks);
        float obtainedMarksFloat = Float.parseFloat(obtainedMarks);

        // Calculate percentage
        if (totalMarksFloat != 0) {
            float percentage = (obtainedMarksFloat / totalMarksFloat) * 100.0f;
            return String.format("%.0f%%", percentage);
        }
        return "";
    }


    private void updateRecyclerView() {
        Collections.sort(studentEvalList, Comparator.comparing(CourseStudentEvaluationListModel::getStudentRollNo));
        if (adapter == null) {
            DataStorageHelperCourseStudentEvaluation.storeCourseStudentEvaluationLocally(
                    this,
                    studentEvalList,
                    courseID,
                    areRepeaters
            );
            adapter = new StudentsCourseEvaluationAdapter(studentEvalList, this);
            recyclerView.setAdapter(adapter);
            ProgressDialogHelper.dismissProgressDialog();
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    private String removeDecimalIfNotNecessary(float value) {
        if (value % 1 == 0) {
            return String.valueOf((int) value);
        } else {
            return String.valueOf(value);
        }
    }

    private String formatPercentage(float percentage) {
        if (percentage % 1 == 0) {
            return String.valueOf((int) percentage);
        } else {
            return String.format("%.2f", percentage);
        }
    }


}
