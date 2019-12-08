package com.sse.iamhere.Utils;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

public class TextFormatter {
    public static String formatPhone(@Nullable String unformattedPhoneNumber) {
        if (unformattedPhoneNumber==null) {
            return "";
        }
        return unformattedPhoneNumber.replaceFirst("(\\d{1})(\\d{3})(\\d{3})(\\d+)", "$1($2)-$3-$4");
    }

    public static Spanned formatAsHTML(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static String prettyCount(String count) {
        Number number = Integer.valueOf(count);
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }
}
