package com.nextgen.hasnatfyp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteEverythingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_everything);

        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.setCancelable(false);
    }

    public void onDeleteEverythingButtonClick(View view) {
        showProgressDialog();
        deleteAllCollections();
    }

    private void deleteAllCollections() {
        final AtomicInteger collectionsDeleted = new AtomicInteger(0);
        final int totalCollections = 2; // Number of collections to delete

        deleteCollection("Classes", () -> {
            collectionsDeleted.incrementAndGet();
            if (collectionsDeleted.get() == totalCollections) {
                dismissProgressDialog();
                Toast.makeText(DeleteEverythingActivity.this, "All collections and subcollections deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });

        deleteCollection("ClassCourses", () -> {
            collectionsDeleted.incrementAndGet();
            if (collectionsDeleted.get() == totalCollections) {
                dismissProgressDialog();
                Toast.makeText(DeleteEverythingActivity.this, "All collections and subcollections deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCollection(String collectionName, final Runnable onComplete) {
        db.collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (final com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection(collectionName)
                                .document(document.getId())
                                .collection(collectionName.equals("Classes") ? "ClassStudents" : "ClassCoursesSubcollection")
                                .get()
                                .addOnSuccessListener(subcollectionSnapshots -> {
                                    for (com.google.firebase.firestore.DocumentSnapshot subdocument : subcollectionSnapshots.getDocuments()) {
                                        subdocument.getReference().delete();
                                    }
                                    document.getReference().delete();
                                    onComplete.run();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DeleteEverythingActivity.this, "Failed to delete subcollection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DeleteEverythingActivity.this, "Failed to delete collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressDialog() {
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}

