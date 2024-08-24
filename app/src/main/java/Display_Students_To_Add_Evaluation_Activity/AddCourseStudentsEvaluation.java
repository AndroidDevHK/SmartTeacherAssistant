package Display_Students_To_Add_Evaluation_Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.CustomKeyboardHelper;
import com.nextgen.hasnatfyp.DisplaySubmittedEvaluationDetailsActivity;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Teacher_Semester_Classes_Acitivity.TeacherClassModel;

import OfflineEvluationManagement.OfflineEvaluationModel;
import OfflineEvluationManagement.StudentEvalMarksOffline;
import View_Class_Students_Activity.StudentModel;

public class AddCourseStudentsEvaluation extends AppCompatActivity {

    private LinearLayout linearLayoutStudentDetails;
    private List<EditText> obtainedMarksEditTextList;
    private List<TextView> ClassIDTextList;

    private List<StudentModel> studentsList;
    private String totalMarks;
    private AtomicInteger StudentProcessed = new AtomicInteger(0);
    TeacherClassModel teacherClass;
    String evaluation;
    boolean areRepeaters;
    List<StudentEvalMarksOffline> studentEvalMarksOfflineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course_students_evaluation);

        linearLayoutStudentDetails = findViewById(R.id.linear_layout_student_details);
        obtainedMarksEditTextList = new ArrayList<>();
        ClassIDTextList = new ArrayList<>();
        studentEvalMarksOfflineList = new ArrayList<>();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            areRepeaters = extras.getBoolean("areRepeaters");
            teacherClass = extras.getParcelable("teacherClass");
            evaluation = extras.getString("evaluation");
            totalMarks = extras.getString("totalMarks");

            studentsList = areRepeaters ? teacherClass.getCourseRepeatersStudents() : teacherClass.getRegularCourseStudents();

            populateStudentDetails(studentsList);
            setupSaveButton();
            setCardDetails();
        }

        ActivityManager.getInstance().addActivityForKill(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);

    }

    @SuppressLint("SetTextI18n")
    private void setCardDetails() {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);
            TextView EvalType = findViewById(R.id.EvalTypetxtView);
            TextView EvalTotalMarks = findViewById(R.id.EvalTMarks);
            classNameTextView.setText(className);
            EvalType.setText(evaluation);
            EvalTotalMarks.setText(totalMarks);
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }
    private void storeStudentsListLocally(OfflineEvaluationModel evaluationModel) {
        String TeacherUserName;
        if(UserInstituteModel.getInstance(this).isSoloUser())
        {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        }
        else
        {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = TeacherUserName + "_EvaluationData";

        File directory = new File(getFilesDir(), folderName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String uniqueKey = evaluationModel.getEvaluationName() + "_" + evaluationModel.getEvaluationTMarks() + "_" + evaluationModel.getCourseId() + "_" + evaluationModel.getCourseName() + "_" + (evaluationModel.isAreRepeaters() ? "Repeaters" : "Regular");

        File file = new File(directory, uniqueKey + ".json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            String studentsListJson = new Gson().toJson(evaluationModel);
            FileWriter writer = new FileWriter(file);
            writer.write(studentsListJson);
            writer.close();


            Toast.makeText(this, "Evaluation stored offline successfully. You can edit attendance from the pending Evaluation menu.", Toast.LENGTH_SHORT).show();
            ActivityManager.getInstance().finishActivitiesForKill();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("SetTextI18n")
    private void populateStudentDetails(List<StudentModel> studentsList) {
        LayoutInflater inflater = LayoutInflater.from(this);
        int serialNumber = 1;

        for (StudentModel student : studentsList) {
            LinearLayout studentLayout = (LinearLayout) inflater.inflate(R.layout.student_evaluation_item, null, false);
            TextView textViewSerialNumber = studentLayout.findViewById(R.id.text_serial_number);
            TextView textViewStudentName = studentLayout.findViewById(R.id.text_student_name);
            TextView textViewStudentRollNo = studentLayout.findViewById(R.id.text_roll_number);
            TextView textViewClassID = studentLayout.findViewById(R.id.classIdTextView);

            EditText editTextObtainedMarks = studentLayout.findViewById(R.id.editText_obtained_marks);

            textViewSerialNumber.setText(serialNumber+".");
            textViewStudentName.setText(student.getStudentName());
            textViewStudentRollNo.setText(student.getRollNo());
            textViewClassID.setText(student.getClassID());

            obtainedMarksEditTextList.add(editTextObtainedMarks);
            ClassIDTextList.add(textViewClassID);
            linearLayoutStudentDetails.addView(studentLayout);

            serialNumber++;
        }
    }

    private void setupSaveButton() {
        MaterialButton buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            if (checkIfObtainedMarksEmpty()) {
                showToast("Please fill in all obtained marks fields");
            } else if (checkIfObtainedMarksGreaterThanTotal()) {
                showToast("Obtained marks cannot be > total marks");
            } else {
                    showConfirmationDialog();
            }
        });
    }
    private boolean checkIfObtainedMarksEmpty() {
        for (EditText editText : obtainedMarksEditTextList) {
            if (editText.getText().toString().isEmpty()) {
                editText.setError("Please fill this field");
                return true;
            }
        }
        return false;
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Add Evaluation Details", true);
    }
    private boolean checkIfObtainedMarksGreaterThanTotal() {
        float total = Float.parseFloat(totalMarks);
        for (EditText editText : obtainedMarksEditTextList) {
            float obtainedMarks = Float.parseFloat(editText.getText().toString());
            if (obtainedMarks > total) {
                editText.setError("Obtained marks cannot be > than total marks");
                return true;
            }
        }
        return false;
    }


    private void showConfirmationDialog() {
        studentEvalMarksOfflineList.clear();
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_evaluation, null);

        LinearLayout layoutEvaluationDetails = dialogView.findViewById(R.id.layoutEvaluationDetails);
        Button buttonOK = dialogView.findViewById(R.id.buttonOK);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // Initialize serial number
        int serialNumber = 1;

        for (int i = 0; i < obtainedMarksEditTextList.size(); i++) {
            EditText editText = obtainedMarksEditTextList.get(i);
            StudentModel student = studentsList.get(i);
            String studentInfo = serialNumber + ". " + student.getStudentName() + " (" + student.getRollNo() + ") - " + editText.getText().toString() + "/" + totalMarks;
            TextView textView = new TextView(this);
            textView.setText(studentInfo);
            textView.setTextColor(Color.WHITE);
            layoutEvaluationDetails.addView(textView);

            serialNumber++; // Increment serial number for the next student

            TextView classIdTextView = ClassIDTextList.get(i);
            String classId = areRepeaters ? classIdTextView.getText().toString() : teacherClass.getClassId();

            // Create StudentEvalMarksOffline object with classId
            StudentEvalMarksOffline studentEvalMarksOffline = new StudentEvalMarksOffline(
                    student.getStudentName(),
                    student.getRollNo(),
                    Double.parseDouble(editText.getText().toString()),
                    classId
            );
            studentEvalMarksOfflineList.add(studentEvalMarksOffline);
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
            if (TeacherInstanceModel.getInstance(this).isOfflineMode()) {
            OfflineEvaluationModel evaluationModel = new OfflineEvaluationModel(studentEvalMarksOfflineList, totalMarks, teacherClass.getCourseId(), teacherClass.getCourseName(), areRepeaters,evaluation,getCurrentDate());
            storeStudentsListLocally(evaluationModel);
        } else {
            ProgressDialogHelper.showProgressDialog(this, "Validating..");
            checkEvalExistence();
        }

            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> {
            showToast("Canceled!");
            dialog.dismiss();
        });

        dialog.show();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void checkEvalExistence() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Evaluations")
                .whereEqualTo("CourseID", teacherClass.getCourseId())
                .whereEqualTo("AreRepeaters", areRepeaters)
                .whereEqualTo("EvalName", evaluation.toUpperCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        showToast("Similar evaluation record already exists.");
                        ProgressDialogHelper.dismissProgressDialog();
                        showChangeEvalNameDialog(db);
                    } else {
                        ProgressDialogHelper.showProgressDialog(this, "Saving Evaluation Details..");
                        saveEvaluationDetails(db);
                    }
                })
                .addOnFailureListener(e -> showToast("Error checking for existing evaluation records: " + e.getMessage()));
    }
    private void showChangeEvalNameDialog(FirebaseFirestore db) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Evaluation Name");

        final EditText input = new EditText(this);
        input.setText(evaluation);
        builder.setView(input);

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String newEvalName = input.getText().toString();
                if (newEvalName.isEmpty()) {
                    input.setError("Evaluation name can't be empty");
                } else {
                    ProgressDialogHelper.showProgressDialog(this, "Validating..");
                    evaluation = newEvalName;
                    checkEvalExistence();
                    alertDialog.dismiss();
                }
            });
        });

        alertDialog.show();
    }


    private void saveEvaluationDetails(FirebaseFirestore db) {
        if (studentsList.isEmpty()) {
            Toast.makeText(this, "No students available for Evaluation", Toast.LENGTH_SHORT).show();
            return;
        }
        WriteBatch batch = db.batch();

        // Generate unique evaluation ID
        String evalId = UUID.randomUUID().toString();

        Map<String, Object> evaluationData = new HashMap<>();
        evaluationData.put("EvalID", evalId);
        evaluationData.put("EvalName", evaluation.toUpperCase());
        evaluationData.put("EvalTMarks", totalMarks);
        evaluationData.put("CourseID", teacherClass.getCourseId());
        evaluationData.put("AreRepeaters", areRepeaters);

        DocumentReference evaluationRef = db.collection("Evaluations").document(evalId);
        batch.set(evaluationRef, evaluationData);

        Map<String, Object> courseEvaluationInfoData = new HashMap<>();
        courseEvaluationInfoData.put("EvalID", evalId);
        courseEvaluationInfoData.put("CourseID", teacherClass.getCourseId());
        courseEvaluationInfoData.put("CreatedBy", "hasnat");
        courseEvaluationInfoData.put("AreRepeaters", areRepeaters);
        courseEvaluationInfoData.put("CreatedWhen", getCurrentDate());
        DocumentReference courseEvaluationInfoRef = db.collection("CourseEvaluationsInfo").document(evalId);
        batch.set(courseEvaluationInfoRef, courseEvaluationInfoData);

        int totalStudents = obtainedMarksEditTextList.size();
        for (int i = 0; i < totalStudents; i++) {
            EditText editText = obtainedMarksEditTextList.get(i);
            TextView classIdTextView = ClassIDTextList.get(i);
            String classId = areRepeaters ? classIdTextView.getText().toString() : teacherClass.getClassId();

            Float obtainedMarks = Float.parseFloat(editText.getText().toString());
            String studentRollNo = studentsList.get(i).getRollNo();

            String documentId = studentRollNo + "_" + evalId;
            DocumentReference evaluationRecordRef = db.collection("CourseStudentsEvaluation").document(documentId);
            Map<String, Object> evaluationRecordData = new HashMap<>();
            evaluationRecordData.put("EvalID", evalId);
            evaluationRecordData.put("StudentRollNo", studentRollNo);
            evaluationRecordData.put("ClassID", classId);
            evaluationRecordData.put("StudentObtMarks", obtainedMarks);
            batch.set(evaluationRecordRef, evaluationRecordData);

            String studentCourseEvalListPath = "StudentCourseEvaluationList/" + studentRollNo + "_" + teacherClass.getCourseId();
            DocumentReference studentCourseEvalListRef = db.document(studentCourseEvalListPath);
            Map<String, Object> studentCourseEvalListData = new HashMap<>();
            studentCourseEvalListData.put("StudentRollNo", studentRollNo);
            studentCourseEvalListData.put("CourseID", teacherClass.getCourseId());
            studentCourseEvalListData.put("ClassID", classId);
            studentCourseEvalListData.put("IsRepeater", areRepeaters);
            batch.set(studentCourseEvalListRef, studentCourseEvalListData, SetOptions.merge());
            batch.update(studentCourseEvalListRef, "EvaluationIDs", FieldValue.arrayUnion(evalId));
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    showToast("All students' Evaluation Submitted Successfully.");
                    ProgressDialogHelper.dismissProgressDialog();
                    finishActivity(studentsList);
                })
                .addOnFailureListener(e -> {
                    showToast("Error occurred in updating some records");
                    Log.e(TAG, "Error saving evaluation records: " + e.getMessage());
                    ProgressDialogHelper.dismissProgressDialog();
                    finishActivity(studentsList);
                });
    }

    private void finishActivity(List<StudentModel> studentsList) {
        Intent intent = new Intent(AddCourseStudentsEvaluation.this, DisplaySubmittedEvaluationDetailsActivity.class);
        ArrayList<String> studentNames = new ArrayList<>();
        ArrayList<String> studentRollNos = new ArrayList<>();
        ArrayList<String> obtainedMarksList = new ArrayList<>(); // Changed to ArrayList<Float>

        for (int i = 0; i < studentsList.size(); i++) {
            StudentModel student = studentsList.get(i);
            studentNames.add(student.getStudentName());
            studentRollNos.add(student.getRollNo());
            obtainedMarksList.add(String.valueOf(Float.parseFloat(obtainedMarksEditTextList.get(i).getText().toString()))); // Parsing as Float
        }

        intent.putStringArrayListExtra("studentNames", studentNames);
        intent.putStringArrayListExtra("studentRollNos", studentRollNos);
        intent.putStringArrayListExtra("obtainedMarksList", obtainedMarksList); // Use putSerializableExtra for ArrayList<Float>
        intent.putExtra("totalMarks", totalMarks);
        intent.putExtra("evalType", evaluation);

        startActivity(intent);
        ActivityManager.getInstance().finishActivitiesForKill();
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }
    
 
}
