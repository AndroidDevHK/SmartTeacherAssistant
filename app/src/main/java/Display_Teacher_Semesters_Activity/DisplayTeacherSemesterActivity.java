package Display_Teacher_Semesters_Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.SimpleLoadingDialog;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import Display_Teacher_Semester_Classes_Acitivity.DataStorageHelperTeacherSemesters;

public class DisplayTeacherSemesterActivity extends AppCompatActivity {

    private static final String TAG = "DisplayTeacherSemester";

    private RecyclerView recyclerView;
    private TeacherSemesterAdapter adapter;
    private List<TeacherSemestersModel> teacherSemestersList;

    private SearchView searchView;
    private TextView noResultTextView;
    private SimpleLoadingDialog loadingDialog;
    String TeacherUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_teacher_semester);


        loadingDialog = new SimpleLoadingDialog(this, "Loading....");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        teacherSemestersList = new ArrayList<>();
        noResultTextView = findViewById(R.id.noResultTextView);
        if(UserInstituteModel.getInstance(this).isSoloUser()) {
            TeacherUserName = UserInstituteModel.getInstance(this).getInstituteId();
            Log.e(TAG, TeacherUserName);

        }
        else
        {TeacherUserName = TeacherInstanceModel.getInstance(this).getTeacherUsername();}
        adapter = new TeacherSemesterAdapter(teacherSemestersList, this);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.simpleSearchView);
        setupSearchView();
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        if (NetworkUtils.isInternetConnected(this)) {
            retrieveSemesterData(TeacherUserName);
        } else {
            // Read offline data and display a toast
            List<TeacherSemestersModel> offlineData = DataStorageHelperTeacherSemesters.readTeacherSemestersListLocally(this);
            if (!offlineData.isEmpty()) {
                teacherSemestersList.addAll(offlineData);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "No internet connection. Showing recently fetched data.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No internet connection. No offline data available.", Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void setupSearchView() {
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

    private void filter(String query) {
        List<TeacherSemestersModel> filteredList = new ArrayList<>();
        for (TeacherSemestersModel model : teacherSemestersList) {
            if (model.getSemesterName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(model);
            }
        }
        adapter.setClassList(filteredList);
        if (filteredList.isEmpty()) {
            noResultTextView.setVisibility(View.VISIBLE);
        } else {
            noResultTextView.setVisibility(View.GONE);
        }
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "My Semesters / Years", true);
    }

    private void retrieveSemesterData(String teacherUserName) {
        loadingDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference teacherSemestersRef = db.collection("TeacherSemesters");

        teacherSemestersRef.whereEqualTo("TeacherUserName", teacherUserName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> teacherSemesterDocs = task.getResult().getDocuments();
                        int totalCount = teacherSemesterDocs.size();

                        if (totalCount == 0) {
                            loadingDialog.dismiss();
                            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                            searchView.setVisibility(View.GONE);
                            return;
                        }

                        List<Task<DocumentSnapshot>> semesterTasks = new ArrayList<>();
                        for (DocumentSnapshot document : teacherSemesterDocs) {
                            String semesterID = document.getString("SemesterID");
                            if (semesterID != null) {
                                semesterTasks.add(db.collection("Semesters").document(semesterID).get());
                            }
                        }

                        Tasks.whenAllSuccess(semesterTasks).addOnSuccessListener(semesterSnapshots -> {
                            List<DocumentSnapshot> semesterDocs = new ArrayList<>();
                            for (Object obj : semesterSnapshots) {
                                semesterDocs.add((DocumentSnapshot) obj);
                            }

                            List<Task<QuerySnapshot>> courseTasks = new ArrayList<>();

                            for (DocumentSnapshot document : semesterDocs) {
                                if (document.exists()) {
                                    String semesterID = document.getId();
                                    courseTasks.add(db.collection("TeacherCourses")
                                            .whereEqualTo("SemesterID", semesterID)
                                            .whereEqualTo("TeacherUsername", teacherUserName)
                                            .get());
                                }
                            }

                            Tasks.whenAllSuccess(courseTasks).addOnSuccessListener(courseSnapshots -> {
                                for (int i = 0; i < semesterDocs.size(); i++) {
                                    DocumentSnapshot semesterDoc = semesterDocs.get(i);
                                    QuerySnapshot courseSnapshot = (QuerySnapshot) courseSnapshots.get(i);

                                    if (semesterDoc.exists()) {
                                        boolean isActive = semesterDoc.getBoolean("isActive");
                                        String semesterName = semesterDoc.getString("semesterName");
                                        String startDate = semesterDoc.getString("startDate");
                                        String endDate = semesterDoc.getString("endDate");

                                        if (isActive && semesterName != null && startDate != null && endDate != null) {
                                            int numberOfClasses = courseSnapshot.size();
                                            if (numberOfClasses != 0)
                                            {TeacherSemestersModel model = new TeacherSemestersModel(semesterDoc.getId(), semesterName, startDate, endDate, numberOfClasses);
                                            teacherSemestersList.add(model);
                                        }
                                        }
                                    }
                                }
                                DataStorageHelperTeacherSemesters.storeTeacherSemestersListLocally(DisplayTeacherSemesterActivity.this, teacherSemestersList);

                                adapter.notifyDataSetChanged();
                                loadingDialog.dismiss();

                                if (teacherSemestersList.isEmpty()) {
                                    findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                                }

                                if (teacherSemestersList.size() > 3) {
                                    searchView.setVisibility(View.VISIBLE);
                                } else {
                                    searchView.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Error retrieving teacher courses: ", e);
                                loadingDialog.dismiss();
                                findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                            });
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error retrieving semester documents: ", e);
                            loadingDialog.dismiss();
                            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                        });
                    } else {
                        Log.e(TAG, "Error retrieving semester IDs: ", task.getException());
                        loadingDialog.dismiss();
                        findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                    }
                });
    }
}
