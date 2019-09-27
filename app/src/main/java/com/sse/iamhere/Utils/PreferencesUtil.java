package com.sse.iamhere.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesUtil {
    // Permissions Util
    /* Below two functions are used to store in the shared preferences,
        whether a certain permission request was asked before, or if first time*/
    static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime){
        SharedPreferences sharedPreference = context.getSharedPreferences(Constants.PERMISSION_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(Constants.PERMISSION_PREFS, MODE_PRIVATE).getBoolean(permission, true);
    }
}
