package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;


public class StudentLoginTestActivity extends AppCompatActivity {

    private EditText editTextUserId;
    private EditText editTextStudentRollNo;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login_test);

        // Initialize views
        editTextUserId = findViewById(R.id.editTextUserId);
        editTextStudentRollNo = findViewById(R.id.editTextStudentRollNo);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Set click listener for login button
        buttonLogin.setOnClickListener(view -> {
            // Get user ID and roll number entered by user
            String userId = editTextUserId.getText().toString().trim();
            String rollNo = editTextStudentRollNo.getText().toString().trim();

            // Check if both user ID and roll number are not empty
            if (!userId.isEmpty() && !rollNo.isEmpty()) {
                // Proceed to next activity (replace StudentDashboardActivity.class with your actual activity class)
                Intent intent = new Intent(StudentLoginTestActivity.this, StudentDashboardActivity.class);
                intent.putExtra("USER_ID", userId); // Pass user ID to the next activity
                intent.putExtra("ROLL_NO", rollNo); // Pass roll number to the next activity
                startActivity(intent);
            } else {
                // Show error message if either user ID or roll number is empty
                if (userId.isEmpty()) {
                    editTextUserId.setError("Please enter User ID");
                    editTextUserId.requestFocus();
                }
                if (rollNo.isEmpty()) {
                    editTextStudentRollNo.setError("Please enter Student Roll No");
                    editTextStudentRollNo.requestFocus();
                }
            }
        });
    }
}
