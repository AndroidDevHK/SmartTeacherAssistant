package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.List;

public class DisplaySubmittedEvaluationDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_submitted_evaluation_details);

        Intent intent = getIntent();
        if (intent != null) {
            List<String> studentNames = intent.getStringArrayListExtra("studentNames");
            List<String> studentRollNos = intent.getStringArrayListExtra("studentRollNos");
            List<String> obtainedMarksList = intent.getStringArrayListExtra("obtainedMarksList");
            String totalMarks = intent.getStringExtra("totalMarks");
            String evalType = intent.getStringExtra("evalType");
            populateData(studentNames, studentRollNos, obtainedMarksList, totalMarks, evalType, totalMarks);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Evaluation Details", true);
    }
    @SuppressLint("SetTextI18n")
    private void populateData(List<String> studentNames, List<String> studentRollNos, List<String> obtainedMarksList, String totalMarks, String evalType, String evalTotalMarks) {
        // Find TextViews
        TextView classNameTextView = findViewById(R.id.classNameTextView);
        TextView courseNameTextView = findViewById(R.id.courseNameTextView);
        TextView evalTypeTextView = findViewById(R.id.EvalTypetxtView);
        TextView evalTotalMarksTextView = findViewById(R.id.EvalTMarksTxtView);
        TableLayout tableLayoutAttendance = findViewById(R.id.tableLayoutAttendance);

        // Set class and course names
        classNameTextView.setText(TeacherInstanceModel.getInstance(this).getClassName());
        courseNameTextView.setText(TeacherInstanceModel.getInstance(this).getCourseName());
        evalTypeTextView.setText(evalType);
        evalTotalMarksTextView.setText(totalMarks);

        // Populate student details in the table
        for (int i = 0; i < studentNames.size(); i++) {
            String studentName = studentNames.get(i);
            String studentRollNo = studentRollNos.get(i);
            Float obtainedMarksFloat = Float.valueOf(obtainedMarksList.get(i));
            String obtainedMarks = obtainedMarksFloat % 1 == 0 ? String.valueOf(obtainedMarksFloat.intValue()) : String.valueOf(obtainedMarksFloat);

            // Create a new row
            TableRow row = new TableRow(this);

            // Add TextViews for student name, roll number, and obtained marks
            TextView srNoTextView = new TextView(this);
            srNoTextView.setText((i + 1) + ". " + getShortenedName(studentName)); // Concatenate sr# for each student
            srNoTextView.setTextSize(14);
            srNoTextView.setPadding(8, 8, 8, 8);
            row.addView(srNoTextView);

            TextView rollNoTextView = new TextView(this);
            rollNoTextView.setText(studentRollNo);
            rollNoTextView.setTextSize(14);
            rollNoTextView.setPadding(8, 8, 8, 8);
            row.addView(rollNoTextView);

            TextView marksTextView = new TextView(this);
            marksTextView.setText(obtainedMarks);
            marksTextView.setTextSize(14);
            marksTextView.setGravity(Gravity.CENTER);
            marksTextView.setPadding(8, 8, 8, 8);
            row.addView(marksTextView);

            // Add the row to the table layout
            tableLayoutAttendance.addView(row);
        }
    }
    private String getShortenedName(String fullName) {
        String shortname = fullName.replaceAll("\\b(?i)(Muhammad|Mohammad|Mohd|Mohammed)\\b", "M.");
        return shortname;
    }


}
