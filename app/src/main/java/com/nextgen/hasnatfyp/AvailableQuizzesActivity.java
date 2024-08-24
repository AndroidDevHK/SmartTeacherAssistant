package com.nextgen.hasnatfyp;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Teacher_Semester_Classes_Acitivity.TeacherClassModel;

public class AvailableQuizzesActivity extends AppCompatActivity {

    private static final String TAG = "AvailableQuizzesActivity";
    private RecyclerView recyclerViewAvailableQuizzes;
    private QuizAdapter quizAdapter;
    private List<StudentQuizModel> availableQuizzesList;
    private FirebaseFirestore db;
    private String studentRollNo;
    private TextView StudentNameTextView;
    private TextView StudentRollNoTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_quizzes);

        ProgressDialogHelper.showProgressDialog(this,"Loading Quizzes");
        // Assume you get the student's roll number from the login session or intent
        studentRollNo = StudentSessionInfo.getInstance(this).getStudentRollNo();

        db = FirebaseFirestore.getInstance();
        availableQuizzesList = new ArrayList<>();

        recyclerViewAvailableQuizzes = findViewById(R.id.recyclerViewAvailableQuizzes);
        recyclerViewAvailableQuizzes.setLayoutManager(new LinearLayoutManager(this));
        StudentNameTextView = findViewById(R.id.StudentNameTextView);
        StudentRollNoTextView = findViewById(R.id.RollNoTextView);
        // Create and set the adapter
        quizAdapter = new QuizAdapter(availableQuizzesList);
        recyclerViewAvailableQuizzes.setAdapter(quizAdapter);

        Log.d(TAG, "onCreate: Fetching available quizzes");
        fetchAvailableQuizzes();
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        SetStudentNameAndStudentRollNo();
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);

    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetStudentNameAndStudentRollNo() {

        StudentNameTextView.setText(StudentSessionInfo.getInstance(this).getStudentName());
        StudentRollNoTextView.setText(StudentSessionInfo.getInstance(this).getStudentRollNo());
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Pending Quizzes", true);
    }
    private void fetchAvailableQuizzes() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));
        String currentDateTime = sdf.format(new Date());

        Log.d(TAG, "fetchAvailableQuizzes: Current date and time in PKT: " + currentDateTime);

        // Fetch quizzes from Firestore
        db.collection("CourseQuizzes")
                .whereArrayContains("StudentRollNumbers", studentRollNo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<StudentQuizModel> allQuizzes = new ArrayList<>();
                        AtomicInteger counter = new AtomicInteger(0); // Counter to manage asynchronous tasks
                        int totalDocuments = task.getResult().size(); // Total documents fetched

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference courseRef = document.getDocumentReference("CourseRef");

                            courseRef.get().addOnSuccessListener(courseDoc -> {
                                String courseName = courseDoc.getString("CourseName");
                                String quizId = document.getString("QuizID");
                                String CourseID = document.getString("CourseID");
                                String availableWhen = document.getString("AvailableWhen");
                                String quizDuration = document.getString("QuizDuration");
                                String questionWeightage = document.getString("QuestionWeightage");

                                StudentQuizModel quiz = new StudentQuizModel(quizId, courseName, availableWhen, quizDuration, questionWeightage, new ArrayList<>(), new ArrayList<>()); // Pass courseName instead of courseId
                                quiz.setCourseId(CourseID);
                                allQuizzes.add(quiz);

                                // Check if all documents have been processed
                                if (counter.incrementAndGet() == totalDocuments) {
                                    // Filter quizzes by availability time and update UI
                                    List<StudentQuizModel> availableQuizzes = filterQuizzesByTime(allQuizzes, currentDateTime);
                                    availableQuizzesList.clear();
                                    availableQuizzesList.addAll(availableQuizzes);
                                    if (availableQuizzesList.isEmpty()) {
                                        showNoQuizzesDialog();
                                    } else {
                                        fetchQuizDetailsForAvailableQuizzes(); // Fetch quiz questions and attempted questions
                                    }                                }
                            }).addOnFailureListener(e -> {
                            });
                        }
                    } else {
                        Log.w(TAG, "Error getting available quizzes.", task.getException());
                    }
                });
    }

    private void showNoQuizzesDialog() {
        ProgressDialogHelper.dismissProgressDialog();
        new AlertDialog.Builder(this)
                .setTitle("No Quizzes Available")
                .setMessage("There are currently no quizzes available for you.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }



    private List<StudentQuizModel> filterQuizzesByTime(List<StudentQuizModel> allQuizzes, String currentDateTime) {
        List<StudentQuizModel> availableQuizzes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));
        Date currentDate;
        try {
            currentDate = sdf.parse(currentDateTime);
            Log.d(TAG, "filterQuizzesByTime: Parsed current date and time: " + currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "filterQuizzesByTime: Error parsing current date time", e);
            return availableQuizzes;
        }

        for (StudentQuizModel quiz : allQuizzes) {
            try {
                Date quizAvailableDate = sdf.parse(quiz.getAvailableWhen());
                long durationMillis = parseDuration(quiz.getQuizDuration());
                Date quizEndDate = new Date(quizAvailableDate.getTime() + durationMillis);

                Log.d(TAG, "filterQuizzesByTime: Quiz ID: " + quiz.getQuizId() + ", Available Date: " + quizAvailableDate + ", End Date: " + quizEndDate);
                Log.d(TAG, "filterQuizzesByTime: Current Date: " + currentDate);

                // Adjusted logic: include quizzes available in the future
                if (quizAvailableDate != null && quizEndDate.after(currentDate)) {
                    availableQuizzes.add(quiz);
                    Log.d(TAG, "filterQuizzesByTime: Quiz ID: " + quiz.getQuizId() + " is available");
                } else {
                    Log.d(TAG, "filterQuizzesByTime: Quiz ID: " + quiz.getQuizId() + " is not available");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(TAG, "filterQuizzesByTime: Error parsing quiz available date", e);
            }
        }
        return availableQuizzes;
    }

    private long parseDuration(String duration) {
        // Example: "1 hours, 10 minutes" or "0 hours, 10 minutes"
        long durationMillis = 0;

        String[] parts = duration.split(",");
        for (String part : parts) {
            part = part.trim(); // Remove leading/trailing spaces
            if (part.contains("hours")) {
                int hours = Integer.parseInt(part.split(" ")[0].trim());
                durationMillis += hours * 60 * 60 * 1000; // Convert hours to milliseconds
            } else if (part.contains("minutes")) {
                int minutes = Integer.parseInt(part.split(" ")[0].trim());
                durationMillis += minutes * 60 * 1000; // Convert minutes to milliseconds
            }
        }

        Log.d(TAG, "parseDuration: Parsed duration to " + durationMillis + " milliseconds");
        return durationMillis;
    }

    private void fetchQuizDetailsForAvailableQuizzes() {
        // Track number of quizzes processed
        AtomicInteger quizzesProcessed = new AtomicInteger(0);

        for (StudentQuizModel quiz : availableQuizzesList) {
            // Fetch quiz questions and correct options
            fetchQuizQuestions(quiz.getQuizId(), quiz, () -> {
                // After fetching quiz details, increment processed count
                int count = quizzesProcessed.incrementAndGet();

                // If all quizzes are processed, log the list and notify adapter
                if (count == availableQuizzesList.size()) {
                    ProgressDialogHelper.dismissProgressDialog();
                    Log.d(TAG, "fetchQuizDetailsForAvailableQuizzes: Entire StudentQuizModel list: " + availableQuizzesList);
                    quizAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void fetchQuizQuestions(String quizId, StudentQuizModel studentQuizModel, Runnable onComplete) {
        db.collection("Quizzes")
                .whereEqualTo("QuizID", quizId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuizModel> quizQuestions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String questionNo = document.getString("QuestionNo"); // Fetch QuestionNo
                            String question = document.getString("Question");
                            String CourseID = document.getString("CourseID");

                            List<String> options = (List<String>) document.get("OptionsArrayList");
                            String correctOption = document.getString("CorrectOption");

                            // Assuming options are always four in your document structure
                            if (options != null && options.size() == 4) {
                                QuizModel quizModel = new QuizModel(questionNo, question, options.get(0), options.get(1), options.get(2), options.get(3), correctOption);
                                quizQuestions.add(quizModel);
                            } else {
                                Log.w(TAG, "fetchQuizQuestions: Quiz options array size is not as expected for Quiz ID: " + quizId);
                            }
                        }

                        // Sort quiz questions by QuestionNo
                        Collections.sort(quizQuestions, new Comparator<QuizModel>() {
                            @Override
                            public int compare(QuizModel q1, QuizModel q2) {
                                // Assuming QuestionNo is a string like "Q1", "Q2", etc. Use substring(1) to sort numerically
                                int q1No = Integer.parseInt(q1.getQuestionNo().substring(1));
                                int q2No = Integer.parseInt(q2.getQuestionNo().substring(1));
                                return Integer.compare(q1No, q2No);
                            }
                        });

                        // Set the quiz questions and correct options to the studentQuizModel
                        studentQuizModel.setQuizModels(quizQuestions);

                        // Now fetch student attempted questions
                        fetchStudentAttemptedQuestions(studentQuizModel.getCourseName(), studentQuizModel.getQuizId(), studentRollNo, studentQuizModel, onComplete);
                    } else {
                        Log.w(TAG, "Error getting quiz questions for Quiz ID: " + quizId, task.getException());
                        onComplete.run(); // Still complete the operation to move to next quiz
                    }
                });
    }

    private void fetchStudentAttemptedQuestions(String courseId, String quizId, String studentRollNo, StudentQuizModel studentQuizModel, Runnable onComplete) {
        db.collection("StudentAttemptedQuestions")
                .whereEqualTo("QuizID", quizId)
                .whereEqualTo("StudentRollNo", studentRollNo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve the MapQuestionsAttempted field
                            // Assuming you have a Map<String, String> in your model for attempted questions
                            @SuppressWarnings("unchecked")
                            Map<String, String> attemptedQuestions = (Map<String, String>) document.get("MapQuestionsAttempted");

                            // Convert this map into your StudentAttemptedQuestionModel list or use it directly
                            List<StudentAttemptedQuestionModel> attemptedQuestionModels = new ArrayList<>();
                            for (Map.Entry<String, String> entry : attemptedQuestions.entrySet()) {
                                StudentAttemptedQuestionModel attemptedQuestionModel = new StudentAttemptedQuestionModel(entry.getKey(), entry.getValue());
                                attemptedQuestionModels.add(attemptedQuestionModel);
                            }

                            // Sort attempted questions by question ID alphabetically
                            Collections.sort(attemptedQuestionModels, new Comparator<StudentAttemptedQuestionModel>() {
                                @Override
                                public int compare(StudentAttemptedQuestionModel a1, StudentAttemptedQuestionModel a2) {
                                    return a1.getQuestionId().compareTo(a2.getQuestionId());
                                }
                            });

                            // Set attempted questions to the studentQuizModel
                            studentQuizModel.setAttemptedQuestions(attemptedQuestionModels);

                            Log.d(TAG, "fetchStudentAttemptedQuestions: Retrieved attempted questions for Quiz ID: " + quizId
                                    + " and Student Roll No: " + studentRollNo);
                        }
                    } else {
                        Log.w(TAG, "Error getting attempted questions for Quiz ID: " + quizId
                                + " and Student Roll No: " + studentRollNo, task.getException());
                    }

                    onComplete.run(); // Complete the operation after attempting to fetch student questions
                });
    }
}
