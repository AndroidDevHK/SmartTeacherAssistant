package com.nextgen.hasnatfyp;

import com.google.firebase.firestore.FirebaseFirestore;


public class UserRepository {

    private FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void login(String username, String password, OnLoginListener listener) {
        db.collection("users").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String storedPassword = documentSnapshot.getString("Password");
                        if (storedPassword.equals(password)) {
                            String role = documentSnapshot.getString("Role");
                            String fullName = documentSnapshot.getString("FullName");
                            String instituteId = documentSnapshot.getString("InstituteId");
                            if (role.equals("Individual")) {
                                listener.onIndividualLogin();
                            } else if (role.equals("Admin")) {
                                listener.onAdminLogin(fullName, instituteId);
                            } else if (role.equals("Student")) {
                                listener.onStudentLogin(username, instituteId);
                            } else if (role.equals("Principal")) {
                                listener.onPrincipalLogin(username, instituteId);
                            }
                        } else {
                            listener.onLoginFailure("Invalid username or password");
                        }
                    } else {
                        listener.onLoginFailure("Invalid username or password");
                    }
                })
                .addOnFailureListener(e -> listener.onLoginFailure("Failed to login"));
    }

    public interface OnLoginListener {
        void onIndividualLogin();
        void onAdminLogin(String fullName, String instituteId);
        void onStudentLogin(String username, String instituteId);
        void onPrincipalLogin(String username, String instituteId);
        void onLoginFailure(String errorMessage);
    }
}
