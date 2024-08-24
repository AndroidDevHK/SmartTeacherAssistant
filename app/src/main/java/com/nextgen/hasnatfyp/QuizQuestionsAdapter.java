package com.nextgen.hasnatfyp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class QuizQuestionsAdapter extends RecyclerView.Adapter<QuizQuestionsAdapter.QuizViewHolder> {

    private List<QuizModel> questionList;

    public QuizQuestionsAdapter(List<QuizModel> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item, parent, false);
        return new QuizViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        // Bind data to each item in the RecyclerView
        QuizModel quizModel = questionList.get(position);
        holder.bind(quizModel);
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the RecyclerView
        return questionList.size();
    }
    public List<QuizModel> getQuestionList() {
        return questionList;
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder {

        // Views within the RecyclerView item
        private EditText textViewQuestion;
        private EditText editTextOptionA;
        private EditText editTextOptionB;
        private EditText editTextOptionC;
        private EditText editTextOptionD;
        private RadioGroup radioGroupOptions;
        private MaterialButton btnAddQuestion;

        // Store the current QuizModel
        private QuizModel currentQuizModel;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            textViewQuestion = itemView.findViewById(R.id.editTextQuestion);
            editTextOptionA = itemView.findViewById(R.id.editTextOption1);
            editTextOptionB = itemView.findViewById(R.id.editTextOption2);
            editTextOptionC = itemView.findViewById(R.id.editTextOption3);
            editTextOptionD = itemView.findViewById(R.id.editTextOption4);
            radioGroupOptions = itemView.findViewById(R.id.radioGroupOptions);
            btnAddQuestion = itemView.findViewById(R.id.btnAddQuestion);
        }

        public void bind(QuizModel quizModel) {
            currentQuizModel = quizModel; // Store the current QuizModel

            // Set initial values from QuizModel to EditText fields and RadioGroup
            textViewQuestion.setText(quizModel.getQuizQuestion());
            editTextOptionA.setText(quizModel.getOptionA());
            editTextOptionB.setText(quizModel.getOptionB());
            editTextOptionC.setText(quizModel.getOptionC());
            editTextOptionD.setText(quizModel.getOptionD());

            // Clear the checked state of RadioGroup first
            radioGroupOptions.clearCheck();

            // Determine which RadioButton to check based on the correct option
            switch (quizModel.getCorrectOption()) {
                case "A":
                    radioGroupOptions.check(R.id.radioButtonOption1);
                    break;
                case "B":
                    radioGroupOptions.check(R.id.radioButtonOption2);
                    break;
                case "C":
                    radioGroupOptions.check(R.id.radioButtonOption3);
                    break;
                case "D":
                    radioGroupOptions.check(R.id.radioButtonOption4);
                    break;
                default:
                    break;
            }

            // Set listener for "Add Question" button
            btnAddQuestion.setOnClickListener(v -> {
                boolean changesMade = false;

                // Update QuizModel with the latest input values
                String newQuestion = textViewQuestion.getText().toString().trim();
                String newOptionA = editTextOptionA.getText().toString().trim();
                String newOptionB = editTextOptionB.getText().toString().trim();
                String newOptionC = editTextOptionC.getText().toString().trim();
                String newOptionD = editTextOptionD.getText().toString().trim();

                // Check if question or options have changed
                if (!currentQuizModel.getQuizQuestion().equals(newQuestion)) {
                    currentQuizModel.setQuizQuestion(newQuestion);
                    changesMade = true;
                }
                if (!currentQuizModel.getOptionA().equals(newOptionA)) {
                    currentQuizModel.setOptionA(newOptionA);
                    changesMade = true;
                }
                if (!currentQuizModel.getOptionB().equals(newOptionB)) {
                    currentQuizModel.setOptionB(newOptionB);
                    changesMade = true;
                }
                if (!currentQuizModel.getOptionC().equals(newOptionC)) {
                    currentQuizModel.setOptionC(newOptionC);
                    changesMade = true;
                }
                if (!currentQuizModel.getOptionD().equals(newOptionD)) {
                    currentQuizModel.setOptionD(newOptionD);
                    changesMade = true;
                }

                // Check if correct option has changed
                String oldCorrectOption = currentQuizModel.getCorrectOption();
                String newCorrectOption = getSelectedOption();
                if (!oldCorrectOption.equals(newCorrectOption)) {
                    currentQuizModel.setCorrectOption(newCorrectOption);
                    changesMade = true;
                    String message = "Correct option changed: Old: " + oldCorrectOption + " -> New: " + newCorrectOption;
                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                }

                // Notify the user about the changes made
                if (changesMade) {
                    Toast.makeText(itemView.getContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged(); // Or use notifyItemChanged(getAdapterPosition()) for efficiency
                } else {
                    Toast.makeText(itemView.getContext(), "No changes to save", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private String getSelectedOption() {
            // Determine the correct option based on the selected RadioButton
            int checkedId = radioGroupOptions.getCheckedRadioButtonId();
            if (checkedId == R.id.radioButtonOption1) {
                return "A";
            } else if (checkedId == R.id.radioButtonOption2) {
                return "B";
            } else if (checkedId == R.id.radioButtonOption3) {
                return "C";
            } else if (checkedId == R.id.radioButtonOption4) {
                return "D";
            } else {
                return ""; // This case should ideally not happen if RadioGroup is properly managed
            }
        }

    }
}
