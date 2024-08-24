package View_Semester_Classes_Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.Collections;
import java.util.List;

public class ManageClassesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private ClassViewModel classViewModel;
    private String semesterId;
    private SearchView searchView;
    private TextView noResultTextView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_class);

        if (!NetworkUtils.isInternetConnected(this))
        {
            Toast.makeText(this,"Please Connect Internet to Continue..",Toast.LENGTH_LONG).show();
                finish();
        }

        semesterId = UserInstituteModel.getInstance(this).getSemesterId();
        initializeViews();
        setupRecyclerView();
        setupViewModel();
        setupSearchView();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.ClassRecyclerView);
        searchView = findViewById(R.id.simpleSearchView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
        noResultTextView = findViewById(R.id.noResultTextView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Classes...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ActivityManager.getInstance().addActivityForKillCourseDeletion(this);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivityForKillCourseDeletion(this);
    }
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classViewModel = new ViewModelProvider(this).get(ClassViewModel.class);
        adapter = new ClassAdapter(this, classViewModel);
        adapter.setResultTextView(noResultTextView);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        classViewModel = new ViewModelProvider(this).get(ClassViewModel.class);
        classViewModel.setSemesterId(semesterId);
        classViewModel.getClassList().observe(this, this::updateClassList);
    }

    private void updateClassList(List<ClassModel> classList) {
        Collections.sort(classList, (class1, class2) -> class1.getClassName().compareToIgnoreCase(class2.getClassName()));
        adapter.setClassList(classList);
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Manage Classes - " + UserInstituteModel.getInstance(this).getSemesterName(), true);
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }
}
