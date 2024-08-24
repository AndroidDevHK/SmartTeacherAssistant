package Display_Semester_Teacher_All_Courses_Attendance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.BorderRadius;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.nextgen.hasnatfyp.R;

import Display_Course_Attendance_Activity.DisplayClassCourseAttendanceActivity;
import Display_Course_Attendance_Activity.StudentAttendanceRecordModel;
import Display_Teacher_Courses_Attendance_Activity.TeacherCourseAttendanceModel;
import Report_Making_Files.ExcelReportGeneratorCompleteCourseAttendance;
import Report_Making_Files.PDFCourseAttendanceSummaryGenerator;
import Report_Making_Files.SemesterTeachersAttendanceReportGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplaySemesterTeachersAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "SemesterTeachersAttendance";
    private FirebaseFirestore db;
    private List<SemesterTeachersAttendanceModel> teachersAttendanceList;
    private RecyclerView recyclerView;
    private SemesterTeachersAttendanceAdapter adapter;
    private ProgressDialog progressDialog;
    private  SearchView searchView;
    private TextView NoResultTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_semester_teachers_attendance);

        db = FirebaseFirestore.getInstance();
        teachersAttendanceList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view_teacher_attendance);
        adapter = new SemesterTeachersAttendanceAdapter(teachersAttendanceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        NoResultTextView = findViewById(R.id.noResultTextView);
        searchView = findViewById(R.id.simpleSearchView);
        showLoadingDialog();
        String semesterID = extractSemesterIDFromIntent();
        retrieveTeacherUsernames(semesterID);


        setupToolbar();

        setupSearchFunctionality();


        setupSecondLogoClickListener();
    }

    @SuppressLint("SetTextI18n")
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("View Attendance Report");
    }

    private void filterTeachersAttendanceList(String query) {
        List<SemesterTeachersAttendanceModel> filteredList = new ArrayList<>();
        for (SemesterTeachersAttendanceModel record : teachersAttendanceList) {
            if (record.getTeacherName().toLowerCase().contains(query.toLowerCase())
                    || record.getTeacherUserName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(record);
            }
        }
        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            NoResultTextView.setVisibility(View.VISIBLE);
        } else {
            NoResultTextView.setVisibility(View.GONE);
        }
    }

    private void setupSearchFunctionality() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTeachersAttendanceList(newText);
                return true;
            }
        });
    }


    private void setupSecondLogoClickListener() {
        ImageView logo2 = findViewById(R.id.logo2);
        logo2.setOnClickListener(v -> {
            Uri pdfUri = new SemesterTeachersAttendanceReportGenerator(this).generatePdf(teachersAttendanceList);
            if (pdfUri != null) {
                showPdfOption(pdfUri);
            } else {
                Toast.makeText(this, "PDF URI is null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPdfOption(Uri pdfUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("PDF Options")
                .setMessage("Choose an option:")
                .setPositiveButton("View PDF", (dialog, which) -> {
                    viewPdf(pdfUri);
                })
                .setNegativeButton("Share PDF", (dialog, which) -> {
                    sharePdf(pdfUri);
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(viewIntent);
    }


    @Override
    public boolean onSupportNavigateUp() {
        // Handle back button press
        onBackPressed();
        return true;
    }

    private String extractSemesterIDFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("semesterID")) {
            return intent.getStringExtra("semesterID");
        } else {
            throw new IllegalArgumentException("Semester ID not found in intent extras.");
        }
    }


    private void retrieveTeacherUsernames(String semesterId) {
        Log.d(TAG, "retrieveTeacherUsernames: Retrieving teacher usernames for semester ID: " + semesterId);

        CollectionReference teacherCoursesRef = db.collection("TeacherCourses");

        teacherCoursesRef.whereEqualTo("SemesterID", semesterId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> teacherUsernames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String teacherUsername = document.getString("TeacherUsername");
                            if (!teacherUsernames.contains(teacherUsername)) {
                                teacherUsernames.add(teacherUsername);
                            }
                        }
                        if (teacherUsernames.isEmpty()) {
                            // No teachers found, dismiss the dialog and show an alert dialog
                            dismissLoadingDialog();
                            showNoTeachersFoundDialog();
                            return;
                        }
                        Log.d(TAG, "retrieveTeacherUsernames: Retrieved teacher usernames: " + teacherUsernames);

                        // Track total number of teachers and processed teachers
                        AtomicInteger totalTeachers = new AtomicInteger(teacherUsernames.size());
                        AtomicInteger processedTeachers = new AtomicInteger(0);

                        // For each teacher username, retrieve teacher name and attendance
                        for (String username : teacherUsernames) {
                            retrieveTeacherNameAndAttendance(username, totalTeachers, processedTeachers);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showNoTeachersFoundDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Teachers Found")
                .setMessage("There are no teachers available.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void retrieveTeacherNameAndAttendance(String teacherUsername, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        Log.d(TAG, "retrieveTeacherNameAndAttendance: Retrieving teacher name and attendance for username: " + teacherUsername);

        CollectionReference teachersRef = db.collection("Teachers");

        teachersRef.whereEqualTo("Username", teacherUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot teacherDocument : task.getResult()) {
                            String teacherName = teacherDocument.getString("TeacherName");

                            Log.d(TAG, "retrieveTeacherNameAndAttendance: Retrieved teacher name: " + teacherName);

                            // Retrieve all courses taught by this teacher
                            retrieveTeacherCourses(teacherUsername, teacherName, totalTeachers, processedTeachers);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                })
                .addOnCompleteListener(task -> {
                    // Check if all teachers' details have been processed
                });
    }



    private void retrieveTeacherCourses(String teacherUsername, String teacherName, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        Log.d(TAG, "retrieveTeacherCourses: Retrieving courses for teacher: " + teacherName + " (Username: " + teacherUsername + ")");

        CollectionReference teacherCoursesRef = db.collection("TeacherCourses");
        teacherCoursesRef.whereEqualTo("TeacherUsername", teacherUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> courseDocuments = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            courseDocuments.add(doc);
                        }
                        if (courseDocuments.isEmpty()) {
                            // No courses for this teacher
                            checkAllTeachersProcessed(totalTeachers, processedTeachers);
                            return;
                        }

                        AtomicInteger totalCourses = new AtomicInteger(courseDocuments.size());
                        AtomicInteger processedCourses = new AtomicInteger(0);

                        for (DocumentSnapshot courseDocument : courseDocuments) {
                            String courseId = courseDocument.getString("CourseID");
                            String classId = courseDocument.getString("ClassID");

                            Log.d(TAG, "retrieveTeacherCourses: Retrieved course ID: " + courseId + " and class ID: " + classId + " for teacher: " + teacherName);

                            // Retrieve course details
                            retrieveCourseDetails(courseId, teacherUsername, teacherName, classId, totalCourses, processedCourses, totalTeachers, processedTeachers);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void checkAllTeachersProcessed(AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        int total = totalTeachers.get();
        int processed = processedTeachers.incrementAndGet();
        if (processed >= total) {
            // All teachers' attendance data processed
            dismissLoadingDialog();
        }
    }


    private void retrieveCourseDetails(String courseId, String teacherUsername, String teacherName, String classId, AtomicInteger totalCourses, AtomicInteger processedCourses, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        Log.d(TAG, "retrieveCourseDetails: Retrieving details for course ID: " + courseId);

        CollectionReference classCoursesRef = db.collection("ClassCourses");
        classCoursesRef.document(classId).collection("ClassCoursesSubcollection").document(courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot courseDocument = task.getResult();
                        if (courseDocument.exists()) {
                            String courseName = courseDocument.getString("CourseName");
                            String creditHoursStr = courseDocument.getString("CreditHours");
                            int creditHours;
                            try {
                                creditHours = Integer.parseInt(creditHoursStr);
                            } catch (NumberFormatException e) {
                                // Handle parsing error
                                Log.e(TAG, "retrieveCourseDetails: Error parsing credit hours string", e);
                                return;
                            }

                            Log.d(TAG, "retrieveCourseDetails: Retrieved course details for course ID: " + courseId);
                            Log.d(TAG, "retrieveCourseDetails: Course Name: " + courseName + ", Credit Hours: " + creditHours);

                            // Retrieve class name
                            retrieveClassName(classId, courseId, teacherUsername, teacherName, courseName, creditHours, totalCourses, processedCourses, totalTeachers, processedTeachers);
                        } else {
                            Log.d(TAG, "retrieveCourseDetails: No course document found for course ID: " + courseId);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }


    private void retrieveClassName(String classId, String courseId, String teacherUsername, String teacherName, String courseName, int creditHours, AtomicInteger totalCourses, AtomicInteger processedCourses, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        CollectionReference classRef = db.collection("Classes");
        classRef.document(classId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot classDocument = task.getResult();
                        if (classDocument.exists()) {
                            String className = classDocument.getString("ClassName");
                            retrieveAttendanceData(courseId, teacherUsername, teacherName, courseName, className, classId, creditHours, totalCourses, processedCourses, totalTeachers, processedTeachers);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }


    private void retrieveAttendanceData(String courseId, String teacherUsername, String teacherName, String courseName, String className, String classId, int creditHours, AtomicInteger totalCourses, AtomicInteger processedCourses, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        db.collection("CourseAttendance")
                .whereEqualTo("CourseID", courseId)
                .whereEqualTo("IsRepeater", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.isEmpty()) {
                            Log.d(TAG, "No attendance data found for course: " + courseName + " and teacher: " + teacherName);

                            // Increment processed courses and teachers
                            processedCourses.incrementAndGet();
                            if (processedCourses.get() == totalCourses.get() && processedTeachers.get() == totalTeachers.get()) {
                                dismissLoadingDialog();
                            }

                            // Call processAttendanceData with default values
                            processAttendanceData(courseId, teacherUsername, teacherName, courseName, className, classId, creditHours, "N/A", "N/A", Collections.emptyList(), totalCourses, processedCourses, totalTeachers, processedTeachers);
                            return;
                        }

                        int totalAttendanceToProcess = documents.size(); // Get total attendance records
                        processedCourses.incrementAndGet();
                        if (processedCourses.get() == totalCourses.get() && processedTeachers.get() == totalTeachers.get()) {
                            dismissLoadingDialog();
                        }

                        AtomicInteger attendanceProcessedCount = new AtomicInteger(0); // Initialize processed count

                        List<String> allAttendanceDates = new ArrayList<>(); // List to store all attendance dates

                        for (DocumentSnapshot document : documents) {
                            String attendanceId = document.getString("AttendanceID");
                            final List<String> teacherAttendance = new ArrayList<>(); // List to store attendance dates for this iteration
                            readAttendanceData(attendanceId, new AttendanceDateCallback() {
                                @Override
                                public void onAttendanceDateRead(String attendanceDate) {
                                    teacherAttendance.add(attendanceDate);
                                }

                                @Override
                                public void onAllAttendanceDatesRead() {
                                    allAttendanceDates.addAll(teacherAttendance);
                                    attendanceProcessedCount.getAndIncrement();

                                    if (attendanceProcessedCount.get() == totalAttendanceToProcess) {
                                        Collections.sort(allAttendanceDates);

                                        String firstAttendanceDate = allAttendanceDates.get(0);
                                        String lastAttendanceDate = allAttendanceDates.get(allAttendanceDates.size() - 1);

                                        processAttendanceData(courseId, teacherUsername, teacherName, courseName, className, classId, creditHours, firstAttendanceDate, lastAttendanceDate, allAttendanceDates, totalCourses, processedCourses, totalTeachers, processedTeachers);
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void processAttendanceData(String courseId, String teacherUsername, String teacherName, String courseName, String className, String classId, int creditHours, String firstAttendanceDate, String lastAttendanceDate, List<String> allAttendanceDates, AtomicInteger totalCourses, AtomicInteger processedCourses, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        int classesTaken = allAttendanceDates.size();

        // Calculate the expected classes based on the credit hours
        int expectedClasses = 0;

        // Calculate the percentage
        double percentage = 0;
        List<String> teacherAttendance = new ArrayList<>(allAttendanceDates);
        TeacherCourseAttendanceModel attendanceModel = new TeacherCourseAttendanceModel(
                allAttendanceDates.size(), // Number of classes taken
                className,
                courseName,
                creditHours,
                firstAttendanceDate,
                lastAttendanceDate,
                teacherAttendance, // Teacher attendance list
                expectedClasses, // Initialize expected classes to 0
                percentage // Initialize percentage to 0.0
        );

        // Add the attendance model to the respective teacher's attendance list
        addAttendanceToTeacherList(teacherUsername, teacherName, attendanceModel,totalTeachers,processedTeachers);
    }

    private void addAttendanceToTeacherList(String teacherUsername, String teacherName, TeacherCourseAttendanceModel attendanceModel, AtomicInteger totalTeachers, AtomicInteger processedTeachers) {
        // Check if the teacher's attendance list already exists
        SemesterTeachersAttendanceModel semesterTeachersAttendanceModel = findTeacherAttendanceModel(teacherUsername);

        if (semesterTeachersAttendanceModel != null) {
            // Add the course attendance to the existing teacher's attendance list
            semesterTeachersAttendanceModel.addCourseAttendance(attendanceModel);
        } else {
            semesterTeachersAttendanceModel = new SemesterTeachersAttendanceModel(teacherUsername, teacherName);
            semesterTeachersAttendanceModel.addCourseAttendance(attendanceModel);
            teachersAttendanceList.add(semesterTeachersAttendanceModel);
        }

        // Sort the courses within the semesterTeachersAttendanceModel by course name in ascending order
        Collections.sort(semesterTeachersAttendanceModel.getTeacherAttendance(), Comparator.comparing(TeacherCourseAttendanceModel::getCourseName));

        // Sort the teachersAttendanceList by teacher username in ascending order
        Collections.sort(teachersAttendanceList, Comparator.comparing(SemesterTeachersAttendanceModel::getTeacherUserName));

        // Check if all teachers' attendance data has been processed
        int total = totalTeachers.get();
        int processed = processedTeachers.incrementAndGet();
        if (processed >= total) {
            // All teachers' attendance data processed
            dismissLoadingDialog();
            adapter.notifyDataSetChanged();
        }
    }



    private SemesterTeachersAttendanceModel findTeacherAttendanceModel(String teacherUsername) {
        // Check if the teacher's attendance list already exists
        for (SemesterTeachersAttendanceModel model : teachersAttendanceList) {
            if (model.getTeacherUserName().equals(teacherUsername)) {
                return model;
            }
        }
        return null;
    }


    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    interface AttendanceDateCallback {
        void onAttendanceDateRead(String attendanceDate);
        void onAllAttendanceDatesRead();
    }



    private void readAttendanceData(String attendanceId, AttendanceDateCallback callback) {
        db.collection("Attendance")
                .document(attendanceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot attendanceSnapshot = task.getResult();
                        if (attendanceSnapshot.exists()) {
                            String attendanceDate = attendanceSnapshot.getString("AttendanceDate");
                            callback.onAttendanceDateRead(attendanceDate);
                        }
                    } else {
                        Log.d(TAG, "Error getting attendance document: ", task.getException());
                    }
                    callback.onAllAttendanceDatesRead();
                });
    }



}