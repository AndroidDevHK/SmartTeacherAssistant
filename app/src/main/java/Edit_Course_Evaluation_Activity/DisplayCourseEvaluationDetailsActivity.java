package Edit_Course_Evaluation_Activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.CourseEvaluationDetailsModel;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Course_Evaluations_List_Activity.CourseEvaluationInfoModel;
import Display_Course_Evaluations_List_Activity.DisplayCourseEvaluationListActivity;

public class DisplayCourseEvaluationDetailsActivity extends AppCompatActivity {

    private List<CourseEvaluationDetailsModel> studentDetailsList;
    private CourseEvaluationInfoModel evaluationInfo;
    private List<EditText> obtainedMarksEditTexts; // Add this list to store EditText views
    boolean areRepeaters;
    ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_course_evaluation_details);

        // Get intent data and populate UI
        populateUI();
        setupSaveButton();
        activityManager = (ActivityManager) getApplication();
        activityManager.addActivityForKill(this);


    }

    private void populateUI() {
        evaluationInfo = getIntent().getParcelableExtra("evaluationInfo");
        areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
        studentDetailsList = evaluationInfo.getEvaluationInfoList();

        setCardInfo(evaluationInfo);
        populateStudentDetails(evaluationInfo.getEvaluationInfoList());
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Edit Evaluation Details", true);
    }

    private void setCardInfo(CourseEvaluationInfoModel evaluationInfo) {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);
            TextView EvalNameTextView = findViewById(R.id.EvalNameTxtView);
            TextView EvalTMTextView = findViewById(R.id.EvalTMtxtview);

            classNameTextView.setText(className);
            EvalNameTextView.setText(evaluationInfo.getEvaluationName()+"");
            EvalTMTextView.setText((evaluationInfo.getEvaluationTotalMarks() == (int) evaluationInfo.getEvaluationTotalMarks()) ?
                    String.valueOf((int) evaluationInfo.getEvaluationTotalMarks()) :
                    String.valueOf(evaluationInfo.getEvaluationTotalMarks()));
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }
    @SuppressLint("SetTextI18n")
    private void populateStudentDetails(List<CourseEvaluationDetailsModel> studentDetailsList) {
        LinearLayout linearLayout = findViewById(R.id.linear_layout_student_details);
        obtainedMarksEditTexts = new ArrayList<>(); // Initialize the list here

        for (int i = 0; i < studentDetailsList.size(); i++) {
            CourseEvaluationDetailsModel studentDetails = studentDetailsList.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.student_evaluation_item, linearLayout, false);
            TextView serialNumberTextView = itemView.findViewById(R.id.text_serial_number);
            TextView studentNameTextView = itemView.findViewById(R.id.text_student_name);
            TextView rollNumberTextView = itemView.findViewById(R.id.text_roll_number);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);

            serialNumberTextView.setText((i + 1) + "."); // Set the serial number
            studentNameTextView.setText(studentDetails.getStudentName());
            rollNumberTextView.setText(studentDetails.getStudentRollNo());
            setObtainedMarks(obtainedMarksEditText, studentDetails.getObtainedMarks());

            linearLayout.addView(itemView);

            obtainedMarksEditTexts.add(obtainedMarksEditText);
        }
    }


    private void setObtainedMarks(EditText obtainedMarksEditText, double obtainedMarks) {
        if (obtainedMarks % 1 == 0) {
            obtainedMarksEditText.setText(String.valueOf((int) obtainedMarks));
        } else {
            obtainedMarksEditText.setText(String.valueOf(obtainedMarks));
        }
    }

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
        LinearLayout linearLayout = findViewById(R.id.linear_layout_student_details);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View itemView = linearLayout.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            if (obtainedMarksEditText.getText().toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfObtainedMarksGreaterThanTotal() {
        LinearLayout linearLayout = findViewById(R.id.linear_layout_student_details);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View itemView = linearLayout.getChildAt(i);
            EditText obtainedMarksEditText = itemView.findViewById(R.id.editText_obtained_marks);
            double obtainedMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            double totalMarks = evaluationInfo.getEvaluationTotalMarks();
            if (obtainedMarks > totalMarks) {
                obtainedMarksEditText.setError("Obtained marks cannot be greater than total marks");
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

        for (int i = 0; i < studentDetailsList.size(); i++) {
            CourseEvaluationDetailsModel studentDetails = studentDetailsList.get(i);
            EditText obtainedMarksEditText = obtainedMarksEditTexts.get(i);
            double newMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            double oldMarks = studentDetails.getObtainedMarks();

            // Format marks to remove ".0" for integer values
            String oldMarksText = formatMarks(oldMarks);
            String newMarksText = formatMarks(newMarks);

            // Check if marks have been changed
            if (newMarks != oldMarks) {
                // Construct student information with marks before and after change
                String studentInfo = serialNumber + ". " + studentDetails.getStudentName() + " (" + studentDetails.getStudentRollNo() + ") - " +
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
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(marks);
    }


    private void updateMarks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicInteger changedMarksCount = new AtomicInteger(0); // Counter for changed marks
        ProgressDialogHelper.showProgressDialog(this,"Updating Marks...");
        for (int i = 0; i < studentDetailsList.size(); i++) {
            CourseEvaluationDetailsModel studentDetails = studentDetailsList.get(i);
            EditText obtainedMarksEditText = obtainedMarksEditTexts.get(i);
            double newMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            double originalMarks = studentDetails.getObtainedMarks();

            // Check if marks have changed
            if (newMarks != originalMarks) {
                changedMarksCount.incrementAndGet();

                String documentId = studentDetails.getStudentRollNo() + "_" + evaluationInfo.getEvalId();

                db.collection("CourseStudentsEvaluation")
                        .document(documentId)
                        .update("StudentObtMarks", newMarks)
                        .addOnSuccessListener(aVoid -> {
                            // Increment the successful update count
                            int count = updateCount.incrementAndGet();
                            // Check if all updates are completed
                            if (count == changedMarksCount.get()) { // Finish activity if all changed marks are updated
                                showToast("Marks updated successfully");
                                ProgressDialogHelper.dismissProgressDialog();
                                Intent intent = new Intent(this, DisplayCourseEvaluationListActivity.class);
                                intent.putExtra("evaluationInfo", evaluationInfo);
                                intent.putExtra("AreRepeaters", areRepeaters);
                                startActivity(intent);
                                activityManager.finishActivitiesForKill();

                            }
                        })
                        .addOnFailureListener(e -> showToast("Error updating marks: " + e.getMessage()));
            }
        }
    }






    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private boolean changesMade() {
        for (int i = 0; i < studentDetailsList.size(); i++) {
            EditText obtainedMarksEditText = obtainedMarksEditTexts.get(i);
            double obtainedMarks = Double.parseDouble(obtainedMarksEditText.getText().toString());
            double originalMarks = studentDetailsList.get(i).getObtainedMarks();
            if (obtainedMarks != originalMarks) {
                return true;
            }
        }
        return false;
    }

}
