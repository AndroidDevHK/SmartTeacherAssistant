package Display_Teacher_Semesters_Activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import Display_Teacher_Semester_Classes_Acitivity.DisplayTeacherSemesterClassesActivity;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.util.List;

public class TeacherSemesterAdapter extends RecyclerView.Adapter<TeacherSemesterAdapter.TeacherSemesterViewHolder> {

    private List<TeacherSemestersModel> teacherSemestersList;
    private Context context;

    public TeacherSemesterAdapter(List<TeacherSemestersModel> teacherSemestersList, Context context) {
        this.teacherSemestersList = teacherSemestersList;
        this.context = context;
    }

    @NonNull
    @Override
    public TeacherSemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_semester_item, parent, false);
        return new TeacherSemesterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherSemesterViewHolder holder, int position) {
        TeacherSemestersModel semester = teacherSemestersList.get(position);
        holder.bind(semester);
    }

    @Override
    public int getItemCount() {
        return teacherSemestersList.size();
    }

    public void setClassList(List<TeacherSemestersModel> filteredList) {
        teacherSemestersList = filteredList;
        notifyDataSetChanged();
    }


    public class TeacherSemesterViewHolder extends RecyclerView.ViewHolder {
        private TextView textSemesterName;
        private TextView textNumberOfClasses;
        private LinearLayout imageViewViewClasses;

        public TeacherSemesterViewHolder(@NonNull View itemView) {
            super(itemView);
            textSemesterName = itemView.findViewById(R.id.text_semester_name);
            textNumberOfClasses = itemView.findViewById(R.id.text_number_of_classes);
            imageViewViewClasses = itemView.findViewById(R.id.image_view_view_classes_layout);

            imageViewViewClasses.setOnClickListener(v -> {
                TeacherSemestersModel semester = teacherSemestersList.get(getAdapterPosition());
                String semesterID = semester.getSemesterID();
                String semesterName = semester.getSemesterName();
                String semesterStartDate = semester.getStartDate();
                String semesterEndDate = semester.getEndDate();
                TeacherInstanceModel.getInstance(itemView.getContext()).setSemesterName(semesterName);
                TeacherInstanceModel.getInstance(itemView.getContext()).setSemesterStartDate(semesterStartDate);
                TeacherInstanceModel.getInstance(itemView.getContext()).setSemesterEndDate(semesterEndDate);
                Intent intent = new Intent(context, DisplayTeacherSemesterClassesActivity.class);
                intent.putExtra("semesterID", semesterID);
                context.startActivity(intent);
            });
        }

        public void bind(TeacherSemestersModel semester) {
            textSemesterName.setText(semester.getSemesterName());
            textNumberOfClasses.setText("My Classes: " + semester.getNumberOfClasses());
        }
    }
}
