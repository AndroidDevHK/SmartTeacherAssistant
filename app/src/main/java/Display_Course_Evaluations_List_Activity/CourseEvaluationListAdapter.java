package Display_Course_Evaluations_List_Activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import Edit_Course_Evaluation_Activity.DisplayCourseEvaluationDetailsActivity;

import com.google.firebase.firestore.WriteBatch;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.ViewSpecificEvaluationDetailsActivity;

import java.util.List;

public class CourseEvaluationListAdapter extends RecyclerView.Adapter<CourseEvaluationListAdapter.ViewHolder> {

    private List<CourseEvaluationInfoModel> courseEvaluationList;
    boolean AreRepeaters;
    private TextView EvalCount;
    private int evalCount;

    public CourseEvaluationListAdapter(List<CourseEvaluationInfoModel> courseEvaluationList, boolean areRepeaters, TextView totalEvalCountTextView, int totalEvaluations) {
        this.courseEvaluationList = courseEvaluationList;
        this.AreRepeaters = areRepeaters;
        this.EvalCount = totalEvalCountTextView;
        this.evalCount = totalEvaluations;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_evaluation_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseEvaluationInfoModel evaluationInfo = courseEvaluationList.get(position);

        holder.textEvaluationName.setText(evaluationInfo.getEvaluationName());
        if (evaluationInfo.getEvaluationTotalMarks() == (int) evaluationInfo.getEvaluationTotalMarks()) {
            holder.textTotalMarks.setText("Total Marks : " + (int) evaluationInfo.getEvaluationTotalMarks());
        } else {
            holder.textTotalMarks.setText("Total Marks : " + evaluationInfo.getEvaluationTotalMarks());
        }
        holder.text_date.setText("Dated: " + evaluationInfo.getDate());
        holder.LayoutEditDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DisplayCourseEvaluationDetailsActivity.class);
            intent.putExtra("evaluationInfo", evaluationInfo);
            intent.putExtra("AreRepeaters", AreRepeaters);
            v.getContext().startActivity(intent);
        });

        holder.layoutViewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ViewSpecificEvaluationDetailsActivity.class);
            intent.putExtra("evaluationInfo", evaluationInfo);
            intent.putExtra("AreRepeaters", AreRepeaters);
            v.getContext().startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> {
            showEditEvaluationNameDialog(v, evaluationInfo);
        });


        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(v.getContext(), evaluationInfo);
        });
    }

    private void showDeleteConfirmationDialog(Context context, CourseEvaluationInfoModel evaluationInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Evaluation");
        builder.setMessage("Are you sure you want to delete the evaluation '" + evaluationInfo.getEvaluationName() + "'?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Deleting...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            deleteEvaluationFromDatabase(evaluationInfo, progressDialog,context);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void deleteEvaluationFromDatabase(CourseEvaluationInfoModel evaluationInfo, ProgressDialog progressDialog, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Delete the evaluation document
        DocumentReference evaluationRef = db.collection("Evaluations").document(evaluationInfo.getEvalId());
        batch.delete(evaluationRef);

        // Delete from CourseEvaluationsInfo collection
        db.collection("CourseEvaluationsInfo").whereEqualTo("EvalID", evaluationInfo.getEvalId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        batch.delete(documentSnapshot.getReference());
                    }

                    // Delete from CourseStudentsEvaluation collection
                    db.collection("CourseStudentsEvaluation").whereEqualTo("EvalID", evaluationInfo.getEvalId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {
                                    batch.delete(documentSnapshot.getReference());
                                }

                                // Update StudentCourseEvaluationList collection
                                db.collection("StudentCourseEvaluationList").whereArrayContains("EvaluationIDs", evaluationInfo.getEvalId())
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots2) {
                                                batch.update(documentSnapshot.getReference(), "EvaluationIDs", FieldValue.arrayRemove(evaluationInfo.getEvalId()));
                                            }

                                            // Commit the batch
                                            commitBatch(batch, evaluationInfo, progressDialog, context);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle the error
                                            progressDialog.dismiss();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                // Handle the error
                                progressDialog.dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    progressDialog.dismiss();
                });
    }

    private void commitBatch(WriteBatch batch, CourseEvaluationInfoModel evaluationInfo, ProgressDialog progressDialog, Context context) {
        batch.commit().addOnSuccessListener(aVoid -> {
            // If all operations are successful, update UI and handle post-deletion logic
            courseEvaluationList.remove(evaluationInfo);
            notifyDataSetChanged();
            evalCount--;
            EvalCount.setText(String.valueOf(evalCount));
            progressDialog.dismiss();
            if (courseEvaluationList.isEmpty()) {
                if (context instanceof Activity) {
                    Toast.makeText(context, "All evaluations have been deleted.", Toast.LENGTH_LONG).show();
                    ((Activity) context).finish();
                }
            }
        }).addOnFailureListener(e -> {
            // Handle failure
            progressDialog.dismiss();
        });
    }

    private void updateEvaluationNameInDatabaseAndLocally(CourseEvaluationInfoModel evaluationInfo, String newName, ProgressDialog progressDialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Evaluations").document(evaluationInfo.getEvalId());
        docRef
                .update("EvalName", newName.toUpperCase())
                .addOnSuccessListener(aVoid -> {
                    evaluationInfo.setEvaluationName(newName.toUpperCase());
                    notifyDataSetChanged();
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    progressDialog.dismiss();
                });
    }

    private void showEditEvaluationNameDialog(View v, CourseEvaluationInfoModel evaluationInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Edit Evaluation Name");

        final EditText input = new EditText(v.getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(layoutParams);
        input.setText(evaluationInfo.getEvaluationName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                ProgressDialog progressDialog = new ProgressDialog(v.getContext());
                progressDialog.setMessage("Updating...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                updateEvaluationNameInDatabaseAndLocally(evaluationInfo, newName, progressDialog);
            } else {
                input.setError("Evaluation name cannot be empty");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    @Override
    public int getItemCount() {
        return courseEvaluationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textEvaluationName;
        TextView textTotalMarks;
        TextView text_date;
        LinearLayout layoutViewDetails;
        ImageButton btnEdit;
        ImageButton btnDelete;
        LinearLayout LayoutEditDetails;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textEvaluationName = itemView.findViewById(R.id.text_evaluation_name);
            text_date = itemView.findViewById(R.id.text_date);
            textTotalMarks = itemView.findViewById(R.id.text_total_marks);
            layoutViewDetails = itemView.findViewById(R.id.LayoutViewDetails);
            LayoutEditDetails= itemView.findViewById(R.id.LayoutEditDetails);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }}
