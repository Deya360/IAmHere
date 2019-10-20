package com.sse.iamhere.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;
import static com.sse.iamhere.Utils.Constants.SETTINGS_PREFS;

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

    // Settings Activity
    public static boolean getPrefByName(Context context, String setting, boolean defaultState){
        return context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getBoolean(setting, defaultState);
    }

    public static void setPrefByName(Context context, String setting, boolean state){
        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(setting, state).apply();
    }

    public static String getPrefByName(Context context, String setting, String defaultValue){
        return context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getString(setting, defaultValue);
    }

    public static void setPrefByName(Context context, String setting, String value){
        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putString(setting, value).apply();
    }
}
