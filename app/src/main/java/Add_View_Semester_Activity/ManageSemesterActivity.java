package Add_View_Semester_Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.List;

public class ManageSemesterActivity extends AppCompatActivity implements SemesterDialogHelper.SemesterDialogListener {

    private static final String TAG = "ManageSemester";
    private SemesterViewModel semesterViewModel;
    private SemesterAdapter adapter;
    private List<SemesterModel> originalSemesters;
    UserInstituteModel userInstituteModel;
    RecyclerView recyclerView;
    TextView emptyStateTextView;
    private RelativeLayout emptyStateLayout;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_semester);


        if (NetworkUtils.isInternetConnected(this))
        {
            showProgressDialog();
            initializeViews();
            setupRecyclerView();
            setupViewModel();
        }
        else
        {
            Toast.makeText(this,"Please Connect Internet to Continue..",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    public void showNoSemestersFoundToast() {
        hideProgressDialog();
        showEmptyState();
    }

    private void setupViewModel() {
        semesterViewModel = new ViewModelProvider(this).get(SemesterViewModel.class);
        semesterViewModel.setContext(this);
        semesterViewModel.getSemesters(userInstituteModel.getInstituteId()).observe(this, semesters -> {
            if (semesters != null) {
                originalSemesters = semesters;
                filterSemesters("");
                adapter = new SemesterAdapter(ManageSemesterActivity.this, originalSemesters, semesterViewModel);
                recyclerView.setAdapter(adapter);
                hideProgressDialog();
                if (originalSemesters.isEmpty()) {
                    showEmptyState();
                    hideProgressDialog();
                } else {
                    hideEmptyState();
                }

            } else {
                adapter = new SemesterAdapter(ManageSemesterActivity.this, originalSemesters, semesterViewModel);
                Log.d(TAG, "Semester list is null");
                Toast.makeText(ManageSemesterActivity.this, "Error fetching semesters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void initializeViews() {
        // Initialize floating action button
        ImageView fabAddSemester = findViewById(R.id.addSemesterButton);
        ActivityManager activityManager = (ActivityManager) getApplication();
        activityManager.addActivity(this);

        // Set click listener to open dialog when floating action button is clicked
        fabAddSemester.setOnClickListener(view -> openSemesterDialog());
        userInstituteModel = UserInstituteModel.getInstance(this);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        // Initialize SearchView
        SearchView searchView = findViewById(R.id.simpleSearchView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        SetupSearchBar(searchView);
    }

    private void SetupSearchBar(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSemesters(newText); // Filter semesters based on the search query
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.semesterRecyclerView);
        adapter = new SemesterAdapter(this, new ArrayList<>(), semesterViewModel); // Pass the ViewModel
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize empty state TextView
        emptyStateTextView = findViewById(R.id.noResultTextView);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Manage Semesters / Years", true);
    }

    // Method to open semester dialog
    private void openSemesterDialog() {
        SemesterDialogHelper dialogHelper = new SemesterDialogHelper(this);
        dialogHelper.openSemesterDialog(this,null); // Pass the activity as the listener
    }

    // Method to filter semesters based on search query
    private void filterSemesters(String query) {
        if (originalSemesters != null) {
            List<SemesterModel> filteredList = new ArrayList<>();
            for (SemesterModel semester : originalSemesters) {
                if (TextUtils.isEmpty(query) || semester.getSemesterName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(semester);
                }
            }
            adapter.setSemesters(filteredList);

            if (filteredList.isEmpty() && !(originalSemesters.size()==0)) {
                showNoResultMessage();
            } else {
                hideNoResultMessage();
            }
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showNoResultMessage() {
        TextView noResultTextView = findViewById(R.id.noResultTextView);
        noResultTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoResultMessage() {
        TextView noResultTextView = findViewById(R.id.noResultTextView);
        noResultTextView.setVisibility(View.GONE);
    }

    private void showSearchBar() {
        SearchView searchView = findViewById(R.id.simpleSearchView);
        searchView.setVisibility(View.VISIBLE);
    }

    private void hideSearchBar() {
        SearchView searchView = findViewById(R.id.simpleSearchView);
        searchView.setVisibility(View.GONE);
    }

    // Callback method to handle the addition of a new semester
    @Override
    public void onSemesterAdded(SemesterModel semester) {
        if (originalSemesters == null) {
            originalSemesters = new ArrayList<>(); // Initialize the list if null
        }
        originalSemesters.add(semester); // Add the new semester to the list
        adapter.setSemesters(originalSemesters); // Refresh the adapter with the updated list

        // Check if the "No semester/year found" state is visible and hide it
        if (originalSemesters.size() > 0 && emptyStateLayout.getVisibility() == View.VISIBLE) {
            hideEmptyState();
        }

        // Show search bar and RecyclerView
        showSearchBar();
        recyclerView.setVisibility(View.VISIBLE);
        hideNoResultMessage();
    }
}
