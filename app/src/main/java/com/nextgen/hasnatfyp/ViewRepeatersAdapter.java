package com.nextgen.hasnatfyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewRepeatersAdapter extends RecyclerView.Adapter<ViewRepeatersAdapter.ViewHolder> {

    private List<RepeaterIndicator> repeaterIndicators;
    private Context context;

    public ViewRepeatersAdapter(Context context, List<RepeaterIndicator> repeaterIndicators) {
        this.context = context;
        this.repeaterIndicators = repeaterIndicators;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeater_student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepeaterIndicator repeater = repeaterIndicators.get(position);
        holder.textStudentName.setText(repeater.getRepeaterName());
        holder.textRollNo.setText(repeater.getRollNo());
    }

    @Override
    public int getItemCount() {
        return repeaterIndicators.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textStudentName;
        TextView textRollNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textStudentName = itemView.findViewById(R.id.text_student_name);
            textRollNo = itemView.findViewById(R.id.text_roll_no);
        }
    }
}
