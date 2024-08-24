package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentQuizResultAdapter extends RecyclerView.Adapter<StudentQuizResultAdapter.QuizResultViewHolder> {

    private Context context;
    private List<QuizModel> quizModels;
    private List<StudentAttemptedQuestionModel> attemptedQuestions;

    public StudentQuizResultAdapter(Context context, List<QuizModel> quizModels, List<StudentAttemptedQuestionModel> attemptedQuestions) {
        this.context = context;
        this.quizModels = quizModels;
        this.attemptedQuestions = attemptedQuestions;
    }

    @NonNull
    @Override
    public QuizResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_q_answer_item, parent, false);
        return new QuizResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizResultViewHolder holder, int position) {
        QuizModel quiz = quizModels.get(position);
        String userAnswer = attemptedQuestions.get(position).getSelectedOption();
        String correctAnswer = quiz.getCorrectOption();
        holder.bind(quiz, userAnswer, correctAnswer, context);
    }

    @Override
    public int getItemCount() {
        return quizModels.size();
    }

    static class QuizResultViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewQuestion, textViewOptionA, textViewOptionB, textViewOptionC, textViewOptionD, textViewSubmittedAnswer, textViewResult;

        public QuizResultViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textViewQuestion);
            textViewOptionA = itemView.findViewById(R.id.textViewOptionA);
            textViewOptionB = itemView.findViewById(R.id.textViewOptionB);
            textViewOptionC = itemView.findViewById(R.id.textViewOptionC);
            textViewOptionD = itemView.findViewById(R.id.textViewOptionD);
            textViewSubmittedAnswer = itemView.findViewById(R.id.textViewSubmittedAnswer);
            textViewResult = itemView.findViewById(R.id.textViewResult);
        }

        @SuppressLint("SetTextI18n")
        void bind(QuizModel quiz, String userAnswer, String correctAnswer, Context context) {
            textViewQuestion.setText(quiz.getQuestionNo() +" " +quiz.getQuizQuestion());
            textViewOptionA.setText("A) " + quiz.getOptionA());
            textViewOptionB.setText("B) " + quiz.getOptionB());
            textViewOptionC.setText("C) " + quiz.getOptionC());
            textViewOptionD.setText("D) " + quiz.getOptionD());

            textViewOptionA.setTextColor(context.getResources().getColor(android.R.color.black));
            textViewOptionB.setTextColor(context.getResources().getColor(android.R.color.black));
            textViewOptionC.setTextColor(context.getResources().getColor(android.R.color.black));
            textViewOptionD.setTextColor(context.getResources().getColor(android.R.color.black));

            switch (correctAnswer) {
                case "A":
                    textViewOptionA.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "B":
                    textViewOptionB.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "C":
                    textViewOptionC.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "D":
                    textViewOptionD.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
            }

            if ("N/A".equals(userAnswer)) {
                textViewSubmittedAnswer.setText("Your Answer: Not Attempted");
                textViewSubmittedAnswer.setTypeface(null, Typeface.BOLD);
                textViewResult.setVisibility(View.GONE);
            } else {
                textViewSubmittedAnswer.setText("Your Answer: " + userAnswer);
                textViewResult.setVisibility(View.VISIBLE);
                textViewResult.setText(userAnswer.equals(correctAnswer) ? "Correct!" : "Incorrect");
                textViewResult.setTextColor(context.getResources().getColor(userAnswer.equals(correctAnswer) ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
            }
        }
    }
}
