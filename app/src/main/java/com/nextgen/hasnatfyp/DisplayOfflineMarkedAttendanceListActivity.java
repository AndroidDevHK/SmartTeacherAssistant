package com.nextgen.hasnatfyp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import Mark_Course_Students_Attendance_Activity.OfflineEvaluationModel;
import View_Class_Students_Activity.StudentModel;

public class DisplayOfflineMarkedAttendanceListActivity extends AppCompatActivity {

    private List<OfflineEvaluationModel> offlineAttendanceList;
    private RecyclerView recyclerView;
    private OfflineAttendanceAdapter adapter;
    private ActivityManager activityManager;
    private MaterialButton submitAllAttendanceButton;
    private FirebaseFirestore db;
    private List<String> alreadyExistingAttendanceRecords = new ArrayList<>();
    private ProgressDialog progressDialog;
    private SearchView searchView;
    private TextView noResultText;

    private CardView noAttendanceCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_offline_marked_attendance_list);

        initializeViews();
        loadOfflineAttendances();
        setupSubmitAttendanceAllButton();
        setupSearchFunctionality();
    }

    private void setupSubmitAttendanceAllButton() {
        submitAllAttendanceButton.setOnClickListener(v -> {
            if(!TeacherInstanceModel.getInstance(this).isOfflineMode()) {
                if (isInternetConnected()) {
                    submitAllAttendancesToFirestore();
                } else {
                    showError("No internet connection available");
                }
            }
            else
            {
                showError(this);
            }
        });
    }
    public void showError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error")
                .setMessage("You can't submit attendance in offline mode. Please connect to the internet, restart the app, and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_atttendace);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityManager = ActivityManager.getInstance();
        activityManager.addActivityForKill(this);
        submitAllAttendanceButton = findViewById(R.id.button_generate_report);

        db = FirebaseFirestore.getInstance();
        searchView = findViewById(R.id.simpleSearchView);
        noResultText = findViewById(R.id.noResultText);
        offlineAttendanceList = new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.toolbar);
        noAttendanceCard = findViewById(R.id.noAttendanceCard);
        SetupToolbar(toolbar);

    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Offline Marked Attendances", true);
    }
    private void setupSearchFunctionality() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAttendanceList(newText);
                return true;
            }
        });
    }


    private void filterAttendanceList(String query) {
        List<OfflineEvaluationModel> filteredList = new ArrayList<>();
        for (OfflineEvaluationModel attendance : offlineAttendanceList) {
            if (attendance.getCourseName().toLowerCase().contains(query.toLowerCase())
                    || isDateMatch(attendance.getAttendanceDate(), query)) {
                filteredList.add(attendance);
            }
        }
        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE);
        } else {
            noResultText.setVisibility(View.GONE);
        }
    }

    private boolean isDateMatch(String actualDate, String queryDate) {
        String formattedDate = formatDate(actualDate);
        return formattedDate.toLowerCase().contains(queryDate.toLowerCase());
    }

    private String formatDate(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void loadOfflineAttendances() {
        offlineAttendanceList = new ArrayList<>();
        List<String> attendanceFiles = getAttendanceFileNames();

        for (String fileName : attendanceFiles) {
            OfflineEvaluationModel attendance = readAttendanceFromFile(fileName);
            if (attendance != null) {
                offlineAttendanceList.add(attendance);
            }
        }

        Collections.sort(offlineAttendanceList, (attendance1, attendance2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = format.parse(attendance1.getAttendanceDate());
                Date date2 = format.parse(attendance2.getAttendanceDate());
                return date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });


        if (offlineAttendanceList.isEmpty()) {
            noAttendanceCard.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchView.setVisibility(View.GONE);
            submitAllAttendanceButton.setVisibility(View.GONE);
        } else {
            noAttendanceCard.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new OfflineAttendanceAdapter(offlineAttendanceList, this);
            recyclerView.setAdapter(adapter);
        }
    }


    private List<String> getAttendanceFileNames() {
        String TeacherUserName;
        if(UserInstituteModel.getInstance(this).isSoloUser())
        {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        }
        else
        {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = TeacherUserName+ "_AttendanceData";
        File directory = new File(getFilesDir(), folderName);
        List<String> fileNames = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }

    private OfflineEvaluationModel readAttendanceFromFile(String fileName) {
        String TeacherUserName;
        if(UserInstituteModel.getInstance(this).isSoloUser())
        {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        }
        else
        {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = TeacherUserName + "_AttendanceData";
        File directory = new File(getFilesDir(), folderName);
        File file = new File(directory, fileName);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Type type = new TypeToken<OfflineEvaluationModel>() {}.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void submitAllAttendancesToFirestore() {
        showProgressDialog(this);
        alreadyExistingAttendanceRecords.clear();
        submitAttendanceSequentially(0);
    }
    private void submitAttendanceSequentially(int index) {
        if (index < offlineAttendanceList.size()) {
            OfflineEvaluationModel attendance = offlineAttendanceList.get(index);
            String attendanceId = UUID.randomUUID().toString();  // Generate a unique ID for each attendance record
            submitAttendanceRecord(attendance, attendanceId, () -> {
                submitAttendanceSequentially(index + 1);
            });
        } else {
            showToast("All attendances submitted successfully");
            dismissProgressDialog();
            if (!alreadyExistingAttendanceRecords.isEmpty()) {
                showErrorMessageForExistingAttendance();
            }
            else
            {
                finish();
            }
        }
    }
    private void submitAttendanceRecord(OfflineEvaluationModel attendance, String attendanceId, Runnable onComplete) {
        String courseId = attendance.getCourseId();
        String attendanceDate = attendance.getAttendanceDate();
        String courseName = attendance.getCourseName();
        boolean isRepeater = attendance.isAreRepeaters();

        // Check if attendance already exists for this course and date
        db.collection("Attendance")
                .whereEqualTo("CourseID", courseId)
                .whereEqualTo("AttendanceDate", attendanceDate)
                .whereEqualTo("IsRepeater", isRepeater)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No attendance record exists, proceed with submission
                        Map<String, Object> attendanceData = new HashMap<>();
                        attendanceData.put("AttendanceDate", attendanceDate);
                        attendanceData.put("CourseID", courseId);
                        attendanceData.put("IsRepeater", isRepeater);

                        db.collection("Attendance")
                                .document(attendanceId)
                                .set(attendanceData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    submitCourseAttendanceRecord(attendance, attendanceId, onComplete);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error adding attendance record", e);
                                });
                    } else {
                        String alreadyExistingRecord = courseName + (isRepeater ? " (Repeaters)" : "") + ", " + attendanceDate;
                        alreadyExistingAttendanceRecords.add(alreadyExistingRecord);
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error querying attendance record", e);
                });
    }
    private void submitCourseAttendanceRecord(OfflineEvaluationModel attendance, String attendanceId, Runnable onComplete) {
        String courseId = attendance.getCourseId();
        Map<String, Object> courseAttendanceData = new HashMap<>();
        courseAttendanceData.put("CourseID", courseId);
        courseAttendanceData.put("AttendanceID", attendanceId);
        courseAttendanceData.put("IsRepeater", attendance.isAreRepeaters());

        db.collection("CourseAttendance")
                .document(courseId + "_" + attendanceId)
                .set(courseAttendanceData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    submitStudentAttendanceRecords(attendance, attendanceId, onComplete);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding course attendance record", e);
                });
    }
    private void submitStudentAttendanceRecords(OfflineEvaluationModel attendance, String attendanceId, Runnable onComplete) {
        List<StudentModel> studentsList = attendance.getStudentsList();
        String CourseID = attendance.getCourseId();
        Boolean AreRepeaters = attendance.isAreRepeaters();
        updateStudentCourseAttendanceList(attendanceId, studentsList, onComplete,CourseID,AreRepeaters,attendance);
    }
    private void updateStudentCourseAttendanceList(String attendanceId, List<StudentModel> studentsList, Runnable onComplete, String courseID, Boolean areRepeaters, OfflineEvaluationModel attendance) {
        if (studentsList.isEmpty()) {
            onComplete.run();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        AtomicInteger successfulSubmissions = new AtomicInteger(0);

        for (StudentModel student : studentsList) {
            String rollNo = student.getRollNo();
            String documentId = rollNo + "_" + courseID;
            String documentPath = "StudentCourseAttendanceList/" + documentId;

            Map<String, Object> data = new HashMap<>();
            data.put("StudentRollNo", rollNo);
            data.put("CourseID", courseID);
            data.put("IsRepeater", areRepeaters);

            DocumentReference studentDocRef = db.document(documentPath);
            batch.set(studentDocRef, data, SetOptions.merge());

            // Update the "AttendanceIDs" field in a single operation
            batch.update(studentDocRef, "AttendanceIDs", FieldValue.arrayUnion(attendanceId));

            successfulSubmissions.incrementAndGet();
        }

        // Commit the batch operation
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // After updating all student course attendance records, start adding attendance for students
                    addCourseStudentsAttendance(attendanceId, studentsList, courseID, areRepeaters, onComplete, attendance);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating student course attendance list", e);
                });
    }
    private void addCourseStudentsAttendance(String attendanceId, List<StudentModel> studentsList, String courseID, boolean isRepeater, Runnable onComplete, OfflineEvaluationModel attendance) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        for (StudentModel student : studentsList) {
            String rollNo = student.getRollNo();
            String attendanceStatus = student.getAttendanceStatus();
            String classID = student.getClassID();
            String documentId = rollNo + "_" + attendanceId;

            Map<String, Object> courseStudentsAttendanceData = new HashMap<>();
            courseStudentsAttendanceData.put("AttendanceID", attendanceId);
            courseStudentsAttendanceData.put("StudentRollNo", rollNo);
            courseStudentsAttendanceData.put("ClassID", classID);
            courseStudentsAttendanceData.put("CourseID", courseID);
            courseStudentsAttendanceData.put("AttendanceStatus", attendanceStatus);

            DocumentReference docRef = db.collection("CourseStudentsAttendance").document(documentId);
            batch.set(docRef, courseStudentsAttendanceData);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    deleteAttendanceFile(attendance);
                    onComplete.run();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error adding course student attendance", e));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Submitting Attendance...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void createAndAddHeaderRow(LinearLayout parentLayout) {
        // Create the header row LinearLayout
        LinearLayout headerRowLayout = new LinearLayout(this);
        headerRowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerRowLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create TextView for course name header
        TextView courseNameHeader = new TextView(this);
        LinearLayout.LayoutParams courseNameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        courseNameHeader.setLayoutParams(courseNameParams);
        courseNameHeader.setText("Course");
        courseNameHeader.setTextSize(12);
        courseNameHeader.setTextColor(getResources().getColor(android.R.color.white));
        courseNameHeader.setGravity(Gravity.CENTER);
        courseNameHeader.setPadding(8, 8, 8, 8);
        courseNameHeader.setTypeface(null, Typeface.BOLD);
        courseNameHeader.setBackgroundColor(getResources().getColor(androidx.cardview.R.color.cardview_dark_background));

        // Create TextView for attendance date header
        TextView attendanceDateHeader = new TextView(this);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        attendanceDateHeader.setLayoutParams(dateParams);
        attendanceDateHeader.setText("Attendance Date");
        attendanceDateHeader.setTextSize(12);
        attendanceDateHeader.setTextColor(getResources().getColor(android.R.color.white));
        attendanceDateHeader.setGravity(Gravity.CENTER);
        attendanceDateHeader.setPadding(8, 8, 8, 8);
        attendanceDateHeader.setTypeface(null, Typeface.BOLD);
        attendanceDateHeader.setBackgroundColor(getResources().getColor(androidx.cardview.R.color.cardview_dark_background));

        // Add TextViews to the header row
        headerRowLayout.addView(courseNameHeader);
        headerRowLayout.addView(attendanceDateHeader);

        // Add the header row to the parent layout
        parentLayout.addView(headerRowLayout);
    }
    private void showErrorMessageForExistingAttendance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error: Attendance Already Exists");

        LinearLayout recordsLayout = new LinearLayout(this);
        recordsLayout.setOrientation(LinearLayout.VERTICAL);

        createAndAddHeaderRow(recordsLayout);

        for (String record : alreadyExistingAttendanceRecords) {
            LinearLayout recordLayout = new LinearLayout(this);
            recordLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            recordLayout.setOrientation(LinearLayout.HORIZONTAL);
            recordLayout.setBackgroundColor(getResources().getColor(R.color.record_background_color)); // Set background color for record

            String[] recordParts = record.split(",");
            String courseName = recordParts[0].trim();
            String attendanceDate = recordParts[1].trim();

            TextView courseNameTextView = new TextView(this);
            courseNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            courseNameTextView.setText(courseName);
            courseNameTextView.setPadding(10, 8, 10, 8);
            courseNameTextView.setTextColor(getResources().getColor(android.R.color.black));
            courseNameTextView.setGravity(Gravity.START);

            TextView attendanceDateTextView = new TextView(this);
            attendanceDateTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            attendanceDateTextView.setText(attendanceDate);
            attendanceDateTextView.setPadding(10, 8, 10, 8);
            attendanceDateTextView.setTextColor(getResources().getColor(android.R.color.black));
            attendanceDateTextView.setGravity(Gravity.CENTER);

            recordLayout.addView(courseNameTextView);
            recordLayout.addView(attendanceDateTextView);

            recordsLayout.addView(recordLayout);
        }

        builder.setView(recordsLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setOnDismissListener(dialog -> {
            recordsLayout.removeAllViews();
            finish();
        });

        builder.create().show();
    }
    private void deleteAttendanceFile(OfflineEvaluationModel attendance) {
        String fileName = attendance.getAttendanceDate() + "_" + attendance.getCourseId() + "_" + attendance.getCourseName() + "_" + (attendance.isAreRepeaters() ? "Repeaters" : "Regular" +".json");
        String TeacherUserName;
        if(UserInstituteModel.getInstance(this).isSoloUser())
        {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        }
        else
        {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = TeacherUserName + "_AttendanceData";
        File directory = new File(getFilesDir(), folderName);
        File file = new File(directory, fileName);
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "Attendance file deleted successfully: " + fileName);
            } else {
                Log.e(TAG, "Failed to delete attendance file: " + fileName);
            }
        } else {
            Log.d(TAG, "Attendance file does not exist: " + fileName);
        }
    }

}

