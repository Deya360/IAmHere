package com.sse.iamhere.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;
import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Server.ServiceGen;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.sse.iamhere.Utils.Constants.ROLE_TYPE;
import static com.sse.iamhere.Utils.Constants.SETTINGS_PREFS;
import static com.sse.iamhere.Utils.Constants.SETTINGS_PREFS_SECRET;

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

    public static ArrayList<String> getStringArrayPrefByName(Context context, String setting, String defaultString){
        ArrayList<String> returnArr = new ArrayList<>();
        String tempStr = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).getString(setting, defaultString);
        if (tempStr!=null && !tempStr.equals("")) {
            returnArr.addAll(Arrays.asList(tempStr.split("\\s*,\\s*")));
        }
        return returnArr;
    }

    public static void setStringArrayByName(Context context, String setting, List<String> array){
        SharedPreferences sharedPreference = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);
        sharedPreference.edit().putString(setting, TextUtils.join(",",array)).apply();
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
    public static TokenData getTokenData(Context context, Constants.Role role)
                        throws GeneralSecurityException, IOException {
        String key =  MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences encryptedSharedPrefs = EncryptedSharedPreferences.create(
                SETTINGS_PREFS_SECRET,
                key,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        return new Gson().fromJson(encryptedSharedPrefs.getString(role.getPrefsKey(), null), TokenData.class);
    }

    public static void setToken(Context context, TokenData tokenData, Constants.Role role)
                        throws GeneralSecurityException, IOException {
        String key =  MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences encryptedSharedPrefs = EncryptedSharedPreferences.create(
                SETTINGS_PREFS_SECRET,
                key,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        encryptedSharedPrefs.edit().putString(role.getPrefsKey(), new Gson().toJson(tokenData)).apply();
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
        ServiceGen.resetCachedServices();
    }

    // Convenience methods:
    public static boolean isTokenAvailableForCurrentRole(Context context) {
        Constants.Role role;
        if ((role =getRole(context, null))!=null) {
            return isTokenAvailableForRole(context, role);
        }
        return false;
    }

    public static boolean isTokenAvailableForRole(Context context, Constants.Role role) {
        try {
            if (getTokenData(context, role)!=null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
