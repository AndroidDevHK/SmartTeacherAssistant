package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.lambdapioneer.argon2kt.Argon2Kt;
import com.lambdapioneer.argon2kt.Argon2KtResult;
import com.lambdapioneer.argon2kt.Argon2Mode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class ValidateCampus extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button validateUserCampusButton;
    private FirebaseFirestore db;
    private Argon2Kt argon2Kt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_campus);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Argon2Kt
        argon2Kt = new Argon2Kt();

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        validateUserCampusButton = findViewById(R.id.validateUserCampusButton);

        // Set click listener for validateUserCampusButton
        validateUserCampusButton.setOnClickListener(v -> validateUserCampus());
    }

    private void validateUserCampus() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Query Firestore to get all documents where the username matches
        Query query = db.collection("UserCollection").whereEqualTo("Username", username);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    // Verify password hash for each user found
                    String storedHash = document.getString("password");
                    boolean passwordVerified = argon2Kt.verify(Argon2Mode.ARGON2_I, storedHash, password.getBytes());
                    if (passwordVerified) {
                        // Password hash verified, show toast with institute ID and user type
                        String instituteId = document.getString("InstituteID");
                        String userType = document.getString("userType");
                        String message = "Institute ID: " + instituteId + "\nUser Type: " + userType;
                        Toast.makeText(ValidateCampus.this, message, Toast.LENGTH_SHORT).show();
                        return; // Exit the method after finding a matching user
                    }
                }
                // If no matching user found or password not verified for any user
                Toast.makeText(ValidateCampus.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
            } else {
                // Error fetching documents
                Toast.makeText(ValidateCampus.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
