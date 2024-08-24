package Add_View_Semester_Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nextgen.hasnatfyp.DateHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.HashMap;
import java.util.Map;

public class SemesterDialogHelper {

    private final Context context;
    private AlertDialog alertDialog;
    private EditText editTextSemesterName, editTextStartDate, editTextEndDate;
    private final FirebaseFirestore db;
    private final CollectionReference semestersCollection;
    private SemesterDialogListener listener;
    private SemesterModel editingSemester;
    private TextView textViewHeading;


    public SemesterDialogHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.semestersCollection = db.collection("Semesters");
    }

    public void openSemesterDialog(SemesterDialogListener listener, SemesterModel semester) {
        this.listener = listener;
        this.editingSemester = semester;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_add_semester, null);
        builder.setView(dialogView);

        editTextSemesterName = dialogView.findViewById(R.id.editTextSemesterName);
        editTextStartDate = dialogView.findViewById(R.id.editTextStartDate);
        editTextEndDate = dialogView.findViewById(R.id.editTextEndDate);
        textViewHeading = dialogView.findViewById(R.id.textViewHeading);
        if (semester != null) {
            fillSemesterDetails(semester);
        }

        setupDialogButtons(dialogView);
        setupDateSelection(editTextStartDate);
        setupDateSelection(editTextEndDate);

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false); // Dialog should not dismiss on touch outside
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void fillSemesterDetails(SemesterModel semester) {
        textViewHeading.setText("Edit Year/Semester");
        editTextSemesterName.setText(semester.getSemesterName());
        editTextStartDate.setText(semester.getStartDate());
        editTextEndDate.setText(semester.getEndDate());
        disableDateEditing();
    }
    private void disableDateEditing() {
        editTextStartDate.setEnabled(false);
        editTextEndDate.setEnabled(false);
    }
    private void setupDialogButtons(View dialogView) {
        MaterialButton buttonSave = dialogView.findViewById(R.id.buttonSave);
        MaterialButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        buttonSave.setOnClickListener(view -> {
            saveSemesterInfo();
        });

        buttonCancel.setOnClickListener(view -> {
            alertDialog.dismiss();
        });
    }


    private void setupDateSelection(EditText editText) {
        editText.setOnClickListener(view -> DateHelper.openDatePicker(context, editText));
    }

    private void saveSemesterInfo() {
        String semesterName = editTextSemesterName.getText().toString().trim().toUpperCase();
        String startDate = editTextStartDate.getText().toString().trim();
        String endDate = editTextEndDate.getText().toString().trim();


        if (TextUtils.isEmpty(semesterName)) {
            editTextSemesterName.setError("Please fill in the field.");
            return;
        }
        if (TextUtils.isEmpty(startDate)) {
            editTextStartDate.setError("Please select a start date");
            return;
        }
        if (TextUtils.isEmpty(endDate)) {
            editTextEndDate.setError("Please select an end date");
            return;
        }
        if (startDate.equals(endDate)) {
            Toast.makeText(context, "Start and end dates cannot be the same", Toast.LENGTH_SHORT).show();
            return;
        }
        if (editingSemester != null && semesterName.equals(editingSemester.getSemesterName())) {
            Toast.makeText(context, "No Changes to Save", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, Object> semesterData = new HashMap<>();
        semesterData.put("semesterName", semesterName);
        semesterData.put("startDate", startDate);
        semesterData.put("endDate", endDate);
        semesterData.put("isActive", true);

        checkSemesterNameExistenceAndUpdate(semesterName, progressDialog, semesterData);
    }

    private void checkSemesterNameExistenceAndUpdate(String semesterName, ProgressDialog progressDialog, Map<String, Object> semesterData) {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context.getApplicationContext());
        String instituteId = userInstituteModel.getInstituteId();

        // Query the InstituteSemesters collection
        FirebaseFirestore.getInstance().collection("InstituteSemesters")
                .whereEqualTo("InstituteID", instituteId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean semesterNameExists = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Perform case-insensitive comparison for semester names
                        String existingSemesterName = document.getString("semesterName");
                        if (existingSemesterName != null && existingSemesterName.equalsIgnoreCase(semesterName)) {
                            semesterNameExists = true;
                            break;
                        }
                    }

                    if (!semesterNameExists) {
                        // Semester name does not exist in the InstituteSemesters collection, proceed to save/update
                        if (editingSemester != null) {
                            updateSemester(editingSemester, semesterData, progressDialog);
                        } else {
                            addSemester(semesterData, progressDialog);
                        }
                    } else {
                        // Semester name already exists in the InstituteSemesters collection, show error
                        Toast.makeText(context, "Semester name already exists for this institute", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss(); // Dismiss loading
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error checking semester name existence in InstituteSemesters: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Dismiss loading
                });
    }
    private void updateSemester(SemesterModel semester, Map<String, Object> semesterData, ProgressDialog progressDialog) {
        semestersCollection.document(semester.getSemesterID())
                .update(semesterData)
                .addOnSuccessListener(aVoid -> {
                    updateLocalSemester(semester, semesterData);
                    updateOtherStructure(semester, semesterData,progressDialog); // Update semester name in other structure

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error updating semester details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Dismiss loading
                });
    }

    private void updateOtherStructure(SemesterModel semester, Map<String, Object> semesterData, ProgressDialog progressDialog) {
        String semesterId = semester.getSemesterID();
        String semesterName = (String) semesterData.get("semesterName");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference instituteSemestersCollection = db.collection("InstituteSemesters");

        instituteSemestersCollection.whereEqualTo("SemesterID", semesterId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Update the SemesterName field in each document
                        String documentId = document.getId();
                        instituteSemestersCollection.document(documentId)
                                .update("semesterName", semesterName)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss(); // Dismiss loading
                                    alertDialog.dismiss(); // Dismiss dialog after updating
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure if needed
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure if needed
                });
    }

    private void updateLocalSemester(SemesterModel semester, Map<String, Object> semesterData) {
        semester.setSemesterName((String) semesterData.get("semesterName"));
        semester.setStartDate((String) semesterData.get("startDate"));
        semester.setEndDate((String) semesterData.get("endDate"));
        listener.onSemesterAdded(semester);
    }

    private void addSemester(Map<String, Object> semesterData, ProgressDialog progressDialog) {
        semestersCollection
                .add(semesterData)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    String semesterName = (String) semesterData.get("semesterName");

                    SemesterModel newSemester = new SemesterModel(id, semesterName.toUpperCase(),
                            true, (String) semesterData.get("startDate"), (String) semesterData.get("endDate"));
                    listener.onSemesterAdded(newSemester);

                    addInstituteSemester(id, progressDialog, semesterName); // Add institute semester after successfully adding semester
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error saving semester details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Dismiss loading
                });
    }

    private void addInstituteSemester(String semesterId, ProgressDialog progressDialog, String semesterName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference instituteSemestersCollection = db.collection("InstituteSemesters");
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context.getApplicationContext());
        String InstituteID = userInstituteModel.getInstituteId();
        Map<String, Object> instituteSemesterData = new HashMap<>();
        instituteSemesterData.put("SemesterID", semesterId);
        instituteSemesterData.put("semesterName", semesterName.toUpperCase());
        instituteSemesterData.put("InstituteID", InstituteID);

        instituteSemestersCollection
                .add(instituteSemesterData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, semesterName + " Added Successfully!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Dismiss loading
                    alertDialog.dismiss(); // Dismiss dialog after adding
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error adding semester " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Dismiss loading
                });
    }

    public interface SemesterDialogListener {
        void onSemesterAdded(SemesterModel semester);
    }
}
