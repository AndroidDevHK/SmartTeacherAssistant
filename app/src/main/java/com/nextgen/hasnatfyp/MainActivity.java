package com.nextgen.hasnatfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private Button signUpButton;
    private TextView instituteIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpButton = findViewById(R.id.signUpButton);
        instituteIdTextView = findViewById(R.id.welcomeTextView);

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Query Firestore for Institute ID where AdminUsername is "HK_Khalid"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("institutes")
                .whereEqualTo("AdminUsername", "HK_Khalid")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String instituteId = document.getId();
                            instituteIdTextView.setText("Institute ID: " + instituteId);
                        } else {
                            instituteIdTextView.setText("No Institute found for AdminUsername: HK_Khalid");
                        }
                    } else {
                        instituteIdTextView.setText("Failed to retrieve Institute ID");
                    }
                });
    }
}
