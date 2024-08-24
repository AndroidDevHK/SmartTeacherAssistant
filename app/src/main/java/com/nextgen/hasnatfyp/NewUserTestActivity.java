package com.nextgen.hasnatfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lambdapioneer.argon2kt.Argon2Kt;
import com.lambdapioneer.argon2kt.Argon2KtResult;
import com.lambdapioneer.argon2kt.Argon2Mode;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class NewUserTestActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText userTypeEditText;
    private EditText instituteIdEditText;
    private FirebaseFirestore db;
    private Button validateUserCampusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_test);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize EditText fields
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        userTypeEditText = findViewById(R.id.userTypeEditText);
        instituteIdEditText = findViewById(R.id.instituteIdEditText);

        validateUserCampusButton = findViewById(R.id.ValidateUserCampus);

        // Set click listener for validateUserCampusButton
        validateUserCampusButton.setOnClickListener(v -> validateUserCampus());
    }

    private void validateUserCampus() {
        Intent intent = new Intent(NewUserTestActivity.this, ValidateCampus.class);
        startActivity(intent);
    }

    public void onNxt(View view) {
        // Get user input from EditText fields
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String userType = userTypeEditText.getText().toString().trim();
        String instituteId = instituteIdEditText.getText().toString().trim();

        // Insert user data
        insertUserData(username, password, userType, instituteId);
    }

    private void insertUserData(String username, String password, String userType, String instituteId) {
        // Hash the password
        String hashedPassword = hashPassword(password);

        // Create a map to store user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("Username", username);
        userData.put("password", hashedPassword); // Store hashed password
        userData.put("userType", userType);
        userData.put("InstituteID", instituteId);

        // Add the user data to UserCollection
        db.collection("UserCollection")
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    // Store the user's information in InstituteTeachers
                    Map<String, Object> instituteTeachersData = new HashMap<>();
                    instituteTeachersData.put("Username", username);
                    instituteTeachersData.put("InstituteID", instituteId);

                    // Add the user's information to InstituteTeachers
                    db.collection("InstituteTeachers")
                            .add(instituteTeachersData)
                            .addOnSuccessListener(documentReference1 -> {
                                Toast.makeText(NewUserTestActivity.this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
                                // Clear input fields
                                usernameEditText.setText("");
                                passwordEditText.setText("");
                                userTypeEditText.setText("");
                                instituteIdEditText.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(NewUserTestActivity.this, "Failed to insert data in InstituteTeachers", Toast.LENGTH_SHORT).show();
                                System.err.println("Error adding document to InstituteTeachers: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NewUserTestActivity.this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                    System.err.println("Error adding document to UserCollection: " + e.getMessage());
                });
    }

    private String hashPassword(String password) {
        Argon2Kt argon2Kt = new Argon2Kt();

        // Generate a random salt
        byte[] salt = generateSalt();

        // Hash the password with the random salt
        Argon2KtResult hashResult = argon2Kt.hash(Argon2Mode.ARGON2_I, password.getBytes(), salt);

        return hashResult.encodedOutputAsString();
    }

    // Method to generate a random salt
    private byte[] generateSalt() {
        byte[] salt = new byte[16]; // Salt length is 16 bytes
        new SecureRandom().nextBytes(salt); // Fill the salt with random bytes
        return salt;
    }
}
