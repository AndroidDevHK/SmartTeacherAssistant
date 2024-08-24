package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentSemesterCoursesAdapter extends RecyclerView.Adapter<StudentSemesterCoursesAdapter.ViewHolder> {

    private List<StudentCourseModel> coursesList;
    private Context context;
    private String rollNo;

    public StudentSemesterCoursesAdapter(Context context, List<StudentCourseModel> coursesList, String rollNo) {
        this.context = context;
        this.coursesList = coursesList;
        this.rollNo = rollNo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_course_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentCourseModel course = coursesList.get(position);
        holder.textCourseName.setText(course.getCourseName());

        // Set the onClick listener for layout_view_attendance
        holder.layoutViewAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(context, DisplayStudentCourseAttendanceActivity.class);
            intent.putExtra("ROLL_NO", rollNo);
            intent.putExtra("COURSE_ID", course.getCourseID());
            StudentSessionInfo.getInstance(context).setCourseName(course.getCourseName());
            context.startActivity(intent);
        });
        holder.layoutViewEvaluation.setOnClickListener(v -> {
            Intent intent = new Intent(context, DisplayStudentCourseEvaluationActivity.class);
            intent.putExtra("ROLL_NO", rollNo);
            intent.putExtra("COURSE_ID", course.getCourseID());
            StudentSessionInfo.getInstance(context).setCourseName(course.getCourseName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return coursesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCourseName;
        LinearLayout layoutViewAttendance;
        LinearLayout layoutViewEvaluation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCourseName = itemView.findViewById(R.id.text_Course_name);
            layoutViewAttendance = itemView.findViewById(R.id.layout_view_attendance);
            layoutViewEvaluation = itemView.findViewById(R.id.layout_view_results);
        }
    }
}
