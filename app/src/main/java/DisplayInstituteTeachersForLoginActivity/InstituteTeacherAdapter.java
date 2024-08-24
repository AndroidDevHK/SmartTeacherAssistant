package DisplayInstituteTeachersForLoginActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.LoadingDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserCredentialsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstituteTeacherAdapter extends RecyclerView.Adapter<InstituteTeacherAdapter.TeacherViewHolder> {

    private List<InstituteTeacherModel> teacherList;
    private final Context context;
    private final ActivityManager activityManager;
    private static String courseName;
    private static LoadingDialogHelper loadingDialogHelper;
    private FirebaseAuth mAuth;

    public InstituteTeacherAdapter(Context context, List<InstituteTeacherModel> teacherList, ActivityManager activityManager, String courseName) {
        this.teacherList = teacherList;
        this.context = context;
        this.activityManager = activityManager;
        InstituteTeacherAdapter.courseName = courseName;
        InstituteTeacherAdapter.loadingDialogHelper = new LoadingDialogHelper(context);
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void setTeacherList(List<InstituteTeacherModel> teacherList) {
        this.teacherList = teacherList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teachers_permission_item, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        InstituteTeacherModel teacher = teacherList.get(position);
        holder.bind(teacher, activityManager);
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha);
        if (animation != null) {
            animation.setStartOffset(position * 200); // Adjust the delay as needed
            viewToAnimate.startAnimation(animation);
        } else {
            Log.e("Animation", "Failed to load animation");
        }
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public class TeacherViewHolder extends RecyclerView.ViewHolder {
        private TextView teacherNameTextView;
        private TextView teacherUsernameTextView;
        private TextView qualificationTextView;
        private TextView departmentTextView;
        private Switch accountStatusSwitch;
        private Switch attendancePermissionSwitch;
        private TextView resetPasswordBtn;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            teacherNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            teacherUsernameTextView = itemView.findViewById(R.id.teacherUsernameTextView);
            qualificationTextView = itemView.findViewById(R.id.qualificationTextView);
            departmentTextView = itemView.findViewById(R.id.departmentTextView);
            accountStatusSwitch = itemView.findViewById(R.id.accountStatusSwitch);
            attendancePermissionSwitch = itemView.findViewById(R.id.attendancePermissionSwitch);
            resetPasswordBtn = itemView.findViewById(R.id.ResetPasswordBtn);
        }

        @SuppressLint("SetTextI18n")
        public void bind(InstituteTeacherModel teacher, ActivityManager activityManager) {
            teacherNameTextView.setText("Name: " + teacher.getTeacherName());
            teacherUsernameTextView.setText("Email: " + teacher.getTeacherUsername());
            qualificationTextView.setText("Qualification: " + teacher.getQualification());
            departmentTextView.setText("Department: " + teacher.getDepartment());
            accountStatusSwitch.setChecked(teacher.isAccountStatus());
            attendancePermissionSwitch.setChecked(teacher.isPastAPermission());

            accountStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateAccountStatus(teacher.getTeacherUsername(), isChecked);
            });

            attendancePermissionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateAttendancePermission(teacher.getTeacherUsername(), isChecked);
            });

            resetPasswordBtn.setOnClickListener(v -> {
                resetPassword(teacher.getTeacherUsername());
            });
        }

        private void updateAccountStatus(String username, boolean isActive) {
            showLoadingDialog("Updating...");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put("AccountStatus", isActive);

            db.collection("Teachers")
                    .whereEqualTo("Username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String documentId = task.getResult().getDocuments().get(0).getId();
                            db.collection("Teachers").document(documentId)
                                    .update(updates)
                                    .addOnCompleteListener(updateTask -> {
                                        dismissLoadingDialog();
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(itemView.getContext(), "Account status updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(itemView.getContext(), "Failed to update account status", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            dismissLoadingDialog();
                            Toast.makeText(itemView.getContext(), "Failed to find teacher", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void updateAttendancePermission(String username, boolean hasPermission) {
            showLoadingDialog("Updating...");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put("PastAPermission", hasPermission);

            db.collection("Teachers")
                    .whereEqualTo("Username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String documentId = task.getResult().getDocuments().get(0).getId();
                            db.collection("Teachers").document(documentId)
                                    .update(updates)
                                    .addOnCompleteListener(updateTask -> {
                                        dismissLoadingDialog();
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(itemView.getContext(), "Attendance permission updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(itemView.getContext(), "Failed to update attendance permission", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            dismissLoadingDialog();
                            Toast.makeText(itemView.getContext(), "Failed to find teacher", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void resetPassword(String username) {
            showLoadingDialog("Resetting Password...");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("UserCollection")
                    .whereEqualTo("UserEmail", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String currentPassword = task.getResult().getDocuments().get(0).getString("password");

                            mAuth.signInWithEmailAndPassword(username, currentPassword)
                                    .addOnCompleteListener(signInTask -> {
                                        if (signInTask.isSuccessful()) {
                                            FirebaseUser user = signInTask.getResult().getUser();
                                            if (user != null) {
                                                user.updatePassword(username)
                                                        .addOnCompleteListener(updateTask -> {
                                                            if (updateTask.isSuccessful()) {
                                                                updateUserCollectionPassword(username);
                                                            } else {
                                                                dismissLoadingDialog();
                                                                Toast.makeText(itemView.getContext(), "Failed to reset password", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                dismissLoadingDialog();
                                                Toast.makeText(itemView.getContext(), "Failed to find user", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            dismissLoadingDialog();
                                            Toast.makeText(itemView.getContext(), "Failed to authenticate", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            dismissLoadingDialog();
                            Toast.makeText(itemView.getContext(), "Failed to find user", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void updateUserCollectionPassword(String username) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("UserCollection")
                    .whereEqualTo("UserEmail", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String documentId = task.getResult().getDocuments().get(0).getId();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("password", username); // Update password to the email

                            db.collection("UserCollection").document(documentId)
                                    .update(updates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            reLoginAdminAndShowSuccess(username);
                                        } else {
                                            dismissLoadingDialog();
                                            Toast.makeText(itemView.getContext(), "Failed to update password in UserCollection", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            dismissLoadingDialog();
                            Toast.makeText(itemView.getContext(), "Failed to find user in UserCollection", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void reLoginAdminAndShowSuccess(String username) {
            mAuth.signOut();
            String adminEmail = UserCredentialsManager.getEmail(itemView.getContext());
            String adminPassword = UserCredentialsManager.getPassword(itemView.getContext());

            if (adminEmail != null && adminPassword != null) {
                mAuth.signInWithEmailAndPassword(adminEmail, adminPassword)
                        .addOnCompleteListener(signInTask -> {
                            dismissLoadingDialog();
                            if (signInTask.isSuccessful()) {
                                showSuccessDialog(username);
                            } else {
                                Toast.makeText(itemView.getContext(), "Failed to re-login admin", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                dismissLoadingDialog();
                Toast.makeText(itemView.getContext(), "Admin credentials not found", Toast.LENGTH_SHORT).show();
            }
        }

        private void showSuccessDialog(String username) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Password Reset Successful")
                    .setMessage("The password for user " + username + " has been reset successfully.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void showLoadingDialog(String message) {
            loadingDialogHelper.showLoadingDialog(message);
        }

        private void dismissLoadingDialog() {
            loadingDialogHelper.dismissLoadingDialog();
        }
    }
}
