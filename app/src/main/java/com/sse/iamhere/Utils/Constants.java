package com.sse.iamhere.Utils;

import com.sse.iamhere.R;

public class Constants {
    public static final boolean DEBUG_MODE = true; //TODO: remove debug

    // Preference folder names
    static final String PERMISSION_PREFS = "USER_PERMISSIONS";
    static final String SETTINGS_PREFS = "APP_SETTINGS";
    static final String SETTINGS_PREFS_SECRET = "APP_SETTINGS_SECRET";


    // Request Codes
    public static final int AUTHENTICATION_RQ = 6581 ;
    public static final int AUTHENTICATION_RELOG_RQ = 6582 ;
    public static final int ACCOUNT_RQ = 6567 ;
    public static final int SETTINGS_RQ = 8369 ;
    public static final int PERMISSIONS_RQ = 8069;
    public static final int SCANNER_RQ = 8367;


    // Setting names
    public static final String IS_FIRST_TIME = "IS_FIRST_TIME";
    public static final String APP_LANG = "APP_LANG";

    public static final String ROLE_TYPE = "ROLE_TYPE";
    public static final String APP_FEED = "APP_FEED";


    // Request Manager
    public static final int TOKEN_REFRESH = 847982;
    public static final int TOKEN_ACCESS = 847965;
    public static final int TOKEN_NONE = 847978;


    // Notifications
    public static final String ANNOUNCEMENT_CHANNEL = "ANNOUNCEMENT_CHANNEL";


    // Request Manager - error codes
    public static class RQM_EC {
        public static final int TOKEN_STORE_FAIL = 101;
        public static final int NO_INTERNET_CONNECTION = 102;

        public static final int REGISTRATION_UNKNOWN = 1001;
        public static final int REGISTRATION_BAD_PHONE = 1002;
        public static final int REGISTRATION_BAD_ROLE = 1003;
        public static final int REGISTRATION_USER_EXISTS = 1004;

        public static final int LOGIN_UNKNOWN = 2001;
        public static final int LOGIN_BAD_PHONE = 2002;
        public static final int LOGIN_BAD_ROLE = 2003;
        public static final int LOGIN_USER_NOT_FOUND = 2004;

        public static final int LOGOUT_UNKNOWN = 3001;

        public static final int REFRESH_REFRESH_EXPIRED = 4001;
        public static final int REFRESH_UNSUPPORTED_TOKEN= 4002;

        public static final int REFRESH_CALL_FAIL = 5001;
        public static final int REFRESH_CALL_BAD_RESPONSE = 5002;

        public static final int CHECK_UNKNOWN = 6001;


        public static final int GET_CREDENTIALS_UNKNOWN = 7001;
        public static final int GET_CREDENTIALS_USER_NOT_FOUND = 7002; //conflict

        public static final int SET_CREDENTIALS_UNKNOWN = 8001;
        public static final int SET_CREDENTIALS_USER_NOT_FOUND = 8002; //conflict

        public static final int CODE_WORDS_UNKNOWN = 9001;
        public static final int CODE_WORDS_USER_NOT_FOUND = 9002; //conflict

        public static final int QR_CODE_UNKNOWN = 1101;
        public static final int QR_CODE_USER_NOT_FOUND = 1102; //conflict
        public static final int QR_CODE_SUBJECT_NOT_FOUND = 1103; //bad request

        public static final int GET_USER_UNKNOWN = 1201;
        public static final int GET_USER_INVALID = 1202; //bad request
        public static final int GET_USER_USER_NOT_FOUND = 1201; //conflict

        public static final int FIND_BY_CODE_UNKNOWN = 1301;

        public static final int FIND_BY_CODES_UNKNOWN = 1401;
        public static final int FIND_BY_CODES_USER_NOT_FOUND = 1402; //conflict

        public static final int JOIN_UNKNOWN = 1501;
        public static final int JOIN_USER_NOT_FOUND = 1502; //conflict
        public static final int JOIN_CODE_MISMATCH = 1503; //failed dependency

        public static final int LEAVE_UNKNOWN = 1601;
        public static final int LEAVE_USER_NOT_FOUND = 1602; //conflict
        public static final int LEAVE_PARTY_NOT_FOUND = 1603; //failed dependency


        public static final int ATTENDEE_GET_PARTIES_UNKNOWN = 1701;

        public static final int ATTENDEE_GET_EVENTS_BY_DATE_UNKNOWN = 1801;
        public static final int ATTENDEE_GET_EVENTS_BY_DATE_USER_NOT_FOUND = 1802; //conflict

        public static final int ATTENDEE_GET_VISITS_BY_DATE_UNKNOWN = 1901;


        public static final int HOST_GET_EVENTS_UNKNOWN = 2101;

        public static final int HOST_GET_EVENTS_BY_DATE_UNKNOWN = 2201;
        public static final int HOST_GET_EVENTS_BY_DATE_USER_NOT_FOUND = 2202; //conflict

        public static final int HOST_GET_PARTIES_BY_EVENT_ID_UNKNOWN = 2301;
        public static final int HOST_GET_PARTIES_BY_EVENT_ID_USER_NOT_FOUND = 2302; //conflict

        public static final int HOST_GET_PARTIES_UNKNOWN = 2401;

        public static final int HOST_GET_ATTENDANCE_UNKNOWN = 2501;

        public static final int HOST_GET_EVENT_UNKNOWN = 2601;

        public static final int HOST_GET_PARTY_UNKNOWN = 2701;

        public static final int HOST_SEND_ANNOUNCEMENT_UNKNOWN = 2801;
        public static final int HOST_SEND_ANNOUNCEMENT_USER_NOT_FOUND = 2802; //conflict
        public static final int HOST_SEND_ANNOUNCEMENT_SUBJECT_NOT_FOUND = 2803; //failed dependency

    }



    // Verification
    public static final int OTP_TIMEOUT = 60*2;

    // Verification - error codes
    public static class VerifiEC {
        public static final int VERIFICATION_FAILED = 0;
        public static final int SIGNIN_FAILED = 1;
        public static final int REVERIFY_PHONE = 2;
    }

    public enum Role {
        NONE,
        ATTENDEE,
        HOST,
        MANAGER;

        private int role;
        Role() {
            this.role = ordinal();
        }

        public int toIdx() {
            return role;
        }

        public int toStringRes() {
            switch (role) {
                default: case 0: throw new RuntimeException("toString: idx is out of bounds");
                case 1: return R.string.onboard_attendee_label;
                case 2: return R.string.onboard_host_label;
                case 3: return R.string.onboard_manager_label;
            }
        }

        public String toSerializedJSON() {
            switch (role) {
                default: case 0: throw new RuntimeException("toSerializedJSON: idx is out of bounds");
                case 1: return "ACCOUNT_PARTICIPATOR";
                case 2: return "ACCOUNT_HOST";
                case 3: return "ACCOUNT_MANGER";
            }
        }

        public int getTheme() {
            switch (role) {
                default: case 0: return R.style.AppTheme;
                case 1: return R.style.AppThemeAttendee;
                case 2: return R.style.AppThemeHost;
                case 3: return R.style.AppThemeManager;
            }
        }

        public int getSideNavMenuItem() {
            switch (role) {
                default: case 0: throw new RuntimeException("getSideNavMenuItem: idx is out of bounds");
                case 1: return R.id.nav_role_attendee;
                case 2: return R.id.nav_role_host;
                case 3: return R.id.nav_role_manager;
            }
        }

        public String getPrefsKey() {
            switch (role) {
                default: case 0: throw new RuntimeException("getSideNavMenuItem: idx is out of bounds");
                case 1: return "TD_KEY_ATTENDEE";
                case 2: return "TD_KEY_HOST";
                case 3: return "TD_KEY_MANAGER";
            }
        }

        public int getFeedType() {
            switch (role) {
                default: case 0: throw new RuntimeException("getSideNavMenuItem: idx is out of bounds");
                case 1: return 0;
                case 2: return 1;
            }
        }
    }

}
