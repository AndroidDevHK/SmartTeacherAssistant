package View_Semester_Classes_Activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import Add_Class_Courses_Activities.AddClassCourseViaExcelActivity;
import Add_Class_Courses_Activities.AddClassCoursesManuallyActivity;
import View_Class_Students_Activity.DisplayClassStudentsActivity;
import View_Class_Courses_Activity.ManageCoursesActivity;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    private final Context context;
    private List<ClassModel> classList;
    private final List<ClassModel> filteredList; // Add filtered list
    private TextView noResultTextView;

    private final ClassViewModel viewModel; // Add ViewModel member

    public ClassAdapter(Context context, ClassViewModel viewModel) {
        this.context = context;
        this.classList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.viewModel = viewModel; // Initialize ViewModel
    }
    public void setClassList(List<ClassModel> classList) {
        this.classList = classList;
        this.filteredList.clear();
        this.filteredList.addAll(classList); // Update filtered list
        notifyDataSetChanged();
    }

    // Filter method
    public void filter(String query) {
        filteredList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(classList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (ClassModel item : classList) {
                if (item.getClassName().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
        if (filteredList.isEmpty()) {
            showNoResultMessage();
        } else {
            hideNoResultMessage();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.semester_class_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassModel classModel = filteredList.get(position); // Use filtered list
        holder.bind(classModel);
        setAnimation(holder.itemView,position);

    }
    private void setAnimation(View viewToAnimate, int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha);
        if (animation != null) {
            animation.setStartOffset(position * 50); // Adjust the delay as needed
            viewToAnimate.startAnimation(animation);
        } else {
            Log.e("Animation", "Failed to load animation");
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size(); // Use filtered list size
    }

    public void setResultTextView(TextView noResultTextView) {
        this.noResultTextView = noResultTextView;
    }

    public void showNoResultMessage() {
        if (noResultTextView != null) {
            noResultTextView.setVisibility(View.VISIBLE);
        }
    }
    public void hideNoResultMessage() {
        if (noResultTextView != null) {
            noResultTextView.setVisibility(View.GONE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView classNameTextView;
        private final TextView view_report;

        private final TextView numberOfStudentsTextView;
        private final TextView CourseCountTextView;

        private final View editButton;
        private final View deleteButton;
        private final View viewStudentsButton;
        private final View layoutAddCourse;
        private final View layoutManageCourse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classNameTextView = itemView.findViewById(R.id.text_Class_name);
            numberOfStudentsTextView = itemView.findViewById(R.id.text_number_of_classes);
            editButton = itemView.findViewById(R.id.btn_edit);
            CourseCountTextView = itemView.findViewById(R.id.text_number_of_courses);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            viewStudentsButton = itemView.findViewById(R.id.LayoutViewStudents);
            layoutAddCourse = itemView.findViewById(R.id.LayoutAddCourse);
            view_report = itemView.findViewById(R.id.view_report);
            layoutManageCourse = itemView.findViewById(R.id.LayoutAssignCourse);
        }

        public void bind(ClassModel classModel) {
            classNameTextView.setText(classModel.getClassName());
            numberOfStudentsTextView.setText("No. Of Students: " + classModel.getNumberOfStudents());
            CourseCountTextView.setText("Courses : " + classModel.getCoursesCount());
            editButton.setOnClickListener(view -> openEditDialog(classModel));
            deleteButton.setOnClickListener(view -> showConfirmationDialog(classModel.getClassId(),context));
            viewStudentsButton.setOnClickListener(view -> {
                if (classModel.getNumberOfStudents() > 0) {
                    openDisplayStudentsActivity(classModel.getClassId(),classModel.getClassName());
                } else {
                    Toast.makeText(context, "No students to display", Toast.LENGTH_SHORT).show();
                }
            });
            layoutAddCourse.setOnClickListener(view -> showPopupMenu(view, classModel));
            layoutManageCourse.setOnClickListener(view -> {
                if (classModel.getCoursesCount() > 0) {
                   OpenViewCoursesActivity(classModel);
                } else {
                    Toast.makeText(context, "No Courses to display", Toast.LENGTH_SHORT).show();
                }
            });

            view_report.setOnClickListener(v -> {
                if (classModel.getCoursesCount() > 0) {
                } else {
                    Toast.makeText(context, "No Courses added yet..", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void openEditDialog(ClassModel classModel) {
            ClassEditDialog dialog = new ClassEditDialog(context, classModel.getClassId(), classModel.getClassName(), (classID, updatedClassName) -> {
                classModel.setClassName(updatedClassName);
                notifyDataSetChanged();
            });
            dialog.show();
        }

        private  void OpenViewCoursesActivity(ClassModel classModel)
        {
            UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(itemView.getContext());
            userInstituteModel.setClassId(classModel.getClassId());
            userInstituteModel.setClassName(classModel.getClassName());
            Intent intent = new Intent(context, ManageCoursesActivity.class);
            context.startActivity(intent);
        }
        private void showConfirmationDialog(String classId, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Class")
                    .setMessage("Are you sure you want to delete this class?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Call deleteClass method from the ViewModel
                        viewModel.deleteClass(classId,context);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Do nothing
                    })
                    .show();
        }

        private void openDisplayStudentsActivity(String classId, String className) {
            Intent intent = new Intent(context, DisplayClassStudentsActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("className", className);
            UserInstituteModel.getInstance(context).setClassId(classId);
            UserInstituteModel.getInstance(context).setClassName(className);
            context.startActivity(intent);
        }

        private void showPopupMenu(View view, ClassModel classModel) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.menu_add_course);
            popupMenu.setOnMenuItemClickListener(item -> handleMenuItemClick(item, classModel));
            popupMenu.show();
        }

        private boolean handleMenuItemClick(MenuItem item, ClassModel classModel) {
            Intent intent;
            if (item.getItemId() == R.id.menu_add_courses_via_excel) {
                intent = new Intent(context, AddClassCourseViaExcelActivity.class);
            } else if (item.getItemId() == R.id.menu_add_courses_manually) {
                intent = new Intent(context, AddClassCoursesManuallyActivity.class);
            } else {
                return false;
            }


            UserInstituteModel.getInstance(context).setClassId(classModel.getClassId());
            UserInstituteModel.getInstance(context).setClassName(classModel.getClassName());
            intent.putExtra("className",  classModel.getClassName());
            intent.putExtra("classId", classModel.getClassId());
            context.startActivity(intent);
            return true;
        }
    }


}
