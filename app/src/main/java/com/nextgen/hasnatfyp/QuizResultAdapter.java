package com.nextgen.hasnatfyp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class QuizResultAdapter extends RecyclerView.Adapter<QuizResultAdapter.QuizViewHolder> {

    private static final String TAG = "QuizResultAdapter";

    private List<StudentQuizModel> quizList;

    public QuizResultAdapter(List<StudentQuizModel> quizList) {
        this.quizList = quizList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_quiz_item, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        StudentQuizModel quiz = quizList.get(position);
        holder.bind(quiz);
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {

        private TextView quizIdTextView;
        private TextView courseIdTextView;
        private TextView availableWhenTextView;
        private TextView quizDurationTextView;
        private TextView questionWeightageTextView;
        private TextView timeRemainingTextView;
        private TextView timeRemainingLbl;
        private Button solveQuizButton;
        private Handler handler = new Handler(Looper.getMainLooper());
        private Runnable runnable;
        private StudentQuizModel currentQuiz;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews(itemView);
            setupButtonClickListener();
        }

        private void initializeViews(View itemView) {
            quizIdTextView = itemView.findViewById(R.id.quizIdTextView);
            courseIdTextView = itemView.findViewById(R.id.courseNameTextView);
            availableWhenTextView = itemView.findViewById(R.id.availableWhenTextView);
            quizDurationTextView = itemView.findViewById(R.id.quizDurationTextView);
            questionWeightageTextView = itemView.findViewById(R.id.questionWeightageTextView);
            timeRemainingTextView = itemView.findViewById(R.id.timeRemainingTextView);
            timeRemainingLbl = itemView.findViewById(R.id.timeRemainingLbl);
            solveQuizButton = itemView.findViewById(R.id.solveQuizButton);
        }

        private void setupButtonClickListener() {
            solveQuizButton.setOnClickListener(v -> {
                if (currentQuiz != null) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DisplayStudentQuizResultsActivity.class);
                    intent.putExtra("StudentQuizModel", currentQuiz);
                    context.startActivity(intent);
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(StudentQuizModel quiz) {
            currentQuiz = quiz;
            courseIdTextView.setText(quiz.getCourseName());
            availableWhenTextView.setText(DateUtils.formatAvailableWhen(quiz.getAvailableWhen()));
            quizDurationTextView.setText(DateUtils.formatQuizDuration(quiz.getQuizDuration()));
            questionWeightageTextView.setText(quiz.getQuestionWeightage());
            timeRemainingLbl.setText("Quiz Status : ");
            timeRemainingTextView.setText("Closed");
            solveQuizButton.setText("View My Quiz Result");



        }


}}
