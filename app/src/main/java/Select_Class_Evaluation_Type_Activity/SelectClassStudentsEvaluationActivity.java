package Select_Class_Evaluation_Type_Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.nextgen.hasnatfyp.ActivityManager;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.SetupToolbar;
import com.nextgen.hasnatfyp.TeacherInstanceModel;

import Display_Teacher_Semester_Classes_Acitivity.TeacherClassModel;
import Display_Students_To_Add_Evaluation_Activity.AddCourseStudentsEvaluation;

public class SelectClassStudentsEvaluationActivity extends AppCompatActivity {
    TeacherClassModel teacherClass;
    boolean areRepeaters;
    private TextInputEditText evaluationEditText;
    private TextInputEditText totalMarksEditText;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_class_students_evaluation);
        Intent intent = getIntent();
        teacherClass = getTeacherClassFromIntent(intent);
        areRepeaters = getAreRepeatersFromIntent(intent);
        findViews();
        setupButtonClickListener();
        setClassAndCourseName();
        ActivityManager.getInstance().addActivityForKill(this);

    }

    private void findViews() {
        evaluationEditText = findViewById(R.id.editText_evaluation);
        totalMarksEditText = findViewById(R.id.editText_total_marks);
        nextButton = findViewById(R.id.button_next);
        Toolbar toolbar = findViewById(R.id.customtoolbar);
        SetupToolbar(toolbar);
    }

    private void SetupToolbar(Toolbar toolbar) {
        SetupToolbar.setup(this, toolbar, "Add Evaluation", true);
    }

    private void setupButtonClickListener() {
        nextButton.setOnClickListener(v -> handleInput());
    }

    @SuppressLint("SetTextI18n")
    private void setClassAndCourseName() {
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(this);
        if (teacherInstanceModel != null) {
            String className = teacherInstanceModel.getClassName();
            String courseName = teacherInstanceModel.getCourseName();

            TextView classNameTextView = findViewById(R.id.classNameTextView);
            TextView courseNameTextView = findViewById(R.id.courseNameTextView);

            classNameTextView.setText(className);
            courseNameTextView.setText(courseName + repeaterStatus);
        }
    }

    private void handleInput() {
        String evaluation = evaluationEditText.getText().toString().trim().toUpperCase();
        String totalMarks = totalMarksEditText.getText().toString().trim();

        if (evaluation.isEmpty()) {
            evaluationEditText.setError("Evaluation field cannot be empty");
            return;
        }

        if (totalMarks.isEmpty()) {
            totalMarksEditText.setError("Total Marks field cannot be empty");
            return;
        }
        openAddEvaluationActivity(evaluation, totalMarks);

    }


    private void openAddEvaluationActivity(String evaluation, String totalMarks) {
        Intent intent = new Intent(this, AddCourseStudentsEvaluation.class);
        intent.putExtra("areRepeaters", areRepeaters);
        intent.putExtra("teacherClass", teacherClass);
        intent.putExtra("evaluation", evaluation);
        intent.putExtra("totalMarks", totalMarks);
        startActivity(intent);
    }

    private boolean getAreRepeatersFromIntent(Intent intent) {
        return intent.getBooleanExtra("areRepeaters", false);
    }

    private TeacherClassModel getTeacherClassFromIntent(Intent intent) {
        return intent.getParcelableExtra("teacherClass");
    }
}
