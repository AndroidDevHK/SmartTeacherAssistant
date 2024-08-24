package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import Add_Class_Courses_Activities.AddClassCoursesManuallyActivity;
import Display_Teacher_Semester_Classes_Acitivity.DisplayTeacherSemesterClassesActivity;
import View_Semester_Classes_Activity.ManageClassesActivity;

public class SubmitCourseQuizActivity extends AppCompatActivity {

    private ArrayList<QuizModel> quizQuestionsList;
    private String courseId;
    private String classId;
    private FirebaseFirestore db;
    private List<String> studentRollNoList;

    private DocumentReference courseRef;
    private TextView courseNameTextView, totalStdTextView, quizQuestionsTextView, submittedQuestionsTextView;
    private EditText quizStartTimeEditText, quizDurationHoursEditText, quizDurationMinutesEditText, quizWeightageEditText;
    private MaterialButton btnSubmitQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_course_quiz);

        // Initialize views
        initializeViews();

        // Get the intent data
        retrieveIntentData();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (courseId != null) {
            courseRef = db.collection("ClassCourses")
                    .document(classId)
                    .collection("ClassCoursesSubcollection")
                    .document(courseId);
        }

        studentRollNoList = new ArrayList<>();

        // Populate the card view
        populateCardView();

        // Set up submit button click listener
        btnSubmitQuiz.setOnClickListener(v -> fetchStudentRollNumbersAndProceed());

        // Set up date and time picker
        quizStartTimeEditText.setOnClickListener(v -> showDateTimePicker(quizStartTimeEditText));
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);

    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Submit Quiz", true);
    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void initializeViews() {
        courseNameTextView = findViewById(R.id.CourseNameTextView);
        totalStdTextView = findViewById(R.id.TotalStdTextView);
        quizQuestionsTextView = findViewById(R.id.quizQuestions);
        quizStartTimeEditText = findViewById(R.id.editTextQuizStartTime);
        quizDurationHoursEditText = findViewById(R.id.editTextQuizDurationHours);
        quizDurationMinutesEditText = findViewById(R.id.editTextQuizDurationMinutes);
        quizWeightageEditText = findViewById(R.id.editTextQuizWeightage);
        btnSubmitQuiz = findViewById(R.id.btnProceed);
        quizDurationHoursEditText.setText("0");
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            quizQuestionsList = intent.getParcelableArrayListExtra("QUIZ_QUESTIONS_LIST");
            courseId = intent.getStringExtra("CourseID");
            classId = intent.getStringExtra("ClassID");
        }
    }

    private void populateCardView() {
        courseNameTextView.setText(TeacherInstanceModel.getInstance(this).getCourseName());
        totalStdTextView.setText("N/A");
        if (quizQuestionsList != null) {
            quizQuestionsTextView.setText(String.valueOf(quizQuestionsList.size()));
        }
    }

    private void fetchStudentRollNumbersAndProceed() {
        ProgressDialogHelper.showProgressDialog(this,"Submitting Quiz..");
        db.collection("CoursesStudents")
                .whereEqualTo("CourseID", courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentRollNoList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentRollNo = document.getString("StudentRollNo");
                            studentRollNoList.add(studentRollNo);
                        }
                        proceedToQuizSubmission();
                    } else {
                        Log.w("Firestore", "Error getting student roll numbers.", task.getException());
                        Toast.makeText(SubmitCourseQuizActivity.this, "Error fetching student data. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Making Quiz")
                .setMessage("Are you sure you want to quit making the quiz?")
                .setPositiveButton("OK", (dialog, which) -> {
                    ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void proceedToQuizSubmission() {
        String questionWeightage = quizWeightageEditText.getText().toString().trim();
        String dateTime = quizStartTimeEditText.getText().toString().trim();
        String hours = quizDurationHoursEditText.getText().toString().trim();
        String minutes = quizDurationMinutesEditText.getText().toString().trim();

        if (validateInput(questionWeightage, dateTime, hours, minutes)) {
            String quizDuration = hours + " hours, " + minutes + " minutes";
            String quizId = UUID.randomUUID().toString();
            String availableWhen = formatDateTime(dateTime); // Combine date and time

            Task<Void> insertCourseQuizTask = insertCourseQuiz(courseId, quizId, availableWhen, quizDuration, questionWeightage);
            Task<Void> insertQuizQuestionsTask = insertQuizQuestions(quizId);
            Task<Void> insertStudentAttemptedQuestionsTask = insertStudentAttemptedQuestions(courseId, quizId);

            // Combine all tasks and wait for completion
            Task<Void> allTasks = Tasks.whenAll(insertCourseQuizTask, insertQuizQuestionsTask, insertStudentAttemptedQuestionsTask);

            allTasks.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ProgressDialogHelper.dismissProgressDialog();
                    ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
                    Toast.makeText(this, "Quiz submitted successfully!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Error submitting quiz. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("SubmitCourseQuiz", "Error submitting quiz", task.getException());
                }
            });
        } else {
            Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String questionWeightage, String dateTime, String hours, String minutes) {
        if (questionWeightage.isEmpty() || dateTime.isEmpty() || hours.isEmpty() || minutes.isEmpty()) {
            ProgressDialogHelper.dismissProgressDialog();
            return false;
        }
        int intHours = Integer.parseInt(hours);
        int intMinutes = Integer.parseInt(minutes);

        if (intHours > 24 || intMinutes >= 60) {
            Toast.makeText(this, "Hours cannot be greater than 24 and minutes cannot be greater than 60", Toast.LENGTH_SHORT).show();
            ProgressDialogHelper.dismissProgressDialog();
            return false;
        }
        if (intHours == 24 && intMinutes > 0) {
            Toast.makeText(this, "If hours are 24, minutes cannot be greater than 0", Toast.LENGTH_SHORT).show();
            ProgressDialogHelper.dismissProgressDialog();
            return false;
        }
        return true;
    }

    private String formatDateTime(String dateTime) {
        return dateTime.replace(" ", "T") + ":00Z"; // Example transformation to ISO format
    }

    private Task<Void> insertCourseQuiz(String courseId, String quizId, String availableWhen, String quizDuration, String questionWeightage) {
        Map<String, Object> courseQuizData = new HashMap<>();
        courseQuizData.put("CourseRef", courseRef);
        courseQuizData.put("QuizID", quizId);
        courseQuizData.put("CourseID", courseId);
        courseQuizData.put("AvailableWhen", availableWhen);
        courseQuizData.put("QuizDuration", quizDuration);
        courseQuizData.put("CreatedBy", TeacherInstanceModel.getInstance(this).getTeacherUsername());
        courseQuizData.put("QuestionWeightage", questionWeightage);
        courseQuizData.put("StudentRollNumbers", studentRollNoList);

        return db.collection("CourseQuizzes")
                .add(courseQuizData)
                .continueWith(task -> null); // Return a Void task to signal completion
    }

    private Task<Void> insertQuizQuestions(String quizId) {
        List<Task<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < quizQuestionsList.size(); i++) {
            QuizModel quizModel = quizQuestionsList.get(i);

            Map<String, Object> quizData = new HashMap<>();
            quizData.put("QuizID", quizId);
            quizData.put("QuestionNo", "Q" + (i + 1)); // Question number generation
            quizData.put("Question", quizModel.getQuizQuestion());
            quizData.put("OptionsArrayList", Arrays.asList(quizModel.getOptionA(), quizModel.getOptionB(), quizModel.getOptionC(), quizModel.getOptionD()));
            quizData.put("CorrectOption", quizModel.getCorrectOption());

            Task<Void> task = db.collection("Quizzes")
                    .add(quizData)
                    .continueWith(t -> null); // Return a Void task to signal completion

            tasks.add(task);
        }

        return Tasks.whenAll(tasks); // Return a task that completes when all quiz questions are added
    }

    private Task<Void> insertStudentAttemptedQuestions(String courseId, String quizId) {
        List<Task<Void>> tasks = new ArrayList<>();

        for (String studentRollNo : studentRollNoList) {
            Map<String, Object> studentAttemptedData = new HashMap<>();
            studentAttemptedData.put("CourseID", courseId);
            studentAttemptedData.put("QuizID", quizId);
            studentAttemptedData.put("StudentRollNo", studentRollNo);

            // Initialize MapQuestionsAttempted with N/A for each question
            Map<String, String> questionsAttempted = new HashMap<>();
            for (int i = 1; i <= quizQuestionsList.size(); i++) {
                questionsAttempted.put("Q" + i, "N/A");
            }
            studentAttemptedData.put("MapQuestionsAttempted", questionsAttempted);

            Task<Void> task = db.collection("StudentAttemptedQuestions")
                    .add(studentAttemptedData)
                    .continueWith(t -> null); // Return a Void task to signal completion

            tasks.add(task);
        }

        return Tasks.whenAll(tasks); // Return a task that completes when all student attempted questions are added
    }

    private void showDateTimePicker(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String formattedDateTime = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d",
                        year, month + 1, dayOfMonth, hourOfDay, minute);
                editText.setText(formattedDateTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
