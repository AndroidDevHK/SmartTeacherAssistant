package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TeacherQuizAdapter extends RecyclerView.Adapter<TeacherQuizAdapter.QuizViewHolder> {

    private List<TeacherCourseQuizModel> quizList;
    private Context context;

    public TeacherQuizAdapter(Context context, List<TeacherCourseQuizModel> quizList) {
        this.context = context;
        this.quizList = quizList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quiz_item_layout, parent, false);
        return new QuizViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        TeacherCourseQuizModel quizModel = quizList.get(position);
        holder.bind(quizModel);

        // Set click listener for the view responses button
        holder.viewResponsesButton.setOnClickListener(v -> {
            // Handle logic to pass quiz responses to the next activity
            passQuizResponsesToNextActivity(quizModel);
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder {

        private TextView courseIdTextView;
        private TextView quizIdTextView;
        private TextView availableWhenTextView;
        private TextView quizDurationTextView;
        private TextView questionWeightageTextView;
        private Button viewResponsesButton; // Button reference

        public QuizViewHolder(View itemView) {
            super(itemView);
            courseIdTextView = itemView.findViewById(R.id.courseIdTextView);
            quizIdTextView = itemView.findViewById(R.id.quizIdTextView);
            availableWhenTextView = itemView.findViewById(R.id.availableWhenTextView);
            quizDurationTextView = itemView.findViewById(R.id.quizDurationTextView);
            questionWeightageTextView = itemView.findViewById(R.id.questionWeightageTextView);
            viewResponsesButton = itemView.findViewById(R.id.viewResponsesButton); // Initialize the button
        }

        @SuppressLint("SetTextI18n")
        public void bind(TeacherCourseQuizModel quizModel) {
            courseIdTextView.setText( quizModel.getCourseName());
            quizIdTextView.setText(quizModel.getQuizId());
            quizIdTextView.setVisibility(View.GONE);
            availableWhenTextView.setText(DateUtils.formatAvailableWhen(quizModel.getAvailableWhen()));
            quizDurationTextView.setText(DateUtils.formatQuizDuration(quizModel.getQuizDuration()));
            try {
                int weightage = Integer.parseInt(quizModel.getQuestionWeightage());
                String weightageText = "Q's Weightage: " + weightage + (weightage > 1 ? " Marks each" : " Mark each");
                questionWeightageTextView.setText(weightageText);
            } catch (NumberFormatException e) {
                Log.e("Adapter", "Invalid QuestionWeightage: " + quizModel.getQuestionWeightage(), e);
                questionWeightageTextView.setText("Q's Weightage: Invalid value");
            }
        }
    }

    private void passQuizResponsesToNextActivity(TeacherCourseQuizModel quizModel) {
        Intent intent = new Intent(context, DisplayQuizStudentsActivity.class);
        intent.putExtra("quizModel", quizModel); // Passing the quiz model
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line
        context.startActivity(intent);
    }

}
