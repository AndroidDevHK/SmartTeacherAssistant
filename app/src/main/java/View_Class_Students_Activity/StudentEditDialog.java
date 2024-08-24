package View_Class_Students_Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.R;

public class StudentEditDialog extends Dialog {

    private EditText editTextStudentName;
    private String rollNo;
    private String classID;
    private OnUpdateClickListener onUpdateClickListener;
    private ProgressDialog progressDialog;

    public StudentEditDialog(@NonNull Context context, String rollNo, String initialStudentName, String classID, OnUpdateClickListener onUpdateClickListener) {
        super(context);
        this.rollNo = rollNo;
        this.classID = classID;
        this.onUpdateClickListener = onUpdateClickListener;
        createDialog(initialStudentName);
    }

    private void createDialog(String initialStudentName) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.edit_student_name_dialog, null);

        editTextStudentName = dialogView.findViewById(R.id.edit_text_name);
        editTextStudentName.setText(initialStudentName);

        Button buttonUpdate = dialogView.findViewById(R.id.btn_update);
        Button buttonCancel = dialogView.findViewById(R.id.btn_cancel);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);

        buttonUpdate.setOnClickListener(view -> {
            String updatedStudentName = editTextStudentName.getText().toString().trim();
            updateStudentName(updatedStudentName);
        });

        buttonCancel.setOnClickListener(view -> dismiss());

        setContentView(dialogView);
        setCanceledOnTouchOutside(false);
    }

    private void updateStudentName(String updatedStudentName) {
        // Check if the update process is already in progress
        if (progressDialog.isShowing()) {
            return; // If the loading dialog is already shown, exit the method
        }

        progressDialog.show(); // Show loading dialog

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Classes").document(classID)
                .collection("ClassStudents")
                .whereEqualTo("RollNo", rollNo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming roll number is unique)
                        String studentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // Update the student's name in the Firestore database
                        db.collection("Classes").document(classID)
                                .collection("ClassStudents").document(studentId)
                                .update("StudentName", updatedStudentName)
                                .addOnSuccessListener(aVoid -> {
                                    onUpdateClickListener.onUpdateClick(rollNo, updatedStudentName); // Update locally
                                    dismiss(); // Dismiss dialog after update
                                })
                                .addOnFailureListener(e -> {
                                    // Handle error
                                })
                                .addOnCompleteListener(task -> progressDialog.dismiss()); // Hide loading dialog after update attempt completes
                    } else {
                        // Student with the provided roll number not found
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> progressDialog.dismiss()); // Error occurred while querying the database
    }


    public interface OnUpdateClickListener {
        void onUpdateClick(String rollNo, String updatedStudentName);
    }
}
