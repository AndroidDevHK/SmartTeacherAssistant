package View_Class_Students_Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nextgen.hasnatfyp.PDFClassStudentListGenerator;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.StudentViewModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.List;


public class DisplayClassStudentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private StudentViewModel studentViewModel;
    private String classId;
    private SearchView searchView;
    private TextView noResultsTextView;
    private List<StudentModel> studentList; // Store the student list for PDF generation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_students);
        extractClassIdFromIntent();
        initializeRecyclerView();
        initializeViewModel();
        observeStudentListChanges();
        setupSearchView();
        setupDownloadButton(); // Setup the download button
        noResultsTextView = findViewById(R.id.no_results_text_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Class - " + UserInstituteModel.getInstance(this).getClassName(), true);
    }

    private void extractClassIdFromIntent() {
        classId = getIntent().getStringExtra("classId");
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_students);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initializeViewModel() {
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);
        studentViewModel.init(classId, this);
    }

    private void setupSearchView() {
        searchView = findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStudents(newText);
                return true;
            }
        });
    }

    private void filterStudents(String query) {
        studentViewModel.filterStudents(query);
    }

    private void observeStudentListChanges() {
        studentViewModel.getStudentList().observe(this, students -> {
            this.studentList = students; // Update the stored student list
            updateRecyclerView(students);
        });
    }

    private void updateRecyclerView(List<StudentModel> studentList) {
        if (studentList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noResultsTextView.setVisibility(View.GONE);
            if (adapter == null) {
                setupRecyclerView(studentList);
            } else {
                adapter.setStudentList(studentList);
            }
        }
    }

    private void setupRecyclerView(List<StudentModel> studentList) {
        adapter = new StudentAdapter(this, studentList, classId);
        recyclerView.setAdapter(adapter);
    }

    private void setupDownloadButton() {
        MaterialButton downloadButton = findViewById(R.id.download_button); // Assuming you have a download button in your layout
        downloadButton.setOnClickListener(v -> {
            if (studentList != null && !studentList.isEmpty()) {
                Uri pdfUri = PDFClassStudentListGenerator.generatePdf(studentList, this);
                if (pdfUri != null) {
                    showPdfOption(pdfUri);
                } else {
                    showError("Failed to generate PDF.");
                }
            } else {
                showError("No students available to generate the report.");
            }
        });
    }

    private void showPdfOption(Uri pdfUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("PDF Options")
                .setMessage("Choose an option:")
                .setPositiveButton("View PDF", (dialog, which) -> viewPdf(pdfUri))
                .setNegativeButton("Share PDF", (dialog, which) -> sharePdf(pdfUri))
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void sharePdf(Uri pdfUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share PDF using"));
    }

    private void viewPdf(Uri pdfUri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(pdfUri, "application/pdf");
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(viewIntent);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
