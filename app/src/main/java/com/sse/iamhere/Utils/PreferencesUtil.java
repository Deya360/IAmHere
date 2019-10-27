package com.sse.iamhere.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;
import com.sse.iamhere.Server.Body.TokenData;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static android.content.Context.MODE_PRIVATE;
import static com.sse.iamhere.Utils.Constants.ROLE_TYPE;
import static com.sse.iamhere.Utils.Constants.SETTINGS_PREFS;
import static com.sse.iamhere.Utils.Constants.SETTINGS_PREFS_SECRET;
import static com.sse.iamhere.Utils.Constants.TD_KEY;

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


    // Methods to get and set prefs of different types;
    public static boolean getPrefByName(Context context, String setting, boolean defaultState){
        return context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getBoolean(setting, defaultState);
    }

    public static void setPrefByName(Context context, String setting, boolean state){
        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(setting, state).apply();
    }

//    public static String getPrefByName(Context context, String setting, String defaultValue){
//        return context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getString(setting, defaultValue);
//    }
//
//    public static void setPrefByName(Context context, String setting, String value){
//        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
//        sharedPreference.edit().putString(setting, value).apply();
//    }

    public static int getPrefByName(Context context, String setting, int defaultValue){
        return context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getInt(setting, defaultValue);
    }

    public static void setPrefByName(Context context, String setting, int value){
        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putInt(setting, value).apply();
    }

    // Methods for obtaining and storing token data in encrypted shared preferences
    public static TokenData getTokenData(Context context) throws GeneralSecurityException, IOException {
        String key =  MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences encryptedSharedPrefs = EncryptedSharedPreferences.create(
                SETTINGS_PREFS_SECRET,
                key,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        return new Gson().fromJson(encryptedSharedPrefs.getString(TD_KEY, null), TokenData.class);
    }

    public static void setToken(Context context, TokenData tokenData) throws GeneralSecurityException, IOException {
        String key =  MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences encryptedSharedPrefs = EncryptedSharedPreferences.create(
                SETTINGS_PREFS_SECRET,
                key,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        encryptedSharedPrefs.edit().putString(TD_KEY, new Gson().toJson(tokenData)).apply();
    }

    // Methods for getting role
    public static Constants.Role getRole(Context context, Constants.Role defaultRole) {
        String roleStr = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getString(ROLE_TYPE, null);
        if (roleStr==null) return defaultRole;
        else return new Gson().fromJson(roleStr, Constants.Role.class);
    }

    public static void setRole(Context context, Constants.Role role) {
        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putString(ROLE_TYPE,  new Gson().toJson(role)).apply();
    }

}
