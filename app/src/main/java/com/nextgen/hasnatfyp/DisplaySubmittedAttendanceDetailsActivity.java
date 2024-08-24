package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import View_Class_Students_Activity.StudentModel;

public class DisplaySubmittedAttendanceDetailsActivity extends AppCompatActivity {

    private TableLayout tableLayoutAttendance;
    private TextView classNameTextView;
    private TextView courseNameTextView;
    private TextView attendanceDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_submitted_attendance_details);

        initializeViews();
        displayAttendanceDetails();

    }

    private void initializeViews() {
        tableLayoutAttendance = findViewById(R.id.tableLayoutAttendance);
        classNameTextView = findViewById(R.id.classNameTextView);
        courseNameTextView = findViewById(R.id.courseNameTextView);
        attendanceDateTextView = findViewById(R.id.EvalTypetxtView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Attendance Details", true);
    }
    private void displayAttendanceDetails() {
        Intent intent = getIntent();
        List<StudentModel> studentsList = getStudentsListFromIntent(intent);
        String selectedDate = getSelectedDateFromIntent(intent);
        String formattedDate = formatDate(selectedDate);

        classNameTextView.setText(TeacherInstanceModel.getInstance(this).getClassName());
        courseNameTextView.setText(TeacherInstanceModel.getInstance(this).getCourseName());
        attendanceDateTextView.setText(formattedDate);
        populateTableLayout(studentsList);
    }
    public String splitName(String fullNameWithSerial) {
        String[] parts = fullNameWithSerial.split("\\.", 2);

        if (parts.length == 2) {
            return parts[1].trim();
        } else {
            return fullNameWithSerial;
        }
    }
    private List<StudentModel> getStudentsListFromIntent(Intent intent) {
        return (List<StudentModel>) intent.getSerializableExtra("studentsList");
    }
    private String getSelectedDateFromIntent(Intent intent) {
        return intent.getStringExtra("selectedDate");
    }
    private Boolean getRepeaterStatus(Intent intent) {
        return intent.getBooleanExtra("R",false);
    }
    private void populateTableLayout(List<StudentModel> studentsList) {
        int srNo = 1; // Start the serial number from 1
        for (StudentModel student : studentsList) {
            TableRow row = createTableRow(student, srNo);
            tableLayoutAttendance.addView(row);
            srNo++;
        }
    }

    private TableRow createTableRow(StudentModel student, int srNo) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        TextView srNoTextView = createTextView(String.valueOf(srNo));
        TextView studentNameTextView = createTextView(getShortenedName(student.getStudentName()));
        TextView rollNoTextView = createTextView(student.getRollNo());
        TextView attendanceStatusTextView = createTextView(student.getAttendanceStatus());
        attendanceStatusTextView.setGravity(Gravity.CENTER);
        row.addView(srNoTextView);
        row.addView(studentNameTextView);
        row.addView(rollNoTextView);
        row.addView(attendanceStatusTextView);

        return row;
    }
    private String getShortenedName(String fullName) {
        String name = splitName(fullName);
        String shortname = name.replaceAll("\\b(?i)(Muhammad|Mohammad|Mohd|Mohammed)\\b", "M.");
        return shortname;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        return textView;
    }
    private String formatDate(String selectedDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            return outputFormat.format(sdf.parse(selectedDate));
        } catch (Exception e) {
            e.printStackTrace();
            return selectedDate; // Return original date if parsing fails
        }
    }
}
