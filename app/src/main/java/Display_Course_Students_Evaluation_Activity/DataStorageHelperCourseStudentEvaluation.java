package Display_Course_Students_Evaluation_Activity;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataStorageHelperCourseStudentEvaluation {

    // Save student evaluation list offline
    public static void storeCourseStudentEvaluationLocally(Context context, List<CourseStudentEvaluationListModel> studentEvalList, String courseID, boolean isRepeater) {
        String studentEvalListJson = new Gson().toJson(studentEvalList);
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("studentEvalListJson", studentEvalListJson);
        editor.apply();
    }

    // Read student evaluation list offline
    public static List<CourseStudentEvaluationListModel> readCourseStudentEvaluationLocally(Context context, String courseID, boolean isRepeater) {
        List<CourseStudentEvaluationListModel> studentEvalList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        String studentEvalListJson = sharedPreferences.getString("studentEvalListJson", "");

        if (!studentEvalListJson.isEmpty()) {
            Type listType = new TypeToken<ArrayList<CourseStudentEvaluationListModel>>() {}.getType();
            studentEvalList = new Gson().fromJson(studentEvalListJson, listType);
        }

        return studentEvalList;
    }

    // Delete stored file
    public static void deleteExistingFile(Context context, String courseID, boolean isRepeater) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getFileName(courseID, isRepeater), Context.MODE_PRIVATE);
        if (sharedPreferences.contains("studentEvalListJson")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("studentEvalListJson");
            editor.apply();
        }
    }

    // Get file name based on course ID and repeater status
    private static String getFileName(String courseID, boolean isRepeater) {
        return courseID + "_" + (isRepeater ? "Repeater" : "Regular") + "_StudentEvalList";
    }
}

