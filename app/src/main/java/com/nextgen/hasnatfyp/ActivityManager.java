package com.nextgen.hasnatfyp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager extends Application {
    private static ActivityManager instance;
    private List<Activity> activityList = new ArrayList<>();
    private List<Activity> activitiesForKill = new ArrayList<>();
    private List<Activity> activitiesForKillCourseDeletion = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (!isInternetConnected()) {
            TeacherInstanceModel.getInstance(this).setOfflineMode(true);
        }
    }

    public static ActivityManager getInstance() {
        return instance;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void addActivityForKill(Activity activity) {
        activitiesForKill.add(activity);
    }

    public void addActivityForKillCourseDeletion(Activity activity) {
        activitiesForKillCourseDeletion.add(activity);
    }

    public void finishActivitiesExceptMainMenuActivity() {
        List<Activity> activitiesToKeep = new ArrayList<>();
        for (Activity activity : activityList) {
            if (activity instanceof MainMenuActivity) {
                activitiesToKeep.add(activity);
            } else {
                activity.finish();
            }
        }
        activityList.clear();
        activityList.addAll(activitiesToKeep);
    }

    public void finishActivitiesForKill() {
        for (Activity activity : activitiesForKill) {
            activity.finish();
        }
        activitiesForKill.clear();
    }

    public void finishActivitiesForKillCourseDeletion() {
        for (Activity activity : activitiesForKillCourseDeletion) {
            activity.finish();
        }
        activitiesForKillCourseDeletion.clear();
    }

    public void removeActivityForKillCourseDeletion(Context context) {
        for (Activity activity : activitiesForKillCourseDeletion) {
            if (activity.getBaseContext().equals(context)) {
                activitiesForKillCourseDeletion.remove(activity);
                break;
            }
        }
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
