package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DisplayStudentsQuizQuestionsResponsesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewQuizResponses;
    private QuizResponseAdapter quizResponseAdapter;
    private TextView studentNameTextView;
    private TextView studentRollNoTextView;
    private TextView attemptedQuestionTextView;
    private TextView marksTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_students_quiz_questions_responses);

        initializeViews();
        setupRecyclerView();

        if (getIntent() != null) {
            ArrayList<QuizModel> quizQuestions = getIntent().getParcelableArrayListExtra("quizQuestions");
            QuizStudentAttemptedQuestionsModel studentResponses = getIntent().getParcelableExtra("studentResponses");

            int attemptedQuestions = getIntent().getIntExtra("attemptedQuestions", 0);
            int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
            String formattedTotalMarks = getIntent().getStringExtra("formattedTotalMarks");
            String formattedObtainedMarks = getIntent().getStringExtra("formattedObtainedMarks");

            populateCardView(studentResponses, attemptedQuestions, totalQuestions, formattedTotalMarks, formattedObtainedMarks);
            setupAdapter(quizQuestions, studentResponses);
        }
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Student Quiz Result", true);
    }
    private void initializeViews() {
        studentNameTextView = findViewById(R.id.StudentNameTextView);
        studentRollNoTextView = findViewById(R.id.StudentRollNoStdTextView);
        attemptedQuestionTextView = findViewById(R.id.AttemptedQuestionStdTextView);
        marksTextView = findViewById(R.id.MarksStdTextView);
    }

    private void setupRecyclerView() {
        recyclerViewQuizResponses = findViewById(R.id.recyclerViewQuizResponses);
        recyclerViewQuizResponses.setLayoutManager(new LinearLayoutManager(this));
    }

    @SuppressLint("SetTextI18n")
    private void populateCardView(QuizStudentAttemptedQuestionsModel studentResponses, int attemptedQuestions, int totalQuestions, String formattedTotalMarks, String formattedObtainedMarks) {
        studentNameTextView.setText(studentResponses.getStudentName());
        studentRollNoTextView.setText(studentResponses.getStudentRollNo());
        attemptedQuestionTextView.setText(attemptedQuestions + "/" + totalQuestions);
        marksTextView.setText(formattedObtainedMarks + "/" + formattedTotalMarks);
    }

    private void setupAdapter(ArrayList<QuizModel> quizQuestions, QuizStudentAttemptedQuestionsModel studentResponses) {
        quizResponseAdapter = new QuizResponseAdapter(this, quizQuestions, studentResponses);
        recyclerViewQuizResponses.setAdapter(quizResponseAdapter);
    }
}
