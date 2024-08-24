package View_Class_Courses_Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.ExcludeCourseRegularStudentsActivity;
import com.nextgen.hasnatfyp.ManageCourseRepeatersActivity;
import com.nextgen.hasnatfyp.R;
import View_Classes_For_Repeaters_Selection_Activity.SelectClassForRepeatersAcitvity;
import com.nextgen.hasnatfyp.UserInstituteModel;
import Display_Course_Repeaters_Activity.ViewClassCourseRepeaters;
import Display_Institute_Teachers_To_Assign_Him_Course_Activity.DisplayInstituteTeachersActivity;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<CourseModel> courseList;
    private Context context;
    private ActivityManager activityManager;

    public CourseAdapter(ManageCoursesActivity manageCoursesActivity, ActivityManager activityManager) {
        this.courseList = new ArrayList<>();
        this.context = manageCoursesActivity;
        this.activityManager = activityManager;
    }

    public void setCourseList(List<CourseModel> courseList) {
        this.courseList = courseList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseModel course = courseList.get(position);
        holder.bind(course);
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
        return courseList.size();
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView courseDetailsTextView;
        private TextView teacherUserNameTextView;
        private TextView teacherfullNameTextView;
        private Switch semesterStatusSwitch;
        private ImageButton editButton;
        private ImageButton deleteButton;
        private TextView assignTeacherTextView;
        private View namedivider;
        private ProgressDialog progressDialog;
        private LinearLayout LayoutExcludeRegStudents;
        private LinearLayout layoutAddRepeaterStudents;
        private FrameLayout LayoutTeachers;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseDetailsTextView = itemView.findViewById(R.id.text_Course_Details);
            teacherUserNameTextView = itemView.findViewById(R.id.teacherUsernameTextView);
            teacherfullNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            semesterStatusSwitch = itemView.findViewById(R.id.switch_semester_status);
            editButton = itemView.findViewById(R.id.btn_edit);
            namedivider = itemView.findViewById(R.id.NameDivider);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            LayoutExcludeRegStudents = itemView.findViewById(R.id.LayoutExcludeRegStudents);
            layoutAddRepeaterStudents = itemView.findViewById(R.id.LayoutAddRepeater);
            assignTeacherTextView = itemView.findViewById(R.id.assignCourseTeacherTextView);
            LayoutTeachers = itemView.findViewById(R.id.LayoutTeachers);
            if (UserInstituteModel.getInstance(itemView.getContext()).isSoloUser()){
                assignTeacherTextView.setVisibility(View.GONE);
                LayoutTeachers.setVisibility(View.GONE);
            } else {
                assignTeacherTextView.setVisibility(View.VISIBLE);
                LayoutTeachers.setVisibility(View.VISIBLE);
            }
        }

        public void bind(CourseModel course) {
            // Set course details
            String courseDetails = "Course: " + course.getCourseName();
            courseDetailsTextView.setText(courseDetails);

            // Check if teacher username is empty
            if (course.getCourseTeacher().isEmpty()) {
                teacherUserNameTextView.setText("Teacher: Not Assigned yet");
                teacherfullNameTextView.setText("N/A");
                teacherfullNameTextView.setVisibility(View.GONE);
                namedivider.setVisibility(View.GONE);
            } else {
                teacherUserNameTextView.setText("User: " + course.getCourseTeacher());
                teacherfullNameTextView.setText("Name : " + course.getCourseTeacherFullName());
                teacherfullNameTextView.setVisibility(View.VISIBLE); // Ensure visibility when teacher is assigned
                namedivider.setVisibility(View.VISIBLE);
            }

            semesterStatusSwitch.setChecked(course.isCourseActive());

            if (!course.getCourseTeacher().isEmpty()) {
                assignTeacherTextView.setText("Change Course Teacher");
            } else {
                assignTeacherTextView.setText("Assign Teacher");
            }


            editButton.setOnClickListener(v -> {
                showCourseEditDialog(itemView.getContext(), course);
            });

            assignTeacherTextView.setOnClickListener(v -> {
                if (!course.getCourseTeacher().isEmpty()) {
                    showConfirmationDialog(itemView.getContext(), course);
                } else {
                    goToDisplayInstituteTeachersActivity(itemView.getContext(), course);
                }
            });

            LayoutExcludeRegStudents.setOnClickListener(v -> {
                // Open ViewClassCourseRepeaters activity
                Intent intent = new Intent(itemView.getContext(), ExcludeCourseRegularStudentsActivity.class);
                UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(itemView.getContext());
                userInstituteModel.setCourseId(course.getCourseId());
                intent.putExtra("courseName", course.getCourseName());
                itemView.getContext().startActivity(intent);
            });

            layoutAddRepeaterStudents.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ManageCourseRepeatersActivity.class);
                UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(itemView.getContext());
                userInstituteModel.setCourseId(course.getCourseId());
                intent.putExtra("courseName", course.getCourseName());
                itemView.getContext().startActivity(intent);
            });
        }


        private void showCourseEditDialog(Context context, CourseModel course) {
            // Initialize the CourseEditDialog
            CourseEditDialog dialog = new CourseEditDialog(
                    context,
                    course.getCourseId(),
                    course.getCourseName(),
                    String.valueOf(course.getCreditHours()),
                    UserInstituteModel.getInstance(itemView.getContext()).getClassId(),
                    (courseId, updatedCourseName) -> {
                        int position = findCoursePosition(courseId);
                        if (position != -1) {
                            courseList.get(position).setCourseName(updatedCourseName);
                            notifyItemChanged(position);
                        }
                    });

            // Show the dialog
            dialog.show();
        }

        private int findCoursePosition(String courseId) {
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getCourseId().equals(courseId)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private void showConfirmationDialog(Context context, CourseModel course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");

        // Build the message with HTML formatting
        String message = "<b>Are you sure you want to change the following course teacher?</b><br/><br/>" +
                "<u>Course:</u><br/>" + course.getCourseName() + "<br/><br/>" +
                "<u>Teacher:</u><br/>" + course.getCourseTeacher();
        builder.setMessage(Html.fromHtml(message));

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Proceed to DisplayInstituteTeachersActivity
            goToDisplayInstituteTeachersActivity(context, course);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // Dismiss the dialog
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToDisplayInstituteTeachersActivity(Context context, @NonNull CourseModel course) {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context);
        userInstituteModel.setCourseId(course.getCourseId());

        Intent intent = new Intent(context, DisplayInstituteTeachersActivity.class);
        intent.putExtra("courseName", course.getCourseName());
        intent.putExtra("teacherUserName", course.getCourseTeacher());
        intent.putExtra("teacherFullName", course.getCourseTeacherFullName());

        context.startActivity(intent);
    }
}
