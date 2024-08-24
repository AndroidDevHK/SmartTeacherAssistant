package Mark_Course_Students_Attendance_Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;

import com.nextgen.hasnatfyp.CustomLoadingDialog;
import com.nextgen.hasnatfyp.DisplaySubmittedAttendanceDetailsActivity;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Display_Teacher_Semester_Classes_Acitivity.TeacherClassModel;
import View_Class_Students_Activity.StudentModel;

public class MarkStudentsAttendanceActivity extends AppCompatActivity {

    private TextView courseNameTextView;
    private TextView classNameTextView;
    private LinearLayout linearLayoutStudents;
    private Spinner selectSpinner;
    TeacherClassModel teacherClass;
    boolean areRepeaters;
    TextView dateTextView;
    ImageView DateIcon;

    CustomLoadingDialog loadingDialog;
    private TextView DateTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_students_attendance);

        initializeViews();

        Intent intent = getIntent();
        loadingDialog = new CustomLoadingDialog(MarkStudentsAttendanceActivity.this, "attendance_marking.json", "Marking Attendance...");
        teacherClass = getTeacherClassFromIntent(intent);
        areRepeaters = getAreRepeatersFromIntent(intent);

        setCourseAndClassName(teacherClass);

        populateLinearLayout(teacherClass, areRepeaters);
        DateTextView = findViewById(R.id.text_view_date);

        selectSpinner = findViewById(R.id.selectSpinner);
        setupSpinner();

        findViewById(R.id.submitButton).setOnClickListener(view -> submitAttendance());
        DateIcon = findViewById(R.id.date_icon);

        dateTextView = findViewById(R.id.selected_date_text_view);
        setInitialSelectedDate(dateTextView);
        DateIcon.setOnClickListener(v -> {
            setNextDateIfSunday(dateTextView);

            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            Log.d("Adjusted date", "Adjusted date: " + currentYear + "/" + (currentMonth + 1) + "/" + currentDay);

            try {
                showCustomCalendarDialog(currentYear, currentMonth, currentDay, dateTextView);
            } catch (Exception e) {
                Log.e("CalendarDialog", "Error showing custom calendar dialog", e);
            }
        });
        if (!UserInstituteModel.getInstance(this).isSoloUser()) {
            setupAttendancePermission(DateIcon,DateTextView);
        }


    }

    @SuppressLint("SetTextI18n")
    private void setupAttendancePermission(ImageView dateIcon, TextView dateTextView) {
        if (!TeacherInstanceModel.getInstance(this).isPastAttendancePermission())
        {
            dateIcon.setVisibility(View.GONE);
            dateTextView.setText("Today Date : ");
        }
    }

    private void SetupToolbar(Toolbar toolbar) {
        String Title;
        if (areRepeaters) {
            Title="Mark Repeaters Attendance";
        } else {
            Title="Mark Attendance";
        }
        SetupToolbar.setup(this, toolbar, Title, true);
    }

    private void setNextDateIfSunday(TextView dateTextView) {
        // Get the current date from the dateTextView
        String dateString = dateTextView.getText().toString().trim();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            // Parse the current date
            calendar.setTime(dateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Return if there's an error parsing the date
        }

        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Check if the current day is Sunday
        if (currentDayOfWeek == Calendar.SUNDAY) {
            // If it's Sunday, set the date to the next day (Monday)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            // Get the next day's year, month, and day
            int nextYear = calendar.get(Calendar.YEAR);
            int nextMonth = calendar.get(Calendar.MONTH);
            int nextDay = calendar.get(Calendar.DAY_OF_MONTH);

            // Format the next date
            String nextDate = nextDay + "/" + (nextMonth + 1) + "/" + nextYear;

            // Update the dateTextView with the next date
            dateTextView.setText(nextDate);
        }
    }


    public void showAttendanceMarkedDialog(String courseName, boolean isRepeater, String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String repeaterText = isRepeater ? "(Repeater)" : "";

        String message = "<b>Course:</b> " + courseName + " " + repeaterText + "<br/><b>Date:</b> " + date + "<br/><br/>You can edit it from the pending attendances menu.";

        builder.setTitle("Attendance Already Marked")
                .setMessage(Html.fromHtml(message))
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void storeStudentsListLocally(OfflineEvaluationModel attendanceModel) {// Assuming this method exists
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
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String uniqueKey = attendanceModel.getAttendanceDate() + "_" + attendanceModel.getCourseId() + "_" + attendanceModel.getCourseName() + "_" + (attendanceModel.isAreRepeaters() ? "Repeaters" : "Regular");

        File file = new File(directory, uniqueKey + ".json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            String studentsListJson = new Gson().toJson(attendanceModel);
            FileWriter writer = new FileWriter(file);
            writer.write(studentsListJson);
            writer.close(); // Close the FileWriter after writing

            Toast.makeText(this, "Attendance stored offline successfully. You can edit attendance from the pending attendance menu.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setInitialSelectedDate(TextView dateEditText) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        String selectedDate = currentDay + "/" + (currentMonth + 1) + "/" + currentYear;
        dateEditText.setText(selectedDate);
    }

    private void showCustomCalendarDialog(int currentYear, int currentMonth, int currentDay, TextView editText) {
        String dateString = dateTextView.getText().toString().trim();
        String[] dateParts = dateString.split("/");
        int initialYear = currentYear;
        int initialMonth = currentMonth;
        int initialDay = currentDay;

        if (dateParts.length == 3) {
            initialDay = Integer.parseInt(dateParts[0]);
            initialMonth = Integer.parseInt(dateParts[1]) - 1;
            initialYear = Integer.parseInt(dateParts[2]);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDatePickerDialogTheme, null, initialYear, initialMonth, initialDay) {
            private Calendar lastValidDate = getCalendarFromDateString(dateString);

            @Override
            public void onDateChanged(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                if (selectedCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    Toast.makeText(MarkStudentsAttendanceActivity.this, "Sunday cannot be selected.", Toast.LENGTH_SHORT).show();
                    view.updateDate(lastValidDate.get(Calendar.YEAR), lastValidDate.get(Calendar.MONTH), lastValidDate.get(Calendar.DAY_OF_MONTH));
                } else {
                    lastValidDate.set(year, month, dayOfMonth);
                }
            }

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                DatePicker datePicker = getDatePicker();

                String semesterStartDate = TeacherInstanceModel.getInstance(getContext()).getSemesterStartDate();
                String semesterEndDate = getCurrentDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar startCalendar = Calendar.getInstance();
                Calendar endCalendar = Calendar.getInstance();
                try {
                    startCalendar.setTime(dateFormat.parse(semesterStartDate));
                    endCalendar.setTime(dateFormat.parse(semesterEndDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datePicker.setMinDate(startCalendar.getTimeInMillis());
                datePicker.setMaxDate(endCalendar.getTimeInMillis());
                disableSundays(datePicker);
                datePicker.updateDate(lastValidDate.get(Calendar.YEAR), lastValidDate.get(Calendar.MONTH), lastValidDate.get(Calendar.DAY_OF_MONTH));
            }

            private void disableSundays(DatePicker datePicker) {
                try {
                    Field[] datePickerFields = datePicker.getClass().getDeclaredFields();
                    for (Field datePickerField : datePickerFields) {
                        if ("mDaySpinner".equals(datePickerField.getName()) || "mDayPicker".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setEnabled(false);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            private Calendar getCalendarFromDateString(String dateString) {
                Calendar calendar = Calendar.getInstance();
                if (!dateString.isEmpty()) {
                    String[] dateParts = dateString.split("/");
                    if (dateParts.length == 3) {
                        int year = Integer.parseInt(dateParts[2]);
                        int month = Integer.parseInt(dateParts[1]) - 1; // Subtract 1 as months are 0-based
                        int day = Integer.parseInt(dateParts[0]);
                        calendar.set(year, month, day);
                    }
                }
                return calendar;
            }
        };

        datePickerDialog.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
            dateTextView.setText(selectedDate);
        });

        datePickerDialog.show();
    }
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        return currentDay + "/" + (currentMonth + 1) + "/" + currentYear;
    }
    private void initializeViews() {
        courseNameTextView = findViewById(R.id.courseNameTextView);
        classNameTextView = findViewById(R.id.classNameTextView);
        linearLayoutStudents = findViewById(R.id.linearLayoutStudents);
        selectSpinner = findViewById(R.id.selectSpinner);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
    }
    private void setupSpinner() {
        selectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedItem = adapterView.getItemAtPosition(position).toString();
                // Handle spinner item selection
                setAttendanceStatusForAllStudents(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }
    private void setAttendanceStatusForAllStudents(String selectedItem) {
        String status;
        switch (selectedItem) {
            case "Present All":
                status = "P";
                break;
            case "Absent All":
                status = "A";
                break;
            default:
                status = "";
                break;
        }

        LinearLayout linearLayout = findViewById(R.id.linearLayoutStudents);
        int childCount = linearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = linearLayout.getChildAt(i);
            if (childView instanceof ViewGroup) {
                ViewGroup studentViewGroup = (ViewGroup) childView;
                RadioGroup radioGroup = studentViewGroup.findViewById(R.id.radio_group);
                switch (status) {
                    case "P":
                        ((RadioButton) radioGroup.findViewById(R.id.radio_present)).setChecked(true);
                        break;
                    case "A":
                        ((RadioButton) radioGroup.findViewById(R.id.radio_absent)).setChecked(true);
                        break;
                }
            }
        }
    }
    private TeacherClassModel getTeacherClassFromIntent(Intent intent) {
        return intent.getParcelableExtra("teacherClass");
    }
    private boolean getAreRepeatersFromIntent(Intent intent) {
        return intent.getBooleanExtra("areRepeaters", false);
    }

    private void setCourseAndClassName(TeacherClassModel teacherClass) {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";

        courseNameTextView.setText(teacherClass.getCourseName()+repeaterStatus);
        classNameTextView.setText(teacherClass.getClassName());
    }

    private void populateLinearLayout(TeacherClassModel teacherClass, boolean areRepeaters) {
        List<StudentModel> studentsList;
        if (areRepeaters) {
            studentsList = teacherClass.getCourseRepeatersStudents();
        } else {
            studentsList = teacherClass.getRegularCourseStudents();
        }

        linearLayoutStudents.removeAllViews(); // Clear existing views

        int serialNumber = 1; // Start serial number from 1

        for (StudentModel student : studentsList) {
            View studentView = getLayoutInflater().inflate(R.layout.select_attendance_item, null);

            TextView nameTextView = studentView.findViewById(R.id.nameTextView);
            TextView rollNoTextView = studentView.findViewById(R.id.rollNoTextView);
            TextView classIdTextView = studentView.findViewById(R.id.classIdTextView);
            RadioGroup radioGroup = studentView.findViewById(R.id.radio_group);

            nameTextView.setText(serialNumber + ". " + student.getStudentName()); // Concatenate serial number with name
            rollNoTextView.setText(student.getRollNo());
            classIdTextView.setText(student.getClassID());

            radioGroup.clearCheck();

            linearLayoutStudents.addView(studentView);

            serialNumber++; // Increment serial number for the next student
        }
    }




    private void submitAttendance() {
        List<StudentModel> studentsList = getStudentsListFromLinearLayout(linearLayoutStudents);
        if (studentsList.isEmpty()) {
            Toast.makeText(this, "No students available for attendance", Toast.LENGTH_SHORT).show();
            return;
        }
        for (StudentModel student : studentsList) {
            if (student.getAttendanceStatus().isEmpty()) {
                Toast.makeText(this, "Please select attendance status for all students", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String courseId = teacherClass.getCourseId();
        String courseName = teacherClass.getCourseName();
        String selectedDate = extractDateFromEditText(dateTextView);
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        try {
            // Parse the selected date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = sdf.parse(selectedDate);

            // Check if the selected date is a future date
            if (parsedDate != null && parsedDate.after(currentDate)) {
                showToast("Attendance cannot be marked for a future date.");
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Return if there's an error parsing the date
        }
        if (isSunday(selectedDate)) {
            showToast("Sunday is not allowed for attendance.");
            return;
        }

        String uniqueKey = selectedDate + "_" + courseId + "_" + courseName + "_" + (areRepeaters ? "Repeaters" : "Regular") +".json";

        boolean isAttendanceMarked = isAttendanceMarked(uniqueKey);
        if (isAttendanceMarked) {
            showAttendanceMarkedDialog(courseName, areRepeaters, selectedDate);
            return;
        }
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_attendance);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Set background to transparent

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Set custom width here
        dialog.getWindow().setAttributes(layoutParams);

        LinearLayout layoutPresent = dialog.findViewById(R.id.layoutPresent);
        LinearLayout layoutAbsent = dialog.findViewById(R.id.layoutAbsent);
        LinearLayout layoutLeave = dialog.findViewById(R.id.layoutLeave);

        int presentSerialNumber = 1;
        int absentSerialNumber = 1;
        int leaveSerialNumber = 1;

        for (StudentModel student : studentsList) {
            String studentInfo = splitName(student.getStudentName()) + " - " + student.getRollNo();
            TextView textView = new TextView(this);
            textView.setText(studentInfo);
            textView.setTextColor(Color.WHITE);

            switch (student.getAttendanceStatus()) {
                case "P":
                    textView.setText(presentSerialNumber + ". " + studentInfo);
                    layoutPresent.addView(textView);
                    presentSerialNumber++;
                    break;
                case "A":
                    textView.setText(absentSerialNumber + ". " + studentInfo);
                    layoutAbsent.addView(textView);
                    absentSerialNumber++;
                    break;
                case "L":
                    textView.setText(leaveSerialNumber + ". " + studentInfo);
                    layoutLeave.addView(textView);
                    leaveSerialNumber++;
                    break;
            }
        }

        if (layoutPresent.getChildCount() == 0) {
            TextView textViewPresent = dialog.findViewById(R.id.textViewPresent);
            textViewPresent.setVisibility(View.GONE);
        }

        if (layoutAbsent.getChildCount() == 0) {
            TextView textViewAbsent = dialog.findViewById(R.id.textViewAbsent);
            textViewAbsent.setVisibility(View.GONE);
        }

        if (layoutLeave.getChildCount() == 0) {
            TextView textViewLeave = dialog.findViewById(R.id.textViewLeave);
            textViewLeave.setVisibility(View.GONE);
        }

        MaterialButton buttonOK = dialog.findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(view -> {
            if (TeacherInstanceModel.getInstance(this).isOfflineMode()) {
                OfflineEvaluationModel attendanceModel = new OfflineEvaluationModel(studentsList, selectedDate, courseId, courseName, areRepeaters);
                storeStudentsListLocally(attendanceModel);
            } else {
                SubmitStudentAttendance();
            }
            dialog.dismiss();
        });

        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
    private boolean isSunday(String selectedDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(selectedDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            System.out.println("Selected date: " + selectedDate);
            System.out.println("Day of week: " + dayOfWeek);
            return dayOfWeek == Calendar.SUNDAY;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String splitName(String fullNameWithSerial) {
        String[] parts = fullNameWithSerial.split("\\.", 2);

        if (parts.length == 2) {
            return parts[1].trim();
        } else {
            return fullNameWithSerial;
        }
    }


    private boolean isAttendanceMarked(String uniqueKey) {
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

        File file = new File(directory, uniqueKey);
        return file.exists();
    }

    private List<StudentModel> getStudentsListFromLinearLayout(LinearLayout linearLayout) {
        List<StudentModel> studentsList = new ArrayList<>();
        int childCount = linearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = linearLayout.getChildAt(i);
            if (childView instanceof ViewGroup) {
                // Assuming each child view is a ViewGroup containing student information
                ViewGroup studentViewGroup = (ViewGroup) childView;
                String studentName = ((TextView) studentViewGroup.findViewById(R.id.nameTextView)).getText().toString();
                String rollNo = ((TextView) studentViewGroup.findViewById(R.id.rollNoTextView)).getText().toString();
                String classId = ((TextView) studentViewGroup.findViewById(R.id.classIdTextView)).getText().toString(); // Get classId
                RadioButton radioPresent = studentViewGroup.findViewById(R.id.radio_present);
                RadioButton radioAbsent = studentViewGroup.findViewById(R.id.radio_absent);
                RadioButton radioLeave = studentViewGroup.findViewById(R.id.radio_leave);
                String attendanceStatus = ""; // Default to present


                if (radioPresent.isChecked()) {
                    attendanceStatus = "P";
                }
                else if (radioAbsent.isChecked()) {
                    attendanceStatus = "A";
                } else if (radioLeave.isChecked()) {
                    attendanceStatus = "L";
                }

                StudentModel student = new StudentModel(studentName, rollNo, true, attendanceStatus, classId); // Pass classId
                studentsList.add(student);
            }
        }
        return studentsList;
    }

    private void SubmitStudentAttendance() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String selectedDate = extractDateFromEditText(dateTextView);
        loadingDialog.show();
        db.collection("Attendance")
                .whereEqualTo("AttendanceDate", selectedDate)
                .whereEqualTo("CourseID", teacherClass.getCourseId())
                .whereEqualTo("IsRepeater", areRepeaters)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        showToast("Attendance record for the selected date already exists.");
                        loadingDialog.dismiss();
                    } else {
                        Map<String, Object> attendanceData = new HashMap<>();
                        attendanceData.put("AttendanceDate", selectedDate);
                        attendanceData.put("CourseID", teacherClass.getCourseId());
                        attendanceData.put("IsRepeater", areRepeaters);

                        // Start a batched write
                        WriteBatch batch = db.batch();

                        // Add the new attendance record
                        DocumentReference attendanceRef = db.collection("Attendance").document();
                        batch.set(attendanceRef, attendanceData);

                        // Add course attendance record
                        DocumentReference courseAttendanceRef = db.collection("CourseAttendance").document();
                        Map<String, Object> courseAttendanceData = new HashMap<>();
                        courseAttendanceData.put("CourseID", teacherClass.getCourseId());
                        courseAttendanceData.put("AttendanceID", attendanceRef.getId());
                        courseAttendanceData.put("IsRepeater", areRepeaters);
                        batch.set(courseAttendanceRef, courseAttendanceData);

                        // Add student attendance records
                        List<StudentModel> studentsList = getStudentsListFromLinearLayout(linearLayoutStudents);
                        for (StudentModel student : studentsList) {
                            String rollNo = student.getRollNo();
                            String attendanceStatus = student.getAttendanceStatus();
                            String ClassID = student.getClassID();
                            String documentId = rollNo + "_" + attendanceRef.getId();
                            DocumentReference studentAttendanceRef = db.collection("CourseStudentsAttendance").document(documentId);
                            Map<String, Object> studentAttendanceData = new HashMap<>();
                            studentAttendanceData.put("AttendanceID", attendanceRef.getId());
                            studentAttendanceData.put("StudentRollNo", rollNo);
                            studentAttendanceData.put("ClassID", ClassID);
                            studentAttendanceData.put("CourseID", teacherClass.getCourseId());
                            studentAttendanceData.put("AttendanceStatus", attendanceStatus);
                            batch.set(studentAttendanceRef, studentAttendanceData);

                            // Update student course attendance list
                            String documentPath = "StudentCourseAttendanceList/" + rollNo + "_" + teacherClass.getCourseId();
                            DocumentReference studentCourseAttendanceRef = db.document(documentPath);
                            Map<String, Object> studentCourseAttendanceData = new HashMap<>();
                            studentCourseAttendanceData.put("StudentRollNo", rollNo);
                            studentCourseAttendanceData.put("CourseID", teacherClass.getCourseId());
                            studentCourseAttendanceData.put("IsRepeater", areRepeaters);
                            batch.set(studentCourseAttendanceRef, studentCourseAttendanceData, SetOptions.merge());
                            batch.update(studentCourseAttendanceRef, "AttendanceIDs", FieldValue.arrayUnion(attendanceRef.getId()));
                        }

                        // Commit the batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> showToastAndFinishActivity("All students' Attendance submitted successfully", studentsList))
                                .addOnFailureListener(e -> Log.e(TAG, "Error submitting attendance records", e))
                                .addOnCompleteListener(task -> loadingDialog.dismiss());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking attendance record", e);
                    loadingDialog.dismiss();
                });
    }

    private void showToastAndFinishActivity(String message, List<StudentModel> studentsList) {

        loadingDialog.dismiss();
        showToast(message);
        Intent intent = new Intent(this, DisplaySubmittedAttendanceDetailsActivity.class);
        intent.putExtra("studentsList", (Serializable) studentsList);
        intent.putExtra("selectedDate", extractDateFromEditText(dateTextView));
        intent.putExtra("R", areRepeaters);
        TeacherInstanceModel.getInstance(this).setCourseName(courseNameTextView.getText().toString());
        TeacherInstanceModel.getInstance(this).setClassName(classNameTextView.getText().toString());
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private String extractDateFromEditText(TextView dateEditText) {
        String dateString = dateEditText.getText().toString().trim();

        // Define the input and output date formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            // Parse the input date string
            Date date = inputFormat.parse(dateString);

            // Format the parsed date into the desired format
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle parsing errors gracefully
        }
    }
}
