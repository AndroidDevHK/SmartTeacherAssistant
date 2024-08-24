package Display_Teacher_Courses_Attendance_Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.WeekWiseAttendanceCalculator;

import java.util.List;

public class TeacherCourseAttendanceAdapter extends RecyclerView.Adapter<TeacherCourseAttendanceAdapter.ViewHolder> {

    private List<TeacherCourseAttendanceModel> courseAttendanceList;
    private static final String defaultStartDate = "2024-04-01"; // Default start date
    private static final String defaultEndDate = "2024-05-05";   // Default end date

    public TeacherCourseAttendanceAdapter(List<TeacherCourseAttendanceModel> courseAttendanceList) {
        this.courseAttendanceList = courseAttendanceList;
    }
    public List<TeacherCourseAttendanceModel> getCourseAttendanceList() {
        return courseAttendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_teacher_attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherCourseAttendanceModel attendanceModel = courseAttendanceList.get(position);
        holder.bind(attendanceModel);
    }

    @Override
    public int getItemCount() {
        return courseAttendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView classesTakenTextView;
        private final TextView classNameTextView;
        private final TextView courseNameTextView;
        private final TextView creditHoursTextView;
        private final TextView weekWiseAttendanceTextView;
        private TextView attendanceDurationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classesTakenTextView = itemView.findViewById(R.id.classesTakenTextView);
            classNameTextView = itemView.findViewById(R.id.text_Class_name);
            courseNameTextView = itemView.findViewById(R.id.text_course_name);
            creditHoursTextView = itemView.findViewById(R.id.creditHoursTextView);
            attendanceDurationTextView = itemView.findViewById(R.id.attendance_duration);
            weekWiseAttendanceTextView = itemView.findViewById(R.id.WeekWiseAttendanceTextView);
        }

        public void bind(TeacherCourseAttendanceModel attendanceModel) {
            classNameTextView.setText("Class Name: " + attendanceModel.getClassName());
            courseNameTextView.setText("Course Name: " + attendanceModel.getCourseName());
            classesTakenTextView.setText("Classes Taken: " + attendanceModel.getClassesTaken());
            attendanceDurationTextView.setText("Attendance: " + attendanceModel.getFirstAttendanceDate() + " to " + attendanceModel.getLastAttendanceDate());
            weekWiseAttendanceTextView.setText("Week Wise Attendance:\n" + WeekWiseAttendanceCalculator.calculate(defaultStartDate, attendanceModel.getLastAttendanceDate(), attendanceModel.getTeacherAttendance(), creditHoursTextView, attendanceModel.getCreditHours()));
            attendanceModel.setExpectedClasses(extractExpectedClasses(creditHoursTextView.getText().toString()));
        }
        private int extractExpectedClasses(String text) {
            String[] parts = text.split(":");
            if (parts.length >= 2) {
                String countText = parts[1].trim();
                return Integer.parseInt(countText);
            } else {
                return 0; // or throw an exception
            }
        }

    }
}
