package Display_Complete_Course_Att_Eval_data_Activity;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataStorageHelperCompleteCourseDetails {

    // Save student evaluation list offline
    public static void storeCompleteCourseDetailsLocally(Context context, List<CourseStudentDetailsModel> completeCourseDetails, String courseID, boolean isRepeater) {
        deleteExistingFile(context, courseID, isRepeater);
        String completeCourseDetailsJson = new Gson().toJson(completeCourseDetails);
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("completeCourseDetailsJson", completeCourseDetailsJson);
        editor.apply();
    }

    // Read student evaluation list offline
    public static List<CourseStudentDetailsModel> readCompleteCourseDetailsLocally(Context context, String courseID, boolean isRepeater) {
        List<CourseStudentDetailsModel> completeCourseDetails = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        String completeCourseDetailsJson = sharedPreferences.getString("completeCourseDetailsJson", "");

        if (!completeCourseDetailsJson.isEmpty()) {
            Type listType = new TypeToken<ArrayList<CourseStudentDetailsModel>>() {}.getType();
            completeCourseDetails = new Gson().fromJson(completeCourseDetailsJson, listType);
        }

        return completeCourseDetails;
    }

    // Delete existing stored file
    public static void deleteExistingFile(Context context, String courseID, boolean isRepeater) {
        File file = new File(context.getFilesDir(), getFileName(courseID, isRepeater) + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    // Get the file name based on courseID and repeater status
    private static String getFileName(String courseID, boolean isRepeater) {
        String repeaterStatus = isRepeater ? "_Repeaters" : "";
        return courseID + repeaterStatus + "_CompleteReport";
    }
}

