package com.nextgen.hasnatfyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nextgen.hasnatfyp.SCattendanceModel;

import java.util.List;

public class SCAttendanceAdapter extends RecyclerView.Adapter<SCAttendanceAdapter.ViewHolder> {
    private Context context;
    private List<SCattendanceModel> attendanceList;

    public SCAttendanceAdapter(Context context, List<SCattendanceModel> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attendance_details_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SCattendanceModel attendance = attendanceList.get(position);
        holder.dateTextView.setText(attendance.getDate());
        holder.statusTextView.setText(attendance.getStatus());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
