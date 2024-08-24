package com.nextgen.hasnatfyp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class PreferenceManager {
    private static final String PREF_NAME = "MySharedPrefs";
    private static final String KEY_SOLO_USER_ID = "solo_user_id";

    public static String generateAndSaveSoloUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Generate solo user id logic
        String soloUserId = "S" + UUID.randomUUID().toString(); // Example: S123e4567-e89b-12d3-a456-426614174000

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SOLO_USER_ID, soloUserId);
        editor.apply();

        return soloUserId;
    }

    public static String getOrCreateSoloUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(KEY_SOLO_USER_ID)) {
            return generateAndSaveSoloUserId(context);
        } else {
            return sharedPreferences.getString(KEY_SOLO_USER_ID, null);
        }
    }
}

