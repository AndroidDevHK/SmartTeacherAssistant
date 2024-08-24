package View_Classes_For_Repeaters_Selection_Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectClassForRepeatersAcitvity extends AppCompatActivity {

    private static final String TAG = "SelectClassForRepeaters";
    private RecyclerView recyclerView;
    private RepeaterClassAdapter adapter;
    private String classID;
    private AtomicInteger fetchCounter;
    private ProgressDialog progressDialog;
    List<RepeaterClassModel> repeaterClassList = new ArrayList<>();
    private TextView noResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_class_for_repeaters_acitvity);

        // Initialize adapter and RecyclerView
        adapter = new RepeaterClassAdapter();
        recyclerView = findViewById(R.id.recycler_view_repeater_classes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Get class ID from UserInstituteModel
        classID = UserInstituteModel.getInstance(this).getClassId();
        showLoadingDialog("Fetching Classes...");
        // Check if the repeater class list is empty
        if (UserInstituteModel.getInstance(this).getRepeaterClassList().isEmpty()) {
            fetchSemesterClassesData();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        noResultText = findViewById(R.id.noResultText);

        SearchView searchView = findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Select Class to Add Repeaters", true);
    }
    public void filter(String text) {
        List<RepeaterClassModel> filteredList = new ArrayList<>();
        for (RepeaterClassModel item : repeaterClassList) {
            if (item.getClassName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.setRepeaterClassList(filteredList);

        if (filteredList.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE);

        } else {
            noResultText.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!UserInstituteModel.getInstance(this).getRepeaterClassList().isEmpty()) {
            UserInstituteModel.getInstance(this).getRepeaterClassList().clear();
        }
    }

    private void fetchSemesterClassesData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the SemesterClasses collection
        CollectionReference semesterClassesRef = db.collection("SemesterClasses");

        // Get the semester ID from UserInstituteModel
        String semesterID = UserInstituteModel.getInstance(this).getSemesterId();

        semesterClassesRef.whereEqualTo("SemesterID", semesterID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int numberOfClasses = task.getResult().size();

                        if (numberOfClasses == 1) {
                            // No classes available, show the message and finish the activity
                            showNoClassesMessageAndFinish();
                            return;
                        }
                        fetchCounter = new AtomicInteger(task.getResult().size() - 1);

                        // Loop through the result documents
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String classID = document.getString("ClassID");
                            String className = document.getString("ClassName");

                            // Fetch student data for each class
                            fetchStudentDataForClass(classID, className);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showNoClassesMessageAndFinish() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // Show message
        new AlertDialog.Builder(this)
                .setTitle("No Classes Available")
                .setMessage("There are no classes available for repeater selection.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    private void fetchStudentDataForClass(String classID, String className) {
        if (!classID.equals(this.classID)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Reference to the ClassStudents collection
            CollectionReference classStudentsRef = db.collection("Classes")
                    .document(classID)
                    .collection("ClassStudents");

            classStudentsRef.whereEqualTo("IsActive", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int activeStudentCount = task.getResult().size();

                            // If there are active students in the class
                            if (activeStudentCount > 0) {
                                // Create a repeater class model
                                RepeaterClassModel repeaterClass = new RepeaterClassModel();
                                repeaterClass.setRepeaterClassID(classID);
                                repeaterClass.setClassName(className);
                                repeaterClass.setStudentCount(activeStudentCount);

                                // Add repeaterClass to the list in UserInstituteModel
                                UserInstituteModel.getInstance(this).getRepeaterClassList().add(repeaterClass);


                                // Decrement the counter
                                if (fetchCounter.decrementAndGet() == 0) {
                                    // Once all classes are fetched, sort the list by class name
                                    Collections.sort(UserInstituteModel.getInstance(this).getRepeaterClassList(),
                                            (c1, c2) -> c1.getClassName().compareToIgnoreCase(c2.getClassName()));

                                    repeaterClassList = UserInstituteModel.getInstance(this).getRepeaterClassList();
                                    adapter.setRepeaterClassList(UserInstituteModel.getInstance(this).getRepeaterClassList());
                                    dismissLoadingDialog();
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void showLoadingDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
