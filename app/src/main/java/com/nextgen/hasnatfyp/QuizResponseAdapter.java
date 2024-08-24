package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizResponseAdapter extends RecyclerView.Adapter<QuizResponseAdapter.ViewHolder> {

    private Context context;
    private List<QuizModel> quizQuestions;
    private QuizStudentAttemptedQuestionsModel studentResponses;

    public QuizResponseAdapter(Context context, List<QuizModel> quizQuestions, QuizStudentAttemptedQuestionsModel studentResponses) {
        this.context = context;
        this.quizQuestions = quizQuestions;
        this.studentResponses = studentResponses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_q_answer_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizModel question = quizQuestions.get(position);
        holder.textViewQuestion.setText(question.getQuestionNo() + " " + question.getQuizQuestion());
        holder.textViewOptionA.setText("A) " + question.getOptionA());
        holder.textViewOptionB.setText("B) " + question.getOptionB());
        holder.textViewOptionC.setText("C) " + question.getOptionC());
        holder.textViewOptionD.setText("D) " + question.getOptionD());

        String studentAnswer = studentResponses.getMapQuestionsAttempted().get(question.getQuestionNo());

        // Reset styles and apply correct answer styles
        resetOptionStyles(holder);
        applyCorrectAnswerStyles(holder, question.getCorrectOption());

        if (studentAnswer != null && !studentAnswer.equals("N/A")) {
            holder.textViewSubmittedAnswer.setText("Your Answer: " + studentAnswer);
            holder.textViewResult.setVisibility(View.VISIBLE);
            if (studentAnswer.equals(question.getCorrectOption())) {
                holder.textViewResult.setText("Correct!");
                holder.textViewResult.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else {
                holder.textViewResult.setText("Incorrect!");
                holder.textViewResult.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }
        } else {
            holder.textViewSubmittedAnswer.setTypeface(null, Typeface.BOLD);
            holder.textViewSubmittedAnswer.setText("Not Attempted");
            holder.textViewResult.setVisibility(View.GONE);
        }
    }

    private void resetOptionStyles(ViewHolder holder) {
        holder.textViewOptionA.setTypeface(null, Typeface.NORMAL);
        holder.textViewOptionA.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.textViewOptionB.setTypeface(null, Typeface.NORMAL);
        holder.textViewOptionB.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.textViewOptionC.setTypeface(null, Typeface.NORMAL);
        holder.textViewOptionC.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.textViewOptionD.setTypeface(null, Typeface.NORMAL);
        holder.textViewOptionD.setTextColor(ContextCompat.getColor(context, android.R.color.black));
    }

    private void applyCorrectAnswerStyles(ViewHolder holder, String correctOption) {
        switch (correctOption) {
            case "A":
                holder.textViewOptionA.setTypeface(null, Typeface.BOLD);
                holder.textViewOptionA.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                break;
            case "B":
                holder.textViewOptionB.setTypeface(null, Typeface.BOLD);
                holder.textViewOptionB.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                break;
            case "C":
                holder.textViewOptionC.setTypeface(null, Typeface.BOLD);
                holder.textViewOptionC.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                break;
            case "D":
                holder.textViewOptionD.setTypeface(null, Typeface.BOLD);
                holder.textViewOptionD.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return quizQuestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewQuestion, textViewOptionA, textViewOptionB, textViewOptionC, textViewOptionD, textViewSubmittedAnswer, textViewResult;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textViewQuestion);
            textViewOptionA = itemView.findViewById(R.id.textViewOptionA);
            textViewOptionB = itemView.findViewById(R.id.textViewOptionB);
            textViewOptionC = itemView.findViewById(R.id.textViewOptionC);
            textViewOptionD = itemView.findViewById(R.id.textViewOptionD);
            textViewSubmittedAnswer = itemView.findViewById(R.id.textViewSubmittedAnswer);
            textViewResult = itemView.findViewById(R.id.textViewResult);
        }
    }
}
