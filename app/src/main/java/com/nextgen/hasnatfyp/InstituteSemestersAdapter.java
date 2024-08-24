package com.nextgen.hasnatfyp;


import android.annotation.SuppressLint;
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

import Display_Semester_Teacher_All_Courses_Attendance.DisplaySemesterTeachersAttendanceActivity;

public class InstituteSemestersAdapter extends RecyclerView.Adapter<InstituteSemestersAdapter.InstituteSemesterViewHolder> {

    private List<InstituteSemesterModel> semesterList;
    private Context context;

    public InstituteSemestersAdapter(List<InstituteSemesterModel> semesterList, Context context) {
        this.semesterList = semesterList;
        this.context = context;
    }

    @NonNull
    @Override
    public InstituteSemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_semester_item, parent, false);
        return new InstituteSemesterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstituteSemesterViewHolder holder, int position) {
        InstituteSemesterModel semester = semesterList.get(position);
        holder.bind(semester);
    }

    @Override
    public int getItemCount() {
        return semesterList.size();
    }

    public void setSemesterList(List<InstituteSemesterModel> filteredList) {
        semesterList = filteredList;
        notifyDataSetChanged();
    }


    public class InstituteSemesterViewHolder extends RecyclerView.ViewHolder {
        private TextView textSemesterName;
        private LinearLayout imageViewViewClasses;
        private TextView textNumberOfClasses;
        private TextView ViewMyClasses;

        @SuppressLint("SetTextI18n")
        public InstituteSemesterViewHolder(@NonNull View itemView) {
            super(itemView);
            textSemesterName = itemView.findViewById(R.id.text_semester_name);
            imageViewViewClasses = itemView.findViewById(R.id.image_view_view_classes_layout);
            textNumberOfClasses = itemView.findViewById(R.id.text_number_of_classes);
            ViewMyClasses = itemView.findViewById(R.id.ViewMyClasses);
            textNumberOfClasses.setVisibility(View.GONE);
            ViewMyClasses.setText("View All Teachers Attendance");
            imageViewViewClasses.setOnClickListener(v -> {
                InstituteSemesterModel semester = semesterList.get(getAdapterPosition());
                String semesterID = semester.getSemesterID();
                String semesterName = semester.getSemesterName();
                UserInstituteModel.getInstance(context).setSemesterName(semesterName);
                Intent intent = new Intent(context, DisplaySemesterTeachersAttendanceActivity.class);
                intent.putExtra("semesterID", semesterID);
                context.startActivity(intent);
            });
        }

        public void bind(InstituteSemesterModel semester) {
            textSemesterName.setText(semester.getSemesterName());
        }
    }
}
