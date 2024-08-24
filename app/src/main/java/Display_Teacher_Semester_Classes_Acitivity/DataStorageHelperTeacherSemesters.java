package Display_Teacher_Semester_Classes_Acitivity;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Display_Teacher_Semesters_Activity.TeacherSemestersModel;

public class DataStorageHelperTeacherSemesters {

    // Save teacher semesters list offline
    public static void storeTeacherSemestersListLocally(Context context, List<TeacherSemestersModel> teacherSemestersList) {
        deleteExistingFile(context);
        String teacherSemestersJson = new Gson().toJson(teacherSemestersList);
        SharedPreferences sharedPreferences = context.getSharedPreferences("TeacherSemesters", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("teacherSemestersJson", teacherSemestersJson);
        editor.apply();
    }

    // Read teacher semesters list offline
    public static List<TeacherSemestersModel> readTeacherSemestersListLocally(Context context) {
        List<TeacherSemestersModel> teacherSemestersList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("TeacherSemesters", Context.MODE_PRIVATE);
        String teacherSemestersJson = sharedPreferences.getString("teacherSemestersJson", "");

        if (!teacherSemestersJson.isEmpty()) {
            Type listType = new TypeToken<ArrayList<TeacherSemestersModel>>() {}.getType();
            teacherSemestersList = new Gson().fromJson(teacherSemestersJson, listType);
        }

        return teacherSemestersList;
    }

    // Delete existing stored file
    public static void deleteExistingFile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TeacherSemesters", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("teacherSemestersJson")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("teacherSemestersJson");
            editor.apply();
        }
    }
}
