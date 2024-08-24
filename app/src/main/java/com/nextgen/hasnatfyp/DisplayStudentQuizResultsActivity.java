package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class DisplayStudentQuizResultsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StudentQuizResultAdapter adapter;

    private TextView courseNameTextView, studentNameTextView, rollNoTextView, questionResultsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_quiz_results);

        recyclerView = findViewById(R.id.recyclerViewStudentQuizResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionResultsTextView = findViewById(R.id.QuestionResults);

        // Retrieve data from intent
        StudentQuizModel studentQuizModel = getIntent().getParcelableExtra("StudentQuizModel");
        List<QuizModel> quizModels = studentQuizModel.getQuizModels();
        List<StudentAttemptedQuestionModel> attemptedQuestions = studentQuizModel.getAttemptedQuestions();

        adapter = new StudentQuizResultAdapter(this, quizModels, attemptedQuestions);
        recyclerView.setAdapter(adapter);


        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        initializeViews(quizModels,attemptedQuestions,studentQuizModel);
    }
    private void initializeViews(List<QuizModel> quizModels, List<StudentAttemptedQuestionModel> attemptedQuestions, StudentQuizModel studentQuizModel) {
        courseNameTextView = findViewById(R.id.courseNameTextView);
        studentNameTextView = findViewById(R.id.StudentNameTextView);
        rollNoTextView = findViewById(R.id.RollNoTextView);
        questionResultsTextView = findViewById(R.id.QuestionResults);

        // Setting default texts (These could be dynamic based on passed data)
        courseNameTextView.setText(studentQuizModel.getCourseName());
        studentNameTextView.setText(StudentSessionInfo.getInstance(this).getStudentName());
        rollNoTextView.setText(StudentSessionInfo.getInstance(this).getStudentRollNo());
        updateResultsDisplay(quizModels, attemptedQuestions);

    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "My Quiz Result", true);
    }
    private void updateResultsDisplay(List<QuizModel> quizModels, List<StudentAttemptedQuestionModel> attemptedQuestions) {
        int totalQuestions = quizModels.size();
        int correctAnswers = 0;
        int incorrectAnswers = 0;

        for (int i = 0; i < quizModels.size(); i++) {
            QuizModel quiz = quizModels.get(i);
            String correctOption = quiz.getCorrectOption();
            String studentAnswer = attemptedQuestions.get(i).getSelectedOption();

            if (studentAnswer.equals(correctOption)) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
        }

        String resultsText = totalQuestions + " | " + correctAnswers + " | " + incorrectAnswers;
        questionResultsTextView.setText(resultsText);
    }
}
