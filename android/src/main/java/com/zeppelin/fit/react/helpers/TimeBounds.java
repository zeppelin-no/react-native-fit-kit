package com.zeppelin.fit.react.helpers;

import android.util.Log;

import com.facebook.react.bridge.ReadableMap;

// date:
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public class TimeBounds {

  private static final String TAG = "RCTFitKit";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private static long dateToInt(String dateString, long fallback, boolean startOfDay) {
    long dateTimeStamp = 1;

    try {
      Date parsedDate = dateFormat.parse(dateString);
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      cal.setTime(parsedDate);

      if (startOfDay) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
      }

      dateTimeStamp = cal.getTimeInMillis();
    } catch(Exception e) {
      Log.e(TAG, "error with the time string, using " + fallback);
      Log.e(TAG, Log.getStackTraceString(e));
      dateTimeStamp = fallback;
    }

    return dateTimeStamp;
  }

  public static long[] getTimeBounds(ReadableMap options) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    Date now = new Date();
    cal.setTime(now);
    long endDate = cal.getTimeInMillis();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    long startDateMin = cal.getTimeInMillis();
    cal.add(Calendar.WEEK_OF_YEAR, -10);
    long startDate = cal.getTimeInMillis();

    if (options.hasKey("endDate") && !options.isNull("endDate")) {
      endDate = dateToInt(options.getString("endDate"), endDate, false);
    }

    if (options.hasKey("startDate") && !options.isNull("startDate")) {
      startDate = dateToInt(options.getString("startDate"), startDate, true);
      if (startDate > startDateMin) {
        startDate = startDateMin;
      }
    }

    long[] timeBounds = {startDate, endDate};

    return timeBounds;
  }
}
