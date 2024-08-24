package View_Semester_Classes_Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClassViewModel extends ViewModel {

    private static final String TAG = "ClassViewModel";

    private FirebaseFirestore db;
    private MutableLiveData<List<ClassModel>> classListLiveData;
    private String semesterId;
    private boolean isDataLoaded = false;
    private int expectedSize = 0;


    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
        loadClasses();
    }

    public LiveData<List<ClassModel>> getClassList() {
        if (classListLiveData == null) {
            classListLiveData = new MutableLiveData<>();
        }
        return classListLiveData;
    }

    private void loadClasses() {
        if (semesterId == null) {
            Log.e(TAG, "Semester ID is null. Please set the semester ID using setSemesterId() method.");
            return;
        }

        db = FirebaseFirestore.getInstance();

        db.collection("SemesterClasses")
                .whereEqualTo("SemesterID", semesterId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expectedSize = queryDocumentSnapshots.size();
                    onClassesRetrieved(queryDocumentSnapshots);
                })
                .addOnFailureListener(this::onClassesRetrieveFailed);
    }

    private void onClassesRetrieved(QuerySnapshot queryDocumentSnapshots) {
        List<ClassModel> classList = new ArrayList<>();
        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
            String classId = documentSnapshot.getString("ClassID");
            retrieveClassData(classId, classList);
        }
    }

    private void onClassesRetrieveFailed(Exception e) {
        Log.e(TAG, "Error retrieving classes", e);
    }

    private void retrieveClassData(String classId, List<ClassModel> classList) {
        db.collection("Classes").document(classId)
                .collection("ClassStudents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> onClassDataRetrieved(classId, classList, queryDocumentSnapshots))
                .addOnFailureListener(e -> onClassDataRetrieveFailed(classId, e));
    }

    private void onClassDataRetrieved(String classId, List<ClassModel> classList, QuerySnapshot queryDocumentSnapshots) {
        int numberOfStudents = queryDocumentSnapshots.size();
        retrieveCoursesCount(classId, classList, numberOfStudents);
    }
    public void deleteClass(String classId, Context context) {
        showProgressDialog(context); // Show progress dialog

        db.collection("Classes").document(classId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // On successful deletion, remove the class from the local list
                    removeClassLocally(classId);

                    dismissProgressDialog(); // Dismiss progress dialog
                    showToast(context, "Class deleted successfully");
                })
                .addOnFailureListener(e -> {
                    dismissProgressDialog(); // Dismiss progress dialog on failure

                    Log.e(TAG, "Error deleting class", e);
                });
    }
    private ProgressDialog progressDialog;
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    private void showProgressDialog(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Deleting...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    private void removeClassLocally(String classId) {
        List<ClassModel> currentClasses = classListLiveData.getValue();
        if (currentClasses != null) {
            // Iterate through the list of classes and remove the one with the matching ID
            for (ClassModel classModel : currentClasses) {
                if (classModel.getClassId().equals(classId)) {
                    List<ClassModel> classList = new ArrayList<>(currentClasses);
                    classList.remove(classModel);
                    // Update the LiveData with the modified list
                    classListLiveData.setValue(classList);
                    // Exit loop as the class has been found and removed
                    return;
                }
            }
        }
        // If the class was not found or the list is empty/null, log a message or handle it as needed
        Log.d(TAG, "Class with ID " + classId + " not found in LiveData or LiveData is empty/null");
    }


    private void onClassDataRetrieveFailed(String classId, Exception e) {
        Log.e(TAG, "Error retrieving students for class: " + classId, e);
    }

    private void retrieveCoursesCount(String classId, List<ClassModel> classList, int numberOfStudents) {
        db.collection("ClassCourses").document(classId)
                .collection("ClassCoursesSubcollection")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int coursesCount = queryDocumentSnapshots.size();
                    updateClassModelWithCoursesCount(classId, classList, numberOfStudents, coursesCount);
                })
                .addOnFailureListener(e -> onCoursesCountRetrieveFailed(classId, e));
    }

    private void updateClassModelWithCoursesCount(String classId, List<ClassModel> classList, int numberOfStudents, int coursesCount) {
        db.collection("Classes").document(classId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String className = documentSnapshot.getString("ClassName");
                        ClassModel classModel = new ClassModel(classId, className, numberOfStudents, coursesCount);
                        classList.add(classModel);
                        if (classList.size() == expectedSize && !isDataLoaded) {
                            classListLiveData.setValue(classList);
                            isDataLoaded = true;
                        }
                    } else {
                        Log.d(TAG, "Class document not found for ID: " + classId);
                    }
                })
                .addOnFailureListener(e -> onClassNameRetrieveFailed(classId, e));
    }

    private void onCoursesCountRetrieveFailed(String classId, Exception e) {
        Log.e(TAG, "Error retrieving courses count for class: " + classId, e);
    }

    private void onClassNameRetrieveFailed(String classId, Exception e) {
        Log.e(TAG, "Error retrieving class name for ID: " + classId, e);
    }
}
