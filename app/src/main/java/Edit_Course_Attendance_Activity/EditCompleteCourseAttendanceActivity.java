package Edit_Course_Attendance_Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.AttendanceStudentDetails;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Course_Attendance_List_Activity.AttendanceInfoModel;

public class EditCompleteCourseAttendanceActivity extends AppCompatActivity {

    private String attendanceID;
    ActivityManager activityManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_complete_course_attendance);
        activityManager = ActivityManager.getInstance();
        activityManager.addActivityForKill(this);
        Intent intent = getIntent();
        AttendanceInfoModel attendanceInfo = intent.getParcelableExtra("attendanceInfo");
        attendanceID = attendanceInfo.getAttendanceID();
        Boolean areRepeaters = intent.getBooleanExtra("AreRepeaters", false);
        setCardInfo(areRepeaters, attendanceInfo.getAttendanceDate());
        displayStudentAttendanceDetails(attendanceInfo.getStudentList());
        findViewById(R.id.submitButton).setOnClickListener(view -> submitAttendance());
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        setupSpinner();
    }

    private void setupSpinner() {
        Spinner selectSpinner = findViewById(R.id.selectSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.select_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectSpinner.setAdapter(adapter);
        selectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (selectedItem) {
                    case "Select":
                        break;
                    case "Present All":
                        setAllStudentsAttendanceStatus("P");
                        break;
                    case "Absent All":
                        setAllStudentsAttendanceStatus("A");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setAllStudentsAttendanceStatus(String status) {
        LinearLayout layout = findViewById(R.id.attendanceDetailsLayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View studentView = layout.getChildAt(i);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);
            switch (status) {
                case "P":
                    radioGroup.check(R.id.radio_present);
                    break;
                case "A":
                    radioGroup.check(R.id.radio_absent);
                    break;
            }
        }
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Edit Attendance Details", true);
    }

    private void setCardInfo(Boolean areRepeaters, String attendanceDate) {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);
            TextView attendanceDateTextView = findViewById(R.id.attendanceDateTextView);

            classNameTextView.setText(className);
            attendanceDateTextView.setText(attendanceDate);
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }

    private void displayStudentAttendanceDetails(List<AttendanceStudentDetails> studentAttendanceList) {
        LinearLayout layout = findViewById(R.id.attendanceDetailsLayout);

        for (AttendanceStudentDetails student : studentAttendanceList) {
            View studentView = getLayoutInflater().inflate(R.layout.select_attendance_item, null);

            TextView nameTextView = studentView.findViewById(R.id.nameTextView);
            TextView rollNoTextView = studentView.findViewById(R.id.rollNoTextView);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);

            nameTextView.setText(student.getStudentName());
            rollNoTextView.setText(student.getStudentRollNo());

            // Set the original attendance status as a tag for the RadioGroup
            radioGroup.setTag(student.getAttendanceStatus());

            // Set the attendance status based on the student's data
            switch (student.getAttendanceStatus()) {
                case "P":
                    radioGroup.check(R.id.radio_present);
                    break;
                case "A":
                    radioGroup.check(R.id.radio_absent);
                    break;
                case "L":
                    radioGroup.check(R.id.radio_leave);
                    break;
            }

            layout.addView(studentView);
        }
    }

    private boolean checkForChanges(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View studentView = layout.getChildAt(i);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);

            String originalStatus = (String) radioGroup.getTag();

            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

            RadioButton selectedRadioButton = studentView.findViewById(selectedRadioButtonId);

            if (selectedRadioButton != null) {
                String selectedStatus = selectedRadioButton.getText().toString();
                if (!selectedStatus.equals(originalStatus)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String buildChangedStudentsDetails(LinearLayout layout) {
        StringBuilder changedStudentsDetails = new StringBuilder();
        int srNo = 1; // SR# variable

        for (int i = 0; i < layout.getChildCount(); i++) {
            View studentView = layout.getChildAt(i);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);

            String originalStatus = (String) radioGroup.getTag();

            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

            RadioButton selectedRadioButton = studentView.findViewById(selectedRadioButtonId);

            if (selectedRadioButton != null) {
                String selectedStatus = selectedRadioButton.getText().toString();
                if (!selectedStatus.equals(originalStatus)) {
                    TextView nameTextView = studentView.findViewById(R.id.nameTextView);
                    TextView rollNoTextView = studentView.findViewById(R.id.rollNoTextView);

                    // Append SR# from the separate variable
                    changedStudentsDetails.append(srNo).append(". ");

                    changedStudentsDetails.append(nameTextView.getText())
                            .append(" (")
                            .append(rollNoTextView.getText())
                            .append(") - ")
                            .append(originalStatus)
                            .append(" -> ")
                            .append(selectedStatus)
                            .append("\n");

                    srNo++; // Increment SR# for next student
                }
            }
        }

        return changedStudentsDetails.toString();
    }



    private void showConfirmAttendanceDialog(String updates) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_attendance);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textViewUpdateAttendance = dialog.findViewById(R.id.textViewUpdateAttendance);
        LinearLayout layoutUpdatedAttendance = dialog.findViewById(R.id.layoutUpdatedAttendance);

        textViewUpdateAttendance.setText(updates);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);
        MaterialButton buttonOK = dialog.findViewById(R.id.buttonOK);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonOK.setOnClickListener(v -> {
            // Show progress dialog before starting the update
            ProgressDialogHelper.showProgressDialog(this, "Updating attendance...");

            // Update attendance in the database for each student
            updateAttendanceInDatabase(findViewById(R.id.attendanceDetailsLayout));

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateAttendanceInDatabase(LinearLayout layout) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicInteger completedUpdates = new AtomicInteger(0);
        int totalUpdates = layout.getChildCount();

        for (int i = 0; i < layout.getChildCount(); i++) {
            View studentView = layout.getChildAt(i);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);
            String originalStatus = (String) radioGroup.getTag();
            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = studentView.findViewById(selectedRadioButtonId);

            if (selectedRadioButton != null) {
                String selectedStatus = selectedRadioButton.getText().toString();

                if (!selectedStatus.equals(originalStatus)) {
                    TextView rollNoTextView = studentView.findViewById(R.id.rollNoTextView);
                    String studentRollNo = rollNoTextView.getText().toString();

                    // Create a reference to the "CourseStudentsAttendance" collection
                    CollectionReference attendanceRef = db.collection("CourseStudentsAttendance");

                    // Query to find the document with the matching attendance ID and student roll number
                    Query query = attendanceRef
                            .whereEqualTo("AttendanceID", attendanceID)
                            .whereEqualTo("StudentRollNo", studentRollNo);

                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Update the attendance status in the document
                                document.getReference().update("AttendanceStatus", selectedStatus)
                                        .addOnSuccessListener(aVoid -> {
                                            int updatedCount = completedUpdates.incrementAndGet();
                                            if (updatedCount == totalUpdates) {
                                                Toast.makeText(this, "Attendance Updated Successfully", Toast.LENGTH_SHORT).show();
                                                ProgressDialogHelper.dismissProgressDialog();
                                                activityManager.finishActivitiesForKill();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Failed to update attendance in the database
                                            Toast.makeText(this, "Failed to update attendance for " + studentRollNo, Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Error updating attendance for " + studentRollNo, e);
                                            int updatedCount = completedUpdates.incrementAndGet();
                                            if (updatedCount == totalUpdates) {
                                                // All updates completed, dismiss progress dialog and finish activity
                                                ProgressDialogHelper.dismissProgressDialog();
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            // Handle failures while fetching the documents
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            int updatedCount = completedUpdates.incrementAndGet();
                            if (updatedCount == totalUpdates) {
                                // All updates completed, dismiss progress dialog and finish activity
                                ProgressDialogHelper.dismissProgressDialog();
                                finish();
                            }
                        }
                    });
                } else {
                    int updatedCount = completedUpdates.incrementAndGet();
                    if (updatedCount == totalUpdates) {
                        // All updates completed, dismiss progress dialog and finish activity
                        ProgressDialogHelper.dismissProgressDialog();
                        finish();
                    }
                }
            } else {
                int updatedCount = completedUpdates.incrementAndGet();
                if (updatedCount == totalUpdates) {
                    // All updates completed, dismiss progress dialog and finish activity
                    ProgressDialogHelper.dismissProgressDialog();
                    finish();
                }
            }
        }
    }

    private void submitAttendance() {
        LinearLayout layout = findViewById(R.id.attendanceDetailsLayout);
        boolean changesDetected = checkForChanges(layout);

        if (changesDetected) {
            String updates = buildChangedStudentsDetails(layout);
            showConfirmAttendanceDialog(updates);
        } else {
            Toast.makeText(this, "No changes to Save.", Toast.LENGTH_SHORT).show();
        }
    }
}
