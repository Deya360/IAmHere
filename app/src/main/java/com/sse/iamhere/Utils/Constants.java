package com.sse.iamhere.Utils;

public class Constants {
    // Preference folder names
    static final String PERMISSION_PREFS = "USER_PERMISSIONS";
    static final String SETTINGS_PREFS = "APP_SETTINGS";
    static final String SETTINGS_PREFS_SECRET = "APP_SETTINGS_SECRET";

    // Request Codes
    public static final int VERIFICATION_RQ = 8669;


    // Setting names
    public static final String IS_FIRST_TIME = "IS_FIRST_TIME";
    public static final String IS_AUTHORIZED = "IS_AUTHORIZED";
    public static final String TD_KEY = "TD_KEY";


    // Request Manager
    public static final int TOKEN_REFRESH = 847982;
    public static final int TOKEN_ACCESS = 847965;
    public static final int TOKEN_NONE = 847978;

    // Request Manager - error codes
    public static class RQM_EC {
        public static final int TOKEN_STORE_FAIL = 101;

        public static final int REGISTRATION_UNKNOWN = 1001;
        public static final int REGISTRATION_BAD_PHONE = 1002;
        public static final int REGISTRATION_BAD_ROLE = 1003;
        public static final int REGISTRATION_USER_EXISTS = 1004;

        public static final int LOGIN_UNKNOWN = 2001;
        public static final int LOGIN_BAD_PHONE = 2002;
        public static final int LOGIN_BAD_ROLE = 2003;
        public static final int LOGIN_USER_NOT_FOUND = 2004;

        public static final int REFRESH_REFRESH_EXPIRED = 3001;
        public static final int REFRESH_UNSUPPORTED_TOKEN= 3002;

        public static final int REFRESH_CALL_FAIL = 3007;
        public static final int REFRESH_CALL_BAD_RESPONSE = 3008;

        public static final int CHECK_UNKNOWN = 4001;

        public static final int FIND_PARTY_ATTENDEE_UNKNOWN = 5001;
    }



    // Verification
    public static final int OTP_TIMEOUT = 60*2;

    // Verification - error codes
    public static class VerifiEC {
        public static final int VERIFICATION_FAILED = 0;
        public static final int SIGNIN_FAILED = 1;
    }



    //Setup Dialog
    public static class ROLES {
        public static final int ROLE_NONE = 82790;
        public static final int ROLE_ATTENDEE = 82791;
        public static final int ROLE_MANAGER = 82792;
        public static final int ROLE_HOST = 82793;
    }


    public static final boolean DEBUG_MODE = false; //TODO: remove debug

}
