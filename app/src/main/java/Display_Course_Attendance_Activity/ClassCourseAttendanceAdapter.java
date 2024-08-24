package Display_Course_Attendance_Activity;

import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.ArrayList;
import java.util.List;

public class ClassCourseAttendanceAdapter extends RecyclerView.Adapter<ClassCourseAttendanceAdapter.ViewHolder> {

    private List<StudentAttendanceRecordModel> attendanceRecords;

    public ClassCourseAttendanceAdapter(List<StudentAttendanceRecordModel> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentAttendanceRecordModel attendanceRecord = attendanceRecords.get(position);

        holder.studentRollNoTextView.setText("Roll No: " + attendanceRecord.getStudentRollNo());
        holder.bindAttendanceList(attendanceRecord.getAttendanceList(), holder.pieChart, attendanceRecord.getName());

    }

    @Override
    public int getItemCount() {
        return attendanceRecords.size();
    }

    public void updateList(List<StudentAttendanceRecordModel> filteredList) {
        this.attendanceRecords = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView studentRollNoTextView;
        TextView classNameTextView;
        LinearLayout attendanceListLayout;
        PieChart pieChart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentRollNoTextView = itemView.findViewById(R.id.studentRollNoTextView);
            classNameTextView = itemView.findViewById(R.id.studentNameTextView);
            attendanceListLayout = itemView.findViewById(R.id.attendanceListLayout);
            pieChart = itemView.findViewById(R.id.barChart);
        }

        public void bindAttendanceList(List<StudentAttendanceModel> attendanceList, PieChart pieChart, String className) {
            attendanceListLayout.removeAllViews(); // Clear previous views

            int presentCount = 0;
            int absentCount = 0;
            int leaveCount = 0;
            int totalDays = attendanceList.size();

            classNameTextView.setText("Student Name: " + className);

            LinearLayout.LayoutParams dateLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            LinearLayout.LayoutParams statusLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);

            LinearLayout headerLayout = new LinearLayout(itemView.getContext());
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView dateHeader = new TextView(itemView.getContext());
            dateHeader.setText("Date");
            dateHeader.setTextSize(16);
            dateHeader.setTextColor(itemView.getResources().getColor(R.color.black));
            dateHeader.setTypeface(null, Typeface.BOLD); // Make text bold
            dateHeader.setLayoutParams(dateLayoutParams);

            TextView statusHeader = new TextView(itemView.getContext());
            statusHeader.setText("Status");
            statusHeader.setTextSize(16);
            statusHeader.setTextColor(itemView.getResources().getColor(R.color.black));
            statusHeader.setTypeface(null, Typeface.BOLD); // Make text bold
            statusHeader.setLayoutParams(statusLayoutParams);

            headerLayout.addView(dateHeader);
            headerLayout.addView(statusHeader);

            attendanceListLayout.addView(headerLayout);

            // Check if attendanceList is not null and not empty
            if (attendanceList != null && !attendanceList.isEmpty()) {
                // Iterate through the attendance list and populate the layout
                for (StudentAttendanceModel attendance : attendanceList) {
                    // Create a horizontal LinearLayout for each attendance entry
                    LinearLayout rowLayout = new LinearLayout(itemView.getContext());
                    rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                    // Date TextView
                    TextView dateTextView = new TextView(itemView.getContext());
                    dateTextView.setText(attendance.getDate());
                    dateTextView.setTextSize(16);
                    dateTextView.setTextColor(itemView.getResources().getColor(R.color.black));
                    dateTextView.setTypeface(null, Typeface.BOLD); // Make text bold
                    dateTextView.setLayoutParams(dateLayoutParams);

                    // Attendance Status TextView
                    TextView statusTextView = new TextView(itemView.getContext());
                    statusTextView.setText(attendance.getAttendanceStatus());
                    statusTextView.setTextSize(16);
                    statusTextView.setTextColor(itemView.getResources().getColor(R.color.black));
                    statusTextView.setTypeface(null, Typeface.BOLD); // Make text bold
                    statusTextView.setLayoutParams(statusLayoutParams);

                    // Add date and status TextViews to the row layout
                    rowLayout.addView(dateTextView);
                    rowLayout.addView(statusTextView);

                    // Add the row layout to the attendance list layout
                    attendanceListLayout.addView(rowLayout);

                    // Update counts based on attendance status
                    switch (attendance.getAttendanceStatus()) {
                        case "P":
                            presentCount++;
                            break;
                        case "A":
                            absentCount++;
                            break;
                        case "L":
                            leaveCount++;
                            break;
                        default:
                            break;
                    }
                }
            } else {
                TextView emptyTextView = new TextView(itemView.getContext());
                emptyTextView.setText("No attendance records available");
                emptyTextView.setTextSize(16);
                emptyTextView.setTextColor(itemView.getResources().getColor(R.color.black));
                attendanceListLayout.addView(emptyTextView);
            }

            double attendancePercentage = (presentCount / (double) totalDays) * 100;

            initPieChart(pieChart, presentCount, absentCount, leaveCount, attendancePercentage, totalDays);
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

            PieDataSet dataSet = new PieDataSet(entries, "Percentage : " + String.format("%.2f%%", attendancePercentage));
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

            pieChart.setCenterText("Classes: " + totalDays);
            pieChart.setCenterTextSize(10f);
            pieChart.setCenterTextColor(Color.BLACK);

            pieChart.setHoleRadius(40f);
            pieChart.setTransparentCircleRadius(45f);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setDrawSliceText(false);  // Hide text inside slices

            pieChart.invalidate();
        }
    }

    public static class IntValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }
}
