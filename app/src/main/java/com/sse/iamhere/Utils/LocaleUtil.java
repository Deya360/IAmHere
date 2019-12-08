package com.sse.iamhere.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleUtil {
    public static void setConfigLang(Context context) {
        String localeStr = PreferencesUtil.getPrefByName(context, Constants.APP_LANG, null);
        if (localeStr != null) setConfigLang(context, new Locale(localeStr));
    }

    public static void setConfigLang(Context context, Locale newLocale) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        conf.setLocale(newLocale);
        res.updateConfiguration(conf, dm);
        Locale.setDefault(newLocale);
    }
}
