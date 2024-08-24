package Add_View_Semester_Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
public class SemesterViewModel extends ViewModel {

    private static final String TAG = "SemesterViewModel";
    private Context context; // New member variable to hold the context

    private FirebaseFirestore db;
    private MutableLiveData<List<SemesterModel>> semesterLiveData;
    private ProgressDialog progressDialog;

    public LiveData<List<SemesterModel>> getSemesters(String instituteId) {
        if (semesterLiveData == null) {
            semesterLiveData = new MutableLiveData<>();
            loadSemesters(instituteId);
        }
        return semesterLiveData;
    }

    private void loadSemesters(String instituteId) {
        db = FirebaseFirestore.getInstance();
        List<SemesterModel> semesterList = new ArrayList<>();

        db.collection("InstituteSemesters")
                .whereEqualTo("InstituteID", instituteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot instituteSemesters = task.getResult();
                        if (instituteSemesters.isEmpty()) {
                            informActivityNoSemestersFound();
                            return;
                        }

                        // Create a list to hold the semester IDs
                        List<String> semesterIds = new ArrayList<>();

                        // Iterate over each document in the result
                        for (QueryDocumentSnapshot document : instituteSemesters) {
                            // Get the semester ID from each document
                            String semesterId = document.getString("SemesterID");
                            if (semesterId != null) {
                                // Add the semester ID to the list
                                semesterIds.add(semesterId);
                            }
                        }

                        // Fetch semester details and class counts in parallel
                        fetchSemesterDetailsAndClassCount(semesterIds, semesterList);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchSemesterDetailsAndClassCount(List<String> semesterIds, List<SemesterModel> semesterList) {
        // Create a list to hold batched tasks
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        // Fetch semester details for each semester ID
        for (String semesterId : semesterIds) {
            Task<DocumentSnapshot> semesterTask = db.collection("Semesters").document(semesterId).get();
            tasks.add(semesterTask);
        }

        // Combine all tasks into a single task
        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(semesterSnapshots -> {
                    // Process each semester snapshot
                    for (int i = 0; i < semesterSnapshots.size(); i++) {
                        DocumentSnapshot semesterSnapshot = (DocumentSnapshot) semesterSnapshots.get(i);
                        SemesterModel semester = semesterSnapshot.toObject(SemesterModel.class);
                        if (semester != null) {
                            String semesterId = semesterIds.get(i); // Retrieve semester ID from the list
                            semester.setSemesterID(semesterId);
                            semester.setActive(semesterSnapshot.getBoolean("isActive"));

                            // Fetch class count for each semester in parallel
                            fetchClassCountForSemester(semester, semesterList);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "Error getting semester documents: ", e));
    }

    private void fetchClassCountForSemester(SemesterModel semester, List<SemesterModel> semesterList) {
        db.collection("SemesterClasses")
                .whereEqualTo("SemesterID", semester.getSemesterID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int classCount = task.getResult().size();
                        semester.setClassCount(classCount);
                        semesterList.add(semester);
                        updateSemesterLiveData(semesterList);
                    } else {
                        Log.d(TAG, "Error getting class count documents: ", task.getException());
                    }
                });
    }

    private void informActivityNoSemestersFound() {
        // Check if the activity is still active
        if (context instanceof ManageSemesterActivity) {
            ((ManageSemesterActivity) context).showNoSemestersFoundToast();
        }
    }
    public void deleteSemester(String semesterId, Context context, String semesterName) {
        showLoadingDialog(context);
        db.collection("Semesters").document(semesterId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteInstituteSemester(semesterId,context);
                    Log.d(TAG, semesterName + " deleted successfully");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting semester", e));
    }

    private void deleteInstituteSemester(String semesterId, Context context) {
        db.collection("InstituteSemesters")
                .whereEqualTo("SemesterID", semesterId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        db.collection("InstituteSemesters")
                                .document(documentSnapshot.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Log success messagedismissLoadingDialog();
                                    removeSemesterLocally(semesterId,context);
                                })
                                .addOnFailureListener(e -> {
                                    // Log error message
                                    Log.e(TAG, "Error deleting institute semester", e);
                                    // Handle error if needed
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Log error message
                    Log.e(TAG, "Error getting institute semester documents", e);
                    // Handle error if needed
                });
    }

    private void removeSemesterLocally(String semesterId, Context context) {
        List<SemesterModel> currentSemesters = semesterLiveData.getValue();
        if (currentSemesters != null) {
            // Iterate through the list of semesters and remove the one with the matching ID
            for (SemesterModel semester : currentSemesters) {
                if (semester.getSemesterID().equals(semesterId)) {
                    List<SemesterModel> semesterList = new ArrayList<>(currentSemesters);
                    semesterList.remove(semester);
                    // Update the LiveData with the modified list
                    semesterLiveData.setValue(semesterList);
                    // Exit loop as the semester has been found and removed
                    return;
                }
            }
        }
        // If the semester was not found or the list is empty/null, log a message or handle it as needed
        Log.d(TAG, "Semester with ID " + semesterId + " not found in LiveData or LiveData is empty/null");
    }


    private void updateSemesterLiveData(List<SemesterModel> semesterList) {
        semesterLiveData.setValue(semesterList);
    }
    private void showLoadingDialog(Context context) {

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context); // Assuming 'context' is available in your class
            progressDialog.setMessage("Deleting...");
            progressDialog.setCancelable(false); // Prevent dismissal by tapping outside
        }

        // Show the ProgressDialog
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        // Dismiss the ProgressDialog if it's currently showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

