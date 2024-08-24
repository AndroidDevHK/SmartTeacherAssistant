package com.nextgen.hasnatfyp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    private LinearLayout individualLayout, instituteLayout;
    private EditText usernameIndividual, fullnameIndividual, passwordIndividual, confirmPasswordIndividual;
    private EditText usernameInstitute, fullnameInstitute, instituteName, passwordInstitute, confirmPasswordInstitute;
    private Spinner signupTypeSpinner;
    private MaterialButton signUpButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final String ERROR_EMPTY_FIELD = "Please fill in all fields.";
    private static final String ERROR_PASSWORDS_MISMATCH = "Passwords do not match.";
    private static final String ERROR_INVALID_EMAIL = "Please enter a valid email address.";
    private static final String SUCCESS_SIGNUP = "Sign up successful!";
    private static final String FAILURE_SIGNUP = "Failed to sign up.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setupSpinner();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.customToolbar);
        SetupToolbar(toolbar);
        applyFadeInAnimation();

    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Create Account", true);
    }

    private void initializeViews() {
        individualLayout = findViewById(R.id.individualSignupLayout);
        instituteLayout = findViewById(R.id.instituteSignupLayout);
        usernameIndividual = findViewById(R.id.usernameIndividual);
        fullnameIndividual = findViewById(R.id.fullnameIndividual);
        passwordIndividual = findViewById(R.id.passwordIndividual);
        confirmPasswordIndividual = findViewById(R.id.confirmPasswordIndividual);
        usernameInstitute = findViewById(R.id.usernameInstitute);
        fullnameInstitute = findViewById(R.id.fullnameInstitute);
        instituteName = findViewById(R.id.instituteName);
        passwordInstitute = findViewById(R.id.passwordInstitute);
        confirmPasswordInstitute = findViewById(R.id.confirmPasswordInstitute);
        signupTypeSpinner = findViewById(R.id.spinnerSignupTypes);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> signUp());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.signup_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        signupTypeSpinner.setAdapter(adapter);

        signupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();
                individualLayout.setVisibility(selectedItem.equals("Individual") ? View.VISIBLE : View.GONE);
                instituteLayout.setVisibility(selectedItem.equals("Institute") ? View.VISIBLE : View.GONE);
                signUpButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void signUp() {
        String selectedItem = signupTypeSpinner.getSelectedItem().toString();
        if (selectedItem.equals("Individual")) {
            signUpIndividual();
        } else if (selectedItem.equals("Institute")) {
            signUpInstituteAdmin();
        }
    }

    private void signUpIndividual() {
        String email = usernameIndividual.getText().toString().trim();
        String fullName = fullnameIndividual.getText().toString().trim();
        String password = passwordIndividual.getText().toString().trim();
        String confirmPassword = confirmPasswordIndividual.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            showToast(ERROR_EMPTY_FIELD);
            return;
        }
        if (!isValidEmail(email)) {
            showToast(ERROR_INVALID_EMAIL);
            return;
        }
        if (!password.equals(confirmPassword)) {
            showToast(ERROR_PASSWORDS_MISMATCH);
            return;
        }
        showConfirmationDialog(email, fullName, password, "Individual", null);
    }

    private void showConfirmationDialog(String email, String fullName, String password, String userType, String instituteName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Sign Up");

        String message = "<b>Email:</b> " + email +
                "<br><b>Full Name:</b> " + fullName +
                "<br><b>User Type:</b> " + userType;

        if (instituteName != null) {
            message += "<br><b>Institute Name:</b> " + instituteName;
        }

        builder.setMessage(android.text.Html.fromHtml(message));

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            ProgressDialogHelper.showProgressDialog(SignUpActivity.this, "Creating Account...");
            if (userType.equals("Individual")) {
                registerUser(email, password, fullName, userType);
            } else {
                registerUser(email, password, fullName, userType, instituteName);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void signUpInstituteAdmin() {
        String email = usernameInstitute.getText().toString().trim();
        String fullName = fullnameInstitute.getText().toString().trim();
        String institute = instituteName.getText().toString().trim();
        String password = passwordInstitute.getText().toString().trim();
        String confirmPassword = confirmPasswordInstitute.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(institute) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            showToast(ERROR_EMPTY_FIELD);
            return;
        }

        if (!isValidEmail(email)) {
            showToast(ERROR_INVALID_EMAIL);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast(ERROR_PASSWORDS_MISMATCH);
            return;
        }

        showConfirmationDialog(email, fullName, password, "InstituteAdmin", institute);
    }

    private void registerUser(String email, String password, String fullName, String userType) {
        registerUser(email, password, fullName, userType, "Smart Teacher Assistant");
    }

    private void registerUser(String email, String password, String fullName, String userType, String instituteName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        String instituteId = UUID.randomUUID().toString();
                        saveUserData(userId, email, fullName, userType, instituteId, instituteName, password);
                    } else {
                        showToast(FAILURE_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void saveUserData(String userId, String email, String fullName, String userType, String instituteId, String instituteName, String password) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("UserEmail", email);
        userMap.put("password", password); // Consider hashing the password before storing
        userMap.put("UUID", userId);
        userMap.put("InstituteID", instituteId);
        userMap.put("userType", userType);

        db.collection("UserCollection")
                .document(userId)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveAdminData(userId, email, fullName, instituteName, instituteId);
                    } else {
                        showToast(FAILURE_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void saveAdminData(String userId, String email, String fullName, String instituteName, String instituteId) {
        Map<String, Object> adminMap = new HashMap<>();
        adminMap.put("UserEmail", email);
        adminMap.put("UUID", userId);
        adminMap.put("UserFullName", fullName);
        adminMap.put("InstituteName", instituteName);

        db.collection("AdminCollection")
                .document(userId)
                .set(adminMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveInstituteName(instituteName, instituteId);
                    } else {
                        showToast(FAILURE_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void saveInstituteName(String instituteName, String instituteId) {
        Map<String, Object> instituteMap = new HashMap<>();
        instituteMap.put("InstituteName", instituteName);

        db.collection("InstitutesName")
                .document(instituteId)
                .set(instituteMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast(SUCCESS_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                        finish();
                    } else {
                        showToast(FAILURE_SIGNUP);
                        ProgressDialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void applyFadeInAnimation() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.customToolbar).startAnimation(fadeInAnimation);
        findViewById(R.id.logoImageView).startAnimation(fadeInAnimation);
        findViewById(R.id.titleTextView).startAnimation(fadeInAnimation);
        findViewById(R.id.cardView).startAnimation(fadeInAnimation);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
