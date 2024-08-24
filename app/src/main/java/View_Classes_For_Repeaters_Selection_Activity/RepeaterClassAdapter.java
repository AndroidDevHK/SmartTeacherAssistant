package View_Classes_For_Repeaters_Selection_Activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.R;
import View_Students_List_To_Select_Repeaters_Activity.ViewRepeatersForSelectionActivity;

import java.util.ArrayList;
import java.util.List;

public class RepeaterClassAdapter extends RecyclerView.Adapter<RepeaterClassAdapter.ViewHolder> {

    private List<RepeaterClassModel> repeaterClassList; // Store class list

    public RepeaterClassAdapter() {
        this.repeaterClassList = new ArrayList<>();; // Retrieve class list from UserInstituteModel
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item_for_repeaters, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepeaterClassModel repeaterClass = repeaterClassList.get(position);
        holder.bind(repeaterClass);
    }

    @Override
    public int getItemCount() {
        return repeaterClassList.size();
    }

    public void setRepeaterClassList(List<RepeaterClassModel> repeaterClassList) {
        this.repeaterClassList = repeaterClassList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView classNameTextView;
        private TextView studentCountTextView;
        private LinearLayout layoutViewStudents;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classNameTextView = itemView.findViewById(R.id.text_Class_name);
            studentCountTextView = itemView.findViewById(R.id.text_number_of_classes);
            layoutViewStudents = itemView.findViewById(R.id.LayoutViewStudents);
        }

        public void bind(RepeaterClassModel repeaterClass) {
            classNameTextView.setText(repeaterClass.getClassName());
            String studentCountText = "Student: " + repeaterClass.getStudentCount();
            studentCountTextView.setText(studentCountText);

            layoutViewStudents.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ViewRepeatersForSelectionActivity.class);
                intent.putExtra("classId", repeaterClass.getRepeaterClassID());
                intent.putExtra("repeaterClass", repeaterClass);

                itemView.getContext().startActivity(intent);
            });
        }
    }
}
