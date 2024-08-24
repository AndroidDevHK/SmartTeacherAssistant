package OfflineEvluationManagement;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.DisplayOfflineAddedEvaluationListActivity;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import Add_Class_Courses_Activities.AddClassCourseViaExcelActivity;
import OfflineEvluationManagement.OfflineEvaluationModel;
import View_Semester_Classes_Activity.ManageClassesActivity;

public class EditOfflineEvaluationActivity extends AppCompatActivity {

    // Declare TextViews for course details
    private TextView classNameTextView;
    private TextView courseNameTextView;
    private TextView evalTypetxtView;
    private TextView evalTMarksTextView;
    private LinearLayout linearLayoutStudentDetails;
    private OfflineEvaluationModel offlineEvaluation;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_offline_evaluation);

        // Initialize TextViews
        initializeViews();

        // Retrieve OfflineEvaluationModel object from intent extras
        retrieveOfflineEvaluationFromIntent();

        // Setup Save Button
        setupSaveButton();
        SetupToolbar();

    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        SetupToolbar.setup(this, toolbar, "Edit Evaluation Details", true);
    }
    // Method to initialize TextViews
    private void initializeViews() {
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);
        classNameTextView = findViewById(R.id.classNameTextView);
        courseNameTextView = findViewById(R.id.courseNameTextView);
        evalTypetxtView = findViewById(R.id.EvalTypetxtView);
        evalTMarksTextView = findViewById(R.id.EvalTMarks);
        linearLayoutStudentDetails = findViewById(R.id.linear_layout_student_details);
    }

    private void retrieveOfflineEvaluationFromIntent() {
        Intent intent = getIntent();
        offlineEvaluation = intent.getParcelableExtra("offlineEvaluation");
        populateTextViews(offlineEvaluation);
        populateStudentEvaluationDetails(offlineEvaluation);
    }

    @SuppressLint("SetTextI18n")
    private void populateTextViews(OfflineEvaluationModel offlineEvaluation) {
        if (offlineEvaluation != null) {
            classNameTextView.setText("N/A");
            courseNameTextView.setText(offlineEvaluation.getCourseName());
            evalTypetxtView.setText(offlineEvaluation.getEvaluationName());
            evalTMarksTextView.setText(offlineEvaluation.getEvaluationTMarks());
        }
    }

    private void populateStudentEvaluationDetails(OfflineEvaluationModel offlineEvaluation) {
        if (offlineEvaluation != null && offlineEvaluation.getStudentsList() != null) {
            List<StudentEvalMarksOffline> studentsList = offlineEvaluation.getStudentsList();
            LayoutInflater inflater = LayoutInflater.from(this);

            int serialNumber = 1;

            for (StudentEvalMarksOffline studentEval : studentsList) {
                // Inflate the student_eval layout
                View studentEvalView = inflater.inflate(R.layout.student_evaluation_item, null);

                // Find views inside student_eval layout
                TextView textSrNo = studentEvalView.findViewById(R.id.text_serial_number);
                TextView textStudentName = studentEvalView.findViewById(R.id.text_student_name);
                TextView textRollNumber = studentEvalView.findViewById(R.id.text_roll_number);
                EditText editTextObtainedMarks = studentEvalView.findViewById(R.id.editText_obtained_marks);

                // Set student details
                textSrNo.setText(String.valueOf(serialNumber));
                textStudentName.setText(studentEval.getStudentName());
                textRollNumber.setText(studentEval.getRollNo());

                // Format obtained marks to remove decimal point if whole number
                double obtainedMarks = studentEval.getObtainedMarks();
                if (obtainedMarks == (int) obtainedMarks) {
                    editTextObtainedMarks.setText(String.valueOf((int) obtainedMarks));
                } else {
                    editTextObtainedMarks.setText(String.valueOf(obtainedMarks));
                }

                // Add the inflated view to the parent layout
                linearLayoutStudentDetails.addView(studentEvalView);

                serialNumber++;
            }
        }
    }


    // Method to set up Save Button
    private void setupSaveButton() {
        MaterialButton buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            if (checkIfObtainedMarksEmpty()) {
                showToast("Please fill in all obtained marks fields");
            } else if (checkIfObtainedMarksGreaterThanTotal()) {
                showToast("Obtained marks cannot be greater than total marks");
            } else {
                if (changesMade()) {
                    showConfirmationDialog();
                } else {
                    showToast("No changes to save");
                }
            }
        });
    }

    private boolean checkIfObtainedMarksEmpty() {
        for (int i = 0; i < linearLayoutStudentDetails.getChildCount(); i++) {
            View itemView = linearLayoutStudentDetails.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            if (obtainedMarksEditText.getText().toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfObtainedMarksGreaterThanTotal() {
        double totalMarks = Double.parseDouble(evalTMarksTextView.getText().toString());
        for (int i = 0; i < linearLayoutStudentDetails.getChildCount(); i++) {
            View itemView = linearLayoutStudentDetails.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            double obtainedMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            if (obtainedMarks > totalMarks) {
                obtainedMarksEditText.setError("Obtained marks cannot be greater than total marks");
                return true;
            }
        }
        return false;
    }

    private boolean changesMade() {
        for (int i = 0; i < linearLayoutStudentDetails.getChildCount(); i++) {
            View itemView = linearLayoutStudentDetails.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            double newMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            double oldMarks = offlineEvaluation.getStudentsList().get(i).getObtainedMarks();
            if (newMarks != oldMarks) {
                return true;
            }
        }
        return false;
    }

    private void showConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_evaluation, null);
        LinearLayout layoutEvaluationDetails = dialogView.findViewById(R.id.layoutEvaluationDetails);
        Button buttonOK = dialogView.findViewById(R.id.buttonOK);
        TextView textViewConfirmAttendance = dialogView.findViewById(R.id.textViewConfirmAttendance);
        textViewConfirmAttendance.setText("Are you sure you want to update this Evaluation Details?");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // Initialize serial number
        int serialNumber = 1;

        for (int i = 0; i < offlineEvaluation.getStudentsList().size(); i++) {
            StudentEvalMarksOffline studentDetails = offlineEvaluation.getStudentsList().get(i);
            View itemView = linearLayoutStudentDetails.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            double newMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            double oldMarks = studentDetails.getObtainedMarks();

            // Format marks to remove ".0" for integer values
            String oldMarksText = formatMarks(oldMarks);
            String newMarksText = formatMarks(newMarks);

            // Check if marks have been changed
            if (newMarks != oldMarks) {
                // Construct student information with marks before and after change
                String studentInfo = serialNumber + ". " + studentDetails.getStudentName() + " (" + studentDetails.getRollNo() + ") - " +
                        oldMarksText + " -> " + newMarksText;
                TextView textView = new TextView(this);
                textView.setText(studentInfo);
                textView.setTextColor(Color.WHITE);
                layoutEvaluationDetails.addView(textView);

                serialNumber++; // Increment serial number for the next student
            }
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        dialog.getWindow().getDecorView().setPadding(32, 32, 32, 32);

        buttonOK.setOnClickListener(v -> {
            updateMarks();
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String formatMarks(double marks) {
        if (marks == (int) marks) {
            return String.valueOf((int) marks);
        } else {
            return String.valueOf(marks);
        }
    }

    private void updateMarks() {
        for (int i = 0; i < linearLayoutStudentDetails.getChildCount(); i++) {
            View itemView = linearLayoutStudentDetails.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            double newMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            offlineEvaluation.getStudentsList().get(i).setObtainedMarks(newMarks);
        }

        saveOfflineEvaluationToFile(offlineEvaluation);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveOfflineEvaluationToFile(OfflineEvaluationModel evaluation) {
        if (evaluation == null) {
            return;
        }

        try {
            String teacherUserName;
            if (UserInstituteModel.getInstance(this).isSoloUser()) {
                teacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
            } else {
                teacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
            }
            String folderName = teacherUserName + "_EvaluationData";
            File directory = new File(getFilesDir(), folderName);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String uniqueKey = evaluation.getEvaluationName() + "_" + evaluation.getEvaluationTMarks() + "_" + evaluation.getCourseId() + "_" + evaluation.getCourseName() + "_" + (evaluation.isAreRepeaters() ? "Repeaters" : "Regular");
            File file = new File(directory, uniqueKey + ".json");

            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String updatedJson = gson.toJson(evaluation);
                writer.write(updatedJson);
                showToast("Evaluation updated successfully");
                ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
                Intent intent = new Intent(EditOfflineEvaluationActivity.this, DisplayOfflineAddedEvaluationListActivity.class);
                startActivity(intent);
                Log.i(TAG, "Evaluation updated successfully to " + file.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Failed to update evaluation");
            Log.e(TAG, "Failed to update evaluation", e);
        }
    }
}
