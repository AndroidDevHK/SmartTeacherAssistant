package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class MakeCourseQuizActivity extends AppCompatActivity {

    private TextInputEditText quizQuestions;
    private Button makeQuizButton;
    private TextView CourseNameTextView;
    private TextView studentCountTextView;
    private String courseId;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_course_quiz);
        initializeViews();
        setOnClickListeners();
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        populateCardView();

        Intent intent = getIntent();
        courseId = intent.getStringExtra("CourseID");
        classId = intent.getStringExtra("ClassID");
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Make Quiz", true);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Quiz Making")
                .setMessage("Are you sure you want to quit the quiz-making process?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void initializeViews() {
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);
        quizQuestions = findViewById(R.id.quizQuestions);
        makeQuizButton = findViewById(R.id.makeQuizButton);
        studentCountTextView = findViewById(R.id.TotalStdTextView);
        CourseNameTextView = findViewById(R.id.CourseNameTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }

    @SuppressLint("SetTextI18n")
    private void populateCardView() {
        studentCountTextView.setText("N/A");
        CourseNameTextView.setText(TeacherInstanceModel.getInstance(this).getCourseName());
    }

    private void setOnClickListeners() {
        makeQuizButton.setOnClickListener(v -> handleMakeQuizButtonClick());
    }

    private void handleMakeQuizButtonClick() {
        if (validateInputs()) {
            Intent intent = new Intent(MakeCourseQuizActivity.this, GenerateQuizActivity.class);
            intent.putExtra("QUIZ_QUESTIONS", getTextInput(quizQuestions));
            intent.putExtra("CourseID", courseId);
            intent.putExtra("ClassID", classId);
            startActivity(intent);
        }
    }

    private String getTextInput(TextInputEditText inputField) {
        return inputField.getText().toString().trim();
    }

    private boolean validateInputs() {
        return isFieldValid(quizQuestions, "Quiz Questions are required") && isQuestionCountValid();
    }

    private boolean isFieldValid(TextInputEditText field, String errorMessage) {
        if (TextUtils.isEmpty(field.getText())) {
            field.setError(errorMessage);
            return false;
        }
        return true;
    }

    private boolean isQuestionCountValid() {
        String questionCountStr = getTextInput(quizQuestions);
        if (!TextUtils.isEmpty(questionCountStr)) {
            int questionCount = Integer.parseInt(questionCountStr);
            if (questionCount > 100) {
                quizQuestions.setError("Quiz Questions cannot be more than 100");
                return false;
            }
        }
        return true;
    }
}
