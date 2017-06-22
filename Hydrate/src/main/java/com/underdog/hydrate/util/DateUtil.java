package com.underdog.hydrate.util;

import com.underdog.hydrate.constants.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chandrasekhar.dandu on 6/14/2017.
 */

public class DateUtil {

    private static DateUtil utility = null;

    private DateUtil() {

    }

    public static DateUtil getInstance() {
        if (utility == null)
            utility = new DateUtil();
        return utility;
    }


    /**
     * forms where clause values for a given day
     *
     * @param currentTime
     * @return
     */
    public String[] getSelectionArgsForDay(long currentTime) {
        String[] selectionArgs;
        Calendar calendar;
        long fromTime;
        long toTime;

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 59);
        fromTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        toTime = calendar.getTimeInMillis();

        selectionArgs = new String[2];
        selectionArgs[0] = String.valueOf(fromTime);
        selectionArgs[1] = String.valueOf(toTime);

        return selectionArgs;
    }

    public String getDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT);
        return dateFormat.format(new Date(timestamp));
    }

    public long getTimeFromDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT);
        Date dateObj = null;
        try {
            dateObj = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj.getTime();
    }

    public String getSqliteDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_SQLITE);
        Date date = new Date(timestamp);
        Log.d(this.getClass().toString(), "date - " + dateFormat.format(date));
        return dateFormat.format(date);
    }

    public long getTimeFromSqliteDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT_SQLITE);
        Date dateObj = null;
        try {
            dateObj = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj.getTime();
    }

    public long getThisTimeThatDay(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT);
        Date dateObj = null;
        try {
            dateObj = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        calendar.setTime(dateObj);
        calendar.set(Calendar.HOUR_OF_DAY, today.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, today.get(Calendar.MINUTE));
        return calendar.getTimeInMillis();
    }

    public int getDaysSince(long timestamp) {
        long difference = System.currentTimeMillis() - timestamp;
        difference = difference / 86400000l;
        return (int) (difference == 0 ? 1 : difference);
    }

    public int getToday() {
        int day_id;
        Calendar calendar = Calendar.getInstance();
        day_id = calendar.get(Calendar.DAY_OF_WEEK);
        day_id -= 2;
        if (day_id == -1)
            day_id = 6;
        return day_id;
    }

    public int getDay(long timestamp) {
        int day_id;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        day_id = calendar.get(Calendar.DAY_OF_WEEK);
        day_id -= 2;
        if (day_id == -1)
            day_id = 6;
        return day_id;
    }

    public long subGivenTimeInDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, (int) (time / 60));
        calendar.set(Calendar.MINUTE, (int) (time % 60));
        return calendar.getTimeInMillis();
    }

    public int[] getHoursAndMins(String value) {
        String hourMins[] = value.split(":");
        int hour = Integer.parseInt(hourMins[0]);
        int mins = Integer.parseInt(hourMins[1]);
        return new int[]{hour, mins};
    }

    public boolean isToday(long timestamp) {
        String date = getDate(timestamp);
        return isToday(date);
    }

    public boolean isToday(String date) {
        String today = getDate(System.currentTimeMillis());
        return today.equals(date);
    }

}
