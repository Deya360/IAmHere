package com.sse.iamhere.Utils;

import androidx.annotation.Nullable;

public class TextFormatter {
    public static String formatPhone(@Nullable String unformattedPhoneNumber) {
        if (unformattedPhoneNumber==null) {
            return "";
        }
        return unformattedPhoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)-$2-$3");
    }
}
