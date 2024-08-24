package Display_Course_Attendance_Activity;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataStorageHelperCourseAttendance {

    public static void storeCourseAttendanceLocally(Context context, List<StudentAttendanceRecordModel> attendanceRecords, String courseID, boolean isRepeater) {
        deleteExistingFile(context, courseID, isRepeater);
        String attendanceRecordsJson = new Gson().toJson(attendanceRecords);
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("attendanceRecordsJson", attendanceRecordsJson);
        editor.apply();
    }

    public static List<StudentAttendanceRecordModel> readCourseAttendanceLocally(Context context, String courseID, boolean isRepeater) {
        List<StudentAttendanceRecordModel> attendanceRecords = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        String attendanceRecordsJson = sharedPreferences.getString("attendanceRecordsJson", "");

        if (!attendanceRecordsJson.isEmpty()) {
            Type listType = new TypeToken<ArrayList<StudentAttendanceRecordModel>>() {}.getType();
            attendanceRecords = new Gson().fromJson(attendanceRecordsJson, listType);
        }

        return attendanceRecords;
    }

    public static void deleteExistingFile(Context context, String courseID, boolean isRepeater) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        if (sharedPreferences.contains("attendanceRecordsJson")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("attendanceRecordsJson");
            editor.apply();
        }
    }

    private static String getFileName(String courseID, boolean isRepeater) {
        return courseID + (isRepeater ? "_Repeater" : "") + "_AttendanceRecords";
    }
}
