package com.nextgen.hasnatfyp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DisplayQuizQuestionsListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewQuestions;
    private QuizQuestionsAdapter quizAdapter;
    private List<QuizModel> questionList;

    private Button btnProceed;
    private String courseId;
    private String classId;
    private FirebaseFirestore db;
    private List<String> studentRollNoList;
    // Initialize the document reference to the course

    private DocumentReference courseRef;
    private TextView TotalStdTextView;
    private TextView CourseNameTextView;
    private TextView quizQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_quiz_questions_list);

        db = FirebaseFirestore.getInstance();


        studentRollNoList = new ArrayList<>();
        Intent intent = getIntent();

        if (intent != null) {
            questionList = intent.getParcelableArrayListExtra("QUIZ_QUESTIONS_LIST");
            courseId = intent.getStringExtra("CourseID");
            classId = intent.getStringExtra("ClassID");
        }
        if (questionList == null) {
            questionList = generateComputerScienceQuizQuestions();
        }
        if (courseId!=null) {
            courseRef = db.collection("ClassCourses")
                    .document(classId)
                    .collection("ClassCoursesSubcollection")
                    .document(courseId);
        }
        Log.d("MYAPP", courseId);
        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        TotalStdTextView = findViewById(R.id.TotalStdTextView);
        CourseNameTextView = findViewById(R.id.CourseNameTextView);
        quizQuestions = findViewById(R.id.quizQuestions);

        // Create and set the adapter
        quizAdapter = new QuizQuestionsAdapter(questionList);
        recyclerViewQuestions.setAdapter(quizAdapter);

        // Initialize Proceed button
        btnProceed = findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(v -> {
            // Handle Proceed button click here
           StartSubmitQuizActivity();
        });
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        populateCardView();
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);

    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void StartSubmitQuizActivity() {
        Intent intent = new Intent(this, SubmitCourseQuizActivity.class);

        // Pass the list of quiz questions to the next activity
        intent.putParcelableArrayListExtra("QUIZ_QUESTIONS_LIST", new ArrayList<>(questionList));
        intent.putExtra("CourseID", courseId);
        intent.putExtra("ClassID", classId);

        // Start the activity
        startActivity(intent);
    }
    @SuppressLint("MissingSuperCall")
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
    private void populateCardView() {
        TotalStdTextView.setText("N/A");
        CourseNameTextView.setText(TeacherInstanceModel.getInstance(this).getCourseName());
        quizQuestions.setText(String.valueOf(questionList.size()));
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Review Quiz Questions", true);
    }

    private List<QuizModel> generateComputerScienceQuizQuestions() {
        List<QuizModel> computerScienceQuestions = new ArrayList<>();

        // Question 1
        String question1 = "What does CPU stand for?";
        String optionA1 = "Central Processing Unit";
        String optionB1 = "Central Protocol Unit";
        String optionC1 = "Computer Personal Unit";
        String optionD1 = "Central Processor Unit";
        String correctOption1 = "A";
        QuizModel quizModel1 = new QuizModel("Q1", question1, optionA1, optionB1, optionC1, optionD1, correctOption1);
        computerScienceQuestions.add(quizModel1);

        // Question 2
        String question2 = "Which programming language is often used for Android app development?";
        String optionA2 = "Java";
        String optionB2 = "Swift";
        String optionC2 = "Python";
        String optionD2 = "C++";
        String correctOption2 = "A";
        QuizModel quizModel2 = new QuizModel("Q2", question2, optionA2, optionB2, optionC2, optionD2, correctOption2);
        computerScienceQuestions.add(quizModel2);

        // Question 3
        String question3 = "What is the full form of HTML?";
        String optionA3 = "Hyper Trainer Marking Language";
        String optionB3 = "Hyper Text Marketing Language";
        String optionC3 = "Hyper Text Markup Language";
        String optionD3 = "None of the above";
        String correctOption3 = "C";
        QuizModel quizModel3 = new QuizModel("Q3", question3, optionA3, optionB3, optionC3, optionD3, correctOption3);
        computerScienceQuestions.add(quizModel3);

        // Question 4
        String question4 = "Which data structure organizes data in a Last In First Out (LIFO) manner?";
        String optionA4 = "Queue";
        String optionB4 = "Stack";
        String optionC4 = "Heap";
        String optionD4 = "Tree";
        String correctOption4 = "B";
        QuizModel quizModel4 = new QuizModel("Q4", question4, optionA4, optionB4, optionC4, optionD4, correctOption4);
        computerScienceQuestions.add(quizModel4);

        // Question 5
        String question5 = "What does HTTP stand for?";
        String optionA5 = "HyperText Transfer Protocol";
        String optionB5 = "HyperText Type Protocol";
        String optionC5 = "Hyper Transfer Type Protocol";
        String optionD5 = "None of the above";
        String correctOption5 = "A";
        QuizModel quizModel5 = new QuizModel("Q5", question5, optionA5, optionB5, optionC5, optionD5, correctOption5);
        computerScienceQuestions.add(quizModel5);

        return computerScienceQuestions;
    }

}
