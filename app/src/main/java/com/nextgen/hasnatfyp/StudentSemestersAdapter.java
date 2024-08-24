package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class StudentSemestersAdapter extends RecyclerView.Adapter<StudentSemestersAdapter.ViewHolder> {

    private List<StudentSemestersModel> semestersList;
    private Context context;

    public StudentSemestersAdapter(Context context, List<StudentSemestersModel> semestersList) {
        this.context = context;
        this.semestersList = semestersList;
    }
    public void filterList(List<StudentSemestersModel> filteredList) {
        this.semestersList = filteredList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.teacher_semester_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentSemestersModel semester = semestersList.get(position);

        holder.textSemesterName.setText(semester.getSemesterName());
        holder.textNumberOfClasses.setText("My Courses: " + semester.getNumberOfCourses());

        // Set onClickListener for view classes
        holder.imageViewViewClasses.setOnClickListener(view -> {
            // Get the selected semester
            StudentSemestersModel selectedSemester = semestersList.get(holder.getAdapterPosition());

            Intent intent = new Intent(context, DisplayStudentSemesterCourses.class);
            intent.putExtra("classID", selectedSemester.getClassID());
            intent.putExtra("semesterID", selectedSemester.getSemesterID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return semestersList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSemesterName;
        TextView textNumberOfClasses;
        ImageView imageViewViewClasses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSemesterName = itemView.findViewById(R.id.text_semester_name);
            textNumberOfClasses = itemView.findViewById(R.id.text_number_of_classes);
            imageViewViewClasses = itemView.findViewById(R.id.image_view_view_classes);
        }
    }
}

