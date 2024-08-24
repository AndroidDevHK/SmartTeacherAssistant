package Display_Complete_Course_Att_Eval_data_Activity;

import androidx.annotation.NonNull;
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

import Display_Course_Attendance_Activity.DisplayClassCourseAttendanceActivity;
import Report_Making_Files.CourseStudentsCompleteExcelReportGenerator;

import com.nextgen.hasnatfyp.ClassDataStorageHelper;
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

import Display_Course_Attendance_Activity.StudentAttendanceModel;

public class DisplayCompleteCourseStudentsDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";
    private RecyclerView recyclerView;
    private StudentsCourseCompleteDetailsAdapter adapter;
    private String courseID;
    private List<CourseStudentDetailsModel> studentEvalList;
    private boolean areRepeaters;
    private AtomicInteger studentsProcessed = new AtomicInteger(0);
    private int totalStudents;
    private TextView noResultText;
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_complete_course_students_details);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noResultText = findViewById(R.id.noResultText);
        searchView = findViewById(R.id.simpleSearchView);
        studentEvalList = new ArrayList<>();
        setClassAndCourseName();
        if (getIntent().hasExtra("CourseID")) {
            areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
            courseID = getIntent().getStringExtra("CourseID");

            if (NetworkUtils.isInternetConnected(this)) {
                ProgressDialogHelper.showProgressDialog(DisplayCompleteCourseStudentsDetailsActivity.this, "Loading data...");
                retrieveAttendanceData();
            } else {
                List<CourseStudentDetailsModel> completeCourseDetailsList = DataStorageHelperCompleteCourseDetails.readCompleteCourseDetailsLocally(
                        this,
                        courseID,
                        areRepeaters
                );
                if (completeCourseDetailsList.isEmpty()) {
                    Toast.makeText(this, "No Course Report Data to show. Please connect to the internet to load it.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                } else {
                    Toast.makeText(this, "No Internet Connection. Showing previously fetched data.", Toast.LENGTH_LONG).show();
                    studentEvalList.addAll(completeCourseDetailsList);
                    adapter = new StudentsCourseCompleteDetailsAdapter(studentEvalList, this);
                    recyclerView.setAdapter(adapter);
                }
            }
        } else {
            Log.e(TAG, "No Course ID found in intent");
            finish();
        }
        setupToolbar();
        setupFirstLogoClickListener();
        setupSearchFunctionality();
        Button generateReportButton = findViewById(R.id.button_generate_report);
        generateReportButton.setOnClickListener(v -> {
            CourseStudentsCompleteExcelReportGenerator.generateEvaluationReport(studentEvalList, this, areRepeaters);
        });
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

    private void setupFirstLogoClickListener() {
        ImageView logo1 = findViewById(R.id.logo1);
        logo1.setOnClickListener(v -> {
            // Generate Excel report
            Uri excelUri = CourseStudentsCompleteExcelReportGenerator.generateEvaluationReport(studentEvalList, this,areRepeaters);

            if (excelUri != null) {
                // Show Excel options
                showExcelOptions(excelUri);
            } else {
                Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Failed to generate Excel report", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    private void showExcelOptions(Uri excelUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayCompleteCourseStudentsDetailsActivity.this);
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

    private void filterStudentList(String query) {
        List<CourseStudentDetailsModel> filteredList = new ArrayList<>();
        for (CourseStudentDetailsModel record : studentEvalList) {
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

    private void retrieveAttendanceData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CoursesStudents")
                .whereEqualTo("CourseID", courseID)
                .whereEqualTo("isRepeater", areRepeaters)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalStudents = queryDocumentSnapshots.size();
                    if (totalStudents==0) {
                        Toast.makeText(this, "No students available to show for the complete report.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    for (DocumentSnapshot studentDoc : queryDocumentSnapshots.getDocuments()) {
                        String rollNo = studentDoc.getString("StudentRollNo");
                        String ClassIDD = studentDoc.getString("ClassID");

                        retrieveStudentAttendance(db, rollNo, (attendanceList, totalCount, presents, absents, leaves, presentPercentage) -> {
                            retrieveStudentEvaluations(db, rollNo, attendanceList, totalCount, presents, absents, leaves, presentPercentage,ClassIDD);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student attendance records", e);
                    Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving student attendance records", Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveStudentAttendance(@NonNull FirebaseFirestore db, String rollNo, AttendanceDataCallback callback) {
        db.collection("StudentCourseAttendanceList")
                .document(rollNo + "_" + courseID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> attendanceIDs = (List<String>) documentSnapshot.get("AttendanceIDs");
                        if (attendanceIDs != null) {
                            AtomicInteger totalCount = new AtomicInteger(0);
                            AtomicInteger presents = new AtomicInteger(0);
                            AtomicInteger absents = new AtomicInteger(0);
                            AtomicInteger leaves = new AtomicInteger(0);
                            List<StudentAttendanceModel> attendanceList = new ArrayList<>();

                            for (String attendanceID : attendanceIDs) {
                                db.collection("CourseStudentsAttendance")
                                        .document(rollNo + "_" + attendanceID)
                                        .get()
                                        .addOnSuccessListener(attendanceSnapshot -> {
                                            if (attendanceSnapshot.exists()) {
                                                // Attendance record exists
                                                String status = attendanceSnapshot.getString("AttendanceStatus");
                                                if (status != null) {
                                                    switch (status) {
                                                        case "P":
                                                            presents.incrementAndGet();
                                                            break;
                                                        case "A":
                                                            absents.incrementAndGet();
                                                            break;
                                                        case "L":
                                                            leaves.incrementAndGet();
                                                            break;
                                                    }
                                                    totalCount.incrementAndGet();
                                                }

                                                db.collection("Attendance")
                                                        .document(attendanceID)
                                                        .get()
                                                        .addOnSuccessListener(attendanceDoc -> {
                                                            if (attendanceDoc.exists()) {
                                                                String date = attendanceDoc.getString("AttendanceDate");
                                                                StudentAttendanceModel attendanceModel = new StudentAttendanceModel(date, status);
                                                                attendanceList.add(attendanceModel);

                                                                // Check if all attendance records are processed
                                                                if (attendanceList.size() == attendanceIDs.size()) {
                                                                    // Calculate present percentage
                                                                    float presentPercentage = (float) presents.get() / totalCount.get() * 100;
                                                                    // Invoke callback with attendance data
                                                                    callback.onAttendanceDataLoaded(attendanceList, totalCount.get(), presents.get(), absents.get(), leaves.get(), presentPercentage);
                                                                }
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Error retrieving attendance date for attendance ID: " + attendanceID, e);
                                                            Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving attendance date", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error retrieving attendance details for attendance ID: " + attendanceID, e);
                                            Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving attendance details", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    } else {
                        callback.onAttendanceDataLoaded(new ArrayList<>(), 0, 0, 0, 0, 0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student attendance details", e);
                    Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving student attendance details", Toast.LENGTH_SHORT).show();
                });
    }

    public interface AttendanceDataCallback {
        void onAttendanceDataLoaded(List<StudentAttendanceModel> attendanceList, int totalCount, int presents, int absents, int leaves, float presentPercentage);
    }

    private void retrieveStudentEvaluations(FirebaseFirestore db, String rollNo, List<StudentAttendanceModel> attendanceList, int totalCount, int presents, int absents, int leaves, float presentPercentage, String classIDD) {
        db.collection("StudentCourseEvaluationList")
                .document(rollNo + "_" + courseID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> EvaluationIDs = (List<String>) documentSnapshot.get("EvaluationIDs");
                    if (EvaluationIDs != null) {
                        for (String evaluationID : EvaluationIDs) {
                            Log.d(TAG, "Evaluation ID: " + rollNo +evaluationID);
                        }
                        retrieveEvaluationDetails(db, rollNo, EvaluationIDs, attendanceList, totalCount, presents, absents, leaves, presentPercentage,classIDD);

                    } else {
                        // EvaluationIDs list is null, invoke callback directly with empty list
                        retrieveEvaluationDetails(db, rollNo, new ArrayList<>(), attendanceList, totalCount, presents, absents, leaves, presentPercentage,classIDD);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student attendance details", e);
                    Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving student attendance details", Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveEvaluationDetails(FirebaseFirestore db, String rollNo, List<String> EvaluationIDs, List<StudentAttendanceModel> attendanceL, int totalCount, int presents, int absents, int leaves, float presentPercentage, String classIDD) {
        Map<String, StudentEvaluationDetailsModel> evaluationMap = new HashMap<>();

        if (EvaluationIDs == null || EvaluationIDs.isEmpty()) {
            retrieveStudentName(db, rollNo, new ArrayList<>(), classIDD, attendanceL, totalCount, presents, absents, leaves, presentPercentage);
        }
        else {
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
                                                    retrieveStudentName(db, rollNo, attendanceList, ClassID, attendanceL, totalCount, presents, absents, leaves, presentPercentage);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error retrieving attendance data for EvaluationID: " + EvaluationID, e);
                                            Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving attendance data", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error retrieving attendance details for EvaluationID: " + EvaluationID, e);
                            Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving attendance details", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void retrieveStudentName(FirebaseFirestore db, String rollNo, List<StudentEvaluationDetailsModel> evalList, String classID, List<StudentAttendanceModel> attendanceL, int totalCount, int presents, int absents, int leaves, float presentPercentage) {
        if (evalList.isEmpty()) {

            db.collection("Classes")
                    .document(classID)
                    .collection("ClassStudents")
                    .whereEqualTo("RollNo", rollNo)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot studentDoc = querySnapshot.getDocuments().get(0); // Assuming there's only one document with the given roll number
                            String name = studentDoc.getString("StudentName");

                            CourseStudentDetailsModel recordModel = new CourseStudentDetailsModel(name, rollNo, new ArrayList<>(), "0", "0", "0%", attendanceL, totalCount, presents, absents, leaves, presentPercentage);
                            studentEvalList.add(recordModel);

                            if (studentsProcessed.incrementAndGet() == totalStudents) {
                                updateRecyclerView();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error retrieving student name for roll number: " + rollNo, e);
                        Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving student name", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Perform database operations for non-empty evaluation list
            float allEvalTotal = 0.0f;
            float allEvalObtainedMarks = 0.0f;

            for (StudentEvaluationDetailsModel eval : evalList) {
                float totalMarks = Float.parseFloat(eval.getTotalMarks());
                float obtainedMarks = Float.parseFloat(eval.getObtainedMarks());
                allEvalTotal += totalMarks;
                allEvalObtainedMarks += obtainedMarks;
            }
            float percentage = (allEvalObtainedMarks / allEvalTotal) * 100;
            String percentageString = String.format("%.0f%%", percentage); // Format the percentage string



            String finalAllEvalTotal = removeDecimalIfNotNecessary(allEvalTotal);
            String finalAllEvalObtainedMarks = removeDecimalIfNotNecessary(allEvalObtainedMarks);

            db.collection("Classes")
                    .document(classID)
                    .collection("ClassStudents")
                    .whereEqualTo("RollNo", rollNo)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot studentDoc = querySnapshot.getDocuments().get(0); // Assuming there's only one document with the given roll number
                            String name = studentDoc.getString("StudentName");

                            CourseStudentDetailsModel recordModel = new CourseStudentDetailsModel(name, rollNo, evalList, finalAllEvalTotal, finalAllEvalObtainedMarks, percentageString, attendanceL, totalCount, presents, absents, leaves, presentPercentage);
                            studentEvalList.add(recordModel);

                            if (studentsProcessed.incrementAndGet() == totalStudents) {
                                updateRecyclerView();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error retrieving student name for roll number: " + rollNo, e);
                        Toast.makeText(DisplayCompleteCourseStudentsDetailsActivity.this, "Error retrieving student name", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String removeDecimalIfNotNecessary(float value) {
        if (value % 1 == 0) {
            return String.valueOf((int) value);
        } else {
            return String.valueOf(value);
        }
    }

    private void updateRecyclerView() {
        Collections.sort(studentEvalList, Comparator.comparing(CourseStudentDetailsModel::getStudentRollNo));
        if (adapter == null) {
            DataStorageHelperCompleteCourseDetails.storeCompleteCourseDetailsLocally(
                    this,
                    studentEvalList,
                    courseID,
                    areRepeaters
            );
            adapter = new StudentsCourseCompleteDetailsAdapter(studentEvalList, this);
            recyclerView.setAdapter(adapter);
            ProgressDialogHelper.dismissProgressDialog();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

}