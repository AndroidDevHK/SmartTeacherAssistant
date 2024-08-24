package Display_Institute_Teachers_To_Assign_Him_Course_Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.LoadingDialogHelper;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import View_Class_Courses_Activity.CourseModel;
import View_Class_Courses_Activity.ManageCoursesActivity;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private List<TeacherModel> teacherList;
    private final Context context;
    private final ActivityManager activityManager;
    private static String courseName;
    private static LoadingDialogHelper loadingDialogHelper;
    static String teacherUserName;
    public TeacherAdapter(Context context, List<TeacherModel> teacherList, ActivityManager activityManager, String courseName, String teacherUserName) {
        this.teacherList = teacherList;
        this.context = context;
        this.activityManager = activityManager;
        this.teacherUserName = teacherUserName;
        this.courseName = courseName;
        this.loadingDialogHelper = new LoadingDialogHelper(context);
    }

    public void setTeacherList(List<TeacherModel> teacherList) {
        this.teacherList = teacherList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_item, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        TeacherModel teacher = teacherList.get(position);
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

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {
        private TextView teacherNameTextView;
        private TextView teacherUsernameTextView;
        private TextView qualificationTextView;
        private TextView departmentTextView;
        private Button assignButton;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            teacherNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            teacherUsernameTextView = itemView.findViewById(R.id.teacherUsernameTextView);
            qualificationTextView = itemView.findViewById(R.id.qualificationTextView);
            departmentTextView = itemView.findViewById(R.id.departmentTextView);
            assignButton = itemView.findViewById(R.id.assignButton);
        }

        @SuppressLint("SetTextI18n")
        public void bind(TeacherModel teacher, ActivityManager activityManager) {
            teacherNameTextView.setText("Name: " + teacher.getTeacherName());
            teacherUsernameTextView.setText("Username: " + teacher.getTeacherUsername());
            qualificationTextView.setText("Qualification: " + teacher.getQualification());
            departmentTextView.setText("Department: " + teacher.getDepartment());

            assignButton.setOnClickListener(v -> {
                String courseId = UserInstituteModel.getInstance(itemView.getContext()).getCourseId();

                String teacherUserName = extractUsername(teacherUsernameTextView.getText().toString());
                String teacherN = extractUsername(teacherNameTextView.getText().toString());

                LoadingDialogHelper loadingDialogHelper = new LoadingDialogHelper(itemView.getContext());
                loadingDialogHelper.showLoadingDialog("Assigning " + courseName + " to " + teacherUserName +" (" +teacherN+")");

                storeTeacherCourse(courseId, teacherUserName, activityManager, loadingDialogHelper,teacherN);
            });
        }

        private String extractUsername(String text) {
            String[] parts = text.split(":");
            if (parts.length == 2) {
                return parts[1].trim();
            } else {
                return "";
            }
        }
        private void storeTeacherCourse(String courseId, String teacherName, ActivityManager activityManager, LoadingDialogHelper loadingDialogHelper, String teacherN) {
            if (teacherName.equals(teacherUserName)) {
                Toast.makeText(itemView.getContext(), "This Course is already assigned to " + teacherName , Toast.LENGTH_SHORT).show();
                loadingDialogHelper.dismissLoadingDialog();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String semesterId = UserInstituteModel.getInstance(itemView.getContext()).getSemesterId();
            String classId = UserInstituteModel.getInstance(itemView.getContext()).getClassId();

            AtomicInteger counter = new AtomicInteger(2); // Two database operations

            db.collection("TeacherCourses")
                    .whereEqualTo("CourseID", courseId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean teacherAssigned = false;
                            for (DocumentSnapshot document : task.getResult()) {
                                String assignmentId = document.getId();
                                db.collection("TeacherCourses")
                                        .document(assignmentId)
                                        .update("TeacherUsername", teacherName, "SemesterID", semesterId, "ClassID", classId)
                                        .addOnSuccessListener(aVoid -> {
                                            updateCourseModel(courseId, teacherName, activityManager, loadingDialogHelper, counter,teacherN);
                                        })
                                        .addOnFailureListener(e -> {
                                            loadingDialogHelper.dismissLoadingDialog();
                                            checkAndDismissLoadingDialog(counter);
                                        });
                                teacherAssigned = true;
                                break;
                            }
                            if (!teacherAssigned) {
                                Map<String, Object> teacherCourseData = new HashMap<>();
                                teacherCourseData.put("CourseID", courseId);
                                teacherCourseData.put("TeacherUsername", teacherName);
                                teacherCourseData.put("SemesterID", semesterId);
                                teacherCourseData.put("ClassID", classId);
                                db.collection("TeacherCourses")
                                        .add(teacherCourseData)
                                        .addOnSuccessListener(documentReference -> {
                                            updateCourseModel(courseId, teacherName, activityManager, loadingDialogHelper, counter, teacherN);
                                        })
                                        .addOnFailureListener(e -> {
                                            loadingDialogHelper.dismissLoadingDialog();
                                            checkAndDismissLoadingDialog(counter);
                                        });
                            }
                        } else {
                            loadingDialogHelper.dismissLoadingDialog();
                            checkAndDismissLoadingDialog(counter);
                        }
                    });

            db.collection("TeacherSemesters")
                    .whereEqualTo("SemesterID", semesterId)
                    .whereEqualTo("TeacherUserName", teacherName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                Map<String, Object> teacherSemesterData = new HashMap<>();
                                teacherSemesterData.put("SemesterID", semesterId);
                                teacherSemesterData.put("TeacherUserName", teacherName);

                                db.collection("TeacherSemesters")
                                        .add(teacherSemesterData)
                                        .addOnSuccessListener(documentReference -> {
                                            checkAndDismissLoadingDialog(counter);
                                        })
                                        .addOnFailureListener(e -> {
                                            checkAndDismissLoadingDialog(counter);
                                        });
                            } else {
                                checkAndDismissLoadingDialog(counter);
                            }
                        } else {
                            checkAndDismissLoadingDialog(counter);
                        }
                    });
        }

        private void checkAndDismissLoadingDialog(AtomicInteger counter) {
            if (counter.decrementAndGet() == 0) {
                loadingDialogHelper.dismissLoadingDialog();
            }
        }

        private void updateCourseModel(String courseId, String teacherName, ActivityManager activityManager, LoadingDialogHelper loadingDialogHelper, AtomicInteger counter, String teacherN) {
            UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(itemView.getContext());
            List<CourseModel> courseList = userInstituteModel.getCourseList();
            for (CourseModel course : courseList) {
                if (course.getCourseId().equals(courseId)) {
                    course.setCourseTeacher(teacherName);
                    course.setCourseTeacherFullName(teacherN);
                    break;
                }
            }
            startManageCoursesActivity(activityManager, loadingDialogHelper, counter,teacherName);
        }

        private void startManageCoursesActivity(ActivityManager activityManager, LoadingDialogHelper loadingDialogHelper, AtomicInteger counter, String teacherName) {
            Intent intent = new Intent(itemView.getContext(), ManageCoursesActivity.class);
            itemView.getContext().startActivity(intent);
            activityManager.finishActivitiesForKill();
            checkAndDismissLoadingDialog(counter);
            Toast.makeText(itemView.getContext(), courseName + " assigned to " + teacherName + " successfully.", Toast.LENGTH_LONG).show();

        }
    }
}
