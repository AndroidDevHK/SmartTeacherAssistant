package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

public class DisplayTeacherCourseQuizzesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTeacherQuizzes;
    private TeacherQuizAdapter quizAdapter;
    private List<TeacherCourseQuizModel> quizList;
    private FirebaseFirestore db;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_teacher_course_quizzes);

        ProgressDialogHelper.showProgressDialog(this,"Loading Quizzes..");
        teacherId = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        db = FirebaseFirestore.getInstance();
        quizList = new ArrayList<>();

        recyclerViewTeacherQuizzes = findViewById(R.id.recyclerViewTeacherQuizzes);
        recyclerViewTeacherQuizzes.setLayoutManager(new LinearLayoutManager(this));

        fetchTeacherQuizzes();
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);

        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);

    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Quizzes List", true);
    }

    private void fetchTeacherQuizzes() {
        db.collection("CourseQuizzes")
                .whereEqualTo("CreatedBy", teacherId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> tasks = new ArrayList<>();
                        quizList.clear();
                        QuerySnapshot result = task.getResult();

                        if (result.isEmpty()) {
                            ProgressDialogHelper.dismissProgressDialog();
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "No quizzes found for this teacher", Toast.LENGTH_SHORT).show());
                            return; // Stop further execution
                        }

                        AtomicInteger endedQuizCount = new AtomicInteger(0);
                        for (QueryDocumentSnapshot document : result) {
                            DocumentReference courseRef = document.getDocumentReference("CourseRef");
                            String quizId = document.getString("QuizID");
                            String availableWhen = document.getString("AvailableWhen");
                            String quizDuration = document.getString("QuizDuration");
                            String courseID = document.getString("CourseID");

                            String questionWeightage = document.getString("QuestionWeightage");
                            List<String> studentRollNumbers = new ArrayList<>((List<String>) document.get("StudentRollNumbers"));
                            Boolean isSubmitted = document.getBoolean("IsSubmitted");
                            isSubmitted = (isSubmitted != null) ? isSubmitted : false; // Default to false if not set

                            Collections.sort(studentRollNumbers);

                            // Check if the quiz has ended
                            if (hasQuizEnded(availableWhen, quizDuration)) {
                                endedQuizCount.incrementAndGet();
                                // Fetch course name asynchronously and continue processing
                                Boolean finalIsSubmitted = isSubmitted;
                                courseRef.get().addOnSuccessListener(courseDoc -> {
                                    String courseName = courseDoc.getString("CourseName");

                                    // Create a quiz model with the course name and add to the list
                                    TeacherCourseQuizModel quizModel = new TeacherCourseQuizModel(
                                            courseID,
                                            courseName,
                                            quizId,
                                            availableWhen,
                                            quizDuration,
                                            questionWeightage,
                                            studentRollNumbers,
                                            new ArrayList<>(),
                                            new ArrayList<>()
                                    );
                                    quizModel.setSubmitted(finalIsSubmitted);
                                    quizList.add(quizModel);

                                    // Fetch related quiz questions and student responses
                                    tasks.add(fetchQuizQuestions(quizModel));
                                    tasks.add(fetchStudentQuizResponses(quizModel, studentRollNumbers));

                                    // Check if all documents have been processed
                                    if (endedQuizCount.get() == quizList.size()) {
                                        // Wait for all fetch tasks to complete
                                        Tasks.whenAll(tasks)
                                                .addOnCompleteListener(completionTask -> {
                                                    ProgressDialogHelper.dismissProgressDialog();
                                                    if (completionTask.isSuccessful()) {
                                                        if (quizList.isEmpty()) {
                                                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "No past quizzes found", Toast.LENGTH_SHORT).show());
                                                        } else {
                                                            quizAdapter = new TeacherQuizAdapter(getApplicationContext(), quizList);
                                                            recyclerViewTeacherQuizzes.setAdapter(quizAdapter);
                                                            quizAdapter.notifyDataSetChanged();
                                                        }
                                                    } else {
                                                        Log.w("Firestore", "Error completing fetch tasks.", completionTask.getException());
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.w("Firestore", "Failed to fetch course details for quiz: " + quizId, e);
                                });
                            }
                        }
                        // If no ended quizzes were found after processing all documents
                        if (endedQuizCount.get() == 0) {
                            ProgressDialogHelper.dismissProgressDialog();
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "No ended quizzes found", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        ProgressDialogHelper.dismissProgressDialog();
                        Log.w("Firestore", "Error getting quizzes.", task.getException());
                    }
                });
    }

    private boolean hasQuizEnded(String availableWhen, String quizDuration) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));

        try {
            Date availableDate = sdf.parse(availableWhen);
            long durationMillis = parseDuration(quizDuration);
            Date quizEndDate = new Date(availableDate.getTime() + durationMillis);

            return new Date().after(quizEndDate);
        } catch (ParseException e) {
            Log.e("Firestore", "Error parsing date", e);
            return false;
        }
    }

    private long parseDuration(String duration) {
        long durationMillis = 0;
        String[] parts = duration.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("hours")) {
                int hours = Integer.parseInt(part.split(" ")[0].trim());
                durationMillis += hours * 60 * 60 * 1000;
            } else if (part.contains("minutes")) {
                int minutes = Integer.parseInt(part.split(" ")[0].trim());
                durationMillis += minutes * 60 * 1000;
            }
        }
        return durationMillis;
    }

    private Task<Void> fetchQuizQuestions(TeacherCourseQuizModel quizModel) {
        return db.collection("Quizzes")
                .whereEqualTo("QuizID", quizModel.getQuizId())
                .get()
                .continueWith(innerTask -> {
                    if (innerTask.isSuccessful()) {
                        List<QuizModel> fetchedQuestions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : innerTask.getResult()) {
                            String questionNo = document.getString("QuestionNo");
                            String quizQuestion = document.getString("Question");
                            List<String> options = (List<String>) document.get("OptionsArrayList");
                            String correctOption = document.getString("CorrectOption");

                            QuizModel quiz = new QuizModel(questionNo, quizQuestion, options.get(0), options.get(1), options.get(2), options.get(3), correctOption);
                            fetchedQuestions.add(quiz);
                        }
                        fetchedQuestions.sort(Comparator.comparing(QuizModel::getQuestionNo));
                        quizModel.getQuizQuestions().addAll(fetchedQuestions);
                    } else {
                        Log.w("Firestore", "Error fetching quiz questions.", innerTask.getException());
                    }
                    return null;
                });
    }

    private Task<Void> fetchStudentQuizResponses(TeacherCourseQuizModel quizModel, List<String> studentRollNumbers) {
        final AtomicInteger count = new AtomicInteger(0);
        List<Task<Void>> tasks = new ArrayList<>();

        for (String studentRollNo : studentRollNumbers) {
            Task<Void> task = db.collection("StudentAttemptedQuestions")
                    .whereEqualTo("CourseID", quizModel.getCourseId())
                    .whereEqualTo("QuizID", quizModel.getQuizId())
                    .whereEqualTo("StudentRollNo", studentRollNo)
                    .get()
                    .continueWith(responseTask -> {
                        if (responseTask.isSuccessful()) {
                            for (QueryDocumentSnapshot document : responseTask.getResult()) {
                                Map<String, String> mapQuestionsAttempted = (Map<String, String>) document.get("MapQuestionsAttempted");
                                QuizStudentAttemptedQuestionsModel studentAttemptedQuestionsModel = new QuizStudentAttemptedQuestionsModel(studentRollNo, mapQuestionsAttempted);
                                quizModel.getStudentResponses().add(studentAttemptedQuestionsModel);
                            }
                            if (count.incrementAndGet() == studentRollNumbers.size()) {
                                sortStudentResponsesByRollNo(quizModel);
                            }
                        } else {
                            Log.w("Firestore", "Error getting student attempted questions.", responseTask.getException());
                        }
                        return null;
                    });
            tasks.add(task);
        }
        return Tasks.whenAll(tasks);
    }

    private void sortStudentResponsesByRollNo(TeacherCourseQuizModel quizModel) {
        quizModel.getStudentResponses().sort(Comparator.comparing(QuizStudentAttemptedQuestionsModel::getStudentRollNo));
    }
}
