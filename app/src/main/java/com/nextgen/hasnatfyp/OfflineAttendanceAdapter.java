package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Mark_Course_Students_Attendance_Activity.OfflineEvaluationModel;

public class OfflineAttendanceAdapter extends RecyclerView.Adapter<OfflineAttendanceAdapter.ViewHolder> {

    private List<OfflineEvaluationModel> offlineAttendanceList;
    private Context context;

    // Constructor
    public OfflineAttendanceAdapter(List<OfflineEvaluationModel> offlineAttendanceList, Context context) {
        this.offlineAttendanceList = offlineAttendanceList;
        this.context = context;
    }
    public void updateList(List<OfflineEvaluationModel> newList) {
        this.offlineAttendanceList = newList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offline_marked_attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfflineEvaluationModel attendanceModel = offlineAttendanceList.get(position);

        int size = attendanceModel.getStudentsList().size();
        holder.textCourseName.setText("Course: " + attendanceModel.getCourseName());
        holder.textNumberOfStudents.setText("Students: " + size);
        holder.textDate.setText("Attendance: " + formatDate(attendanceModel.getAttendanceDate()));

        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(attendanceModel));

        holder.layoutSubmitAttendance.setOnClickListener(v -> {
            // Handle click on submit attendance layout if needed
        });

        holder.layoutViewAttendance.setOnClickListener(v -> {
            // Open EditOfflineAttendanceActivity with the offline attendance model object
            Intent intent = new Intent(context, EditOfflineAttendanceActivity.class);
            intent.putExtra("attendanceModel", attendanceModel);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return offlineAttendanceList.size();
    }
    private String formatDate(String originalDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(originalDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return originalDate; // Return original date if parsing fails
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCourseName;
        TextView textNumberOfStudents;
        TextView textDate;
        ImageButton btnDelete;
        LinearLayout layoutSubmitAttendance;
        LinearLayout layoutViewAttendance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCourseName = itemView.findViewById(R.id.text_Course_name);
            textNumberOfStudents = itemView.findViewById(R.id.text_number_of_students);
            textDate = itemView.findViewById(R.id.text_number_of_repeaters);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            layoutSubmitAttendance = itemView.findViewById(R.id.layout_submit_attendance);
            layoutViewAttendance = itemView.findViewById(R.id.layout_view_attendance);
        }
    }
    private void showDeleteConfirmationDialog(OfflineEvaluationModel attendanceModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete the attendance for " + attendanceModel.getCourseName() + " on " + formatDate(attendanceModel.getAttendanceDate()) + "?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteAttendanceFile(attendanceModel));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteAttendanceFile(OfflineEvaluationModel attendanceModel) {
        String uniqueKey = attendanceModel.getAttendanceDate() + "_" + attendanceModel.getCourseId() + "_" + attendanceModel.getCourseName() + "_" + (attendanceModel.isAreRepeaters() ? "Repeaters" : "Regular");
        String TeacherUserName;
        if(UserInstituteModel.getInstance(context).isSoloUser())
        {
            TeacherUserName = UserInstituteModel.getInstance(context).getInstituteId();
        }
        else
        {
            TeacherUserName = TeacherInstanceModel.getInstance(context).getTeacherUsername();
        }
        String fileName = uniqueKey + ".json";

        File directory = new File(context.getFilesDir(), TeacherUserName + "_AttendanceData");
        File file = new File(directory, fileName);

        if (file.exists()) {
            if (file.delete()) {
                offlineAttendanceList.remove(attendanceModel);
                notifyDataSetChanged();
                Toast.makeText(context, "Attendance for " + attendanceModel.getCourseName() + " on " + formatDate(attendanceModel.getAttendanceDate()) + " deleted", Toast.LENGTH_SHORT).show();

                if (offlineAttendanceList.isEmpty()) {
                    Toast.makeText(context, "No more offline attendance records", Toast.LENGTH_SHORT).show();
                    ((Activity) context).finish();
                }
            } else {
                Toast.makeText(context, "Failed to delete attendance file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
        }
    }



}
