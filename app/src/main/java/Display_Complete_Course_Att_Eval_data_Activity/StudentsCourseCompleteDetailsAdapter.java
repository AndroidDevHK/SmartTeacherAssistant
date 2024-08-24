package Display_Complete_Course_Att_Eval_data_Activity;

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
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;
import java.util.ArrayList;
import java.util.List;

public class StudentsCourseCompleteDetailsAdapter extends RecyclerView.Adapter<StudentsCourseCompleteDetailsAdapter.ViewHolder> {

    private List<CourseStudentDetailsModel> studentEvalList;
    private Context context;

    public StudentsCourseCompleteDetailsAdapter(List<CourseStudentDetailsModel> studentEvalList, Context context) {
        this.studentEvalList = studentEvalList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_course_completeinfo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseStudentDetailsModel studentEval = studentEvalList.get(position);

        holder.txtStudentName.setText("Name: " + studentEval.getStudentName());
        holder.txtStudentRollNo.setText("Roll No: " + studentEval.getStudentRollNo());

        holder.layoutEvaluationList.removeAllViews();

        addTableRow(holder.layoutEvaluationList, "Evaluation", "Obtained Marks", "Total Marks", Color.BLACK, true);

        if (studentEval.getEvaluationDetailsList().size() == 0 || studentEval.getEvaluationDetailsList() == null) {
            holder.layoutEvaluationList.setVisibility(View.GONE);
            holder.txtNoEvaluation.setVisibility(View.VISIBLE);
            initPieChart(holder.pieChart, studentEval.getPresents(),  studentEval.getAbsents(), studentEval.getLeaves(), studentEval.getPresentPercentage(), studentEval.getTotalCount());

        } else {
            holder.layoutEvaluationList.setVisibility(View.VISIBLE);
            holder.txtNoEvaluation.setVisibility(View.GONE);

            for (StudentEvaluationDetailsModel eval : studentEval.getEvaluationDetailsList()) {
                addTableRow(holder.layoutEvaluationList, eval.getEvaluationName(), eval.getObtainedMarks(), eval.getTotalMarks(), Color.BLACK, false);
            }


            addTableRow(holder.layoutEvaluationList, "Total", String.valueOf(studentEval.getObtainedMarks()), String.valueOf(studentEval.getTotalMarks()), Color.BLACK, false);
            addTableRow(holder.layoutEvaluationList, "Percentage", studentEval.getPercentage(), "", Color.BLACK, true);

            initPieChart(holder.pieChart, studentEval.getPresents(),  studentEval.getAbsents(), studentEval.getLeaves(), studentEval.getPresentPercentage(), studentEval.getTotalCount());
        }
    }

    private void initPieChart(PieChart pieChart, int presentCount, int absentCount, int leaveCount, double attendancePercentage, int totalDays) {
        List<PieEntry> entries = new ArrayList<>();
        if (presentCount > 0) {
            entries.add(new PieEntry(presentCount, "Present"));
        }
        if (absentCount > 0) {
            entries.add(new PieEntry(absentCount, "Absent"));
        }
        if (leaveCount > 0) {
            entries.add(new PieEntry(leaveCount, "Leave"));
        }

        List<Integer> customColors = new ArrayList<>();
        customColors.add(Color.rgb(255, 180, 0));  // Light Orange for "Not Specified"
        customColors.add(Color.rgb(255, 69, 0));  // Red for "Absent"
        customColors.add(Color.rgb(0, 0, 255));  // Blue for "Leave"

        PieDataSet dataSet = new PieDataSet(entries, "Percentage : " +  String.format("%.0f%%", attendancePercentage));
        dataSet.setColors(customColors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueLineColor(Color.BLACK);
        dataSet.setValueLineWidth(1f);
        dataSet.setValueLinePart1OffsetPercentage(80.0f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.5f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);
        ValueFormatter formatter = new IntValueFormatter();
        data.setValueFormatter(formatter);

        pieChart.setData(data);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);

        Legend legend = pieChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(Color.BLACK);

        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        pieChart.animateY(2000, Easing.EaseInOutCubic);

        pieChart.setCenterText("Classes : " + totalDays);
        pieChart.setCenterTextSize(10f);
        pieChart.setCenterTextColor(Color.BLACK);

        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setDrawSliceText(false);  // Hide text inside slices

        pieChart.invalidate();
    }

    public void updateList(List<CourseStudentDetailsModel> filteredList) {
        this.studentEvalList = filteredList;
        notifyDataSetChanged();
    }

    public static class IntValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }

    @Override
    public int getItemCount() {
        return studentEvalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoEvaluation;
        TextView txtStudentName;
        TextView txtStudentRollNo;
        LinearLayout layoutEvaluationList;
        PieChart pieChart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStudentName = itemView.findViewById(R.id.studentNameTextView);
            txtStudentRollNo = itemView.findViewById(R.id.studentRollNoTextView);
            layoutEvaluationList = itemView.findViewById(R.id.EvaluationListLayout);
            pieChart = itemView.findViewById(R.id.barChart);
            txtNoEvaluation = itemView.findViewById(R.id.noEvaluationAvailableTextView);
        }
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
            textView.setTypeface(null, Typeface.BOLD); // Make text bold for Total and Percentage rows
        }
        return textView;
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
}
