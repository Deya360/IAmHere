package com.sse.iamhere.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class PermissionUtil {
    /*
     * Check if version is marshmallow and above.
     * */
    private static boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }
    private static boolean shouldAskPermission(Activity activity, String permission){
        if (shouldAskPermission()) {
            int permissionResult = ActivityCompat.checkSelfPermission(activity, permission);
            return permissionResult != PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
    public static void checkPermission(Activity activity, String[] permissions, PermissionAskListener listener){
        ArrayList<String> onNeedPermission = new ArrayList<>();

        for (String permission : permissions) {
            /* If permission is not granted
             * */
            if (shouldAskPermission(activity, permission)){
                /* If permission denied previously
                 * */
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    listener.onPermissionPreviouslyDenied(permission);
                } else {
                    /* Permission denied or first time requested
                     * */
                    if (PreferencesUtil.isFirstTimeAskingPermission(activity, permission)) {
                        PreferencesUtil.firstTimeAskingPermission(activity, permission, false);
                        onNeedPermission.add(permission);

                    } else {
                        /* Handle the feature without permission or ask user to manually allow permission
                         * */
                        listener.onPermissionDisabled(permission);
                    }
                }
            } else {
                listener.onPermissionGranted(permission);
            }
        }

        // Ask one or multiple permissions at once
        if (onNeedPermission.size()!=0) {
            listener.onNeedPermissions(onNeedPermission);
        }
    }
    public interface PermissionAskListener {
        void onNeedPermissions(ArrayList<String> permissions);
        void onPermissionPreviouslyDenied(String permission);
        void onPermissionDisabled(String permission);
        void onPermissionGranted(String permission);
    }
}

