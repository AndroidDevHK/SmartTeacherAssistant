package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class SolveQuizActivity extends AppCompatActivity {

    private static final String TAG = "SolveQuizActivity";

    private TextView questionTextView, timerTextView;
    private RadioGroup optionsRadioGroup;
    private RadioButton[] optionRadioButtons = new RadioButton[4];
    private MaterialButton nextButton;

    private List<QuizModel> quizModels;
    private List<StudentAttemptedQuestionModel> attemptedQuestions;
    private int currentQuestionIndex = 0;
    private StudentQuizModel studentQuizModel;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long endTimeMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_quiz);

        initializeViews();
        loadQuizData();
        setupQuizTimer();
        setupNextButtonListener();
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);
        setStudentInfo();
    }
    private void setStudentInfo() {
        TextView courseNameTextView = findViewById(R.id.courseNameTextView);
        TextView studentNameTextView = findViewById(R.id.StudentNameTextView);
        TextView rollNoTextView = findViewById(R.id.RollNoTextView);

        StudentSessionInfo session = StudentSessionInfo.getInstance(this);

        courseNameTextView.setText(session.getCourseName());
        studentNameTextView.setText(session.getStudentName());
        rollNoTextView.setText(session.getStudentRollNo());
    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        optionRadioButtons[0] = findViewById(R.id.option1RadioButton);
        optionRadioButtons[1] = findViewById(R.id.option2RadioButton);
        optionRadioButtons[2] = findViewById(R.id.option3RadioButton);
        optionRadioButtons[3] = findViewById(R.id.option4RadioButton);
        nextButton = findViewById(R.id.nextButton);
        timerTextView = findViewById(R.id.timeRemainingTextView);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);

    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Quiz");
        builder.setMessage("Are you sure you want to quit the quiz?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Toast.makeText(this, "Exit Done", Toast.LENGTH_SHORT).show();
            ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();  // Do nothing, just close the dialog, allowing the user to continue with the activity.
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Solve Quiz", true);
    }
    private void loadQuizData() {
        studentQuizModel = getIntent().getParcelableExtra("studentQuizModel");
        if (studentQuizModel != null) {
            quizModels = studentQuizModel.getQuizModels();
            attemptedQuestions = studentQuizModel.getAttemptedQuestions();
            if (attemptedQuestions == null) {
                attemptedQuestions = new ArrayList<>();
            }
            sortQuizModelsByQuestionNo();
            currentQuestionIndex = findFirstUnattemptedQuestion();
            if (currentQuestionIndex != quizModels.size()) {
                displayQuestion(currentQuestionIndex);
            } else {
                showQuizCompletion();
            }
        } else {
            questionTextView.setText("No quiz data available");
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupQuizTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));  // Set the timezone to Karachi as specified

        try {
            Date startTime = sdf.parse(studentQuizModel.getAvailableWhen());
            long durationMillis = parseDuration(studentQuizModel.getQuizDuration());
            Date endTime = new Date(startTime.getTime() + durationMillis);
            endTimeMillis = endTime.getTime();
            updateTimer();
        } catch (ParseException e) {
            e.printStackTrace();
            timerTextView.setText("Error parsing start time or duration");
        }
    }

    private long parseDuration(String duration) {
        long durationMillis = 0;
        String[] parts = duration.split(", ");
        for (String part : parts) {
            if (part.contains("hours")) {
                int hours = Integer.parseInt(part.replaceAll("[^0-9]", ""));
                durationMillis += hours * 3600000;  // hours to milliseconds
            } else if (part.contains("minutes")) {
                int minutes = Integer.parseInt(part.replaceAll("[^0-9]", ""));
                durationMillis += minutes * 60000;  // minutes to milliseconds
            }
        }
        return durationMillis;
    }


    private void updateTimer() {
        timerRunnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                long millisUntilFinished = endTimeMillis - System.currentTimeMillis();
                if (millisUntilFinished > 0) {
                    updateTimerTextView(millisUntilFinished);
                    timerHandler.postDelayed(this, 1000);
                } else {
                    timerTextView.setText("Time's up!");
                    showTimeOutDialog();
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void updateTimerTextView(long millisUntilFinished) {
        long seconds = (millisUntilFinished / 1000) % 60;
        long minutes = (millisUntilFinished / (1000 * 60)) % 60;
        long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
        timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void showTimeOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Time Up");
        builder.setMessage("The quiz time has expired.");
        builder.setPositiveButton("OK", (dialog, which) -> finish());
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }


    private void setupNextButtonListener() {
        nextButton.setOnClickListener(v -> handleNextButtonPress());
    }

    private void handleNextButtonPress() {
        int checkedId = optionsRadioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton selectedOptionButton = findViewById(checkedId);
            handleQuizSubmission(selectedOptionButton.getText().toString());
        } else {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleQuizSubmission(String selectedOption) {
        String optionPrefix = selectedOption.substring(0, 1);  // Extracts the letter (e.g., "A")

        QuizModel currentQuiz = quizModels.get(currentQuestionIndex);
        StudentAttemptedQuestionModel attemptedQuestion = new StudentAttemptedQuestionModel(currentQuiz.getQuestionNo(), optionPrefix);

        updateAttemptedQuestions(attemptedQuestion);
        nextButton.setEnabled(false);
        nextButton.setText("Submitting...");

        // Update status in database
        updateAttemptedQuestionInDatabase(attemptedQuestion);
    }

    private void displayQuestion(int questionIndex) {
        QuizModel quizModel = quizModels.get(questionIndex);
        questionTextView.setText(quizModel.getQuizQuestion());
        optionRadioButtons[0].setText("A. " + quizModel.getOptionA());
        optionRadioButtons[1].setText("B. " + quizModel.getOptionB());
        optionRadioButtons[2].setText("C. " + quizModel.getOptionC());
        optionRadioButtons[3].setText("D. " + quizModel.getOptionD());
        optionsRadioGroup.clearCheck();
    }


    private int findFirstUnattemptedQuestion() {
        for (int i = 0; i < quizModels.size(); i++) {
            if (!isQuestionAttempted(quizModels.get(i).getQuestionNo())) {
                return i;
            }
        }
        return quizModels.size();
    }

    private boolean isQuestionAttempted(String questionNo) {
        for (StudentAttemptedQuestionModel attempted : attemptedQuestions) {
            if (attempted.getQuestionId().equals(questionNo) && !"N/A".equals(attempted.getSelectedOption())) {
                return true;
            }
        }
        return false;
    }

    private void updateAttemptedQuestions(StudentAttemptedQuestionModel attemptedQuestion) {
        attemptedQuestions.removeIf(q -> q.getQuestionId().equals(attemptedQuestion.getQuestionId()));
        attemptedQuestions.add(attemptedQuestion);
    }

    private void updateAttemptedQuestionInDatabase(StudentAttemptedQuestionModel attemptedQuestion) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String quizID = studentQuizModel.getQuizId();
        String studentRollNo = StudentSessionInfo.getInstance(this).getStudentRollNo();

        db.collection("StudentAttemptedQuestions")
                .whereEqualTo("CourseID", studentQuizModel.getCourseId())
                .whereEqualTo("QuizID", quizID)
                .whereEqualTo("StudentRollNo", studentRollNo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("MapQuestionsAttempted." + attemptedQuestion.getQuestionId(), attemptedQuestion.getSelectedOption());
                            db.collection("StudentAttemptedQuestions").document(documentId).update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Successfully updated attempted question in database: " + attemptedQuestion.getQuestionId());
                                        moveNextOrCompleteQuiz();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating attempted question in database: " + e.getMessage());
                                        nextButton.setEnabled(true);
                                        nextButton.setText("Next");
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error querying StudentAttemptedQuestions collection.", task.getException());
                        nextButton.setEnabled(true);
                        nextButton.setText("Next");
                    }
                });
    }

    private void moveNextOrCompleteQuiz() {
        currentQuestionIndex = findFirstUnattemptedQuestion();
        if (currentQuestionIndex < quizModels.size()) {
            displayQuestion(currentQuestionIndex);
            nextButton.setEnabled(true);
            nextButton.setText("Next");
        } else {
            showQuizCompletion();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showQuizCompletion() {
        // Update the UI to indicate quiz completion
        questionTextView.setText("You have completed the quiz!");
        optionsRadioGroup.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        // Create and display an AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Quiz Completed")
                .setMessage("Congratulations! You have completed the quiz.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Dismiss the dialog and take any necessary action
                    dialog.dismiss();
                    ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void sortQuizModelsByQuestionNo() {
        Collections.sort(quizModels, Comparator.comparingInt(q -> Integer.parseInt(q.getQuestionNo().substring(1))));
    }
}
