package com.sse.iamhere.Utils;

import android.content.Context;

import androidx.core.os.ConfigurationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarUtil {

    /* Utility method that returns a new calendar object after subtracting one day to it*/
    public static Calendar getPrvDay(Calendar date) {
        Calendar returnCal = Calendar.getInstance();
        returnCal.setTimeInMillis(date.getTimeInMillis());
        returnCal.add(Calendar.DAY_OF_YEAR, -1);
        return returnCal;
    }

    /* Utility method that returns a new calendar object after adding one day to it*/
    public static Calendar getNextDay(Calendar date) {
        Calendar returnCal = Calendar.getInstance();
        returnCal.setTimeInMillis(date.getTimeInMillis());
        returnCal.add(Calendar.DAY_OF_YEAR, 1);
        return returnCal;
    }

    /* Utility method that returns true if the day, month and year of two calendar objects match*/
    public static boolean isSameDay(Calendar date1, Calendar date2) {
        return  (date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR));
    }

    /* Utility method that returns date in pretty format based on local*/
    public static String getStringDate(Calendar date, Context context) {
        SimpleDateFormat sdf;
        if (isThisYear(date)) {
            sdf = new SimpleDateFormat("EEEE, d MMMM", ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0));
        } else {
            sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0));
        }
        return sdf.format(date.getTime());
    }

    /* Utility method that returns true only if supplied date is within this year */
    public static boolean isThisYear(Calendar date) {
        Calendar thisYear = trimTime(Calendar.getInstance());
        int year = thisYear.get(Calendar.YEAR);
        thisYear.set( year + 1, 0, 1);

        if (date.before(thisYear)) {
            thisYear.set(Calendar.YEAR, year);
            thisYear.add(Calendar.SECOND, -1);

            return date.after(thisYear);
        }

        return false;
    }

    /* Utility method that resets the time of calendar object to 00:00:00.000*/
    public static Calendar trimTime(Calendar date) {
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }


}
