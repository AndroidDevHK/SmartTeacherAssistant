package Display_Course_Attendance_List_Activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.AttendanceStudentDetails;
import com.nextgen.hasnatfyp.ProgressDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DisplayCourseAttendanceListActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceListActivity";
    private List<AttendanceInfoModel> attendanceList = new ArrayList<>();
    private CourseAttendanceListAdapter attendanceAdapter;

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private String courseID;
    private boolean areRepeaters;
    ActivityManager activityManager;

    int totalAttendances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_course_attendance_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        activityManager = ActivityManager.getInstance();
        activityManager.addActivityForKill(this);
        if (getIntent().hasExtra("CourseID")) {
            areRepeaters = getIntent().getBooleanExtra("AreRepeaters", false);
            courseID = getIntent().getStringExtra("CourseID");
            fetchAttendanceDetails(courseID, areRepeaters);
            ProgressDialogHelper.showProgressDialog(this, "Fetching Attendance Details..");
            setClassAndCourseName();
        } else {
            Log.e(TAG, "No Course ID found in intent");
            finish();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }

    @SuppressLint("SetTextI18n")
    private void setClassAndCourseName() {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);

            classNameTextView.setText(className);
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }

    private void fetchAttendanceDetails(String courseID, boolean isRepeater) {
        db.collection("CourseAttendance")
                .whereEqualTo("CourseID", courseID)
                .whereEqualTo("IsRepeater", isRepeater)
                .get()
                .addOnCompleteListener(this::handleAttendanceIdsQuery);
    }

    private void handleAttendanceIdsQuery(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            int totalAttendanceRecords = task.getResult().size();
            totalAttendances = totalAttendanceRecords;
            if (totalAttendanceRecords == 0) {
                showNoAttendanceDialog();
                ProgressDialogHelper.dismissProgressDialog();
                return;
            }

            List<Task<Void>> attendanceTasks = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                String attendanceID = document.getString("AttendanceID");
                TaskCompletionSource<Void> attendanceSource = new TaskCompletionSource<>();
                attendanceTasks.add(attendanceSource.getTask());
                fetchAttendanceDate(attendanceID, attendanceSource);
            }

            Tasks.whenAllComplete(attendanceTasks).addOnCompleteListener(tasks -> {
                sortAttendanceListByDate();
                TextView totalAttendancesCount = findViewById(R.id.TotalAttendanceCountTxtView);
                totalAttendancesCount.setText(String.valueOf(attendanceList.size()));
                attendanceAdapter = new CourseAttendanceListAdapter(attendanceList,areRepeaters,courseID,totalAttendances,totalAttendancesCount);
                recyclerView.setAdapter(attendanceAdapter);
                ProgressDialogHelper.dismissProgressDialog();
            });

        } else {
            Log.e(TAG, "Error getting documents: ", task.getException());
            ProgressDialogHelper.dismissProgressDialog();
        }
    }

    private void fetchAttendanceDate(String attendanceID, TaskCompletionSource<Void> attendanceSource) {
        db.collection("Attendance")
                .document(attendanceID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String rawAttendanceDate = documentSnapshot.getString("AttendanceDate");
                    String formattedAttendanceDate = formatDateString(rawAttendanceDate);
                    fetchAttendanceDetails(attendanceID, formattedAttendanceDate, attendanceSource);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting document", e);
                    attendanceSource.setResult(null);
                });
    }

    private void fetchAttendanceDetails(String attendanceID, String attendanceDate, TaskCompletionSource<Void> attendanceSource) {
        db.collection("CourseStudentsAttendance")
                .whereEqualTo("AttendanceID", attendanceID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> studentTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentRollNo = document.getString("StudentRollNo");
                            String attendanceStatus = document.getString("AttendanceStatus");
                            String classID = document.getString("ClassID");
                            TaskCompletionSource<Void> studentSource = new TaskCompletionSource<>();
                            studentTasks.add(studentSource.getTask());
                            fetchStudentName(classID, studentRollNo, attendanceID, attendanceDate, attendanceStatus, studentSource);
                        }
                        Tasks.whenAllComplete(studentTasks).addOnCompleteListener(tasks -> {
                            attendanceSource.setResult(null);
                        });

                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        attendanceSource.setResult(null);
                    }
                });
    }

    private void fetchStudentName(String classID, String studentRollNo, String attendanceID, String attendanceDate, String attendanceStatus, TaskCompletionSource<Void> studentSource) {
        db.collection("Classes")
                .document(classID)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", studentRollNo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<AttendanceStudentDetails> studentDetailsList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        String studentName = documentSnapshot.getString("StudentName");
                        AttendanceStudentDetails studentDetails = new AttendanceStudentDetails(studentName, studentRollNo, attendanceStatus);
                        studentDetailsList.add(studentDetails);
                    }
                    updateAttendanceInfoModel(attendanceID, attendanceDate, studentDetailsList);
                    studentSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting documents", e);
                    studentSource.setResult(null);
                });
    }

    private synchronized void updateAttendanceInfoModel(String attendanceID, String attendanceDate, List<AttendanceStudentDetails> studentDetailsList) {
        AttendanceInfoModel attendanceInfo = findOrCreateAttendanceInfo(attendanceID, attendanceDate);
        attendanceInfo.getStudentList().addAll(studentDetailsList);
    }

    private void showNoAttendanceDialog() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Attendance Records")
                    .setMessage("No attendance records to display.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Attendances Details", true);
    }

    private void sortAttendanceListByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        Collections.sort(attendanceList, (o1, o2) -> {
            try {
                Date date1 = dateFormat.parse(o1.getAttendanceDate());
                Date date2 = dateFormat.parse(o2.getAttendanceDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    private String formatDateString(String rawDate) {
        SimpleDateFormat rawDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        try {
            Date date = rawDateFormat.parse(rawDate);
            return desiredDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return rawDate;
        }
    }

    private AttendanceInfoModel findOrCreateAttendanceInfo(String attendanceID, String attendanceDate) {
        AttendanceInfoModel attendanceInfo = attendanceList.stream()
                .filter(info -> info.getAttendanceID().equals(attendanceID))
                .findFirst()
                .orElse(null);

        if (attendanceInfo == null) {
            attendanceInfo = new AttendanceInfoModel(attendanceID, attendanceDate, new ArrayList<>());
            attendanceList.add(attendanceInfo);
        }

        return attendanceInfo;
    }
}
