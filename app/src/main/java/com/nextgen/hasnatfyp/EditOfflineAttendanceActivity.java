package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import Mark_Course_Students_Attendance_Activity.OfflineEvaluationModel;
import View_Class_Students_Activity.StudentModel;

public class EditOfflineAttendanceActivity extends AppCompatActivity {

    private TextView courseNameTextView;
    private TextView AttendanceDateTv;
    private Spinner selectSpinner;
    private LinearLayout linearLayoutStudents;
    private MaterialButton submitButton;
    private ActivityManager activityManager;
    private OfflineEvaluationModel attendanceModel;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_offline_attendance);

        initViews();
        setupData();
        setupButtonClick();
        setupSpinner();
        SetupToolbar(toolbar);
    }

    private void initViews() {
        courseNameTextView = findViewById(R.id.courseNameTextView);
        AttendanceDateTv = findViewById(R.id.AttendanceDateTv);
        selectSpinner = findViewById(R.id.selectSpinner);
        linearLayoutStudents = findViewById(R.id.linearLayoutStudents);
        submitButton = findViewById(R.id.submitButton);
        activityManager = ActivityManager.getInstance();
        activityManager.addActivityForKill(this);
        toolbar= findViewById(R.id.customToolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Edit Attendance", true);
    }
    private void setupSpinner() {
        String[] spinnerOptions = getResources().getStringArray(R.array.select_options);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectSpinner.setAdapter(spinnerAdapter);

        selectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if ("Present All".equals(selectedItem)) {
                    updateAllStudentsAttendance("P");
                } else if ("Absent All".equals(selectedItem)) {
                    updateAllStudentsAttendance("A");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupData() {
        attendanceModel = getIntent().getParcelableExtra("attendanceModel");
        if (attendanceModel != null) {
            courseNameTextView.setText(" " +attendanceModel.getCourseName());
            AttendanceDateTv.setText(" " +formatDate(attendanceModel.getAttendanceDate()));
            populateStudentList(attendanceModel.getStudentsList());
        }
    }
    private String formatDate(String originalDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(originalDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return originalDate; // Return original date if parsing fails
        }
    }

    private void populateStudentList(List<StudentModel> studentsList) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (StudentModel student : studentsList) {
            View studentView = inflater.inflate(R.layout.select_attendance_item, linearLayoutStudents, false);
            TextView nameTextView = studentView.findViewById(R.id.nameTextView);
            TextView rollNoTextView = studentView.findViewById(R.id.rollNoTextView);
            TextView classIdTextView = studentView.findViewById(R.id.classIdTextView);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);

            nameTextView.setText(student.getStudentName());
            rollNoTextView.setText(student.getRollNo());
            classIdTextView.setText(student.getClassID());
            setAttendanceStatus(student, radioGroup);

            radioGroup.setTag(student.getAttendanceStatus());

            linearLayoutStudents.addView(studentView);
        }
    }

    private void setAttendanceStatus(StudentModel student, RadioGroup radioGroup) {
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
    }

    private void updateAllStudentsAttendance(String status) {
        for (int i = 0; i < linearLayoutStudents.getChildCount(); i++) {
            View studentView = linearLayoutStudents.getChildAt(i);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);
            switch (status) {
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
        }
    }

    private void setupButtonClick() {
        submitButton.setOnClickListener(v -> {
            String changedStudentsDetails = buildChangedStudentsDetails(linearLayoutStudents);
            if (!changedStudentsDetails.isEmpty()) {
                showConfirmAttendanceDialog(changedStudentsDetails);
            } else {
                showNoChangesDialog();
            }
        });
    }

    private String buildChangedStudentsDetails(LinearLayout layout) {
        StringBuilder changedStudentsDetails = new StringBuilder();

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

                    changedStudentsDetails.append(nameTextView.getText())
                            .append(" (")
                            .append(rollNoTextView.getText())
                            .append(") - ")
                            .append(originalStatus)
                            .append(" -> ")
                            .append(selectedStatus)
                            .append("\n");
                }
            }
        }

        return changedStudentsDetails.toString();
    }

    private void showConfirmAttendanceDialog(String updates) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_attendance);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textViewUpdateAttendance = dialog.findViewById(R.id.textViewUpdateAttendance);
        textViewUpdateAttendance.setText(updates);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);
        MaterialButton buttonOK = dialog.findViewById(R.id.buttonOK);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonOK.setOnClickListener(v -> {
            // Update attendance in the database for each student
            updateAttendanceInDatabase(linearLayoutStudents);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showNoChangesDialog() {
        Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
    }

    private void updateAttendanceInDatabase(LinearLayout layout) {
        if (attendanceModel == null) {
            String currentDate = attendanceModel.getAttendanceDate();

            String TeacherUserName;
            if(UserInstituteModel.getInstance(this).isSoloUser())
            {
                TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
            }
            else
            {
                TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
            }
            String uniqueKey = currentDate + "_" + attendanceModel.getCourseId() + "_" + attendanceModel.getCourseName() + "_" + (attendanceModel.isAreRepeaters() ? "Repeaters" : "Regular");
            String folderName = TeacherUserName + "_AttendanceData";
            File directory = new File(getFilesDir(), folderName);
            File file = new File(directory, uniqueKey + ".json");

            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    // Deserialize the existing attendance model
                    Gson gson = new Gson();
                    attendanceModel = gson.fromJson(reader, OfflineEvaluationModel.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load attendance data", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "Attendance file not found", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        List<StudentModel> studentsList = attendanceModel.getStudentsList();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View studentView = layout.getChildAt(i);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);
            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = studentView.findViewById(selectedRadioButtonId);

            if (selectedRadioButton != null) {
                String selectedStatus = selectedRadioButton.getText().toString();
                TextView rollNoTextView = studentView.findViewById(R.id.rollNoTextView);
                String rollNo = rollNoTextView.getText().toString();

                // Update the attendance status in the students list
                for (StudentModel student : studentsList) {
                    if (student.getRollNo().equals(rollNo)) {
                        student.setAttendanceStatus(selectedStatus);
                        break;
                    }
                }
            }
        }

        // Serialize the updated attendance model back to JSON and save it
        try {
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

            String uniqueKey = attendanceModel.getAttendanceDate() + "_" + attendanceModel.getCourseId() + "_" + attendanceModel.getCourseName() + "_" + (attendanceModel.isAreRepeaters() ? "Repeaters" : "Regular");
            File file = new File(directory, uniqueKey + ".json");

            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String updatedJson = gson.toJson(attendanceModel);
                writer.write(updatedJson);
                Toast.makeText(this, "Attendance updated successfully", Toast.LENGTH_SHORT).show();
                activityManager.finishActivitiesForKill();
                Intent intent = new Intent(EditOfflineAttendanceActivity.this,DisplayOfflineMarkedAttendanceListActivity.class);
                startActivity(intent);

            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to update attendance", Toast.LENGTH_SHORT).show();
        }
    }

}
