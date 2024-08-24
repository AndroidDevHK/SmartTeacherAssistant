package DisplayStudentCompleteAttendanceEvaluation_Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.StudentCourseDetailsAdapter;
import com.nextgen.hasnatfyp.StudentEvaluationModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import Report_Making_Files.StudentWiseReportGenerator;

public class DisplayStudentCompleteAttendanceEvaluationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    String studentRollNo;
 
    String SemesterID;
    private RecyclerView recyclerViewStudents;
    String StudentName;
    private TextView studentNameTextView;
    private TextView rollNoTextView;
    private TextView ClassNameTextView;
    String ClassName;
    private  List<StudentCourseAttendanceEvaluationModel> courseModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_complete_attendance_evaluation);

        db = FirebaseFirestore.getInstance();

       studentRollNo = getIntent().getStringExtra("STUDENT_ROLL_NO");
        String classId = getIntent().getStringExtra("CLASS_ID");
      SemesterID = getIntent().getStringExtra("SEMESTER_ID");
        StudentName = getIntent().getStringExtra("STUDENT_NAME");
        ClassName = getIntent().getStringExtra("CLASS_NAME");

        studentNameTextView = findViewById(R.id.StudentNameTextView);
        rollNoTextView = findViewById(R.id.RollNoTextView);
        ClassNameTextView = findViewById(R.id.ClassNameTextView);
        if (studentRollNo != null && classId != null) {
            fetchStudentCourses(studentRollNo, classId,SemesterID);
        } else {
            Log.e("Activity Error", "Student roll number or class ID is null.");
        }


        setCardDetails(StudentName,studentRollNo,ClassName);

        setupToolbar();

        setupSecondLogoClickListener();
    }
    @SuppressLint("SetTextI18n")
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("View Student Report");
    }
    private void setupSecondLogoClickListener() {
        ImageView logo2 = findViewById(R.id.logo2);
        logo2.setOnClickListener(v -> {
            File directory = getExternalFilesDir(null);
            if (directory != null) {

                StudentWiseReportGenerator reportGenerator = new StudentWiseReportGenerator(this);
                Uri generatedFileUri = reportGenerator.generateReport(UserInstituteModel.getInstance(this).getSemesterName(), StudentName, studentRollNo, courseModels, ClassName);
                if (generatedFileUri != null) {
                    showPdfOption(generatedFileUri);
                } else {
                    Toast.makeText(this, "Failed to generate report", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Error accessing external files directory", Toast.LENGTH_LONG).show();
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

    private void setCardDetails(String studentName, String rollNo, String className) {
        studentNameTextView.setText(studentName);
        rollNoTextView.setText(rollNo);
        ClassNameTextView.setText(className);

    }
    private void fetchStudentCourses(String studentRollNo, String classId, String semesterID) {
        ProgressDialogHelper.showProgressDialog(this, "Loading Data...");
        db.collection("CoursesStudents")
                .whereEqualTo("SemesterID", semesterID)
                .whereEqualTo("StudentRollNo", studentRollNo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<StudentCourseAttendanceEvaluationModel> courseModels = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract course details and add to the evaluation list
                            String courseId = document.getString("CourseID");
                            boolean isRepeater = document.getBoolean("isRepeater");
                            String temp1 = document.getString("ClassID");
                            String temp2 = document.getString("CourseClassID");
                            String classID;
                            if (!isRepeater) {
                                classID = temp1;
                            } else {
                                classID = temp2;
                            }

                            if (courseId != null) {
                                // Create a new instance of StudentCourseAttendanceEvaluationModel
                                StudentCourseAttendanceEvaluationModel courseModel = new StudentCourseAttendanceEvaluationModel(
                                        courseId,
                                        null, // Set studentEvalList to null for now
                                        null, // Set allEvaluationTotal to null for now
                                        null, // Set allEvaluationObtainedMarks to null for now
                                        null, // Set percentage to null for now
                                        0,    // Set totalCount to 0 for now
                                        isRepeater, // Set isRepeater from the parameter
                                        0,    // Set presents to 0 for now
                                        0,    // Set absents to 0 for now
                                        0,    // Set leaves to 0 for now
                                        null, // Set firstDate to null for now
                                        null, // Set lastDate to null for now
                                        0,    // Set presentPercentage to 0 for now
                                        null, // Set courseName to null for now
                                        classID
                                );

                                // Add the courseModel to the list
                                courseModels.add(courseModel);
                            }
                        }

                        if (courseModels.isEmpty()) {
                            ProgressDialogHelper.dismissProgressDialog(); // Dismiss progress dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Error");
                            builder.setMessage("No courses found for the student.");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                finish(); // Finish the activity
                            });
                            builder.setCancelable(false);
                            builder.show();
                        } else {
                            // Now we have all course models with their IDs, proceed to read course names
                            Log.d("Firestore", "Number of courses found: " + courseModels.size());
                            readCourseNames(courseModels, studentRollNo);
                        }
                    } else {
                        Log.e("Firestore Error", "Error fetching courses: ", task.getException());
                        // Handle the error
                    }
                });
    }

    private void readCourseNames(List<StudentCourseAttendanceEvaluationModel> courseModels, String studentRollNo) {
        // Count the number of courses processed
        AtomicInteger coursesProcessed = new AtomicInteger(0);

        // Iterate through each course model to read its course name
        for (StudentCourseAttendanceEvaluationModel courseModel : courseModels) {
            String courseId = courseModel.getCourseId();
            String classID = courseModel.getClassID();


            db.collection("ClassCourses") // Assuming "ClassCourses" is the name of the subcollection
                    .document(classID) // Assuming "classID" identifies the document in the "ClassCourses" subcollection
                    .collection("ClassCoursesSubcollection") // Assuming "ClassCoursesSubcollection" is the name of the subcollection containing course documents
                    .document(courseId) // Assuming "courseId" identifies the course document
                    .get()
                    .addOnSuccessListener(courseSnapshot -> {
                        if (courseSnapshot.exists()) {
                            String courseName = courseSnapshot.getString("CourseName");
                            courseModel.setCourseName(courseName);

                            // Increment the count of processed courses
                            int count = coursesProcessed.incrementAndGet();
                            Log.d("Firestore", "Course name retrieved for courseId: " + courseId + ", Course Name: " + courseName);

                            // Check if all course names are read
                            if (count == courseModels.size()) {
                                // When all course names are read, pass the list to read attendance
                                Log.d("Firestore", "All course names retrieved. Proceeding to read attendance.");
                                readStudentCourseAttendance(courseModels, studentRollNo);
                            }
                        } else {
                            Log.e("Firestore Error", "Course document does not exist for courseId: " + courseId);
                            // Handle the case where the course document does not exist
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore Error", "Error fetching course name for courseId: " + courseId + ". Error: " + e.getMessage());
                        // Handle any errors that occur during the operation
                    });
        }
    }


    private void readStudentCourseAttendance(List<StudentCourseAttendanceEvaluationModel> courses, String studentRollNo) {
        AtomicInteger processedCourses = new AtomicInteger(0); // Counter to track processed courses

        // Iterate over each course in the list
        for (StudentCourseAttendanceEvaluationModel course : courses) {
            // Construct the document ID for the student's course attendance
            String attendanceDocumentId = studentRollNo + "_" + course.getCourseId();

            // Retrieve the attendance data from Firestore
            db.collection("StudentCourseAttendanceList")
                    .document(attendanceDocumentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> attendanceList = (List<String>) documentSnapshot.get("AttendanceIDs");

                            if (attendanceList != null && !attendanceList.isEmpty()) {
                                AtomicInteger presents = new AtomicInteger(0);
                                AtomicInteger absents = new AtomicInteger(0);
                                AtomicInteger leaves = new AtomicInteger(0);
                                List<String> dates = new ArrayList<>();

                                // Iterate over each attendance ID and fetch attendance details
                                for (String attendanceId : attendanceList) {
                                    // Read attendance status from CourseStudentsAttendance collection
                                    db.collection("CourseStudentsAttendance")
                                            .document(studentRollNo + "_" + attendanceId)
                                            .get()
                                            .addOnSuccessListener(attendanceSnapshot -> {
                                                if (attendanceSnapshot.exists()) {
                                                    String attendanceStatus = attendanceSnapshot.getString("AttendanceStatus");

                                                    // Read attendance date from Attendance collection
                                                    db.collection("Attendance")
                                                            .document(attendanceId)
                                                            .get()
                                                            .addOnSuccessListener(dateSnapshot -> {
                                                                if (dateSnapshot.exists()) {
                                                                    String attendanceDate = dateSnapshot.getString("AttendanceDate");

                                                                    if (attendanceStatus != null && attendanceDate != null) {
                                                                        dates.add(attendanceDate);
                                                                        switch (attendanceStatus) {
                                                                            case "P":
                                                                                presents.incrementAndGet();
                                                                                break;
                                                                            case "A":
                                                                                absents.incrementAndGet();
                                                                                break;
                                                                            case "L":
                                                                                leaves.incrementAndGet();
                                                                                break;
                                                                        }
                                                                    }

                                                                    // If all attendance data is processed for the course
                                                                    if (dates.size() == attendanceList.size()) {
                                                                        // Update course details
                                                                        updateCourseDetails(course, presents.get(), absents.get(), leaves.get(), dates);

                                                                        // Increment processed courses counter
                                                                        int count = processedCourses.incrementAndGet();
                                                                        // If all courses are processed, fetch evaluation details
                                                                        if (count == courses.size()) {
                                                                            fetchStudentEvaluationDetails(courses, studentRollNo);
                                                                        }
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> Log.e("Firestore Error", "Error retrieving attendance date: " + e.getMessage()));
                                                }
                                            })
                                            .addOnFailureListener(e -> Log.e("Firestore Error", "Error retrieving attendance data: " + e.getMessage()));
                                }
                            } else {
                                // No attendance data found, update course details with default values
                                updateCourseDetails(course, 0, 0, 0, new ArrayList<>());

                                // Increment processed courses counter
                                int count = processedCourses.incrementAndGet();
                                // If all courses are processed, fetch evaluation details
                                if (count == courses.size()) {
                                    fetchStudentEvaluationDetails(courses, studentRollNo);
                                }
                            }
                        } else {
                            // No document found for attendance data, update course details with default values
                            updateCourseDetails(course, 0, 0, 0, new ArrayList<>());

                            // Increment processed courses counter
                            int count = processedCourses.incrementAndGet();
                            // If all courses are processed, fetch evaluation details
                            if (count == courses.size()) {
                                fetchStudentEvaluationDetails(courses, studentRollNo);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore Error", "Error retrieving attendance document: " + e.getMessage());
                        // Increment processed courses counter
                        int count = processedCourses.incrementAndGet();
                        // If all courses are processed, fetch evaluation details
                        if (count == courses.size()) {
                            fetchStudentEvaluationDetails(courses, studentRollNo);
                        }
                    });
        }
    }
    private void updateCourseDetails(StudentCourseAttendanceEvaluationModel course, int presents, int absents, int leaves, List<String> dates) {
        int totalCount = presents + absents + leaves;
        float presentPercentage = totalCount > 0 ? (float) presents / totalCount * 100 : 0;
        String firstDate = getFirstAttendanceDate(dates);
        String lastDate = getLastAttendanceDate(dates);

        // Update course details
        course.setTotalCount(totalCount);
        course.setPresents(presents);
        course.setAbsents(absents);
        course.setLeaves(leaves);
        course.setPresentPercentage(presentPercentage);
        course.setFirstDate(firstDate);
        course.setLastDate(lastDate);

    }

    private void fetchStudentEvaluationDetails(List<StudentCourseAttendanceEvaluationModel> courseModels, String studentRollNo) {
        AtomicInteger processedCourses = new AtomicInteger(0); // Counter to track processed courses
        int totalCourses = courseModels.size(); // Total number of courses

        // Iterate over each course in the list
        for (StudentCourseAttendanceEvaluationModel course : courseModels) {
            db.collection("StudentCourseEvaluationList")
                    .document(studentRollNo + "_" + course.getCourseId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> evaluationList = (List<String>) documentSnapshot.get("EvaluationIDs");

                            if (evaluationList != null && !evaluationList.isEmpty()) {
                                List<StudentEvaluationModel> evaluationDetailsList = new ArrayList<>();
                                AtomicInteger processedCount = new AtomicInteger(0);

                                // Variables to hold total marks and obtained marks
                                AtomicReference<Double> totalMarks = new AtomicReference<>(0.0);
                                AtomicReference<Double> obtainedMarks = new AtomicReference<>(0.0);

                                for (String evaluationId : evaluationList) {
                                    db.collection("CourseStudentsEvaluation")
                                            .document(studentRollNo + "_" + evaluationId)
                                            .get()
                                            .addOnSuccessListener(evaluationSnapshot -> {
                                                if (evaluationSnapshot.exists()) {
                                                    double studentObtMarks = evaluationSnapshot.getDouble("StudentObtMarks");
                                                    // Format obtained marks
                                                    String formattedObtainedMarks = formatMarks(studentObtMarks);
                                                    obtainedMarks.updateAndGet(v -> v + Double.parseDouble(formattedObtainedMarks));

                                                    // Retrieve evaluation name and total marks from Evaluation collection
                                                    db.collection("Evaluations")
                                                            .document(evaluationId)
                                                            .get()
                                                            .addOnSuccessListener(evaluationDetailsSnapshot -> {
                                                                if (evaluationDetailsSnapshot.exists()) {
                                                                    String evalName = evaluationDetailsSnapshot.getString("EvalName");
                                                                    String evalTMarks = evaluationDetailsSnapshot.getString("EvalTMarks");
                                                                    double evalTotalMarks = Double.parseDouble(evalTMarks);
                                                                    // Format total marks
                                                                    String formattedTotalMarks = formatMarks(evalTotalMarks);
                                                                    totalMarks.updateAndGet(v -> v + evalTotalMarks);

                                                                    // Create StudentEvaluationModel object and add to the list
                                                                    StudentEvaluationModel evalDetails = new StudentEvaluationModel(evalName, formattedObtainedMarks, formattedTotalMarks);
                                                                    evaluationDetailsList.add(evalDetails);
                                                                }

                                                                // Increment processed count
                                                                int count = processedCount.incrementAndGet();
                                                                // Check if all evaluations are processed for the course
                                                                if (count == evaluationList.size()) {
                                                                    // Format total and obtained marks
                                                                    String formattedTotalMarks = formatMarks(totalMarks.get());
                                                                    String formattedTObtainedMarks = formatMarks(obtainedMarks.get());

                                                                    // Calculate percentages
                                                                    double percentage = (obtainedMarks.get() / totalMarks.get()) * 100;

                                                                    // Set total and obtained marks and percentage to the course
                                                                    course.setAllEvaluationTotal(formattedTotalMarks);
                                                                    course.setAllEvaluationObtainedMarks(formattedTObtainedMarks);
                                                                    course.setPercentage(String.valueOf(Math.round(percentage))); // Round off percentage
                                                                    course.setCourses(evaluationDetailsList);
                                                                    // Process the next course
                                                                    processedCourses.incrementAndGet();
                                                                    if (processedCourses.get() == totalCourses) {
                                                                        setupAdapter(courseModels);
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                // Increment processed count on failure
                                                                processedCount.incrementAndGet();
                                                                // Log error
                                                                Log.e(TAG, "Error retrieving evaluation details for evaluationId: " + evaluationId + ". Error: " + e.getMessage());
                                                            });
                                                } else {
                                                    // Increment processed count
                                                    processedCount.incrementAndGet();
                                                    // Log error
                                                    Log.e(TAG, "Evaluation document does not exist for evaluationId: " + evaluationId);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                // Increment processed count on failure
                                                processedCount.incrementAndGet();
                                                // Log error
                                                Log.e(TAG, "Error retrieving evaluation details for evaluationId: " + evaluationId + ". Error: " + e.getMessage());
                                            });
                                }
                            } else {
                                // No evaluations found, callback with an empty list
                                course.setAllEvaluationTotal("0");
                                course.setAllEvaluationObtainedMarks("0");
                                course.setPercentage("0");
                                course.setCourses(new ArrayList<>());
                                // Process the next course
                                processedCourses.incrementAndGet();
                                if (processedCourses.get() == totalCourses) {
                                    setupAdapter(courseModels);
                                }
                            }
                        } else {
                            // Document does not exist, callback with an empty list
                            course.setAllEvaluationTotal("0");
                            course.setAllEvaluationObtainedMarks("0");
                            course.setPercentage("0");
                            course.setCourses(new ArrayList<>());
                            // Process the next course
                            processedCourses.incrementAndGet();
                            if (processedCourses.get() == totalCourses) {
                                setupAdapter(courseModels);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error retrieving student course evaluation document for rollNo: " + studentRollNo + ", courseId: " + course.getCourseId() + ". Error: " + e.getMessage());
                        // Increment processed courses counter
                        processedCourses.incrementAndGet();
                        if (processedCourses.get() == totalCourses) {
                            setupAdapter(courseModels);
                            showToast("All course evaluations read successfully");
                        }
                    });
        }
    }
    private void setupAdapter(List<StudentCourseAttendanceEvaluationModel> studentDetailsList) {
        Collections.sort(studentDetailsList, (course1, course2) -> course1.getCourseName().compareToIgnoreCase(course2.getCourseName()));

        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        ProgressDialogHelper.dismissProgressDialog();
        courseModels = new ArrayList<>();
        courseModels.addAll(studentDetailsList);
        StudentCourseDetailsAdapter adapter = new StudentCourseDetailsAdapter(studentDetailsList, this);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStudents.setAdapter(adapter);
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String formatMarks(double marks) {
        int intMarks = (int) marks;
        if (marks == intMarks) {
            return String.valueOf(intMarks);
        }
        return String.valueOf(marks);
    }

    private String getFirstAttendanceDate(List<String> dates) {
        if (dates != null && !dates.isEmpty()) {
            Collections.sort(dates);
            return dates.get(0);
        }
        return null;
    }

    private String getLastAttendanceDate(List<String> dates) {
        if (dates != null && !dates.isEmpty()) {
            Collections.sort(dates);
            return dates.get(dates.size() - 1);
        }
        return null;
    }
}
