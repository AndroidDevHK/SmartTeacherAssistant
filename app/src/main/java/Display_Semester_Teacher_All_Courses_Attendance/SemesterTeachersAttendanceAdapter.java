package Display_Semester_Teacher_All_Courses_Attendance;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.R;
import Display_Teacher_Courses_Attendance_Activity.TeacherCourseAttendanceModel;

import java.util.List;

public class SemesterTeachersAttendanceAdapter extends RecyclerView.Adapter<SemesterTeachersAttendanceAdapter.ViewHolder> {

    private List<SemesterTeachersAttendanceModel> teachersAttendanceList;

    public SemesterTeachersAttendanceAdapter(List<SemesterTeachersAttendanceModel> teachersAttendanceList) {
        this.teachersAttendanceList = teachersAttendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.complete_teacher_attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SemesterTeachersAttendanceModel teacherAttendance = teachersAttendanceList.get(position);

        holder.textTeacherName.setText("Teacher Name: " + teacherAttendance.getTeacherName());
        holder.textUserName.setText("User : " +teacherAttendance.getTeacherUserName());

        // Clear existing views
        holder.layoutCourseAttendance.removeAllViews();

        // Add label for attendance details
        TextView label = new TextView(holder.layoutCourseAttendance.getContext());
        label.setText("Attendance Details:");
        label.setTextSize(14);
        label.setTextColor(holder.layoutCourseAttendance.getContext().getResources().getColor(android.R.color.white));
        label.setTypeface(null, Typeface.BOLD); // Making text bold
        holder.layoutCourseAttendance.addView(label);

        // Populate attendance details for each course
        for (TeacherCourseAttendanceModel courseAttendance : teacherAttendance.getTeacherAttendance()) {
            View attendanceItemView = LayoutInflater.from(holder.layoutCourseAttendance.getContext())
                    .inflate(R.layout.course_attendance_item, holder.layoutCourseAttendance, false);

            TextView textCourseName = attendanceItemView.findViewById(R.id.text_course_name);
            TextView textClassName = attendanceItemView.findViewById(R.id.text_class_name);

            TextView textCTakenReq = attendanceItemView.findViewById(R.id.text_classes_taken_required);
            TextView textPercentage = attendanceItemView.findViewById(R.id.text_percentage);
            TextView textAttendanceFromTo = attendanceItemView.findViewById(R.id.text_attendance_from_to);

            textCourseName.setText("Course: " + courseAttendance.getCourseName());
            textClassName.setText("Class : " + courseAttendance.getClassName());
            textCTakenReq.setText("Classes Taken : " +  courseAttendance.getClassesTaken());
            textPercentage.setVisibility(View.GONE);
            textAttendanceFromTo.setText("Duration : " + String.format("From %s to %s", courseAttendance.getFirstAttendanceDate(), courseAttendance.getLastAttendanceDate()));

            holder.layoutCourseAttendance.addView(attendanceItemView);
        }
    }


    @Override
    public int getItemCount() {
        return teachersAttendanceList.size();
    }

    public void updateList(List<SemesterTeachersAttendanceModel> filteredList) {
        this.teachersAttendanceList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTeacherName;
        TextView textUserName;
        LinearLayout layoutCourseAttendance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTeacherName = itemView.findViewById(R.id.text_teacher_name);
            textUserName = itemView.findViewById(R.id.text_username_name);
            layoutCourseAttendance = itemView.findViewById(R.id.layout_course_attendance);
        }
    }
}
