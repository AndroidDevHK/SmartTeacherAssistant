package View_Class_Courses_Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.R;

public class CourseEditDialog extends Dialog {

    private EditText courseNameEditText;
    private EditText creditHoursEditText;
    private Button saveButton;
    private Button cancelButton;

    private ProgressDialog progressDialog;
    private String courseId;
    private String classId;
    private String initialCourseName;
    private OnCourseUpdatedListener listener;

    public CourseEditDialog(@NonNull Context context, String courseId, String initialCourseName, String initialCreditHours, String classId, OnCourseUpdatedListener listener) {
        super(context);
        this.courseId = courseId;
        this.classId = classId;
        this.initialCourseName = initialCourseName;
        this.listener = listener;
        createDialog(initialCourseName, initialCreditHours);
    }

    private void createDialog(String initialCourseName, String initialCreditHours) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.course_edit_dialog, null);

        courseNameEditText = dialogView.findViewById(R.id.editTextCourseName);
        creditHoursEditText = dialogView.findViewById(R.id.editTextCreditHours);
        saveButton = dialogView.findViewById(R.id.buttonSave);
        cancelButton = dialogView.findViewById(R.id.buttonCancel);

        courseNameEditText.setText(initialCourseName);
        creditHoursEditText.setText(initialCreditHours);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);

        saveButton.setOnClickListener(view -> {
            String courseName = courseNameEditText.getText().toString().trim().toLowerCase();

            if (TextUtils.isEmpty(courseName)) {
                courseNameEditText.setError("Please enter a course name");
            } else if (courseName.equals(initialCourseName.toLowerCase())) {
                showToast("No changes to save");
            } else {
                showLoadingDialog();
                checkForDuplicateAndSave(courseName);
            }
        });

        cancelButton.setOnClickListener(view -> dismiss());

        setContentView(dialogView);
        setCanceledOnTouchOutside(false);
    }

    private void showLoadingDialog() {
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void checkForDuplicateAndSave(String courseName) {
        FirebaseFirestore.getInstance()
                .collection("ClassCourses")
                .document(classId)
                .collection("ClassCoursesSubcollection")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String existingCourseName = documentSnapshot.getString("CourseName");
                        if (existingCourseName != null && existingCourseName.equalsIgnoreCase(courseName)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (isDuplicate) {
                        showToast("Course name already exists");
                        dismissLoadingDialog();
                    } else {
                        updateCourseInfo(courseName);
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to check for duplicate course name");
                    dismissLoadingDialog();
                });
    }

    private void updateCourseInfo(String courseName) {
        DocumentReference documentReference = FirebaseFirestore.getInstance()
                .collection("ClassCourses")
                .document(classId)
                .collection("ClassCoursesSubcollection")
                .document(courseId);

        documentReference
                .update("CourseName", courseName)
                .addOnSuccessListener(aVoid -> {
                    showToast("Course information updated successfully");
                    listener.onCourseUpdated(courseId, courseName);
                    dismiss();
                    dismissLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update course information");
                    dismissLoadingDialog();
                });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public interface OnCourseUpdatedListener {
        void onCourseUpdated(String courseId, String updatedCourseName);
    }
}
