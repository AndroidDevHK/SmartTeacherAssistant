package View_Students_List_To_Select_Repeaters_Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.R;

import java.util.ArrayList;
import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class RepeaterSelectionAdapter extends RecyclerView.Adapter<RepeaterSelectionAdapter.ViewHolder> {

    private List<StudentModel> studentList;

    public RepeaterSelectionAdapter(List<StudentModel> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_repeater_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(studentList.get(position));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView studentNameTextView;
        private TextView rollNumberTextView;
        private CheckBox selectCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.text_student_name);
            rollNumberTextView = itemView.findViewById(R.id.text_roll_number);
            selectCheckBox = itemView.findViewById(R.id.checkbox_select);

            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    studentList.get(position).setActive(isChecked);
                }
            });

        }

        public void bind(StudentModel student) {
            studentNameTextView.setText(student.getStudentName());
            rollNumberTextView.setText(student.getRollNo());
            selectCheckBox.setChecked(student.isActive());
        }
    }

    public List<StudentModel> getSelectedStudents() {
        List<StudentModel> selectedStudents = new ArrayList<>();
        for (StudentModel student : studentList) {
            if (student.isActive()) {
                selectedStudents.add(student);
            }
        }
        return selectedStudents;
    }

    public void setStudentList(List<StudentModel> studentList) {
        this.studentList = studentList;
        notifyDataSetChanged();
    }

    public List<StudentModel> getAllStudentsWithCurrentStatus() {
        List<StudentModel> studentsWithStatus = new ArrayList<>();
        for (StudentModel student : studentList) {
            StudentModel studentWithStatus = new StudentModel();
            studentWithStatus.setRollNo(student.getRollNo());
            studentWithStatus.setStudentName(student.getStudentName());
            studentWithStatus.setActive(student.isActive());
            studentsWithStatus.add(studentWithStatus);
        }
        return studentsWithStatus;
    }

    public List<StudentModel> getActiveStudents() {
        List<StudentModel> activeStudents = new ArrayList<>();
        for (StudentModel student : studentList) {
            if (student.isActive()) {
                activeStudents.add(student);
            }
        }
        return activeStudents;
    }


}
