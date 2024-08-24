package Edit_Course_Attendance_Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nextgen.hasnatfyp.AttendanceStudentDetails;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import java.util.List;

import Display_Course_Attendance_List_Activity.AttendanceInfoModel;

public class DisplayCourseSpecificDateAttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_course_specific_date_attendance);

        Intent intent = getIntent();
        if (intent != null) {
            Boolean areRepeaters = intent.getBooleanExtra("AreRepeaters", false);
            AttendanceInfoModel attendanceInfo = intent.getParcelableExtra("attendanceInfo");
            if (attendanceInfo != null) {
                List<AttendanceStudentDetails> studentList = attendanceInfo.getStudentList();

                TableLayout tableLayout = findViewById(R.id.tableLayoutAttendance);

                // Header row
                TableRow headerRow = new TableRow(this);
                headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                // Header labels
                TextView srLabel = createTextView(getApplicationContext(), "Sr#", true);
                TextView studentLabel = createTextView(getApplicationContext(), "Student", true);
                TextView attendanceLabel = createTextView(getApplicationContext(), "Attendance", true);

                // Add header labels to header row
                headerRow.addView(srLabel);
                headerRow.addView(studentLabel);
                headerRow.addView(attendanceLabel);

                // Add header row to table layout
                tableLayout.addView(headerRow);

                // Counter for serial numbers
                int serialNumber = 1;

                // Iterate through student list
                for (AttendanceStudentDetails student : studentList) {
                    TableRow row = new TableRow(this);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 0, 0, 16); // Add margin between rows
                    row.setLayoutParams(layoutParams);

                    // Create and add serial number TextView
                    TextView serialNumberTextView = createTextView(getApplicationContext(), String.valueOf(serialNumber), false);
                    row.addView(serialNumberTextView);

                    TextView nameTextView = createTextView(getApplicationContext(), student.getStudentName() + "\n" + student.getStudentRollNo(), false);
                    TextView statusTextView = createTextView(getApplicationContext(), student.getAttendanceStatus(), false);

                    int padding = getResources().getDimensionPixelSize(R.dimen.table_cell_padding);
                    nameTextView.setPadding(padding, padding, padding, padding);
                    statusTextView.setPadding(padding, padding, padding, padding);

                    row.addView(nameTextView);
                    row.addView(statusTextView);

                    tableLayout.addView(row);

                    // Increment serial number
                    serialNumber++;
                }

                setCardInfo(areRepeaters, attendanceInfo);
            }
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        SetupToolbar(toolbar);
    }
    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "View Attendance Details", true);
    }
    private void setCardInfo(Boolean areRepeaters, AttendanceInfoModel attendanceInfo) {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);
            TextView attendanceDateTextView = findViewById(R.id.attendanceDateTextView);

            classNameTextView.setText(className);
            attendanceDateTextView.setText(attendanceInfo.getAttendanceDate());
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }

    private TextView createTextView(Context context, String text, boolean isBold) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setPadding(16, 8, 16, 8);
        textView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        if (isBold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }
}
