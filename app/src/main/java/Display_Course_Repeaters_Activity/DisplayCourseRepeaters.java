package Display_Course_Repeaters_Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.R;

import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class DisplayCourseRepeaters extends RecyclerView.Adapter<DisplayCourseRepeaters.ViewHolder> {

    private List<RepeaterClassStudentsModel> repeaterClass;

    public DisplayCourseRepeaters(List<RepeaterClassStudentsModel> repeaterClass) {
        this.repeaterClass = repeaterClass;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeater_student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepeaterClassStudentsModel repeaterClassModel = repeaterClass.get(position);
        String className = repeaterClassModel.getClassName();
        int repeaterCount = repeaterClassModel.getStudents().size();

        // Concatenate the class name with the repeater count
        String displayText = className + " (" + repeaterCount + ")";
        holder.classNameTextView.setText(displayText);

        // Clear the student list container before adding new students
        holder.studentListContainer.removeAllViews();

        // Add student views to the student list container
        List<StudentModel> repeaterStudents = repeaterClassModel.getStudents();
        for (StudentModel student : repeaterStudents) {
            View studentView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.repeater_class_item, holder.studentListContainer, false);
            TextView studentNameTextView = studentView.findViewById(R.id.text_student_name);
            TextView rollNoTextView = studentView.findViewById(R.id.text_roll_no);
            studentNameTextView.setText(student.getStudentName());
            rollNoTextView.setText(student.getRollNo());
            holder.studentListContainer.addView(studentView);
        }
    }

    @Override
    public int getItemCount() {
        return repeaterClass.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView classNameTextView;
        public LinearLayout studentListContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classNameTextView = itemView.findViewById(R.id.classNameTextView);
            studentListContainer = itemView.findViewById(R.id.studentListContainer);
        }
    }
}
