package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson; // Import Gson
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import OfflineEvluationManagement.EditOfflineEvaluationActivity;
import OfflineEvluationManagement.OfflineEvaluationModel;

public class OfflineEvaluationAdapter extends RecyclerView.Adapter<OfflineEvaluationAdapter.ViewHolder> {

    private List<OfflineEvaluationModel> offlineEvaluationList;
    private Context context;
    private Gson gson; // Declare Gson

    public OfflineEvaluationAdapter(List<OfflineEvaluationModel> offlineEvaluationList, Context context) {
        this.offlineEvaluationList = offlineEvaluationList;
        this.context = context;
        this.gson = new Gson(); // Initialize Gson
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offline_evaluation_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfflineEvaluationModel offlineEvaluation = offlineEvaluationList.get(position);

        holder.textCourseName.setText("Course: " + offlineEvaluation.getCourseName());
        holder.textEvalName.setText("Evaluation: " + offlineEvaluation.getEvaluationName());
        holder.textTMEvalName.setText("Total Marks: " + offlineEvaluation.getEvaluationTMarks());
        holder.textDate.setText("Dated: " + offlineEvaluation.getEvaluationDate());
        holder.textNumStudents.setText("Students: " + offlineEvaluation.getStudentsList().size());

        holder.btnEdit.setOnClickListener(v -> showEditDialog(offlineEvaluation));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(offlineEvaluation));
        holder.layout_view_evaluation.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditOfflineEvaluationActivity.class);
            intent.putExtra("offlineEvaluation", offlineEvaluation);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return offlineEvaluationList.size();
    }

    public void updateList(List<OfflineEvaluationModel> filteredList) {
        this.offlineEvaluationList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCourseName, textEvalName, textTMEvalName, textDate, textNumStudents;
        ImageButton btnEdit, btnDelete;
        LinearLayout layout_view_evaluation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCourseName = itemView.findViewById(R.id.text_Course_name);
            textEvalName = itemView.findViewById(R.id.TextEvalName);
            textTMEvalName = itemView.findViewById(R.id.TextTMEvalName);
            textDate = itemView.findViewById(R.id.text_number_of_repeaters);
            textNumStudents = itemView.findViewById(R.id.text_number_of_students);
            btnEdit = itemView.findViewById(R.id.btn_Edit);
            layout_view_evaluation = itemView.findViewById(R.id.layout_view_evaluation);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void showEditDialog(OfflineEvaluationModel offlineEvaluation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Evaluation Name");

        // Set up the input
        final EditText input = new EditText(context);
        input.setText(offlineEvaluation.getEvaluationName());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newEvaluationName = input.getText().toString();
            if (!newEvaluationName.isEmpty()) {
                // Rename the file and update the evaluation name
                if (renameEvaluationFile(offlineEvaluation, newEvaluationName)) {
                    offlineEvaluation.setEvaluationName(newEvaluationName);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Evaluation name updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to update evaluation name", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Evaluation name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(OfflineEvaluationModel offlineEvaluation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete the evaluation " + offlineEvaluation.getEvaluationName() + " (" + offlineEvaluation.getEvaluationTMarks() + ")?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteOfflineEvaluation(offlineEvaluation));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteOfflineEvaluation(OfflineEvaluationModel offlineEvaluation) {
        String fileName = generateFileName(offlineEvaluation);
        String teacherUserName;

        // Determine the teacherUserName based on whether the user is solo or not
        if (UserInstituteModel.getInstance(context).isSoloUser()) {
            teacherUserName = UserInstituteModel.getInstance(context).getInstituteId();
        } else {
            teacherUserName = TeacherInstanceModel.getInstance(context).getTeacherUsername();
        }

        // Construct the file path
        File directory = new File(context.getFilesDir(), teacherUserName + "_EvaluationData");
        File file = new File(directory, fileName);
        Log.d("DeleteEvaluation", "File path: " + file.getAbsolutePath());

        // Check if the file exists and delete it
        if (file.exists()) {
            if (file.delete()) {
                offlineEvaluationList.remove(offlineEvaluation);
                notifyDataSetChanged();
                Toast.makeText(context, "Offline evaluation deleted", Toast.LENGTH_SHORT).show();

                // If no evaluations remain, finish the activity
                if (offlineEvaluationList.isEmpty() && context instanceof Activity) {
                    Toast.makeText(context, "No more offline evaluation records", Toast.LENGTH_SHORT).show();
                    ((Activity) context).finish();
                }
            } else {
                Toast.makeText(context, "Failed to delete offline evaluation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateFileName(OfflineEvaluationModel evaluation) {
        return evaluation.getEvaluationName() + "_" + evaluation.getEvaluationTMarks() + "_" + evaluation.getCourseId() + "_" + evaluation.getCourseName() + "_" + (evaluation.isAreRepeaters() ? "Repeaters" : "Regular") + ".json";
    }

    private boolean renameEvaluationFile(OfflineEvaluationModel evaluation, String newEvaluationName) {
        String oldFileName = generateFileName(evaluation);
        evaluation.setEvaluationName(newEvaluationName);
        String newFileName = generateFileName(evaluation);

        String teacherUserName;

        // Determine the teacherUserName based on whether the user is solo or not
        if (UserInstituteModel.getInstance(context).isSoloUser()) {
            teacherUserName = UserInstituteModel.getInstance(context).getInstituteId();
        } else {
            teacherUserName = TeacherInstanceModel.getInstance(context).getTeacherUsername();
        }

        // Construct the file paths
        File directory = new File(context.getFilesDir(), teacherUserName + "_EvaluationData");
        File oldFile = new File(directory, oldFileName);
        File newFile = new File(directory, newFileName);

        if (oldFile.exists()) {
            // Rename the file
            if (oldFile.renameTo(newFile)) {
                // Update the contents of the new file if necessary
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    // Write updated evaluation data to the file
                    String json = gson.toJson(evaluation); // Convert evaluation to JSON
                    fos.write(json.getBytes());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
