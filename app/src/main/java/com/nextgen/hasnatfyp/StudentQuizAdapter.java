package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentQuizAdapter extends RecyclerView.Adapter<StudentQuizAdapter.ViewHolder> {

    private Context context;
    private List<QuizStudentAttemptedQuestionsModel> studentResponses;
    private Map<String, String> correctAnswers;
    private float questionWeightage;
    private List<QuizModel> quizQuestions;
    private List<StudentQuizResults> studentQuizResults;

    public StudentQuizAdapter(Context context, List<QuizStudentAttemptedQuestionsModel> studentResponses, Map<String, String> correctAnswers, float questionWeightage, List<QuizModel> quizQuestions) {
        this.context = context;
        this.studentResponses = studentResponses;
        this.correctAnswers = correctAnswers;
        this.questionWeightage = questionWeightage;
        this.quizQuestions = quizQuestions;
        this.studentQuizResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_quiz, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizStudentAttemptedQuestionsModel response = studentResponses.get(position);
        holder.textViewRollNo.setText("Roll No: " + response.getStudentRollNo());
        holder.textViewName.setText("Name: " + response.getStudentName());

        // Calculate the number of questions attempted and the correct ones
        int attemptedQuestions = 0;
        int correctQuestions = 0;
        for (Map.Entry<String, String> entry : response.getMapQuestionsAttempted().entrySet()) {
            if (!entry.getValue().equals("N/A")) {
                attemptedQuestions++;
                if (entry.getValue().equals(correctAnswers.get(entry.getKey()))) {
                    correctQuestions++;
                }
            }
        }

        // Bind data to views
        float totalQuestionsFloat = quizQuestions.size();
        int totalQuestionsInt = (int) totalQuestionsFloat;
        holder.textViewAttemptedQuestions.setText("Attempted Questions: " + attemptedQuestions + "/" + totalQuestionsInt);
        float totalMarks = totalQuestionsFloat * questionWeightage;
        float obtainedMarks = correctQuestions * questionWeightage;

        // Format totalMarks and obtainedMarks to remove decimals if they are whole numbers
        String formattedTotalMarks = (totalMarks == Math.round(totalMarks)) ? String.valueOf((int) totalMarks) : String.valueOf(totalMarks);
        String formattedObtainedMarks = (obtainedMarks == Math.round(obtainedMarks)) ? String.valueOf((int) obtainedMarks) : String.valueOf(obtainedMarks);

        holder.textViewMarks.setText("Marks: " + formattedObtainedMarks + "/" + formattedTotalMarks);

        // Configure onClickListener for the "View Response" button
        int finalAttemptedQuestions1 = attemptedQuestions;
        holder.buttonViewResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayStudentsQuizQuestionsResponsesActivity.class);
                intent.putExtra("quizQuestions", (ArrayList<QuizModel>) quizQuestions);
                intent.putExtra("studentResponses", response);
                intent.putExtra("attemptedQuestions", finalAttemptedQuestions1);
                intent.putExtra("totalQuestions", totalQuestionsInt);
                intent.putExtra("formattedTotalMarks", formattedTotalMarks);
                intent.putExtra("formattedObtainedMarks", formattedObtainedMarks);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return studentResponses.size();
    }

    public List<StudentQuizResults> getStudentQuizResults() {
        return studentQuizResults;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRollNo, textViewAttemptedQuestions, textViewMarks,textViewName;
        com.google.android.material.button.MaterialButton buttonViewResponse;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewRollNo = itemView.findViewById(R.id.textViewRollNo);
            textViewAttemptedQuestions = itemView.findViewById(R.id.textViewAttemptedQuestions);
            textViewMarks = itemView.findViewById(R.id.textViewMarks);
            textViewName = itemView.findViewById(R.id.textViewName);
            buttonViewResponse = itemView.findViewById(R.id.buttonViewResponse);
        }
    }
}
