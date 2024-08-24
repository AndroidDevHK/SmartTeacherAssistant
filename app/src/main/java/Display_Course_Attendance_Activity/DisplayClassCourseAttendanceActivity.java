package Display_Course_Attendance_Activity;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import Display_Complete_Course_Att_Eval_data_Activity.CourseStudentDetailsModel;
import Display_Complete_Course_Att_Eval_data_Activity.DataStorageHelperCompleteCourseDetails;
import Display_Complete_Course_Att_Eval_data_Activity.DisplayCompleteCourseStudentsDetailsActivity;
import Display_Complete_Course_Att_Eval_data_Activity.StudentsCourseCompleteDetailsAdapter;
import Report_Making_Files.ExcelReportGeneratorCompleteCourseAttendance;
import Report_Making_Files.PDFCourseAttendanceSummaryGenerator;

import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayClassCourseAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";

    private RecyclerView recyclerView;
    private ClassCourseAttendanceAdapter adapter;
    private String courseID;
    private List<StudentAttendanceRecordModel> studentAttendanceRecords;
    private AtomicInteger studentsProcessed = new AtomicInteger(0);
    private int totalStudents;
    private TextView noResultText;
    private SearchView searchView;
    private boolean areRepeaters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_class_course_attendance);

        recyclerView = findViewById(R.id.recycler_view_courses);
        noResultText = findViewById(R.id.noResultText);
        searchView = findViewById(R.id.simpleSearchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAttendanceRecords = new ArrayList<>();

        if (getIntent().hasExtra("CourseID")) {
            areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
            courseID = getIntent().getStringExtra("CourseID");

            if (NetworkUtils.isInternetConnected(this)) {
                ProgressDialogHelper.showProgressDialog(DisplayClassCourseAttendanceActivity.this, "Loading data...");
                retrieveAttendanceData();
            } else {
                handleOfflineData();
            }
        } else {
            Log.e(TAG, "No Course ID found in intent");
            finish();
            return;
        }
        setClassAndCourseName();
        setupToolbar();

        setupSearchFunctionality();

        setupFirstLogoClickListener();

        setupSecondLogoClickListener();
    }
    private void handleOfflineData() {
        List<StudentAttendanceRecordModel> retrievedAttendanceRecords = DataStorageHelperCourseAttendance.readCourseAttendanceLocally(
                this, // Context
                courseID, // Course ID
                areRepeaters // Repeater status
        );

        if (retrievedAttendanceRecords.isEmpty()) {
            Toast.makeText(this, "No Course Attendance Data to show. Please connect to the internet to load it.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "No Internet Connection. Showing previously fetched data.", Toast.LENGTH_LONG).show();
            studentAttendanceRecords.addAll(retrievedAttendanceRecords);
            adapter = new ClassCourseAttendanceAdapter(retrievedAttendanceRecords);
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        getSupportActionBar().setTitle("View Attendance Report"); // Set toolbar title
    }

    private void filterStudentAttendanceList(String query) {
        List<StudentAttendanceRecordModel> filteredList = new ArrayList<>();
        for (StudentAttendanceRecordModel record : studentAttendanceRecords) {
            if (record.getName().toLowerCase().contains(query.toLowerCase())
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
                filterStudentAttendanceList(newText);
                return true;
            }
        });
    }

    private void setupFirstLogoClickListener() {
        ImageView logo1 = findViewById(R.id.logo1);
        logo1.setOnClickListener(v -> {
            // Generate Excel report
            Uri excelUri = ExcelReportGeneratorCompleteCourseAttendance.generateExcelReport(studentAttendanceRecords, this, areRepeaters);

            if (excelUri != null) {
                // Show Excel options
                showExcelOptions(excelUri);
            } else {
                Toast.makeText(DisplayClassCourseAttendanceActivity.this, "Failed to generate Excel report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showExcelOptions(Uri excelUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayClassCourseAttendanceActivity.this);
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

    private void setupSecondLogoClickListener() {
        ImageView logo2 = findViewById(R.id.logo2);
        logo2.setOnClickListener(v -> {
            Uri pdfUri = PDFCourseAttendanceSummaryGenerator.generatePdf(studentAttendanceRecords, this, areRepeaters);
            if (pdfUri != null) {
                showPdfOption(pdfUri);
            } else {
                Toast.makeText(DisplayClassCourseAttendanceActivity.this, "PDF URI is null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPdfOption(Uri pdfUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayClassCourseAttendanceActivity.this);
        builder.setTitle("PDF Options")
                .setMessage("Choose an option:")
                .setPositiveButton("View PDF", (dialog, which) -> {
                    // Handle view PDF option
                    viewPdf(pdfUri);
                })
                .setNegativeButton("Share PDF", (dialog, which) -> {
                    // Handle share PDF option
                    sharePdf(pdfUri);
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    // Handle cancel option
                    dialog.dismiss();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void sharePdf(Uri pdfUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share PDF using"));
    }

    private void viewPdf(Uri pdfUri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(pdfUri, "application/pdf");
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(viewIntent);
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
                        Toast.makeText(this, "No students available to display for attendance.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    for (DocumentSnapshot studentDoc : queryDocumentSnapshots.getDocuments()) {
                        String rollNo = studentDoc.getString("StudentRollNo");
                        String ClassID = studentDoc.getString("ClassID");
                        retrieveStudentAttendance(db, rollNo, ClassID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student attendance records", e);
                    Toast.makeText(DisplayClassCourseAttendanceActivity.this, "Error retrieving student attendance records", Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveStudentAttendance(FirebaseFirestore db, String rollNo, String classID) {
        db.collection("StudentCourseAttendanceList")
                .document(rollNo + "_" + courseID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> attendanceIDs = (List<String>) documentSnapshot.get("AttendanceIDs");
                        if (attendanceIDs != null && !attendanceIDs.isEmpty()) {
                            retrieveAttendanceDetails(db, rollNo, attendanceIDs);
                        } else {
                            List<StudentAttendanceModel> attendanceList = new ArrayList<>();
                            retrieveStudentName(db, rollNo, attendanceList, classID);
                        }
                    } else {
                        List<StudentAttendanceModel> attendanceList = new ArrayList<>();
                        retrieveStudentName(db, rollNo, attendanceList, classID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student attendance details", e);
                    Toast.makeText(DisplayClassCourseAttendanceActivity.this, "Error retrieving student attendance details", Toast.LENGTH_SHORT).show();
                });
    }


    private void retrieveAttendanceDetails(FirebaseFirestore db, String rollNo, List<String> attendanceIDs) {
        List<StudentAttendanceModel> attendanceList = new ArrayList<>();

        for (String attendanceID : attendanceIDs) {
            db.collection("CourseStudentsAttendance")
                    .document(rollNo + "_" + attendanceID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String status = documentSnapshot.getString("AttendanceStatus");
                            String ClassID = documentSnapshot.getString("ClassID");

                            db.collection("Attendance")
                                    .document(attendanceID)
                                    .get()
                                    .addOnSuccessListener(attendanceDoc -> {
                                        if (attendanceDoc.exists()) {
                                            String date = attendanceDoc.getString("AttendanceDate");

                                            StudentAttendanceModel attendanceModel = new StudentAttendanceModel(date, status);
                                            attendanceList.add(attendanceModel);

                                            if (attendanceList.size() == attendanceIDs.size()) {
                                                retrieveStudentName(db, rollNo, attendanceList,ClassID);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error retrieving attendance date for attendance ID: " + attendanceID, e);
                                        Toast.makeText(DisplayClassCourseAttendanceActivity.this, "Error retrieving attendance date", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error retrieving attendance details for attendance ID: " + attendanceID, e);
                        Toast.makeText(DisplayClassCourseAttendanceActivity.this, "Error retrieving attendance details", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void retrieveStudentName(FirebaseFirestore db, String rollNo, List<StudentAttendanceModel> attendanceList, String classID) {
        db.collection("Classes")
                .document(classID)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", rollNo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot studentDoc = querySnapshot.getDocuments().get(0); // Assuming there's only one document with the given roll number
                        String name = studentDoc.getString("StudentName");

                        StudentAttendanceRecordModel recordModel = new StudentAttendanceRecordModel(rollNo, name, attendanceList);
                        studentAttendanceRecords.add(recordModel);

                        if (studentsProcessed.incrementAndGet() == totalStudents) {
                            updateRecyclerView();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student name for roll number: " + rollNo, e);
                    Toast.makeText(DisplayClassCourseAttendanceActivity.this, "Error retrieving student name", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateRecyclerView() {
        Collections.sort(studentAttendanceRecords, Comparator.comparing(StudentAttendanceRecordModel::getStudentRollNo));

        for (StudentAttendanceRecordModel record : studentAttendanceRecords) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Collections.sort(record.getAttendanceList(), (o1, o2) -> {
                try {
                    Date date1 = dateFormat.parse(o1.getDate());
                    Date date2 = dateFormat.parse(o2.getDate());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            });

            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            for (StudentAttendanceModel attendance : record.getAttendanceList()) {
                try {
                    Date date = dateFormat.parse(attendance.getDate());
                    attendance.setDate(outputDateFormat.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (adapter == null) {
            ProgressDialogHelper.dismissProgressDialog();
            DataStorageHelperCourseAttendance.storeCourseAttendanceLocally(
                    this,
                    studentAttendanceRecords,
                    courseID,
                    areRepeaters
            );
                    adapter = new ClassCourseAttendanceAdapter(studentAttendanceRecords);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

}
