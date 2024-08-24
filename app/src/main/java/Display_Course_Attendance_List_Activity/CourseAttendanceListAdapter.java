package Display_Course_Attendance_List_Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.nextgen.hasnatfyp.AttendanceStudentDetails;
import com.nextgen.hasnatfyp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import Edit_Course_Attendance_Activity.DisplayCourseSpecificDateAttendanceActivity;
import Edit_Course_Attendance_Activity.EditCompleteCourseAttendanceActivity;

public class CourseAttendanceListAdapter extends RecyclerView.Adapter<CourseAttendanceListAdapter.ViewHolder> {

    private List<AttendanceInfoModel> attendanceList;
    private Context context;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private String CourseID;
    private boolean AreRepeaters;
    private TextView totalAttendanceCountTextView;
    private int totalAttendances;
    public CourseAttendanceListAdapter(List<AttendanceInfoModel> attendanceList, boolean areRepeaters, String courseID, int totalAttendances, TextView totalAttendanceCountTextView) {
        this.attendanceList = attendanceList;
        this.db = FirebaseFirestore.getInstance();
        this.CourseID = courseID;
        this.AreRepeaters = areRepeaters;
        this.totalAttendanceCountTextView = totalAttendanceCountTextView;
        this.totalAttendances = totalAttendances;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_course_attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceInfoModel attendance = attendanceList.get(position);

        holder.textAttendanceDate.setText(attendance.getAttendanceDate());
        holder.textAttendanceDay.setText(getDayOfWeekFromDate(attendance.getAttendanceDate()));
        holder.LayoutEditDetails.setOnClickListener(v -> {
            sortStudentListByRollNo(attendance.getStudentList());
            Intent intent = new Intent(context, EditCompleteCourseAttendanceActivity.class);
            intent.putExtra("attendanceInfo", attendance);
            context.startActivity(intent);
        });

        holder.LayoutViewDetails.setOnClickListener(v -> {
            sortStudentListByRollNo(attendance.getStudentList());
            Intent intent = new Intent(context, DisplayCourseSpecificDateAttendanceActivity.class);
            intent.putExtra("attendanceInfo", attendance);
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(attendance, position));
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    private void sortStudentListByRollNo(List<AttendanceStudentDetails> studentList) {
        Collections.sort(studentList, (student1, student2) -> {
            String rollNo1 = extractNumericPart(student1.getStudentRollNo());
            String rollNo2 = extractNumericPart(student2.getStudentRollNo());

            try {
                long rollNoValue1 = Long.parseLong(rollNo1);
                long rollNoValue2 = Long.parseLong(rollNo2);
                return Long.compare(rollNoValue1, rollNoValue2);
            } catch (NumberFormatException e) {
                // If parsing fails, compare lexicographically
                return rollNo1.compareTo(rollNo2);
            }
        });
    }

    private String extractNumericPart(String rollNo) {
        return rollNo.replaceAll("[^0-9]", "");
    }
    private void showDeleteConfirmationDialog(AttendanceInfoModel attendance, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Attendance")
                .setMessage("Are you sure you want to delete the attendance record for " + attendance.getAttendanceDate() + " ?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAttendance(attendance, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAttendance(AttendanceInfoModel attendance, int position) {
        progressDialog = ProgressDialog.show(context, "", "Deleting...", true, false);
        List<AttendanceStudentDetails> studentList = attendance.getStudentList();
        WriteBatch batch = db.batch();

        for (AttendanceStudentDetails student : studentList) {
            String studentRollNo = student.getStudentRollNo();
            String courseID = CourseID;

            // Remove specific attendance ID from StudentCourseAttendanceList
            DocumentReference studentCourseAttendanceRef = db.collection("StudentCourseAttendanceList")
                    .document(studentRollNo + "_" + courseID);
            batch.update(studentCourseAttendanceRef, "AttendanceIDs", FieldValue.arrayRemove(attendance.getAttendanceID()));

            // Delete from CourseStudentsAttendance
            DocumentReference courseStudentsAttendanceRef = db.collection("CourseStudentsAttendance")
                    .document(studentRollNo + "_" + attendance.getAttendanceID());
            batch.delete(courseStudentsAttendanceRef);
        }

        // Delete from CourseAttendance and Attendance collections after deleting student-specific data
        deleteFromOtherCollections(attendance, position, batch);
    }

    private void deleteFromOtherCollections(AttendanceInfoModel attendance, int position, WriteBatch batch) {
        db.collection("CourseAttendance")
                .whereEqualTo("AttendanceID", attendance.getAttendanceID())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        batch.delete(document.getReference());
                    }

                    // Delete from Attendance collection
                    DocumentReference attendanceRef = db.collection("Attendance")
                            .document(attendance.getAttendanceID());
                    batch.delete(attendanceRef);

                    // Commit the batch
                    commitBatch(batch, position);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    // Handle failure
                });
    }

    private void commitBatch(WriteBatch batch, int position) {
        batch.commit().addOnSuccessListener(aVoid -> {
            // If all operations are successful, remove from adapter
            attendanceList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, attendanceList.size());
            progressDialog.dismiss();
            totalAttendances--;
            totalAttendanceCountTextView.setText(String.valueOf(totalAttendances));
            if (attendanceList.isEmpty()) {
                if (context instanceof Activity) {
                    Toast.makeText(context, "All attendances have been deleted.", Toast.LENGTH_LONG).show();
                    ((Activity) context).finish();
                }
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            // Handle failure
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textAttendanceDate;
        public TextView textAttendanceDay;
        public ImageButton btnDelete;
        public LinearLayout LayoutEditDetails;
        public LinearLayout LayoutViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAttendanceDate = itemView.findViewById(R.id.text_attendance_date);
            textAttendanceDay = itemView.findViewById(R.id.text_attendance_day);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            LayoutEditDetails = itemView.findViewById(R.id.LayouteditDetails);
            LayoutViewDetails = itemView.findViewById(R.id.LayoutViewDetails);
        }
    }

    private String getDayOfWeekFromDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return getDayOfWeek(dayOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "";
        }
    }
}
