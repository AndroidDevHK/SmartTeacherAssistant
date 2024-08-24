package com.nextgen.hasnatfyp;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class StudentViewModel extends ViewModel {

    private static final String TAG = "StudentViewModel";

    private MutableLiveData<List<StudentModel>> studentListLiveData;
    private List<StudentModel> originalStudentList; // Store the original unfiltered list
    private String classId;
    private FirebaseFirestore db;

    private ProgressDialog progressDialog;

    public void init(String classId, Context context) {
        if (studentListLiveData != null) {
            // ViewModel is already initialized
            return;
        }
        this.classId = classId;
        db = FirebaseFirestore.getInstance();
        studentListLiveData = new MutableLiveData<>();
        originalStudentList = new ArrayList<>(); // Initialize the original list
        fetchClassData(context);
    }

    public LiveData<List<StudentModel>> getStudentList() {
        return studentListLiveData;
    }

    public void filterStudents(String query) {
        List<StudentModel> filteredList = new ArrayList<>();
        if (originalStudentList != null) {
            for (StudentModel student : originalStudentList) {
                // Filter students by name or roll number
                if (student.getStudentName().toLowerCase().contains(query.toLowerCase()) ||
                        student.getRollNo().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(student);
                }
            }
        }
        studentListLiveData.setValue(filteredList);
    }

    private void fetchClassData(Context context) {
        showProgressDialog(context);

        db.collection("Classes").document(classId)
                .collection("ClassStudents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<StudentModel> studentList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve student data from Firestore document
                            String studentName = document.getString("StudentName");
                            String rollNo = document.getString("RollNo");
                            boolean isActive = document.getBoolean("IsActive");

                            // Retrieve StudentUserID using classId and rollNo
                            db.collection("StudentSemestersDetails")
                                    .whereEqualTo("ClassID", classId)
                                    .whereEqualTo("StudentRollNo", rollNo)
                                    .get()
                                    .addOnCompleteListener(studentTask -> {
                                        if (studentTask.isSuccessful() && !studentTask.getResult().isEmpty()) {
                                            String studentUserID = studentTask.getResult().getDocuments().get(0).getString("UserID");

                                            // Create StudentModel object
                                            StudentModel student = new StudentModel(studentName, rollNo, isActive, "P", classId);
                                            student.setStudentUserID(studentUserID); // Set the StudentUserID
                                            studentList.add(student);

                                            // Check if all students have been processed
                                            if (studentList.size() == task.getResult().size()) {
                                                originalStudentList.addAll(studentList);
                                                // Sort students by roll number
                                                StudentSortHelper.sortByRollNumber(studentList);

                                                studentListLiveData.setValue(studentList);
                                                dismissProgressDialog();
                                            }
                                        } else {
                                            Log.e(TAG, "Error fetching StudentUserID: ", studentTask.getException());
                                        }
                                    });
                        }

                        // Handle case where no students are found
                        if (task.getResult().isEmpty()) {
                            dismissProgressDialog();
                            studentListLiveData.setValue(studentList); // Set empty list
                        }
                    } else {
                        // Handle errors
                        dismissProgressDialog();
                        Log.e(TAG, "Error fetching student data: ", task.getException());
                    }
                });
    }

    private void showProgressDialog(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Fetching Data...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
