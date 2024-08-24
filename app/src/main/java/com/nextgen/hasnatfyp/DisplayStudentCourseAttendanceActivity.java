package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DisplayStudentCourseAttendanceActivity extends AppCompatActivity {
    private static final String TAG = "DisplayStudentAttendance";
    private FirebaseFirestore db;
    private List<SCattendanceModel> attendanceList;
    private String studentRollNo;
    private String courseId;
    private CardView stdcard;

    private RecyclerView recyclerView;

    private TextView classNameTextView;
    private TextView courseNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_course_attendance);

        db = FirebaseFirestore.getInstance();
        attendanceList = new ArrayList<>();
        getExtrasFromIntent();
        stdcard = findViewById(R.id.stdcard);
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.attendanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stdcard.setVisibility(View.GONE);
        classNameTextView = findViewById(R.id.classNameTextView);
        courseNameTextView = findViewById(R.id.courseNameTextView);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        fetchAttendanceList();
        setCourseAndClassName();
    }
    public void setCourseAndClassName() {
        classNameTextView.setText(StudentSessionInfo.getInstance(this).getClassName());
        courseNameTextView.setText(StudentSessionInfo.getInstance(this).getCourseName());
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Course Attendance", true);
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
        db.collection("StudentCourseAttendanceList")
                .whereEqualTo("StudentRollNo", studentRollNo)
                .whereEqualTo("CourseID", courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            ProgressDialogHelper.dismissProgressDialog();
                        showNoAttendanceDialog();
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
                                List<String> attendanceIds = (List<String>) document.get("AttendanceIDs");
                                if (attendanceIds != null) {
                                    int size = attendanceIds.size();
                                    fetchAttendanceDetails(attendanceIds, size);
                                }
                            }
                        }
                    } else {
                        ProgressDialogHelper.dismissProgressDialog();
                        showNoAttendanceDialog();
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }


    private void showNoAttendanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Attendance Records Found");
        builder.setMessage("There are no attendance records available for this course.");

        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Set the dialog to not cancelable
        dialog.show();
    }



    private void fetchAttendanceDetails(List<String> attendanceIds, int size) {
        for (String attendanceId : attendanceIds) {
            db.collection("CourseStudentsAttendance")
                    .whereEqualTo("AttendanceID", attendanceId)
                    .whereEqualTo("StudentRollNo", studentRollNo)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String status = document.getString("AttendanceStatus");
                                String attendanceIdForDate = document.getString("AttendanceID");
                                fetchAttendanceDate(attendanceIdForDate, status, size);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }

    private void fetchAttendanceDate(String attendanceId, String status, int size) {
        db.collection("Attendance")
                .document(attendanceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String date = document.getString("AttendanceDate");
                        SCattendanceModel model = new SCattendanceModel(date, status);
                        attendanceList.add(model);
                        Log.d(TAG, "Attendance added: " + model.toString());

                        // Check if all attendance documents are fetched
                        if (attendanceList.size() == size) {
                            Log.d(TAG, "All attendances fetched. Size: " + attendanceList.size());
                            // Update UI after all attendances are fetched
                            populateAttendanceUI();
                        }
                    } else {
                        Log.w(TAG, "Error getting document.", task.getException());
                    }
                });
    }

    private void populateAttendanceUI() {
        int totalPresences = 0;
        int totalAbsences = 0;
        int totalLeaves = 0;

        // Calculate totals and populate RecyclerView
        for (SCattendanceModel attendance : attendanceList) {
            switch (attendance.getStatus()) {
                case "P":
                    totalPresences++;
                    break;
                case "A":
                    totalAbsences++;
                    break;
                case "L":
                    totalLeaves++;
                    break;
                default:
                    break;
            }
        }


        int totalAttendance = totalPresences + totalAbsences + totalLeaves;
        float attendancePercentage = (totalAttendance > 0) ? (totalPresences * 100.0f) / totalAttendance : 0;

        // Call method to initialize or update pie chart
        PieChart pieChart = findViewById(R.id.barChart);
        initPieChart(pieChart, totalPresences, totalAbsences, totalLeaves, attendancePercentage, totalAttendance);
        stdcard.setVisibility(View.VISIBLE);
        ProgressDialogHelper.dismissProgressDialog();
        SCAttendanceAdapter adapter = new SCAttendanceAdapter(this, attendanceList);
        recyclerView.setAdapter(adapter);
    }

    private void initPieChart(PieChart pieChart, int presentCount, int absentCount, int leaveCount, float attendancePercentage, int totalDays) {
        List<PieEntry> entries = new ArrayList<>();
        if (presentCount > 0) {
            entries.add(new PieEntry(presentCount, "Present"));
        }
        if (absentCount > 0) {
            entries.add(new PieEntry(absentCount, "Absent"));
        }
        if (leaveCount > 0) {
            entries.add(new PieEntry(leaveCount, "Leave"));
        }

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 180, 0));  // Light Orange for "Not Specified"
        colors.add(Color.rgb(255, 69, 0));  // Red for "Absent"
        colors.add(Color.rgb(0, 0, 255));  // Blue for "Leave"

        PieDataSet dataSet = new PieDataSet(entries, "Percentage : " + String.format("%.2f%%", attendancePercentage));        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueLineColor(Color.BLACK);
        dataSet.setValueLineWidth(1f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieChart.setData(data);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);

        Legend legend = pieChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(Color.BLACK);

        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        pieChart.animateY(1400, Easing.EaseInOutQuad);

        pieChart.setCenterText("Total Days: " + totalDays);
        pieChart.setCenterTextSize(10f);
        pieChart.setCenterTextColor(Color.BLACK);

        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setDrawSliceText(false); // Hide text inside slices

        pieChart.invalidate(); // Refresh chart
    }
}
