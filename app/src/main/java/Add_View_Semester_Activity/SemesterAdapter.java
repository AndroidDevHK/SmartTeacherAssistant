package Add_View_Semester_Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import Add_Class_Activities.AddClassViaExcelFileActivity;
import Add_Class_Activities.AddClassManuallyActivity;
import View_Semester_Classes_Activity.ManageClassesActivity;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.util.List;

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.ViewHolder> implements SemesterDialogHelper.SemesterDialogListener {

    private Context context;
    private List<SemesterModel> semesterList;
    private ProgressDialog progressDialog;
    private SemesterViewModel viewModel;


    public SemesterAdapter(Context context, List<SemesterModel> semesterList,SemesterViewModel viewModel) {
        this.context = context;
        this.semesterList = semesterList;
        this.viewModel = viewModel;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
    }

    public void setSemesters(List<SemesterModel> semesters) {
        this.semesterList = semesters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.semester_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SemesterModel semester = semesterList.get(position);

        holder.bind(semester);
        setAnimation(holder.itemView,position);

    }
    private void setAnimation(View viewToAnimate, int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha);
        if (animation != null) {
            animation.setStartOffset(position * 100); // Adjust the delay as needed
            viewToAnimate.startAnimation(animation);
        } else {
            Log.e("Animation", "Failed to load animation");
        }
    }
    @Override
    public int getItemCount() {
        return semesterList.size();
    }

    @Override
    public void onSemesterAdded(SemesterModel semester) {
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textSemesterName, textNumberOfClasses;
        private Switch switchSemesterStatus;
        private ImageButton btnEdit, btnDelete;
        private LinearLayout imageViewAddClass, imageViewViewClasses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSemesterName = itemView.findViewById(R.id.text_semester_name);
            textNumberOfClasses = itemView.findViewById(R.id.text_number_of_classes);
            switchSemesterStatus = itemView.findViewById(R.id.switch_semester_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            imageViewAddClass = itemView.findViewById(R.id.image_add_classes_layout);
            imageViewViewClasses = itemView.findViewById(R.id.image_view_view_classes_layout);
        }

        public void bind(SemesterModel semester) {
            textSemesterName.setText(semester.getSemesterName());
            textNumberOfClasses.setText("No. Of Classes: " + semester.getClassCount());

            switchSemesterStatus.setChecked(semester.isActive());

            switchSemesterStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                progressDialog.show();
                updateSemesterStatus(semester.getSemesterID(), isChecked);
            });

            btnDelete.setOnClickListener(v -> showConfirmationDialog(semester.getSemesterID(),context,semester.getSemesterName()));
            btnEdit.setOnClickListener(v -> openEditDialog(semester));

            imageViewAddClass.setOnClickListener(v -> showPopupMenu(v, semester));

            imageViewViewClasses.setOnClickListener(v -> {
                if (semester.getClassCount() > 0) {
                    openManageClassActivity(semester);
                } else {
                    Toast.makeText(context, "No classes to display", Toast.LENGTH_SHORT).show();
                }
            });        }

        private void updateSemesterStatus(String semesterId, boolean newStatus) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference semesterRef = db.collection("Semesters").document(semesterId);

            semesterRef.update("isActive", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Semester status updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to update semester status", Toast.LENGTH_SHORT).show();
                    });
        }

        private void showConfirmationDialog(String semesterId, Context context, String semesterName) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SemesterAdapter.this.context);
            builder.setTitle("Delete Semester")
                    .setMessage("Are you sure you want to delete this semester?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Call deleteSemester method from the ViewModel
                        viewModel.deleteSemester(semesterId,context,semesterName);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Do nothing
                    })
                    .show();
        }
    }


        private void openEditDialog(SemesterModel semester) {
            SemesterDialogHelper dialogHelper = new SemesterDialogHelper(context);
            dialogHelper.openSemesterDialog(SemesterAdapter.this, semester);
        }

        private void showPopupMenu(View v, SemesterModel semester) {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.menu_add_class);
            UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_add_via_excel) {
                    Intent intent = new Intent(context, AddClassViaExcelFileActivity.class);
                    userInstituteModel.setSemesterName(semester.getSemesterName());
                    userInstituteModel.setSemesterId(semester.getSemesterID());
                    context.startActivity(intent);
                    return true;
                }
                if (item.getItemId() == R.id.menu_add_manually) {

                    Intent intent = new Intent(context, AddClassManuallyActivity.class);
                    userInstituteModel.setSemesterName(semester.getSemesterName());
                    userInstituteModel.setSemesterId(semester.getSemesterID());
                    context.startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void openManageClassActivity(SemesterModel semester) {
            Intent intent = new Intent(context, ManageClassesActivity.class);
            UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context);
            userInstituteModel.setSemesterName(semester.getSemesterName());
            userInstituteModel.setSemesterId(semester.getSemesterID());
            context.startActivity(intent);
        }
    }
