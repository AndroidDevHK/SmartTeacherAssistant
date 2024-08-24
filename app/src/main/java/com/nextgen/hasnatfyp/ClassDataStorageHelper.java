package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Display_Class_Complete_Attendance_Evaluation_Activity.StudentData;

public class ClassDataStorageHelper {

    private static final String TAG = "ClassDataStorageHelper";
    private static final String PREF_NAME = "ClassData";


    public static void saveDataForClass(Context context, String classId, List<StudentData> studentDataList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(studentDataList);

        editor.putString(classId, json);
        editor.apply();
    }


    public static List<StudentData> getDataForClass(Context context, String classId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String json = sharedPreferences.getString(classId, null);

        List<StudentData> studentDataList = new ArrayList<>();

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<StudentData>>(){}.getType();
            studentDataList = gson.fromJson(json, type);
        } else {
            Log.d(TAG, "No data found for classId: " + classId);
        }

        return studentDataList;
    }
}

