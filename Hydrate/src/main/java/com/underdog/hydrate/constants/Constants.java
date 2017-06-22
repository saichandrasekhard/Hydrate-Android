package com.underdog.hydrate.constants;

import java.io.File;

public class Constants {

    /**
     * Logs enabled or not
     */
    public static final boolean debugLogs = false;
    public static final String PACKAGE = "com.underdog.hydrate";
    public static final String CURRENT_DB_PATH = File.separator + "data"
            + File.separator + PACKAGE + File.separator + "databases"
            + File.separator + "hydrate.db";
    public static final String DATE = "date";
    public static final String POSITION = "position";
    public static final String QUANTITY = "quantity";
    public static final long DAY_HOURS_LONG = 86400000;
    public static final String TIME = "time";
    public static final String DATE_FORMAT = "dd-MMM-yyyy";
    public static final String DATE_FORMAT_SQLITE = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "h:mm a";
    public static final String DATE_TIME_FORMAT = new StringBuffer(DATE_FORMAT)
            .append(" ").append(TIME_FORMAT).toString();
    //updated from first run to accommodate run time permissions
    public static final String FIRST_RUN = "firstRun2";
    public static final String REMINDER_START_HOUR = "reminderStartHour";
    public static final String REMINDER_END_HOUR = "reminderEndHour";
    public static final String REMINDER_START_MIN = "reminderStartMin";
    public static final String REMINDER_END_MIN = "reminderEndMin";
    public static final String REMINDER_ALARM = "reminderAlarm";
    public static final int NOTIFICATION_ID = 001;
    public static final int NOTIFICATION_TARGET_ACHIEVED_ID = 002;
    public static final String SUMMARY_HEADING = "summaryHeading";
    public static final String SUMMARY_CUPS = "summaryCups";
    public static final String SUMMARY_QUANTITY = "summaryQuantity";
    public static final String KEY_NOTIFICATION_ID = "notificationId";
    public static final String REGULARITY = "regularity";
    public static final String DEV_EMAIL = "sai.cs.d1@gmail.com";
    public static final String NOTIFICATION_ACTION_SNOOZE = "snooze";
    public static final String NOTIFICATION_ACTION_DRINK = "drink";
    public static final String NOTIFICATION_ACTION_SHARE = "share";

    /**
     * Backup/Restore related constants
     */
    public static final String BACKUP_FOLDER = "Hydrate";
    public static final String BACKUP_DB_FILE_NAME = "hydrate_back.db";
    public static final String BACKUP_SETTINGS_FILE_NAME = "hydrate_settings_back.txt";

    /* Your ad unit id. Replace with your actual ad unit id. */
    public static final String AD_UNIT_ID = "ca-app-pub-2744293360021656/9095556528";

    /**
     * Default hour
     */
    public static final int DEFAULT_HOUR_START = 8;

    /**
     * Default minute
     */
    public static final int DEFAULT_MINUTE_START = 0;

    /**
     * Default hour
     */
    public static final int DEFAULT_HOUR_END = 21;

    /**
     * Default minute
     */
    public static final int DEFAULT_MINUTE_END = 0;

    public static final String DEFAULT_REMINDER_INTERVAL = "90";
    public static final String DEFAULT_REMINDER_SNOOZE_INTERVAL = "15";

    public static final String JUST_ANOTHER_KEY = "key";

    public static final String DAYS_SELECTED = "daysSelected";
    public static final String NEW_START_TIME = "newStartTime";
    public static final String NEW_END_TIME = "newEndTime";
    public static final String OLD_HOUR = "oldStartHour";
    public static final String OLD_END_HOUR = "oldEndHour";
    public static final String OLD_END_MINS = "oldEndMins";
    public static final String OLD_MINS = "oldStartMins";
    public static final String OLD_TARGET = "oldTarget";
    public static final String NEW_TARGET = "newTarget";

    public static final String GDRIVE_DB_ID = "gDriveDbId";
    public static final String GDRIVE_SETTINGS_ID = "gDriveSettingsId";


    //Constants related to permissions

    /**
     * Id to identify a camera permission request.
     */
    public static final int REQUEST_WRITE_EXTERNAL_STARTUP = 0;
    public static final int REQUEST_WRITE_EXTERNAL_OVERFLOW = 1;

}
