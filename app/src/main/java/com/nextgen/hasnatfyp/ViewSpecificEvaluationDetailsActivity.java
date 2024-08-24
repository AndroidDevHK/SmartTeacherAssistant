package com.nextgen.hasnatfyp;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.google.android.material.button.MaterialButton;

import java.util.List;

import Display_Course_Attendance_Activity.DisplayClassCourseAttendanceActivity;
import Display_Course_Evaluations_List_Activity.CourseEvaluationInfoModel;
import Report_Making_Files.PDFCourseAttendanceSummaryGenerator;

public class ViewSpecificEvaluationDetailsActivity extends AppCompatActivity {
    Boolean areRepeaters;
    CourseEvaluationInfoModel evaluationInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_specific_evaluation_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);

      evaluationInfo = getIntent().getParcelableExtra("evaluationInfo");
        areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);

        if (evaluationInfo != null) {
            populateEvaluationDetails(evaluationInfo);
            populateStudentEvaluationDetails(evaluationInfo.getEvaluationInfoList());
        }
        setupSecondLogoClickListener();
        setupToolbar();

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("View " + evaluationInfo.getEvaluationName() + " Report");
    }
    private void setupSecondLogoClickListener() {
        ImageView logo2 = findViewById(R.id.logo2);
        logo2.setOnClickListener(v -> {
            Uri pdfUri = SpecificEvaluationReportGenerator.generatePdf(ViewSpecificEvaluationDetailsActivity.this, areRepeaters, evaluationInfo);
            if (pdfUri != null) {
                // Show PDF options
                showPdfOptions(pdfUri);
            } else {
                Toast.makeText(ViewSpecificEvaluationDetailsActivity.this, "Failed to generate PDF report", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showPdfOptions(Uri pdfUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewSpecificEvaluationDetailsActivity.this);
        builder.setTitle("PDF Options")
                .setMessage("Choose an option:")
                .setPositiveButton("View PDF", (dialog, which) -> {
                    viewPdf(pdfUri);
                })
                .setNegativeButton("Share PDF", (dialog, which) -> {
                    sharePdf(pdfUri);
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void viewPdf(Uri pdfUri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(pdfUri, "application/pdf");
        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(viewIntent);
    }

    private void sharePdf(Uri pdfUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share PDF using"));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Evaluation Details", true);
    }
    private void populateEvaluationDetails(CourseEvaluationInfoModel evaluationInfo) {
        String repeaterStatus = areRepeaters ? " (Repeaters)" : "";

        TextView classNameTextView = findViewById(R.id.classNameTextView);
        TextView courseNameTextView = findViewById(R.id.courseNameTextView);
        TextView evalTypeTextView = findViewById(R.id.EvalTypetxtView);
        TextView evalTMarksTextView = findViewById(R.id.EvalTMarksTxtView);

        classNameTextView.setText(TeacherInstanceModel.getInstance(this).getClassName());
        courseNameTextView.setText(TeacherInstanceModel.getInstance(this).getCourseName() + repeaterStatus);
        evalTypeTextView.setText(evaluationInfo.getEvaluationName());

        // Format the total marks to remove .0 if it is a whole number
        double totalMarks = evaluationInfo.getEvaluationTotalMarks();
        if (totalMarks == (int) totalMarks) {
            evalTMarksTextView.setText(String.valueOf((int) totalMarks));
        } else {
            evalTMarksTextView.setText(String.valueOf(totalMarks));
        }
    }


    private void populateStudentEvaluationDetails(List<CourseEvaluationDetailsModel> evaluationDetailsList) {
        TableLayout tableLayout = findViewById(R.id.tableLayoutAttendance);
        int serialNumber = 1; // Initialize serial number

        for (CourseEvaluationDetailsModel details : evaluationDetailsList) {
            TableRow row = new TableRow(this);

            // Serial Number TextView
            TextView serialNumberTextView = new TextView(this);
            serialNumberTextView.setText(serialNumber + ".");
            row.addView(serialNumberTextView);

            // Student Name TextView
            TextView studentNameTextView = new TextView(this);
            studentNameTextView.setText(details.getStudentName());
            row.addView(studentNameTextView);

            // Roll Number TextView
            TextView rollNoTextView = new TextView(this);
            rollNoTextView.setText(details.getStudentRollNo());
            row.addView(rollNoTextView);

            // Obtained Marks TextView
            double obtainedMarks = details.getObtainedMarks();
            String formattedMarks = obtainedMarks == (int) obtainedMarks ? String.valueOf((int) obtainedMarks) : String.valueOf(obtainedMarks);
            TextView obtainedMarksTextView = new TextView(this);
            obtainedMarksTextView.setText(formattedMarks);
            obtainedMarksTextView.setGravity(android.view.Gravity.CENTER);
            row.addView(obtainedMarksTextView);

            // Increment serial number for the next row
            serialNumber++;

            // Add the row to the table layout
            tableLayout.addView(row);
        }
    }

}
