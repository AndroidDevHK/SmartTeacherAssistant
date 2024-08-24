package Add_Class_Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.lambdapioneer.argon2kt.Argon2Kt;
import com.lambdapioneer.argon2kt.Argon2KtResult;
import com.lambdapioneer.argon2kt.Argon2Mode;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.UserInstituteModel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import Add_View_Semester_Activity.ManageSemesterActivity;

public class AddClassViaExcelFileActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> filePickerLauncher;
    private EditText classNameEditText;
    private LinearLayout studentListLayout;
    private Button saveButton;

    private List<String> studentNames = new ArrayList<>();
    private List<String> rollNumbers = new ArrayList<>();
    private String semesterId;
    private String SemesterName;
    ActivityManager activityManager;
    private int successfulSaves = 0;
    private CardView addClassCard;
    private Argon2Kt argon2Kt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_admin);

        // Initialize views
        initViews();

        // Set listeners
        setListeners();
        getSemesterData();
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }

    private void getSemesterData() {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(this);
        SemesterName=userInstituteModel.getSemesterName();
        semesterId=userInstituteModel.getSemesterId();
    }

    private void initViews() {

        argon2Kt = new Argon2Kt();

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Process the selected file URI
                        try {
                            readExcelFile(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showToast("Error reading Excel file");
                        }
                    }
                });
        hideClassDetailsCard();

        activityManager = (ActivityManager) getApplication();
        activityManager.addActivity(this);

        // Find views by their IDs
        classNameEditText = findViewById(R.id.edit_text_class_name);
        studentListLayout = findViewById(R.id.studentListLayout);
        addClassCard = findViewById(R.id.addClassCard);
        saveButton = findViewById(R.id.saveButton);
    }
    private void hideAddClassCard() {
        if (addClassCard != null) {
            addClassCard.setVisibility(View.GONE);
        }
    }
    private void setListeners() {
        // Set onClickListener for the file picker button
        Button filePickerButton = findViewById(R.id.filepickerbtn);
        filePickerButton.setOnClickListener(v -> pickExcelFile());

        // Set onClickListener for the save button
        saveButton.setOnClickListener(v -> saveClassDetails());
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Add Class - "+ SemesterName, true);

    }
    private void pickExcelFile() {
        // Launch the file picker
        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    private void displayClassDetailsCard() {
        // Show the class details card
        CardView classDetailsCard = findViewById(R.id.classDetailsCard);
        classDetailsCard.setVisibility(View.VISIBLE);
    }

    private void hideClassDetailsCard() {
        // Hide the class details card
        CardView classDetailsCard = findViewById(R.id.classDetailsCard);
        classDetailsCard.setVisibility(View.GONE);
    }
    private void readExcelFile(Uri uri) throws IOException {
        try (FileInputStream fis = new FileInputStream(getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Validate if the required data headers are present
            if (!validateHeaders(sheet)) {
                showToast("Required data headers not found in the Excel file.");
                return;
            }

            // Process the data if the headers are validated
            String className = findClassName(sheet);
            if (!className.isEmpty()) {
                classNameEditText.setText(className.toUpperCase());
            } else {
                showToast("Class Name not found please set it manually.");
            }

            CellReference studentHeaderCellRef = findHeaderCell(sheet, "student");
            if (studentHeaderCellRef != null) {
                processColumn(sheet, studentHeaderCellRef, this::updateStudentInfo);
            }

            CellReference rollNoHeaderCellRef = findRollNoHeaderCell(sheet);
            if (rollNoHeaderCellRef != null) {
                processColumn(sheet, rollNoHeaderCellRef, this::storeRollNo);
            }

            // Display student information
            displayStudentInfo();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast("File not found");
        }
    }
    private boolean validateHeaders(Sheet sheet) {
        boolean studentHeaderFound = false;
        boolean rollNoHeaderFound = false;
        List<String> rollNoVariations = Arrays.asList("roll no", "uog", "r.no");

        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().toLowerCase();
                    if (cellValue.contains("name")) {
                        studentHeaderFound = true;
                    }
                    for (String variation : rollNoVariations) {
                        if (cellValue.contains(variation)) {
                            rollNoHeaderFound = true;
                            break; // No need to check other variations if one is found
                        }
                    }
                    if (studentHeaderFound && rollNoHeaderFound) {
                        return true; // Stop searching if both headers are found
                    }
                }
            }
        }

        return studentHeaderFound && rollNoHeaderFound;
    }


    private void processColumn(Sheet sheet, CellReference headerCellRef, DataProcessor dataProcessor) {
        int columnIndex = headerCellRef.getCol();
        for (int i = headerCellRef.getRow() + 1; i <= sheet.getLastRowNum(); i++) {
            Row dataRow = sheet.getRow(i);
            if (dataRow != null) {
                Cell dataCell = dataRow.getCell(columnIndex);
                if (dataCell != null) {
                    if (dataCell.getCellType() == CellType.STRING) {
                        String data = dataCell.getStringCellValue().trim();
                        if (data.isEmpty()) {
                            break; // Stop processing when encountering the first empty cell
                        }
                        dataProcessor.processData(data);
                    } else if (dataCell.getCellType() == CellType.NUMERIC) {
                        String data = String.valueOf((int) dataCell.getNumericCellValue());
                        dataProcessor.processData(data);
                    }
                } else {
                    break; // Stop processing when encountering a null cell
                }
            } else {
                break; // Stop processing when there are no more rows
            }
        }
    }

    private interface DataProcessor {
        void processData(String data);
    }

    private CellReference findRollNoHeaderCell(Sheet sheet) {
        List<String> rollNoVariations = Arrays.asList("roll", "r.no");
        return findHeaderCellByContains(sheet, rollNoVariations);
    }

    private CellReference findHeaderCell(Sheet sheet, String headerText) {
        List<String> headerVariations = Arrays.asList(headerText);
        return findHeaderCellByContains(sheet, headerVariations);
    }

    private CellReference findHeaderCellByContains(Sheet sheet, List<String> headerVariations) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().toLowerCase();
                    for (String variation : headerVariations) {
                        if (cellValue.contains(variation.toLowerCase())) {
                            return new CellReference(row.getRowNum(), cell.getColumnIndex());
                        }
                    }
                }
            }
        }
        return null;
    }


    private CellReference findHeaderCellVariations(Sheet sheet, List<String> headerVariations) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().toLowerCase();
                    for (String variation : headerVariations) {
                        if (cellValue.startsWith(variation)) {
                            return new CellReference(row.getRowNum(), cell.getColumnIndex());
                        }
                    }
                }
            }
        }
        return null;
    }

    private String findClassName(Sheet sheet) {
        List<String> classNameVariations = Arrays.asList("class name", "class", "classname", "semester", "bs:");
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().trim().toLowerCase();
                    for (String variation : classNameVariations) {
                        if (cellValue.startsWith(variation)) {
                            return extractClassName(cellValue);
                        }
                    }
                }
            }
        }
        return "";
    }

    private String extractClassName(String cellValue) {
        if (cellValue.contains(":")) {
            return cellValue.substring(cellValue.indexOf(":") + 1).trim();
        } else {
            String[] parts = cellValue.split("\\s+");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return "";
    }
    private ProgressDialog progressDialog;

    // Method to show a progress dialog with a custom message
    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    // Method to dismiss the progress dialog
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateStudentInfo(String studentName) {
        studentNames.add(studentName);
    }

    private void storeRollNo(String rollNo) {
        rollNumbers.add(rollNo);
    }

    private void displayStudentInfo() {
        hideAddClassCard();
        studentListLayout.removeAllViews(); // Clear the existing views
        displayClassDetailsCard();
        for (int i = 0; i < studentNames.size(); i++) {
            View view = getLayoutInflater().inflate(R.layout.item_student, studentListLayout, false);
            TextView srNoTextView = view.findViewById(R.id.text_sr_no); // Find SR No TextView
            TextView nameTextView = view.findViewById(R.id.text_student_name);
            TextView rollNoTextView = view.findViewById(R.id.text_roll_no);

            // Set SR No
            srNoTextView.setText(String.valueOf(i + 1)+"."); // Serial number starts from 1

            // Set student name
            nameTextView.setText(studentNames.get(i));

            // Set roll number
            if (i < rollNumbers.size()) {
                rollNoTextView.setText(rollNumbers.get(i));
            } else {
                rollNoTextView.setText("N/A");
            }

            studentListLayout.addView(view);
        }
    }
    private void saveClassDetails() {
        String className = classNameEditText.getText().toString().trim().toUpperCase();

        if (className.isEmpty()) {
            classNameEditText.setError("Please enter class name");
            return;
        }
        if (studentNames.isEmpty() || rollNumbers.isEmpty()) {
            showToast("Student list is empty. Please use correct excel file.");
            return;
        }
        for (int i = 0; i < studentNames.size(); i++) {
            String studentName = studentNames.get(i).trim();
            if (studentName.isEmpty() || isNumeric(studentName) || "N/A".equals(studentName)) {
                showToast("Student name for student " + (i + 1) + " is missing, empty, or invalid. Please correct the Excel file.");
                return;
            }
        }
        for (int i = 0; i < rollNumbers.size(); i++) {
            String rollNo = rollNumbers.get(i).trim();
            if (rollNo.isEmpty() || "N/A".equals(rollNo)) {
                showToast("Roll number for student " + (i + 1) + " is missing or invalid. Please correct the Excel file.");
                return;
            }
        }

        // Check if the class name already exists
        checkClassNameExists(className);
    }
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void checkClassNameExists(String className) {
        showProgressDialog("Checking Class Existence..");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("SemesterClasses")
                .whereEqualTo("SemesterID", semesterId)
                .whereEqualTo("ClassName", className)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Class name already exists in this semester
                        Toast.makeText(AddClassViaExcelFileActivity.this,
                                "Class already exists in " +UserInstituteModel.getInstance(this).getSemesterName(),
                                Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                    } else {

                        dismissProgressDialog();
                        showConfirmationDialog(className);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while checking
                    Toast.makeText(AddClassViaExcelFileActivity.this,
                            "Error checking class name: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    private void showConfirmationDialog(String className) {
        FirebaseFirestore db2 = FirebaseFirestore.getInstance();
        List<String> studentNames = new ArrayList<>();
        List<String> rollNumbers = new ArrayList<>();
        Set<String> uniqueRollNumbers = new HashSet<>(); // To check uniqueness of roll numbers
        StringBuilder message = new StringBuilder("Class Name: " + className + "\n\nStudents:\n");

        for (int i = 0; i < studentListLayout.getChildCount(); i++) {
            View studentItemView = studentListLayout.getChildAt(i);
            TextView studentNameEditText = studentItemView.findViewById(R.id.text_student_name);
            TextView rollNoEditText = studentItemView.findViewById(R.id.text_roll_no);
            String studentName = studentNameEditText.getText().toString().trim();
            String rollNo = rollNoEditText.getText().toString().trim();
            if (!studentName.isEmpty() && !rollNo.isEmpty()) {
                studentNames.add(studentName);
                rollNumbers.add(rollNo);
                uniqueRollNumbers.add(rollNo); // Add roll number to the set to ensure uniqueness
                message.append(i + 1).append(". ").append(studentName).append(" — ").append(rollNo).append("\n");
            } else {
                // If any field is empty, show a toast and return
                Toast.makeText(AddClassViaExcelFileActivity.this,
                        "Please enter both student name and roll number",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Class Details")
                .setMessage(message.toString())
                .setPositiveButton("Yes", (dialog, which) -> saveClassData(db2))
                .setNegativeButton("No", null)
                .show();
    }


    private void saveClassData(FirebaseFirestore db) {
        showProgressDialog("Saving Class in database...");

        // Fetch the latest UserID for the given institute
        String instituteId = UserInstituteModel.getInstance(this).getInstituteId();
        db.collection("StudentSemestersDetails")
                .orderBy("UserID", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int latestUserIdNumber = -1; // Start from -1 so the first increment results in 0
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String latestUserId = queryDocumentSnapshots.getDocuments().get(0).getString("UserID");
                        latestUserIdNumber = Integer.parseInt(latestUserId.substring(1));
                    }

                    // Prepare class data
                    Map<String, Object> classData = new HashMap<>();
                    classData.put("ClassName", classNameEditText.getText().toString().toUpperCase());

                    // Create a reference for the new class document
                    DocumentReference classRef = db.collection("Classes").document();

                    // Prepare semester class data
                    Map<String, Object> semesterClassData = new HashMap<>();
                    semesterClassData.put("ClassID", classRef.getId());
                    semesterClassData.put("ClassName", classNameEditText.getText().toString().toUpperCase());
                    semesterClassData.put("SemesterID", semesterId);

                    // Create a reference for the new semester class document
                    DocumentReference semesterClassRef = db.collection("SemesterClasses").document();

                    WriteBatch batch = db.batch();
                    batch.set(classRef, classData);
                    batch.set(semesterClassRef, semesterClassData);

                    for (int i = 0; i < studentNames.size(); i++) {
                        Map<String, Object> studentData = new HashMap<>();
                        studentData.put("StudentName", studentNames.get(i));
                        studentData.put("RollNo", i < rollNumbers.size() ? rollNumbers.get(i) : "N/A");
                        studentData.put("IsActive", true); // Set student status as true by default

                        // Create a reference for each student document
                        DocumentReference studentRef = classRef.collection("ClassStudents").document();

                        // Add student data to batch
                        batch.set(studentRef, studentData);

                        // Generate new UserID and hashed password
                        int newUserIdNumber = ++latestUserIdNumber;
                        String newUserId = generateUserId(newUserIdNumber);

                        // Prepare student semester details
                        String studentDocumentId = UUID.randomUUID().toString();
                        Map<String, Object> studentSemesterData = new HashMap<>();
                        studentSemesterData.put("StudentRollNo", i < rollNumbers.size() ? rollNumbers.get(i) : "N/A");
                        studentSemesterData.put("SemesterID", semesterId);
                        studentSemesterData.put("InstituteID", instituteId);
                        studentSemesterData.put("ClassID", classRef.getId());
                        studentSemesterData.put("UserID", newUserId);
                        studentSemesterData.put("HashedPassword", newUserId); // Add hashed password

                        // Create a reference for each student semester document
                        DocumentReference studentSemesterRef = db.collection("StudentSemestersDetails").document(studentDocumentId);

                        // Add student semester data to batch
                        batch.set(studentSemesterRef, studentSemesterData);
                    }

                    // Commit the batch write
                    batch.commit().addOnSuccessListener(aVoid -> {
                        activityManager.finishActivitiesExceptMainMenuActivity();
                        Intent intent = new Intent(AddClassViaExcelFileActivity.this, ManageSemesterActivity.class);
                        startActivity(intent);
                        dismissProgressDialog();
                    }).addOnFailureListener(e -> {
                        onFirestoreError(e);
                        dismissProgressDialog();
                    });
                })
                .addOnFailureListener(e -> {
                    onFirestoreError(e);
                    dismissProgressDialog();
                });
    }

    private String generateUserId(int userIdNumber) {
        return String.format("S%04d", userIdNumber); // Format UserID as S0001, S0002, etc.
    }






    private void onFirestoreError(Exception e) {
        Log.e("Firestore", "Error saving class details to Firestore", e);
        showToast("Error saving class details to Firestore");
    }






}
