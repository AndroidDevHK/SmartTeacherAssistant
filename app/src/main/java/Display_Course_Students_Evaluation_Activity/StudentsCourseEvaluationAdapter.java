package Display_Course_Students_Evaluation_Activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;

import java.util.List;

public class StudentsCourseEvaluationAdapter extends RecyclerView.Adapter<StudentsCourseEvaluationAdapter.ViewHolder> {

    private List<CourseStudentEvaluationListModel> studentEvalList;
    private Context context;

    public StudentsCourseEvaluationAdapter(List<CourseStudentEvaluationListModel> studentEvalList, Context context) {
        this.studentEvalList = studentEvalList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_course_evaluation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseStudentEvaluationListModel studentEval = studentEvalList.get(position);

        holder.txtStudentName.setText("Name: " + studentEval.getStudentName());
        holder.txtStudentRollNo.setText("Roll No: " + studentEval.getStudentRollNo());

        holder.layoutEvaluationList.removeAllViews();

        if (studentEval.getStudentEvalList().isEmpty()) {
            holder.EvalDetailsTxtView.setText("No Evaluation Available.");
        }
        else {
            addTableRow(holder.layoutEvaluationList, "Evaluation", "Obtained Marks", "Total Marks", Color.BLACK, true);

            for (StudentEvaluationDetailsModel eval : studentEval.getStudentEvalList()) {
                addTableRow(holder.layoutEvaluationList, eval.getEvaluationName(), eval.getObtainedMarks(), eval.getTotalMarks(), Color.BLACK, false);
            }

            addTableRow(holder.layoutEvaluationList, "Total", studentEval.getAllEvaluationObtainedMarks(), studentEval.getAllEvaluationTotal(), Color.BLACK, false);

            addTableRow(holder.layoutEvaluationList, "Percentage", studentEval.getPercentage(), "", Color.BLACK, true);
        }
    }

    private void addTableRow(LinearLayout layout, String col1Text, String col2Text, String col3Text, int textColor, boolean isHeader) {
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView col1 = createTextView(col1Text, false, textColor, isHeader);
        TextView col2 = createTextView(col2Text, true, textColor, isHeader);
        TextView col3 = createTextView(col3Text, true, textColor, isHeader);

        col1.setBackgroundResource(R.drawable.bordered_background); // Set background drawable for cell
        col2.setBackgroundResource(R.drawable.bordered_background);
        col3.setBackgroundResource(R.drawable.bordered_background);

        row.addView(col1);
        row.addView(col2);
        row.addView(col3);

        layout.addView(row);
    }

    private TextView createTextView(String text, boolean centerAligned, int textColor, boolean isHeader) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextSize(12); // Set text size to 12
        textView.setTextColor(textColor); // Set text color
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(centerAligned ? Gravity.CENTER : Gravity.START); // Align text to the center or start
        if (isHeader) {
            textView.setTypeface(null, Typeface.BOLD); // Make text bold for header
            textView.setBackgroundColor(Color.LTGRAY); // Set background color for header
        } else if (text.equals("Total") || text.equals("Percentage")) {
            textView.setTypeface(null, Typeface.BOLD); // Make text bold for total row and percentage
        }
        return textView;
    }

    @Override
    public int getItemCount() {
        return studentEvalList.size();
    }

    public void updateList(List<CourseStudentEvaluationListModel> filteredList) {
        this.studentEvalList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStudentName;
        TextView txtStudentRollNo;
        LinearLayout layoutEvaluationList;

        TextView EvalDetailsTxtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStudentName = itemView.findViewById(R.id.studentNameTextView);
            txtStudentRollNo = itemView.findViewById(R.id.studentRollNoTextView);
            EvalDetailsTxtView = itemView.findViewById(R.id.EvalDetailsTxtView);
            layoutEvaluationList = itemView.findViewById(R.id.EvaluationListLayout);
        }
    }
}
