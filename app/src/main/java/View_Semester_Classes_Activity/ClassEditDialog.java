package View_Semester_Classes_Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

public class ClassEditDialog extends Dialog {

    private EditText editTextClassName;
    private OnUpdateClickListener onUpdateClickListener;
    private String classID;
    private ProgressDialog progressDialog;

    public ClassEditDialog(@NonNull Context context, String classID, String initialClassName, OnUpdateClickListener onUpdateClickListener) {
        super(context);
        this.classID = classID;
        this.onUpdateClickListener = onUpdateClickListener;
        createDialog(initialClassName);
    }

    private void createDialog(String initialClassName) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.edit_class_name_dialog, null);

        editTextClassName = dialogView.findViewById(R.id.editTextClassName);
        editTextClassName.setText(initialClassName);

        Button buttonUpdate = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);

        buttonUpdate.setOnClickListener(view -> {
            String updatedClassName = editTextClassName.getText().toString().trim().toUpperCase();
            if (updatedClassName.isEmpty()) {
                editTextClassName.setError("Please enter a class name");
            } else if (updatedClassName.equals(initialClassName)) {
                showToast("No Changes to Save");
            } else {
                checkClassOccurrences(updatedClassName);
            }
        });

        buttonCancel.setOnClickListener(view -> dismiss());

        setContentView(dialogView);
        setCanceledOnTouchOutside(false);
    }


    private void updateClassName(String updatedClassName) {
        progressDialog.show(); // Show loading dialog

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference classRef = db.collection("Classes").document(classID);

        // Update class name in the Classes collection
        classRef.update("ClassName", updatedClassName)
                .addOnSuccessListener(aVoid -> {
                    // Update locally
                    onUpdateClickListener.onUpdateClick(classID, updatedClassName);

                    // Update class name in the SemesterClasses collection
                    db.collection("SemesterClasses")
                            .whereEqualTo("ClassID", classID)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    String semesterClassID = document.getId();
                                    DocumentReference semesterClassRef = db.collection("SemesterClasses").document(semesterClassID);
                                    semesterClassRef.update("ClassName", updatedClassName);
                                }
                                dismiss(); // Dismiss dialog after update
                                progressDialog.dismiss(); // Hide loading dialog
                            })
                            .addOnFailureListener(e -> {
                                // Handle error
                                progressDialog.dismiss(); // Hide loading dialog
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    progressDialog.dismiss(); // Hide loading dialog
                });
    }
    private void checkClassOccurrences(String updatedClassName) {
        progressDialog.show(); // Show loading dialog

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the semester ID from the UserInstituteModel
        String semesterId = UserInstituteModel.getInstance(getContext()).getSemesterId();
        String SemesterName = UserInstituteModel.getInstance(getContext()).getSemesterName();
        Log.d("SemesterID", semesterId); // Log the semester ID

        db.collection("SemesterClasses")
                .whereEqualTo("ClassName", updatedClassName)
                .whereEqualTo("SemesterID", semesterId)  // Check against the current semester
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean classExists = !queryDocumentSnapshots.isEmpty();
                    if (classExists) {
                        // Class exists in the semester, show error
                        showToast("This Class already exists in "+SemesterName+" Please choose a different name.");
                        progressDialog.dismiss(); // Hide loading dialog
                    } else {
                        // Class doesn't exist, proceed with update
                        updateClassName(updatedClassName);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    progressDialog.dismiss(); // Hide loading dialog
                });
    }


    public interface OnUpdateClickListener {
        void onUpdateClick(String classID, String updatedClassName);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
