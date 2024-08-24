package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Display_Teacher_Semester_Classes_Acitivity.TeacherClassModel;

public class DataStorageHelperTeacherClasses {

    private static final String TAG = "DataStorageHelper";

    public static void storeTeacherClassesListLocally(Context context, List<TeacherClassModel> teacherClassesList) {
        deleteExistingFile(context);
        String teacherClassesJson = new Gson().toJson(teacherClassesList);
        SharedPreferences sharedPreferences = context.getSharedPreferences(TeacherInstanceModel.getInstance(context).getTeacherUsername()+"Classes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("teacherClassesJson", teacherClassesJson);
        editor.apply();
    }

    public static List<TeacherClassModel> readTeacherClassesListLocally(Context context) {
        List<TeacherClassModel> teacherClassesList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(TeacherInstanceModel.getInstance(context).getTeacherUsername() +"Classes", Context.MODE_PRIVATE);
        String teacherClassesJson = sharedPreferences.getString("teacherClassesJson", "");

        if (!teacherClassesJson.isEmpty()) {
            Type listType = new TypeToken<ArrayList<TeacherClassModel>>() {}.getType();
            teacherClassesList = new Gson().fromJson(teacherClassesJson, listType);
        }

        return teacherClassesList;
    }

    public static void deleteExistingFile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TeacherClasses", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("teacherClassesJson")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("teacherClassesJson");
            editor.apply();
        }
    }
}

