package com.underdog.hydrate.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.service.NotificationService;
import com.underdog.hydrate.service.SchedulerService;
import com.underdog.hydrate.util.DateUtil;
import com.underdog.hydrate.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String tag = "AlarmReceiver";
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(tag, "onReceive");

        boolean reminderAlarm;
        long instantMute;

        reminderAlarm = intent.getBooleanExtra(Constants.REMINDER_ALARM, false);

        Log.d(tag, "reminder Alarm - " + reminderAlarm);

        if (reminderAlarm) {
            instantMute = PreferenceManager
                    .getDefaultSharedPreferences(context).getLong(
                            context.getString(R.string.key_instant_mute), 0);

            if (instantMute < System.currentTimeMillis()) {

                // Start a different wakeful service to trigger notifications
                Intent notificationService = new Intent(context,
                        NotificationService.class);
                startWakefulService(context, notificationService);
            }
            setNextAlarm(context);
        } else {
            Intent schedulerService = new Intent(context,
                    SchedulerService.class);

            // Start the service, keeping the device awake while it is
            // launching.
            startWakefulService(context, schedulerService);
        }
    }

    /**
     * Method to set an alarm for resetting water count
     *
     * @param context
     */
    public void resetAlarms(Context context) {
        Intent intent;
        PendingIntent pendingIntent;

        Log.d(tag, "In resetWaterCountAlarm");
        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        intent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the alarm's trigger time to 23:59:59
        // Has to be 23.59 otherwise count will be reset on every restart
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        //Cancel any previously set alarms for this intent
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                pendingIntent);
        Log.d(tag, "Reset alarm set");
    }

    /**
     * Method to set alarms
     *
     * @param context
     */
    public void setNextAlarm(Context context) {
        Intent intent;
        PendingIntent pendingIntent;
        Calendar calendar;
        int reminderStartHour;
        int reminderStartMin;
        int reminderEndHour;
        int reminderEndMin;
        Long startTime;
        Long endTime;
        Long currentTime;
        int reminderInterval;
        long reminderTime;
        ArrayList<Integer> times;

        Log.d(tag, "In setAlarm");

        resetAlarms(context);

        times = getStartEndTimes(context);

        // Get reminder start times
        reminderStartHour = times.get(0);
        reminderStartMin = times.get(1);

        // Get reminder end times
        reminderEndHour = times.get(2);
        reminderEndMin = times.get(3);

        // Get reminder interval
        reminderInterval = times.get(4);

        calendar = new GregorianCalendar();

        calendar.set(Calendar.HOUR_OF_DAY, reminderStartHour);
        calendar.set(Calendar.MINUTE, reminderStartMin);

        // Current system time
        currentTime = System.currentTimeMillis();

        // Start time in millis
        startTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, reminderEndHour);
        calendar.set(Calendar.MINUTE, reminderEndMin);

        // end time in millis
        endTime = calendar.getTimeInMillis();

        reminderInterval = reminderInterval * (60000);

        intent = new Intent(context, AlarmReceiver.class);

        // To indicate the the request is to fire a reminder alarm
        intent.putExtra(Constants.REMINDER_ALARM, true);

        // User different request id from resetAlarm
        pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);

        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        // TODO BE DONE - Address the case where user starts his day early and
        // furthers the start time after drinking a glass of water
        // In such a case reminders will not accommodate.
        // If user changes time and then drinks, alarms will obviously
        // accommodate

        // If current time is less than reminder start time
        if (currentTime < startTime) {

            Log.d(tag, "currentTime < startTime");

            if (startTime - currentTime > reminderInterval) {

                Log.d(tag, "Setting alarm at start time");

                // Start alarm at startTime
                alarmManager.set(AlarmManager.RTC_WAKEUP, startTime,
                        pendingIntent);
            } else {

                Log.d(tag, "Setting alarm after interval");
                // Start after interval defined
                alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime
                        + reminderInterval, pendingIntent);
            }
        } else if (currentTime < endTime) {

            Log.d(tag, "currentTime < endTime");
            if (endTime - currentTime > reminderInterval) {

                // IMPLEMENT DND HERE.SET ALARM AT THE TIME RETURNED BY DNDFunc
                reminderTime = currentTime + reminderInterval;
                reminderTime = HydrateDAO.getInstance().checkDnd(reminderTime, context);

                Log.d(tag, "Setting alarm after interval");
                // Start after interval defined
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime,
                        pendingIntent);
            } else {

                // Cancel the pending alarm first because 5mins after drinking
                // this case might lead to reminders
                cancelAlarm(context);

                // Set alarm for next day start time
                // No idea how to handle

            }
        }
    }

    /**
     * Method that snoozes notifications for predefined period of time
     *
     * @param context
     */
    public void snoozeAlarm(Context context) {
        SharedPreferences preferences;
        int snoozeTime;
        Intent intent;
        PendingIntent pendingIntent;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        snoozeTime = Integer.parseInt(preferences.getString(
                context.getString(R.string.reminder_snooze_interval),
                String.valueOf(15)));

        Log.d(tag, "snooze time - " + snoozeTime);

        intent = new Intent(context, AlarmReceiver.class);

        // To indicate the the request is to fire a reminder alarm
        intent.putExtra(Constants.REMINDER_ALARM, true);

        // User different request id from resetAlarm but same as set alarm
        pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);

        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (snoozeTime * 60000), pendingIntent);

    }

    /**
     * Method to cancel set alarms
     *
     * @param context
     */
    public void cancelAlarm(Context context) {
        Intent intent;
        PendingIntent pendingIntent;

        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, AlarmReceiver.class);

        // To indicate the the request is to fire a reminder alarm
        intent.putExtra(Constants.REMINDER_ALARM, true);

        // User different request id from resetAlarm
        pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
        Log.d(tag, "Alarm cancelled");
    }

    private ArrayList<Integer> getStartEndTimes(Context context) {
        // 0 - start hour
        // 1 - start min
        // 2 - end hour
        // 3 - end mins
        // 4 - interval
        ArrayList<Integer> times = new ArrayList<>();
        int hours;
        int mins;
        long startTime;
        long endTime;
        int interval;
        Cursor cursor;

        cursor = context
                .getContentResolver()
                .query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        new String[]{HydrateDatabase.REMINDER_START_TIME,
                                HydrateDatabase.REMINDER_END_TIME,
                                HydrateDatabase.REMINDER_INTERVAL},
                        HydrateDatabase.DAY + "=?",
                        new String[]{String.valueOf(DateUtil.getInstance().getToday())},
                        null);
        cursor.moveToFirst();
        startTime = cursor.getLong(0);
        endTime = cursor.getLong(1);
        interval = cursor.getInt(2);
        hours = (int) (startTime / 60);
        mins = (int) (startTime % 60);
        cursor.close();
        times.add(hours);
        times.add(mins);

        hours = (int) (endTime / 60);
        mins = (int) (endTime % 60);
        cursor.close();
        times.add(hours);
        times.add(mins);
        times.add(interval);

        return times;
    }

}
