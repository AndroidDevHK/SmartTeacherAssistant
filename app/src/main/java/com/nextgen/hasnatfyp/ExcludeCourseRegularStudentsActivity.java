package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcludeCourseRegularStudentsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout linearLayoutStudents;
    private String courseId = UserInstituteModel.getInstance(this).getCourseId();
    private List<Student> studentList = new ArrayList<>();
    private int totalDocuments = 0;
    private int documentsProcessed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_course_regular_students);

        db = FirebaseFirestore.getInstance();
        linearLayoutStudents = findViewById(R.id.linearLayoutStudents);
        MaterialButton submitButton = findViewById(R.id.submitButton);
        Bundle extras = getIntent().getExtras();
        loadStudents();

        submitButton.setOnClickListener(v -> {
            saveChanges();
        });

        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        setCardDetails();
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Exclude Regular Students", true);
    }

    @SuppressLint("SetTextI18n")
    private void setCardDetails() {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(this);
        if (userInstituteModel != null) {
            String className = userInstituteModel.getClassName();
            String courseName = getIntent().getStringExtra("courseName");


            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);
            classNameTextView.setText(className);
            courseNameTextView.setText(courseName);
        }
    }

    private void loadStudents() {
        ProgressDialogHelper.showProgressDialog(this,"Loading Students...");
        db.collection("CoursesStudents")
                .whereEqualTo("CourseID", courseId)
                .whereEqualTo("isRepeater", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        totalDocuments = task.getResult().size();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String classId = document.getString("ClassID");
                            String studentRollNo = document.getString("StudentRollNo");
                            if (classId != null && studentRollNo != null) {
                                loadStudentDetails(classId, studentRollNo, document);
                            } else {
                                // If classId or studentRollNo is null, count as processed
                                documentsProcessed++;
                                checkAndDisplayStudents();
                            }
                        }
                    } else {
                        // Handle the error
                    }
                });
    }

    private void loadStudentDetails(String classId, String studentRollNo, QueryDocumentSnapshot courseStudentDocument) {
        db.collection("Classes").document(classId).collection("ClassStudents")
                .whereEqualTo("RollNo", studentRollNo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addStudentToList(document, courseStudentDocument);
                        }
                    } else {
                        // Handle the error
                    }
                    documentsProcessed++;
                    checkAndDisplayStudents();
                });
    }

    private void addStudentToList(@NonNull QueryDocumentSnapshot classStudentDocument, @NonNull QueryDocumentSnapshot courseStudentDocument) {
        String studentName = classStudentDocument.getString("StudentName");
        String studentRollNo = classStudentDocument.getString("RollNo");
        Boolean isEnrolled = courseStudentDocument.getBoolean("IsEnrolled");

        Student student = new Student(studentName, studentRollNo, isEnrolled, courseStudentDocument.getId());
        studentList.add(student);
    }

    private void checkAndDisplayStudents() {
        if (documentsProcessed >= totalDocuments) {
            sortAndDisplayStudents();
        }
    }

    private void sortAndDisplayStudents() {
        Collections.sort(studentList, Comparator.comparing(Student::getStudentRollNo));
        ProgressDialogHelper.dismissProgressDialog();
        linearLayoutStudents.removeAllViews();
        for (int i = 0; i < studentList.size(); i++) {
            addStudentToView(studentList.get(i), i + 1);
        }
    }


    private void addStudentToView(Student student, int serialNumber) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View studentItemView = inflater.inflate(R.layout.select_repeater_item, linearLayoutStudents, false);

        TextView studentNameTextView = studentItemView.findViewById(R.id.text_student_name);
        TextView studentRollNumberTextView = studentItemView.findViewById(R.id.text_roll_number);
        CheckBox checkBox = studentItemView.findViewById(R.id.checkbox_select);

        studentNameTextView.setText(serialNumber + ". " + student.getStudentName());
        studentRollNumberTextView.setText(student.getStudentRollNo());
        checkBox.setChecked(student.getIsEnrolled() != null && student.getIsEnrolled());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            student.setIsEnrolled(isChecked);
        });

        linearLayoutStudents.addView(studentItemView);
    }


    private void saveChanges() {
        if (!hasChanges()) {
            Toast.makeText(this,"No Changes to save.",Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Changes");

        StringBuilder message = new StringBuilder("The following changes will be applied:\n\n");

        int serialNumber = 1;
        for (Student student : studentList) {
            if (student.isStatusChanged()) {
                String status = student.getIsEnrolled() ? "Enrolled" : "Not Enrolled";
                message.append(serialNumber).append(". ").append(student.getStudentName()).append(" (").append(student.getStudentRollNo()).append(") will be ").append(status).append("\n");
                serialNumber++;
            }
        }

        builder.setMessage(message.toString());

        builder.setPositiveButton("Yes", (dialog, which) -> {
            ProgressDialogHelper.showProgressDialog(ExcludeCourseRegularStudentsActivity.this, "Saving Changes...");
            int totalUpdates = 0;
            AtomicInteger updatesCompleted = new AtomicInteger(0);

            for (Student student : studentList) {
                if (student.isStatusChanged()) {
                    totalUpdates++;
                    int finalTotalUpdates = totalUpdates;
                    db.collection("CoursesStudents").document(student.getDocumentId())
                            .update("IsEnrolled", student.getIsEnrolled())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    int count = updatesCompleted.incrementAndGet();
                                    if (count == finalTotalUpdates) {
                                        // All updates completed, dismiss progress dialog
                                        ProgressDialogHelper.dismissProgressDialog();
                                        Toast.makeText(this,"Save Changes Successfully!",Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                } else {
                                    // Handle the error
                                }
                            });
                }
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private boolean hasChanges() {
        for (Student student : studentList) {
            if (student.isStatusChanged()) {
                return true;
            }
        }
        return false;
    }

    private static class Student {
        private String studentName;
        private String studentRollNo;
        private Boolean isEnrolled;
        private String documentId;
        private Boolean originalIsEnrolled;

        public Student(String studentName, String studentRollNo, Boolean isEnrolled, String documentId) {
            this.studentName = studentName;
            this.studentRollNo = studentRollNo;
            this.isEnrolled = isEnrolled;
            this.documentId = documentId;
            this.originalIsEnrolled = isEnrolled;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getStudentRollNo() {
            return studentRollNo;
        }

        public Boolean getIsEnrolled() {
            return isEnrolled;
        }

        public void setIsEnrolled(Boolean isEnrolled) {
            this.isEnrolled = isEnrolled;
        }

        public String getDocumentId() {
            return documentId;
        }

        public Boolean getOriginalIsEnrolled() {
            return originalIsEnrolled;
        }

        public boolean isStatusChanged() {
            return !isEnrolled.equals(originalIsEnrolled);
        }
    }
}
