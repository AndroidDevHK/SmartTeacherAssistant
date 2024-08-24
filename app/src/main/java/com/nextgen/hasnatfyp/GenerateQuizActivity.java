package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class GenerateQuizActivity extends AppCompatActivity {

    private TextView CourseNameTextView, quizQuestions, TotalStdTextView, submittedQuestions;
    private LinearLayout containerQuestions;
    private List<QuizModel> questionList = new ArrayList<>();
    private int submittedQ = 0;
    private int totalQuestions = 0; // Total number of questions specified in intent
    private View currentQuestionView; // To keep track of the current question view
    private String courseId;
    private String classId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_quiz);

        quizQuestions = findViewById(R.id.quizQuestions);
        submittedQuestions = findViewById(R.id.SQuestions);
        CourseNameTextView = findViewById(R.id.CourseNameTextView);
        TotalStdTextView = findViewById(R.id.TotalStdTextView);

        containerQuestions = findViewById(R.id.containerQuestions);

        Intent intent = getIntent();
        courseId = intent.getStringExtra("CourseID");
        classId = intent.getStringExtra("ClassID");

        // Populate views with data
        populateViewsFromIntent();

        // Initially add one empty question container
        addNewQuestion(null); // Passing null as View parameter

        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);


    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Make Quiz Questions", true);
    }
    private void populateViewsFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String questions = intent.getStringExtra("QUIZ_QUESTIONS");
            setTextViewText(quizQuestions, questions);
            setTextViewText(submittedQuestions, String.valueOf(submittedQ));
            setTextViewText(CourseNameTextView, TeacherInstanceModel.getInstance(this).getCourseName());
            setTextViewText(TotalStdTextView, "N/A");
            try {
                totalQuestions = Integer.parseInt(questions);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Making Quiz")
                .setMessage("Are you sure you want to quit making the quiz?")
                .setPositiveButton("OK", (dialog, which) -> {
                    // User confirmed to quit, call super to handle the back press
                    ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void setTextViewText(TextView textView, String text) {
        if (textView != null && text != null) {
            textView.setText(text);
        }
    }

    public void addNewQuestion(View view) {
        Log.d("GenerateQuizActivity", "addNewQuestion() called");

        // Remove the current question view from the container if exists
        if (currentQuestionView != null) {
            containerQuestions.removeView(currentQuestionView);
        }

        // Inflate the layout for the new question view and add it to the container
        LayoutInflater inflater = LayoutInflater.from(this);
        currentQuestionView = inflater.inflate(R.layout.question_item, containerQuestions, false);
        containerQuestions.addView(currentQuestionView);

        // Get references to views in the current question view
        EditText editTextQuestion = currentQuestionView.findViewById(R.id.editTextQuestion);
        EditText editTextOptionA = currentQuestionView.findViewById(R.id.editTextOption1);
        EditText editTextOptionB = currentQuestionView.findViewById(R.id.editTextOption2);
        EditText editTextOptionC = currentQuestionView.findViewById(R.id.editTextOption3);
        EditText editTextOptionD = currentQuestionView.findViewById(R.id.editTextOption4);
        RadioGroup radioGroupOptions = currentQuestionView.findViewById(R.id.radioGroupOptions);
        MaterialButton btnAddQuestion = currentQuestionView.findViewById(R.id.btnAddQuestion);

        // Set click listener for the "Add Question" button
        btnAddQuestion.setOnClickListener(v -> {
            // Get user input from EditText fields
            String questionText = editTextQuestion.getText().toString().trim();
            String optionA = editTextOptionA.getText().toString().trim();
            String optionB = editTextOptionB.getText().toString().trim();
            String optionC = editTextOptionC.getText().toString().trim();
            String optionD = editTextOptionD.getText().toString().trim();

            // Get the ID of the selected RadioButton from RadioGroup
            int selectedRadioButtonId = radioGroupOptions.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = currentQuestionView.findViewById(selectedRadioButtonId);
            String correctOption = selectedRadioButton != null ? selectedRadioButton.getText().toString().trim() : "";

            // Validate input fields
            if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty() || correctOption.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields and select correct option", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("GenerateQuizActivity", "Question Text: " + questionText);
            Log.d("GenerateQuizActivity", "Option A: " + optionA);
            Log.d("GenerateQuizActivity", "Option B: " + optionB);
            Log.d("GenerateQuizActivity", "Option C: " + optionC);
            Log.d("GenerateQuizActivity", "Option D: " + optionD);
            Log.d("GenerateQuizActivity", "Correct Option: " + correctOption);

            String questionNo = "Q" + (submittedQ + 1);

            // Create QuizModel object and add to questionList
            QuizModel quizModel = new QuizModel(questionNo, questionText, optionA, optionB, optionC, optionD, correctOption);
            questionList.add(quizModel);
            // Increment submitted question count and update UI
            submittedQ++;
            submittedQuestions.setText(String.valueOf(submittedQ));

            // Show success message
            Toast.makeText(this, "Question " + submittedQ + " added successfully", Toast.LENGTH_SHORT).show();
            Log.d("GenerateQuizActivity", "Question " + submittedQ + " added successfully");

            // Clear input fields
            editTextQuestion.getText().clear();
            editTextOptionA.getText().clear();
            editTextOptionB.getText().clear();
            editTextOptionC.getText().clear();
            editTextOptionD.getText().clear();
            radioGroupOptions.clearCheck();

            // Check if all questions are added, proceed to next activity if true
            if (submittedQ >= totalQuestions) {
                showOptionsDialog();
            }
        });
    }




    private void showOptionsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("All Questions Submitted")
                .setMessage("You have submitted all questions. What would you like to do next?")
                .setPositiveButton("Review Questions", (dialog, which) -> {
                    proceedToNextActivity();
                })
                .setNegativeButton("Submit Quiz", (dialog, which) -> {
                    // Handle quiz submission here
                    submitQuiz();
                })
                .setCancelable(false)
                .show();
    }

    private void proceedToNextActivity() {
        // Create intent to start the next activity
        Intent intent = new Intent(this, DisplayQuizQuestionsListActivity.class);

        // Pass the list of quiz questions to the next activity
        intent.putParcelableArrayListExtra("QUIZ_QUESTIONS_LIST", new ArrayList<>(questionList));
        intent.putExtra("CourseID", courseId);
        intent.putExtra("ClassID", classId);

        // Start the activity
        startActivity(intent);
    }

    private void submitQuiz() {
        // Create intent to start the next activity
        Intent intent = new Intent(this, SubmitCourseQuizActivity.class);

        // Pass the list of quiz questions to the next activity
        intent.putParcelableArrayListExtra("QUIZ_QUESTIONS_LIST", new ArrayList<>(questionList));
        intent.putExtra("CourseID", courseId);
        intent.putExtra("ClassID", classId);

        // Start the activity
        startActivity(intent);
    }
}
