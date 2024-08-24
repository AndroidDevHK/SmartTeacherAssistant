package Display_Teacher_Semester_Classes_Acitivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import Display_Course_Attendance_Activity.DisplayClassCourseAttendanceActivity;
import Display_Complete_Course_Att_Eval_data_Activity.DisplayCompleteCourseStudentsDetailsActivity;

import com.nextgen.hasnatfyp.DisplayCourseReportMenuActivity;
import com.nextgen.hasnatfyp.DisplayQuizQuestionsListActivity;
import com.nextgen.hasnatfyp.MakeCourseQuizActivity;
import com.nextgen.hasnatfyp.NetworkUtils;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import Display_Course_Attendance_List_Activity.DisplayCourseAttendanceListActivity;
import Display_Course_Evaluations_List_Activity.DisplayCourseEvaluationListActivity;
import Mark_Course_Students_Attendance_Activity.MarkStudentsAttendanceActivity;
import Select_Class_Evaluation_Type_Activity.SelectClassStudentsEvaluationActivity;

import java.util.List;

public class TeacherClassesAdapter extends RecyclerView.Adapter<TeacherClassesAdapter.TeacherClassViewHolder> {

    private List<TeacherClassModel> teacherClassesList;

    public TeacherClassesAdapter(List<TeacherClassModel> teacherClassesList) {
        this.teacherClassesList = teacherClassesList;
    }

    public void setTeacherClassesList(List<TeacherClassModel> teacherClassesList) {
        this.teacherClassesList = teacherClassesList;
        notifyDataSetChanged(); // Notify adapter about the data change
    }

    @NonNull
    @Override
    public TeacherClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_class_item, parent, false);
        return new TeacherClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherClassViewHolder holder, int position) {
        TeacherClassModel teacherClass = teacherClassesList.get(position);
        holder.bind(teacherClass);
    }

    @Override
    public int getItemCount() {
        return teacherClassesList.size();
    }

    static class TeacherClassViewHolder extends RecyclerView.ViewHolder {

        private TextView classNameTextView;
        private TextView courseNameTextView;
        private TextView numberOfStudentsTextView;
        private TextView numberOfRepeatersTextView;
        private LinearLayout markAttendanceLayout;
        private LinearLayout addEvaluationLayout;
        private LinearLayout LayoutManageAttendance; // Added
        private LinearLayout LayoutManageEvaluation;
        private LinearLayout ViewCourseReportLayout;
        private LinearLayout ViewQuizLayout;

        TeacherClassViewHolder(@NonNull View itemView) {
            super(itemView);
            classNameTextView = itemView.findViewById(R.id.text_Class_name);
            courseNameTextView = itemView.findViewById(R.id.text_Course_name);
            numberOfStudentsTextView = itemView.findViewById(R.id.text_number_of_students);
            numberOfRepeatersTextView = itemView.findViewById(R.id.text_number_of_repeaters);
            markAttendanceLayout = itemView.findViewById(R.id.layout_mark_attendance);
            addEvaluationLayout = itemView.findViewById(R.id.layout_add_evaluation);
            LayoutManageAttendance = itemView.findViewById(R.id.layout_manage_attendance); // Added
            LayoutManageEvaluation = itemView.findViewById(R.id.layout_manage_evaluation); // Added
            ViewCourseReportLayout = itemView.findViewById(R.id.ViewCourseReportLayout);
            ViewQuizLayout = itemView.findViewById(R.id.ViewQuizLayout);

        }

        @SuppressLint("SetTextI18n")
        void bind(TeacherClassModel teacherClass) {
            Context context = itemView.getContext();
            classNameTextView.setText("Class: " + teacherClass.getClassName());
            courseNameTextView.setText("Course: " + teacherClass.getCourseName());
            numberOfStudentsTextView.setText("Students: " + teacherClass.getRegularStudentCount());
            ViewQuizLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, MakeCourseQuizActivity.class);
                TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(itemView.getContext());
                teacherInstanceModel.setCourseName(teacherClass.getCourseName());
                teacherInstanceModel.setClassName(teacherClass.getClassName());
                intent.putExtra("CourseID", teacherClass.getCourseId());
                intent.putExtra("ClassID", teacherClass.getClassId());
                context.startActivity(intent);
            });
            int repeatersCount = teacherClass.getCourseRepeatersStudentsCount();
            if (repeatersCount > 0) {
                numberOfRepeatersTextView.setVisibility(View.VISIBLE);
                numberOfRepeatersTextView.setText("Course Repeaters: " + repeatersCount);
            } else {
                numberOfRepeatersTextView.setVisibility(View.GONE);
            }
            ViewCourseReportLayout.setOnClickListener(v -> {
                if (teacherClass.getCourseRepeatersStudentsCount() > 0) {
                    showPopupMenuChooseStudentCategory(context, teacherClass);
                } else {
                    DisplayCourseReportMenuActivity(context, teacherClass.getCourseId(),teacherClass.getClassId(),false,teacherClass.getClassName(),teacherClass.getCourseName());
                }
            });


            markAttendanceLayout.setOnClickListener(v -> {
                if (TeacherInstanceModel.getInstance(itemView.getContext()).isOfflineMode())
                {
                    if (teacherClass.getCourseRepeatersStudentsCount() > 0) {
                        showPopupMenuAttendance(context, teacherClass);
                    } else {
                        openMarkAttendanceActivity(context, teacherClass, false);
                    }
                }
                else {
                    if (NetworkUtils.isInternetConnected(itemView.getContext())) {
                        if (teacherClass.getCourseRepeatersStudentsCount() > 0) {
                            showPopupMenuAttendance(context, teacherClass);
                        } else {
                            openMarkAttendanceActivity(context, teacherClass, false);
                        }
                    } else {
                        Toast.makeText(itemView.getContext(), "Please Connect Internet to Continue...", Toast.LENGTH_LONG).show();
                    }
                }
            });

            LayoutManageEvaluation.setOnClickListener(v -> {
                if(NetworkUtils.isInternetConnected(itemView.getContext())) {
                    if (teacherClass.getCourseRepeatersStudentsCount() > 0) {
                        showPopupMenuLayoutManageEvaluation(context, teacherClass.getCourseId(),teacherClass.getClassId(),teacherClass.getCourseName(),teacherClass.getClassName());

                    } else {
                        DisplayCourseEvaluationListActivity(context, teacherClass.getCourseId(),teacherClass.getClassId(),teacherClass.getCourseName(),teacherClass.getClassName(),false);
                    }
                }
                else {
                    Toast.makeText(itemView.getContext(),"Please Connect Internet to Continue...",Toast.LENGTH_LONG).show();
                }
            });

            addEvaluationLayout.setOnClickListener(v -> {
                    if (teacherClass.getCourseRepeatersStudentsCount() > 0) {
                        showPopupMenuEvaluation(context, teacherClass);
                    } else {
                        openSelectEvaluationActivity(context, teacherClass, false);
                    }

            });

            LayoutManageAttendance.setOnClickListener(v -> {
                if(NetworkUtils.isInternetConnected(itemView.getContext())) {
                    if (teacherClass.getCourseRepeatersStudentsCount() > 0) {
                        showPopupMenuLayoutManageAttendance(context, teacherClass.getCourseId(),teacherClass.getClassId(),teacherClass.getCourseName(),teacherClass.getClassName());
                    } else {
                        DisplayCourseAttendanceListActivity(context, teacherClass.getCourseId(),teacherClass.getClassId(),teacherClass.getCourseName(),teacherClass.getClassName(),false);
                    }
                }
                else {
                    Toast.makeText(itemView.getContext(),"Please Connect Internet to Continue...",Toast.LENGTH_LONG).show();
                }
            });
        }

        private void DisplayCourseEvaluationListActivity(Context context, String courseId, String classId, String courseName, String className, boolean AreRepeaters) {
            Intent intent = new Intent(context, DisplayCourseEvaluationListActivity.class);
            TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(itemView.getContext());
            teacherInstanceModel.setCourseName(courseName);
            teacherInstanceModel.setClassName(className);
            intent.putExtra("CourseID", courseId);
            intent.putExtra("ClassID", classId);
            intent.putExtra("AreRepeaters", AreRepeaters);
            context.startActivity(intent);
        }

        private void showPopupMenuLayoutManageEvaluation(Context context, String courseId, String classId, String courseName, String className) {
            PopupMenu popupMenu = new PopupMenu(context, LayoutManageAttendance);
            popupMenu.getMenuInflater().inflate(R.menu.menu_manage_evaluation, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_manage_regular_std_eval) {
                    DisplayCourseEvaluationListActivity(context, courseId,classId,courseName,className,false);
                    return true;
                } else if (itemId == R.id.menu_manage_repeaters_std_eval) {
                    DisplayCourseEvaluationListActivity(context, courseId,classId,courseName,className,true);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void DisplayCourseAttendanceListActivity(Context context, String courseId, String classId, String courseName, String className, boolean AreRepeaters) {
            Intent intent = new Intent(context, DisplayCourseAttendanceListActivity.class);
            TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(itemView.getContext());
            teacherInstanceModel.setCourseName(courseName);
            teacherInstanceModel.setClassName(className);
            intent.putExtra("CourseID", courseId);
            intent.putExtra("ClassID", classId);
            intent.putExtra("AreRepeaters", AreRepeaters);
            context.startActivity(intent);
        }

        private void showPopupMenuLayoutManageAttendance(Context context, String courseId, String classId, String courseName, String className) {
            PopupMenu popupMenu = new PopupMenu(context, LayoutManageAttendance);
            popupMenu.getMenuInflater().inflate(R.menu.menu_manage_view_attendance, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_manage_regular_std_attendance) {
                    DisplayCourseAttendanceListActivity(context, courseId,classId,courseName,className,false);
                    return true;
                } else if (itemId == R.id.menu_manage_repeaters_std_attendance) {
                    DisplayCourseAttendanceListActivity(context, courseId,classId,courseName,className,true);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void showPopupMenuChooseStudentCategory(Context context, TeacherClassModel teacherClass) {
            PopupMenu popupMenu = new PopupMenu(context, ViewCourseReportLayout);
            popupMenu.getMenuInflater().inflate(R.menu.menu_choose_student_category, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_view_regular_student_report) {
                    DisplayCourseReportMenuActivity(context, teacherClass.getCourseId(),teacherClass.getClassId(),false, teacherClass.getClassName(), teacherClass.getCourseName());
                    return true;
                } else if (itemId == R.id.menu_view_repeaters_student_report) {
                    DisplayCourseReportMenuActivity(context, teacherClass.getCourseId(),teacherClass.getClassId(),true, teacherClass.getClassName(), teacherClass.getCourseName());
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }
        private void DisplayCourseReportMenuActivity(Context context, String courseId, String classId, boolean AreRepeaters, String className, String CourseName) {
            Intent intent = new Intent(context, DisplayCourseReportMenuActivity.class);
            TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(itemView.getContext());
            teacherInstanceModel.setCourseName(CourseName);
            teacherInstanceModel.setClassName(className);
            intent.putExtra("CourseID", courseId);
            intent.putExtra("ClassID", classId);
            intent.putExtra("AreRepeaters", AreRepeaters);
            context.startActivity(intent);
        }

        private void showPopupMenuEvaluation(Context context, TeacherClassModel teacherClass) {
            PopupMenu popupMenu = new PopupMenu(context, markAttendanceLayout);
            popupMenu.getMenuInflater().inflate(R.menu.menu_add_evaluation, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_regular_std_evaluation) {
                    openSelectEvaluationActivity(context, teacherClass, false);
                    return true;
                } else if (itemId == R.id.menu_repeaters_evaluation) {
                    openSelectEvaluationActivity(context, teacherClass, true);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void showPopupMenuAttendance(Context context, TeacherClassModel teacherClass) {
            PopupMenu popupMenu = new PopupMenu(context, markAttendanceLayout);
            popupMenu.getMenuInflater().inflate(R.menu.menu_mark_attendance, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_regular_std_attendance) {
                    openMarkAttendanceActivity(context, teacherClass, false);
                    return true;
                } else if (itemId == R.id.menu_repeaters_std_attendance) {
                        openMarkAttendanceActivity(context, teacherClass, true);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void openMarkAttendanceActivity(Context context, TeacherClassModel teacherClass,boolean areRepeaters) {
            Intent intent = new Intent(context, MarkStudentsAttendanceActivity.class);
            intent.putExtra("teacherClass", teacherClass);
            intent.putExtra("areRepeaters", areRepeaters);
            context.startActivity(intent);
        }
        private void openSelectEvaluationActivity(Context context, TeacherClassModel teacherClass,boolean areRepeaters) {
            Intent intent = new Intent(context, SelectClassStudentsEvaluationActivity.class);
            intent.putExtra("teacherClass", teacherClass);
            intent.putExtra("areRepeaters", areRepeaters);
            TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(itemView.getContext());
            teacherInstanceModel.setCourseName(teacherClass.getCourseName());
            teacherInstanceModel.setClassName(teacherClass.getClassName());
            context.startActivity(intent);
        }

    }
}
