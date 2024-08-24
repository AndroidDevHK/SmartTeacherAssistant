package com.nextgen.hasnatfyp;


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

import java.util.ArrayList;
import java.util.List;

import DisplayStudentCompleteAttendanceEvaluation_Activity.StudentCourseAttendanceEvaluationModel;

public class StudentCourseDetailsAdapter extends RecyclerView.Adapter<StudentCourseDetailsAdapter.ViewHolder> {

    List<StudentCourseAttendanceEvaluationModel> studentCourseDetailsList;
    private Context context;

    public StudentCourseDetailsAdapter(List<StudentCourseAttendanceEvaluationModel> studentCourseDetailsList, Context context) {
        this.studentCourseDetailsList = studentCourseDetailsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_completeinfo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentCourseAttendanceEvaluationModel courseDetails = studentCourseDetailsList.get(position);

        holder.txtCourseName.setText("Course: " + courseDetails.getCourseName() + (courseDetails.isRepeater() ? "(Repeating)" : ("")));
        holder.txtCourseType.setVisibility(View.GONE);

        holder.layoutEvaluationList.removeAllViews();

        addTableRow(holder.layoutEvaluationList, "Evaluation", "Obtained Marks", "Total Marks", Color.BLACK, true);

        if (courseDetails.getStudentEvalList().size() == 0) {
            holder.layoutEvaluationList.setVisibility(View.GONE);
            holder.txtNoEvaluation.setVisibility(View.VISIBLE);

        } else {
            holder.layoutEvaluationList.setVisibility(View.VISIBLE);
            holder.txtNoEvaluation.setVisibility(View.GONE);

            for (StudentEvaluationModel eval : courseDetails.getStudentEvalList()) {
                addTableRow(holder.layoutEvaluationList, eval.getEvalName(), String.valueOf(eval.getEvalObtMarks()), String.valueOf(eval.getEvalTMarks()), Color.BLACK, false);
            }

            addTableRow(holder.layoutEvaluationList, "Total", courseDetails.getAllEvaluationObtainedMarks(), courseDetails.getAllEvaluationTotal(), Color.BLACK, false);
            addTableRow(holder.layoutEvaluationList, "Percentage", courseDetails.getPercentage(), "", Color.BLACK, true);

        }
        if (courseDetails.getTotalCount() > 0) {
            initPieChart(holder.pieChart, courseDetails.getPresents(), courseDetails.getAbsents(), courseDetails.getLeaves(), courseDetails.getPresentPercentage(), courseDetails.getTotalCount());
        }
        else
        {
            holder.pieChart.setVisibility(View.GONE);
            holder.atttendancetxtview.setText("No Attendance Available to Show.");
        }
    }

    private void initPieChart(PieChart pieChart, int presentCount, int absentCount, int leaveCount, float attendancePercentage, int totalDays) {
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
        customColors.add(Color.rgb(255, 180, 0));  // Light Orange for "Present"
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

    public void updateList(List<StudentCourseAttendanceEvaluationModel> filteredList) {
        this.studentCourseDetailsList = filteredList;
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
        return studentCourseDetailsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoEvaluation;
        TextView txtCourseName;
        TextView txtCourseType;
        LinearLayout layoutEvaluationList;
        PieChart pieChart;
        TextView atttendancetxtview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourseName = itemView.findViewById(R.id.CourseNameTextView);
            txtCourseType = itemView.findViewById(R.id.studentRollNoTextView);
            layoutEvaluationList = itemView.findViewById(R.id.EvaluationListLayout);
            pieChart = itemView.findViewById(R.id.barChart);
            atttendancetxtview = itemView.findViewById(R.id.atttendancetxtview);
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
