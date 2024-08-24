package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayQuizStudentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewStudents;
    private StudentQuizAdapter studentQuizAdapter;
    private TeacherCourseQuizModel quizModel;
    private MaterialButton buttonSaveResults;
    private List<StudentQuizResults> studentResults;
    private Map<String, Boolean> studentRepeaterStatus;
    private Map<String, String> studentClassIDs;
    private TextView CourseNameTextView, TotalStdTextView;
    private Map<String, String> studentNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_quiz_students);

        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        buttonSaveResults = findViewById(R.id.buttonSaveResults);
        CourseNameTextView = findViewById(R.id.CourseNameTextView);
        TotalStdTextView = findViewById(R.id.TotalStdTextView);

        if (getIntent() != null && getIntent().hasExtra("quizModel")) {
            ProgressDialogHelper.showProgressDialog(this,"Loading Quiz Details...");
            quizModel = getIntent().getParcelableExtra("quizModel");
            fetchStudentRepeaterStatus();
        }

        buttonSaveResults.setOnClickListener(this::saveResults);

        setCourseNameAndStudentCount();
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);


    }

    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Display Quiz Details", true);
    }
    private void setCourseNameAndStudentCount() {
        CourseNameTextView.setText(quizModel.getCourseName());
        TotalStdTextView.setText(String.valueOf(quizModel.getStudentResponses().size()));

    }

    private void fetchStudentRepeaterStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CoursesStudents")
                .whereEqualTo("CourseID", quizModel.getCourseId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalStudents = queryDocumentSnapshots.size();  // Total expected student documents
                    AtomicInteger studentsProcessed = new AtomicInteger();  // Counter for processed student documents

                    studentRepeaterStatus = new HashMap<>();
                    studentClassIDs = new HashMap<>();
                    studentNames = new HashMap<>();

                    if (totalStudents == 0) {  // If there are no students, setup the quiz details directly
                        setupQuizDetails();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String studentRollNo = document.getString("StudentRollNo");
                        String classID = document.getString("ClassID");
                        studentRepeaterStatus.put(studentRollNo, document.getBoolean("isRepeater"));
                        studentClassIDs.put(studentRollNo, classID);

                        // Fetch student name from ClassStudents subcollection
                        db.collection("Classes").document(classID)
                                .collection("ClassStudents")
                                .whereEqualTo("RollNo", studentRollNo)
                                .get()
                                .addOnSuccessListener(studentDocs -> {
                                    if (!studentDocs.isEmpty()) {
                                        DocumentSnapshot studentDoc = studentDocs.getDocuments().get(0);
                                        String studentName = studentDoc.getString("StudentName");
                                        studentNames.put(studentRollNo, studentName);
                                        Log.d("StudentInfo", "Roll No: " + studentRollNo + ", Name: " + studentName);
                                    }

                                    studentsProcessed.getAndIncrement();  // Increment the processed counter
                                    if (studentsProcessed.get() == totalStudents) {
                                        setupQuizDetails();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DisplayQuizStudents", "Error fetching student name: ", e);
                                    studentsProcessed.getAndIncrement();  // Increment the processed counter even on failure
                                    if (studentsProcessed.get() == totalStudents) {
                                        setupQuizDetails();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DisplayQuizStudents", "Error fetching student details: ", e);
                    showToast("Error fetching student details.");
                });
    }


    @SuppressLint("SetTextI18n")
    private void setupQuizDetails() {
        // Check if the quiz has already been submitted
        if (quizModel.isSubmitted()) {
            buttonSaveResults.setEnabled(false);
            buttonSaveResults.setText("Quiz Result Submitted");
        } else {
            buttonSaveResults.setEnabled(true);
            buttonSaveResults.setText("Save Results");
            buttonSaveResults.setOnClickListener(this::saveResults);
        }

        // Set student names from the studentNames HashMap
        for (QuizStudentAttemptedQuestionsModel studentResponse : quizModel.getStudentResponses()) {
            String rollNo = studentResponse.getStudentRollNo();
            if (studentNames.containsKey(rollNo)) {
                studentResponse.setStudentName(studentNames.get(rollNo));
            }
        }

        HashMap<String, String> correctAnswers = getCorrectAnswers();
        studentResults = calculateResults(quizModel.getStudentResponses(), correctAnswers, Float.parseFloat(quizModel.getQuestionWeightage()));
        studentQuizAdapter = new StudentQuizAdapter(this, quizModel.getStudentResponses(), correctAnswers, Float.parseFloat(quizModel.getQuestionWeightage()), quizModel.getQuizQuestions());
        recyclerViewStudents.setAdapter(studentQuizAdapter);
        ProgressDialogHelper.dismissProgressDialog();
    }


    private HashMap<String, String> getCorrectAnswers() {
        HashMap<String, String> correctAnswers = new HashMap<>();
        for (QuizModel qm : quizModel.getQuizQuestions()) {
            correctAnswers.put(qm.getQuestionNo(), qm.getCorrectOption());
        }
        return correctAnswers;
    }

    private List<StudentQuizResults> calculateResults(List<QuizStudentAttemptedQuestionsModel> responses, Map<String, String> correctAnswers, float weightage) {
        List<StudentQuizResults> results = new ArrayList<>();
        for (QuizStudentAttemptedQuestionsModel response : responses) {
            float obtainedMarks = 0;
            float totalMarks = quizModel.getQuizQuestions().size() * weightage;

            for (Map.Entry<String, String> entry : response.getMapQuestionsAttempted().entrySet()) {
                if (entry.getValue().equals(correctAnswers.get(entry.getKey()))) {
                    obtainedMarks += weightage;
                }
            }
            results.add(new StudentQuizResults(response.getStudentRollNo(), studentClassIDs.get(response.getStudentRollNo()), String.valueOf(totalMarks), obtainedMarks, studentRepeaterStatus.get(response.getStudentRollNo())));
        }
        return results;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveResults(View view)  {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Evaluation Name");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Quiz 1, Quiz 2, etc.");
        builder.setView(input);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String evaluationName = input.getText().toString().trim();
                if (!evaluationName.isEmpty()) {
                    checkEvalExistence(evaluationName.toUpperCase(), dialog);
                } else {
                    showToast("Evaluation name cannot be empty.");
                }
            });
        });

        dialog.show();
    }

    private void checkEvalExistence(String evaluationName, AlertDialog dialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Evaluations")
                .whereEqualTo("CourseID", quizModel.getCourseId())
                .whereEqualTo("EvalName", evaluationName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        showToast("Similar evaluation record already exists. Please choose a different name.");
                    } else {
                        dialog.dismiss();
                        saveEvaluationDetails(evaluationName);
                    }
                })
                .addOnFailureListener(e -> showToast("Error checking for existing evaluation records: " + e.getMessage()));
    }

    private void saveEvaluationDetails(String evaluationName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving evaluations...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Split students into repeaters and regulars
        List<StudentQuizResults> repeaters = new ArrayList<>();
        List<StudentQuizResults> regulars = new ArrayList<>();
        for (StudentQuizResults result : studentResults) {
            if (result.isRepeater()) {
                repeaters.add(result);
            } else {
                regulars.add(result);
            }
        }

        // Handler for completing the saving process
        Runnable onComplete = () -> {
            updateQuizSubmissionStatus(quizModel.getQuizId(), true,progressDialog);  // Update the quiz submission status after all transactions are complete
        };

        // Handle batch saving in a method that considers both regulars and repeaters
        saveStudentEvaluations(db, regulars, evaluationName, false, () -> {
            if (!repeaters.isEmpty()) {
                // Save repeaters only if they exist
                saveStudentEvaluations(db, repeaters, evaluationName, true, onComplete);
            } else {
                // No repeaters, so we're done after regulars
                onComplete.run();
            }
        });
    }

    private void saveStudentEvaluations(FirebaseFirestore db, List<StudentQuizResults> students, String evaluationName, boolean isRepeater, Runnable onComplete) {
        if (students.isEmpty()) {
            onComplete.run();
            return; // Exit if no students to process
        }

        WriteBatch batch = db.batch();
        String evaluationId = UUID.randomUUID().toString();
        float totalMarks = quizModel.getQuizQuestions().size() * Float.parseFloat(quizModel.getQuestionWeightage());
        String formattedTotalMarks = (totalMarks == (int) totalMarks) ? String.valueOf((int) totalMarks) : String.valueOf(totalMarks);

        // Prepare evaluation data for the Evaluations collection
        Map<String, Object> evaluationData = new HashMap<>();
        evaluationData.put("EvalID", evaluationId);
        evaluationData.put("EvalName", evaluationName);
        evaluationData.put("CourseID", quizModel.getCourseId());
        evaluationData.put("EvalTMarks", formattedTotalMarks);
        evaluationData.put("AreRepeaters", isRepeater);

        // Adding data to the Evaluations collection
        DocumentReference evalRef = db.collection("Evaluations").document(evaluationId);
        batch.set(evalRef, evaluationData);

        // Prepare and add data for each student in the CourseStudentsEvaluation collection
        for (StudentQuizResults result : students) {
            DocumentReference studentEvalRef = db.collection("CourseStudentsEvaluation").document(result.getStudentRollNo() + "_" + evaluationId);
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("StudentRollNo", result.getStudentRollNo());
            studentData.put("EvalID", evaluationId);
            studentData.put("StudentObtMarks", result.getObtainedMarks());
            studentData.put("ClassID", result.getClassID());
            batch.set(studentEvalRef, studentData);

            // Prepare and merge data for the StudentCourseEvaluationList collection
            DocumentReference studentCourseEvalListRef = db.collection("StudentCourseEvaluationList").document(result.getStudentRollNo() + "_" + quizModel.getCourseId());
            Map<String, Object> courseEvalData = new HashMap<>();
            courseEvalData.put("StudentRollNo", result.getStudentRollNo());
            courseEvalData.put("CourseID", quizModel.getCourseId());
            courseEvalData.put("ClassID", result.getClassID());
            courseEvalData.put("IsRepeater", result.isRepeater());
            courseEvalData.put("EvaluationIDs", FieldValue.arrayUnion(evaluationId));
            batch.set(studentCourseEvalListRef, courseEvalData, SetOptions.merge());
        }
        String currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> courseEvaluationInfoData = new HashMap<>();
        courseEvaluationInfoData.put("AreRepeaters", isRepeater);
        courseEvaluationInfoData.put("CourseID", quizModel.getCourseId());
        courseEvaluationInfoData.put("CreatedBy",  UserInstituteModel.getInstance(this).getUsername());  // You might want to replace this with a dynamic value
        courseEvaluationInfoData.put("CreatedWhen", currentDate);
        courseEvaluationInfoData.put("EvalID", evaluationId);

        // Adding data to the CourseEvaluationsInfo collection
        DocumentReference courseEvalInfoRef = db.collection("CourseEvaluationsInfo").document(evaluationId);
        batch.set(courseEvalInfoRef, courseEvaluationInfoData);

        // Commit the batch write
        batch.commit()
                .addOnSuccessListener(aVoid -> onComplete.run())
                .addOnFailureListener(e -> {
                    showToast("Error saving evaluation: " + e.getMessage());
                    onComplete.run(); // Ensure onComplete is called even on failure to dismiss the dialog
                });
    }


    private void updateQuizSubmissionStatus(String quizId, boolean isSubmitted, ProgressDialog progressDialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference quizzesRef = db.collection("CourseQuizzes");

        // Prepare the data to be updated
        Map<String, Object> data = new HashMap<>();
        data.put("IsSubmitted", isSubmitted);

        // Query to find the document with the specific QuizID
        quizzesRef.whereEqualTo("QuizID", quizId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the document ID of the first matching document
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        DocumentReference quizRef = quizzesRef.document(document.getId());

                        // Update the IsSubmitted field
                        quizRef.set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss(); // Dismiss the progress dialog on successful update
                                    showToast("Result saved successfully!");
                                    ActivityManager.getInstance().finishActivitiesForKillCourseDeletion();

                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss(); // Also dismiss the progress dialog on failure
                                    Log.e("UpdateQuiz", "Error inserting quiz submission status.", e);
                                    showToast("Error inserting quiz submission status.");
                                });
                    } else {
                        progressDialog.dismiss(); // Dismiss the progress dialog if no document is found
                        showToast("No matching quiz found.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // Also dismiss the progress dialog on failure
                    Log.e("UpdateQuiz", "Error querying quiz.", e);
                    showToast("Error querying quiz.");
                });
    }
}
