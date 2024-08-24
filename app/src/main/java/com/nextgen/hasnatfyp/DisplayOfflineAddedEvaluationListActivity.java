package com.nextgen.hasnatfyp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.nextgen.hasnatfyp.ProgressDialogHelper.dismissProgressDialog;
import static com.nextgen.hasnatfyp.ProgressDialogHelper.showProgressDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


import OfflineEvluationManagement.OfflineEvaluationModel;
import OfflineEvluationManagement.StudentEvalMarksOffline;
import View_Class_Students_Activity.StudentModel;

public class DisplayOfflineAddedEvaluationListActivity extends AppCompatActivity {

    private List<OfflineEvaluationModel> offlineEvaluationList;
    private RecyclerView recyclerView;
    private OfflineEvaluationAdapter adapter;
    private ActivityManager activityManager;
    private MaterialButton submitAllEvaluationsButton;
    private FirebaseFirestore db;
    private List<String> alreadyExistingEvaluationRecords = new ArrayList<>();
    private SearchView searchView;
    private TextView noResultText;

    private CardView noEvaluationCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_offline_added_evaluation_list);

        initializeViews();
        loadOfflineEvaluations();
        setupSubmitAllEvaluationsButton();
        setupSearchFunctionality();
    }

    private void setupSubmitAllEvaluationsButton() {
        submitAllEvaluationsButton.setOnClickListener(v -> {
            if (!TeacherInstanceModel.getInstance(this).isOfflineMode()) {
                if (isInternetConnected()) {
                    submitAllEvaluationsToFirestore();
                } else {
                    showError("No internet connection available");
                }
            } else {
                showError(this);
            }
        });
    }

    public void showError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error")
                .setMessage("You can't submit evaluation in offline mode. Please connect to the internet, restart the app, and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_evaluation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityManager = ActivityManager.getInstance();
        activityManager.addActivityForKill(this);
        submitAllEvaluationsButton = findViewById(R.id.button_submit_all_evaluations);
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);

        db = FirebaseFirestore.getInstance();
        searchView = findViewById(R.id.simpleSearchView);
        noResultText = findViewById(R.id.noResultText);
        offlineEvaluationList = new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.toolbar);
        noEvaluationCard = findViewById(R.id.noEvaluationCard);
        SetupToolbar(toolbar);
    }
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Offline Added Evaluations", true);
    }

    private void setupSearchFunctionality() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvaluationList(newText);
                return true;
            }
        });
    }

    private void filterEvaluationList(String query) {
        List<OfflineEvluationManagement.OfflineEvaluationModel> filteredList = new ArrayList<>();
        for (OfflineEvluationManagement.OfflineEvaluationModel evaluation : offlineEvaluationList) {
            if (evaluation.getCourseName().toLowerCase().contains(query.toLowerCase())
                    || evaluation.getEvaluationName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(evaluation);
            }
        }
        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE);
        } else {
            noResultText.setVisibility(View.GONE);
        }
    }



    private void loadOfflineEvaluations() {
        offlineEvaluationList = new ArrayList<>();
        List<String> evaluationFiles = getEvaluationFileNames();

        for (String fileName : evaluationFiles) {
            OfflineEvluationManagement.OfflineEvaluationModel evaluation = readEvaluationFromFile(fileName);
            if (evaluation != null) {
                offlineEvaluationList.add(evaluation);
            }
        }


        if (offlineEvaluationList.isEmpty()) {
            noEvaluationCard.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchView.setVisibility(View.GONE);
            submitAllEvaluationsButton.setVisibility(View.GONE);
        } else {
            noEvaluationCard.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new OfflineEvaluationAdapter(offlineEvaluationList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private List<String> getEvaluationFileNames() {
        String TeacherUserName;
        if (UserInstituteModel.getInstance(this).isSoloUser()) {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        } else {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = TeacherUserName + "_EvaluationData";
        File directory = new File(getFilesDir(), folderName);
        List<String> fileNames = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }

    private OfflineEvaluationModel readEvaluationFromFile(String fileName) {
        String TeacherUserName;
        if (UserInstituteModel.getInstance(this).isSoloUser()) {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        } else {
            TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = TeacherUserName + "_EvaluationData";
        File directory = new File(getFilesDir(), folderName);
        File file = new File(directory, fileName);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Type type = new TypeToken<OfflineEvluationManagement.OfflineEvaluationModel>() {
                }.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void submitAllEvaluationsToFirestore() {
        showProgressDialog(this,"Submitting");
        alreadyExistingEvaluationRecords.clear();
        submitEvaluationsSequentially(0);
    }

    private void submitEvaluationsSequentially(int index) {
        if (index < offlineEvaluationList.size()) {
            OfflineEvaluationModel evaluationModel = offlineEvaluationList.get(index);
            String evaluationId = UUID.randomUUID().toString();
            submitEvaluationRecord(evaluationModel, evaluationId, () -> {
                submitEvaluationsSequentially(index + 1);
            });
        } else {
            showToast("All evaluations submitted successfully");
            dismissProgressDialog();
            if (!alreadyExistingEvaluationRecords.isEmpty()) {
                showErrorMessageForExistingEvaluations();
            } else {
                finish();
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void submitEvaluationRecord(OfflineEvaluationModel evaluation, String evaluationId, Runnable onComplete) {
        String courseId = evaluation.getCourseId();
        String evaluationDate = evaluation.getEvaluationDate();
        String courseName = evaluation.getCourseName();
        String evaluationName = evaluation.getEvaluationName();
        String evaluationTotalMarks = evaluation.getEvaluationTMarks();

        boolean areRepeaters = evaluation.isAreRepeaters();

        // Check if evaluation already exists for this course and date
        db.collection("Evaluations")
                .whereEqualTo("CourseID", courseId)
                .whereEqualTo("AreRepeaters", areRepeaters)
                .whereEqualTo("EvalName", evaluationName.toUpperCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No evaluation record exists, proceed with submission
                        Map<String, Object> evaluationData = new HashMap<>();
                        evaluationData.put("EvalID", evaluationId);
                        evaluationData.put("EvalName", evaluationName);
                        evaluationData.put("EvalTMarks", evaluationTotalMarks);
                        evaluationData.put("CourseID", courseId);
                        evaluationData.put("AreRepeaters", areRepeaters);
                        db.collection("Evaluations")
                                .document(evaluationId)
                                .set(evaluationData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    submitCourseEvaluationRecord(evaluation, evaluationId, onComplete);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error adding evaluation record", e);
                                });
                    } else {
                        String alreadyExistingRecord = courseName + (areRepeaters ? " (Repeaters)" : "") + ", " + evaluationName;
                        alreadyExistingEvaluationRecords.add(alreadyExistingRecord);
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error querying evaluation record", e);
                });
    }
    private void deleteEvaluationFileOnSubmit(OfflineEvaluationModel evaluation) {
        String fileName = generateFileName(evaluation);
        String teacherUserName;
        if(UserInstituteModel.getInstance(this).isSoloUser()) {
            teacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
        } else {
            teacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();
        }
        String folderName = teacherUserName + "_EvaluationData";
        File directory = new File(getFilesDir(), folderName);
        File file = new File(directory, fileName);
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "Evaluation file deleted successfully: " + fileName);
            } else {
                Log.e(TAG, "Failed to delete evaluation file: " + fileName);
            }
        } else {
            Log.d(TAG, "Evaluation file does not exist: " + fileName);
        }
    }

    private String generateFileName(OfflineEvaluationModel evaluation) {
        return evaluation.getEvaluationName() + "_" + evaluation.getEvaluationTMarks() + "_" + evaluation.getCourseId() + "_" + evaluation.getCourseName() + "_" + (evaluation.isAreRepeaters() ? "Repeaters" : "Regular") + ".json";
    }

    private void submitCourseEvaluationRecord(OfflineEvaluationModel evaluation, String evaluationId, Runnable onComplete) {
        String courseId = evaluation.getCourseId();
        Map<String, Object> courseEvaluationData = new HashMap<>();
        courseEvaluationData.put("EvalID", evaluationId);
        courseEvaluationData.put("CourseID", courseId);
        courseEvaluationData.put("AreRepeaters", evaluation.isAreRepeaters());
        courseEvaluationData.put("CreatedWhen", evaluation.getEvaluationDate());

        db.collection("CourseEvaluationsInfo")
                .document(courseId + "_" + evaluationId)
                .set(courseEvaluationData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    submitStudentEvaluationRecords(evaluation, evaluationId, onComplete);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding course evaluation record", e);
                });
    }

    private void submitStudentEvaluationRecords(OfflineEvaluationModel evaluation, String evaluationId, Runnable onComplete) {
        List<StudentEvalMarksOffline> studentsList = evaluation.getStudentsList();
        String courseId = evaluation.getCourseId();
        Boolean areRepeaters = evaluation.isAreRepeaters();
        updateStudentCourseEvaluationList(evaluationId, studentsList, onComplete, courseId, areRepeaters, evaluation);
    }

    private void updateStudentCourseEvaluationList(String evaluationId, List<StudentEvalMarksOffline> studentsList, Runnable onComplete, String courseId, Boolean areRepeaters, OfflineEvaluationModel evaluation) {
        if (studentsList.isEmpty()) {
            onComplete.run();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        AtomicInteger successfulSubmissionsCounter = new AtomicInteger(0);

        for (StudentEvalMarksOffline student : studentsList) {
            String studentRollNo = student.getRollNo();
            String classID = student.getClassId();

            String documentId = studentRollNo + "_" + courseId;
            String documentPath = "StudentCourseEvaluationList/" + documentId;

            Map<String, Object> data = new HashMap<>();
            data.put("StudentRollNo", studentRollNo);
            data.put("CourseID", courseId);
            data.put("ClassID", classID);
            data.put("IsRepeater", areRepeaters);

            DocumentReference studentDocRef = db.document(documentPath);
            batch.set(studentDocRef, data, SetOptions.merge());

            // Update the "EvaluationIDs" field in a single operation
            batch.update(studentDocRef, "EvaluationIDs", FieldValue.arrayUnion(evaluationId));

            successfulSubmissionsCounter.incrementAndGet();
        }

        // Commit the batch operation
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // After updating all student course evaluation records, start adding evaluations for students
                    addCourseStudentsEvaluation(evaluationId, studentsList, courseId, areRepeaters, onComplete, evaluation);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating student course evaluation list", e);
                });
    }

    private void addCourseStudentsEvaluation(String evaluationId, List<StudentEvalMarksOffline> studentsList, String courseId, boolean areRepeaters, Runnable onComplete, OfflineEvaluationModel evaluation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        for (StudentEvalMarksOffline student : studentsList) {
            String studentRollNo = student.getRollNo();
            Double studentObtainedMarks = student.getObtainedMarks();
            String classID = student.getClassId();
            String documentId = studentRollNo + "_" + evaluationId;

            Map<String, Object> courseStudentsEvaluationData = new HashMap<>();
            courseStudentsEvaluationData.put("EvalID", evaluationId);
            courseStudentsEvaluationData.put("StudentRollNo", studentRollNo);
            courseStudentsEvaluationData.put("ClassID", classID);
            courseStudentsEvaluationData.put("StudentObtMarks", studentObtainedMarks);

            DocumentReference docRef = db.collection("CourseStudentsEvaluation").document(documentId);
            batch.set(docRef, courseStudentsEvaluationData);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    deleteEvaluationFileOnSubmit(evaluation);
                    onComplete.run();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error adding course student evaluation", e));
    }

    private void showErrorMessageForExistingEvaluations() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error: Evaluation Already Exists");

        LinearLayout evaluationsLayout = new LinearLayout(this);
        evaluationsLayout.setOrientation(LinearLayout.VERTICAL);

        createAndAddHeaderRow(evaluationsLayout);

        for (String record : alreadyExistingEvaluationRecords) {
            LinearLayout recordLayout = new LinearLayout(this);
            recordLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            recordLayout.setOrientation(LinearLayout.HORIZONTAL);
            recordLayout.setBackgroundColor(getResources().getColor(R.color.record_background_color)); // Set background color for record

            String[] recordParts = record.split(",");
            String courseName = recordParts[0].trim();
            String evaluationDate = recordParts[1].trim();

            TextView courseNameTextView = new TextView(this);
            courseNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            courseNameTextView.setText(courseName);
            courseNameTextView.setPadding(10, 8, 10, 8);
            courseNameTextView.setTextColor(getResources().getColor(android.R.color.black));
            courseNameTextView.setGravity(Gravity.START);

            TextView evaluationDateTextView = new TextView(this);
            evaluationDateTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            evaluationDateTextView.setText(evaluationDate);
            evaluationDateTextView.setPadding(10, 8, 10, 8);
            evaluationDateTextView.setTextColor(getResources().getColor(android.R.color.black));
            evaluationDateTextView.setGravity(Gravity.CENTER);

            recordLayout.addView(courseNameTextView);
            recordLayout.addView(evaluationDateTextView);

            evaluationsLayout.addView(recordLayout);
        }

        builder.setView(evaluationsLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setOnDismissListener(dialog -> {
            evaluationsLayout.removeAllViews();
            finish();
        });

        builder.create().show();
    }
    private void createAndAddHeaderRow(LinearLayout parentLayout) {
        // Create the header row LinearLayout
        LinearLayout headerRowLayout = new LinearLayout(this);
        headerRowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerRowLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create TextView for course name header
        TextView courseNameHeader = new TextView(this);
        LinearLayout.LayoutParams courseNameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        courseNameHeader.setLayoutParams(courseNameParams);
        courseNameHeader.setText("Course");
        courseNameHeader.setTextSize(12);
        courseNameHeader.setTextColor(getResources().getColor(android.R.color.white));
        courseNameHeader.setGravity(Gravity.CENTER);
        courseNameHeader.setPadding(8, 8, 8, 8);
        courseNameHeader.setTypeface(null, Typeface.BOLD);
        courseNameHeader.setBackgroundColor(getResources().getColor(androidx.cardview.R.color.cardview_dark_background));

        // Create TextView for evaluation name header
        TextView evaluationNameHeader = new TextView(this);
        LinearLayout.LayoutParams evalNameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        evaluationNameHeader.setLayoutParams(evalNameParams);
        evaluationNameHeader.setText("Evaluation Name");
        evaluationNameHeader.setTextSize(12);
        evaluationNameHeader.setTextColor(getResources().getColor(android.R.color.white));
        evaluationNameHeader.setGravity(Gravity.CENTER);
        evaluationNameHeader.setPadding(8, 8, 8, 8);
        evaluationNameHeader.setTypeface(null, Typeface.BOLD);
        evaluationNameHeader.setBackgroundColor(getResources().getColor(androidx.cardview.R.color.cardview_dark_background));

        // Add TextViews to the header row
        headerRowLayout.addView(courseNameHeader);
        headerRowLayout.addView(evaluationNameHeader);

        // Add the header row to the parent layout
        parentLayout.addView(headerRowLayout);
    }


}

