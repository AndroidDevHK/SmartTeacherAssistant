package View_Class_Students_Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.List;

import DisplayStudentCompleteAttendanceEvaluation_Activity.DisplayStudentCompleteAttendanceEvaluationActivity;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private Context context;
    private List<StudentModel> studentList;
    private ProgressDialog progressDialog;
    public String ClassId;

    public StudentAdapter(Context context, List<StudentModel> studentList, String classId) {
        this.context = context;
        this.studentList = studentList;
        this.ClassId = classId;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentModel student = studentList.get(position);

        // Bind student information to the views
        holder.studentNameTextView.setText(student.getStudentName());
        holder.rollNoTextView.setText(student.getRollNo());
        holder.studentUserIDTextView.setText(student.getStudentUserID()); // Set the StudentUserID

        // Set switch state based on student's active status
        holder.isActiveSwitch.setChecked(student.isActive());

        // Set listener to handle switch state change
        holder.isActiveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Show loading dialog
            progressDialog.show();
            // Update student's active status locally
            student.setActive(isChecked);
            // Update student's active status in the Firestore database
            updateStudentActiveStatus(student.getRollNo(), isChecked);
        });

        holder.editStudentNameButton.setOnClickListener(view -> {
            // Open the edit dialog for the student's name
            StudentEditDialog dialog = new StudentEditDialog(context, student.getRollNo(), student.getStudentName(), ClassId, (rollNo, updatedName) -> {
                // Update student's name locally
                student.setStudentName(updatedName);
                // Notify adapter of the change
                notifyDataSetChanged();
            });
            dialog.show();
        });

        holder.btn_view_report.setOnClickListener(v -> {
            Intent intent = new Intent(context, DisplayStudentCompleteAttendanceEvaluationActivity.class);
            intent.putExtra("STUDENT_ROLL_NO", student.getRollNo());
            intent.putExtra("STUDENT_NAME", student.getStudentName());
            intent.putExtra("CLASS_NAME", UserInstituteModel.getInstance(context).getClassName());
            intent.putExtra("CLASS_ID", UserInstituteModel.getInstance(context).getClassId());
            intent.putExtra("STUDENT_USER_ID", student.getStudentUserID()); // Pass StudentUserID
            intent.putExtra("SEMESTER_ID", UserInstituteModel.getInstance(context).getSemesterId());
            context.startActivity(intent);
        });
    }

    public void setStudentList(List<StudentModel> studentList) {
        this.studentList = studentList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView studentNameTextView;
        public TextView rollNoTextView;
        public TextView studentUserIDTextView; // TextView for StudentUserID
        public Switch isActiveSwitch;
        public ImageButton editStudentNameButton;
        public MaterialButton btn_view_report;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.text_student_name);
            rollNoTextView = itemView.findViewById(R.id.text_roll_no);
            studentUserIDTextView = itemView.findViewById(R.id.text_student_user_id); // Initialize the StudentUserID TextView
            editStudentNameButton = itemView.findViewById(R.id.btn_edit_student_name);
            isActiveSwitch = itemView.findViewById(R.id.switch_active);
            btn_view_report = itemView.findViewById(R.id.btn_view_report);
        }
    }

    private void updateStudentActiveStatus(String rollNo, boolean isActive) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Classes").document(ClassId)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", rollNo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming roll number is unique)
                        String studentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // Update the student's active status in the Firestore database
                        db.collection("Classes").document(ClassId)
                                .collection("ClassStudents").document(studentId)
                                .update("IsActive", isActive)
                                .addOnSuccessListener(aVoid -> progressDialog.dismiss()) // Successfully updated
                                .addOnFailureListener(e -> progressDialog.dismiss()); // Failed to update
                    } else {
                        // Student with the provided roll number not found
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> progressDialog.dismiss()); // Error occurred while querying the database
    }
}
