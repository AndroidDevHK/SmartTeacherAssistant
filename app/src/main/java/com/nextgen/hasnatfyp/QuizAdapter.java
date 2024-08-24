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

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private static final String TAG = "QuizAdapter";

    private List<StudentQuizModel> quizList;

    public QuizAdapter(List<StudentQuizModel> quizList) {
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
            quizIdTextView = itemView.findViewById(R.id.quizIdTextView);
            courseIdTextView = itemView.findViewById(R.id.courseNameTextView);
            availableWhenTextView = itemView.findViewById(R.id.availableWhenTextView);
            quizDurationTextView = itemView.findViewById(R.id.quizDurationTextView);
            questionWeightageTextView = itemView.findViewById(R.id.questionWeightageTextView);
            timeRemainingTextView = itemView.findViewById(R.id.timeRemainingTextView);
            timeRemainingLbl = itemView.findViewById(R.id.timeRemainingLbl);
            solveQuizButton = itemView.findViewById(R.id.solveQuizButton);

            // Handle click on solveQuizButton
            solveQuizButton.setOnClickListener(v -> {
                // Check if currentQuiz is not null
                if (currentQuiz != null) {
                    // Start SolveQuizActivity and pass currentQuiz object
                    Context context = v.getContext();
                    StudentSessionInfo.getInstance(context).setCourseName(currentQuiz.getCourseName());
                    Intent intent = new Intent(context, SolveQuizActivity.class);
                    intent.putExtra("studentQuizModel", currentQuiz);
                    context.startActivity(intent);
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(StudentQuizModel quiz) {
            currentQuiz = quiz; // Store the current quiz model
            courseIdTextView.setText(quiz.getCourseName());
            availableWhenTextView.setText(DateUtils.formatAvailableWhen(quiz.getAvailableWhen()));
            quizDurationTextView.setText(DateUtils.formatQuizDuration(quiz.getQuizDuration()));
            questionWeightageTextView.setText(quiz.getQuestionWeightage());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi")); // Set to Pakistan Standard Time (PKT)

            try {
                Date availableDate = sdf.parse(quiz.getAvailableWhen());
                long durationMillis = parseDuration(quiz.getQuizDuration());
                Date endDate = new Date(availableDate.getTime() + durationMillis);

                Log.d(TAG, "Quiz: " + quiz.getQuizId() + ", Available Date: " + availableDate + ", End Date: " + endDate);

                updateRemainingTime(availableDate, endDate, durationMillis);
                updateButtonStatus(quiz.getAttemptedQuestions());
            } catch (ParseException e) {
                e.printStackTrace();
                timeRemainingTextView.setText("Error calculating time");
                solveQuizButton.setVisibility(View.GONE);
            }
        }

        private void updateButtonStatus(List<StudentAttemptedQuestionModel> attemptedQuestions) {
            if (attemptedQuestions == null || attemptedQuestions.isEmpty()) {
                solveQuizButton.setText("Solve Quiz");
                solveQuizButton.setVisibility(View.VISIBLE);
            } else {
                int countNotAttempted = 0;
                for (StudentAttemptedQuestionModel attempt : attemptedQuestions) {
                    if ("N/A".equals(attempt.getSelectedOption())) {
                        countNotAttempted++;
                    }
                }
                if (countNotAttempted == attemptedQuestions.size()) {
                    solveQuizButton.setText("Solve Quiz");
                } else if (countNotAttempted > 0) {
                    solveQuizButton.setText("Resume Quiz");
                } else {
                    solveQuizButton.setText("Quiz Completed");
                    solveQuizButton.setEnabled(false);
                }
                solveQuizButton.setVisibility(View.VISIBLE);
            }
        }

        private long parseDuration(String duration) {
            long durationMillis = 0;

            String[] parts = duration.split(",");
            for (String part : parts) {
                part = part.trim(); // Remove leading/trailing spaces
                if (part.contains("hours")) {
                    int hours = Integer.parseInt(part.split(" ")[0].trim());
                    durationMillis += hours * 60 * 60 * 1000; // Convert hours to milliseconds
                } else if (part.contains("minutes")) {
                    int minutes = Integer.parseInt(part.split(" ")[0].trim());
                    durationMillis += minutes * 60 * 1000; // Convert minutes to milliseconds
                }
            }

            return durationMillis;
        }

        private void updateRemainingTime(Date availableDate, Date endDate, long durationMillis) {
            runnable = new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    Date currentDate = new Date();
                    long timeRemainingMillis = availableDate.getTime() - currentDate.getTime();

                    if (timeRemainingMillis > 0) {
                        long hours = timeRemainingMillis / (1000 * 60 * 60);
                        long minutes = (timeRemainingMillis / (1000 * 60)) % 60;
                        long seconds = (timeRemainingMillis / 1000) % 60;
                        String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                        timeRemainingLbl.setText("Open After : " );
                        timeRemainingTextView.setText(timeRemaining);
                        solveQuizButton.setVisibility(View.GONE); // Hide solve button until quiz is available
                        handler.postDelayed(this, 1000); // Update every second
                    } else {
                        long quizTimeRemainingMillis = endDate.getTime() - currentDate.getTime();
                        if (quizTimeRemainingMillis > 0) {
                            long hours = quizTimeRemainingMillis / (1000 * 60 * 60);
                            long minutes = (quizTimeRemainingMillis / (1000 * 60)) % 60;
                            long seconds = (quizTimeRemainingMillis / 1000) % 60;
                            String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                            timeRemainingLbl.setText("Time Remaining: " );
                            timeRemainingTextView.setText(timeRemaining);
                            solveQuizButton.setVisibility(View.VISIBLE); // Show solve button once quiz is available
                            handler.postDelayed(this, 1000); // Update every second
                        } else {
                            timeRemainingTextView.setText("Quiz Closed");
                            solveQuizButton.setVisibility(View.GONE); // Hide solve button once quiz is closed
                            handler.removeCallbacks(runnable); // Stop updating
                        }
                    }
                }
            };
            handler.post(runnable);
        }
    }
}
