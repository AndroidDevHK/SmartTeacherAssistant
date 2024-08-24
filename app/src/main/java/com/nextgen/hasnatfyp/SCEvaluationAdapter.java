package com.nextgen.hasnatfyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SCEvaluationAdapter extends RecyclerView.Adapter<SCEvaluationAdapter.ViewHolder> {

    private Context context;
    private List<SCEvaluationModel> evaluationList;

    public SCEvaluationAdapter(Context context, List<SCEvaluationModel> evaluationList) {
        this.context = context;
        this.evaluationList = evaluationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.scevaluation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SCEvaluationModel evaluation = evaluationList.get(position);
        holder.bind(evaluation);
    }

    @Override
    public int getItemCount() {
        return evaluationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView evaluationNameTextView;
        private TextView totalMarksTextView;
        private TextView obtainedMarksTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            evaluationNameTextView = itemView.findViewById(R.id.evaluationNameTextView);
            totalMarksTextView = itemView.findViewById(R.id.totalMarksTextView);
            obtainedMarksTextView = itemView.findViewById(R.id.obtainedMarksTextView);
        }

        public void bind(SCEvaluationModel evaluation) {
            evaluationNameTextView.setText(evaluation.getEvalName());
            totalMarksTextView.setText("Total Marks: " + evaluation.getTotalMarks());
            obtainedMarksTextView.setText("Obtained Marks: " + evaluation.getObtainedMarks());
        }
    }
}
