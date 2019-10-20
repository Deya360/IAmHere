package com.sse.iamhere.Utils;

public class Constants {
    // Preference folder names
    static final String PERMISSION_PREFS = "USER_PERMISSIONS";
    static final String SETTINGS_PREFS = "APP_SETTINGS";

    // Request Codes
    public static final int VERIFICATION_RQ = 8669;


    // Setting names
    public static final String IS_FIRST_TIME = "IS_FIRST_TIME";
    public static final String IS_AUTHORIZED = "IS_AUTHORIZED";
    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    // Verification
    public static final int OTP_TIMEOUT = 60*2;

    // Verification - error codes
    public static class VerifiEC {
        public static final int VERIFICATION_FAILED = 0;
        public static final int SIGNIN_FAILED = 1;
    }

}
