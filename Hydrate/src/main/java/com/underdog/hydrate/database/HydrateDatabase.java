package com.underdog.hydrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.util.Log;

public class HydrateDatabase extends SQLiteOpenHelper {

    public static final String LIMIT="limit";
    // Columns
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_QUANTITY = "quantity";
    // Columns for List View population
    public static final String[] EVENT_COLUMNS = {"_id", COLUMN_QUANTITY,
            COLUMN_TIMESTAMP};
    public static final String COLUMN_REACHED = "reached";
    public static final String COLUMN_DATE = "date";
    public static final String SUM_QUANTITY = "sum(quantity) AS sum";
    public static final String ROW_ID = "rowId";
    public static final String COLUMN_TARGET_QUANTITY = "target_quantity";
    public static final String COLUMN_CONSUMED_QUANTITY = "consumed_quantity";
    public static final String DAY = "day_id";
    public static final String REMINDER_START_TIME = "reminder_start_time";
    public static final String REMINDER_END_TIME = "reminder_end_time";
    public static final String REMINDER_INTERVAL = "reminder_interval";
    public static final String LUNCH_START = "lunch_start";
    public static final String LUNCH_END = "lunch_end";
    public static final String DINNER_START = "dinner_start";
    public static final String DINNER_END = "dinner_end";
    public static final String DB_NAME = "hydrate.db";
    // Columns
    public static final int DATABASE_VERSION = 5;
    public static final String HYDRATE_LOG = "hydrate_log";
    // DDL queries
    public static final String CREATE_TABLE_HYDRATE_LOG = "create table hydrate_log(log_id integer primary key autoincrement,quantity REAL,timestamp integer)";
    public static final String CREATE_TABLE_HYDRATE_TARGETS = "create table hydrate_target(target_id integer primary key autoincrement,date text,target_quantity REAL,consumed_quantity REAL,reached integer)";
    public static final String CREATE_TABLE_HYDRATE_CUPS = "create table hydrate_cups(cup_id integer primary key autoincrement,quantity REAL)";
    public static final String CREATE_TABLE_HYDRATE_DAILY_SCHEDULE = "create table hydrate_daily_schedule(day_id integer primary key,reminder_start_time integer,reminder_end_time integer,reminder_interval integer,target_quantity REAL,lunch_start integer,lunch_end integer,dinner_start integer,dinner_end integer)";
    public static final String CREATE_TABLE_HYDRATE_DND = "create table hydrate_dnd(id integer primary key autoincrement,schedule_name text,start_time integer,end_time integer,days text,status boolean)";
    // Queries - START
    public static final String DELETE_LAST = "delete from hydrate_log where log_id=(select max(log_id) from hydrate_log)";
    // DDL queries
    public static final String FROM_TO_TIME = "timestamp >= ? and timestamp < ?";
    public static final String UPDATE_UNITS_TO_OZ_US = "update hydrate_log set quantity=ROUND((quantity*0.033814),2)";
    public static final String UPDATE_LOG_UNITS_TO_ML = "update hydrate_log set quantity=ROUND(quantity*29.5735)";
    public static final String UPDATE_TARGET_UNITS_TO_ML = "update hydrate_target set target_quantity=ROUND(target_quantity*29.5735),consumed_quantity=ROUND(consumed_quantity*29.5735)";
    public static final String UPDATE_TARGET_UNITS_TO_OZ_US = "update hydrate_target set target_quantity=ROUND((target_quantity*0.033814),2),consumed_quantity=ROUND((consumed_quantity*0.033814),2)";
    public static final String UPDATE_DS_TARGET_UNITS_TO_ML = "update hydrate_daily_schedule set target_quantity=ROUND((target_quantity*29.5735/1000),2)*1000";
    public static final String UPDATE_DS_TARGET_UNITS_TO_OZ_US = "update hydrate_daily_schedule set target_quantity=ROUND((target_quantity*0.033814))";
    // Columns for summary population
    public static final String[] SUMMARY_TOTAL_COLUMNS = {"count(*) AS count",
            "sum(quantity) AS sum"};
    // Queries - END
    public static final String[] cups_ml = new String[]{"150", "200", "250",
            "350", "500"};
    public static final String[] cups_oz = new String[]{"6", "8", "12", "16",
            "20"};
    public static final String HYDRATE_CUPS = "hydrate_cups";
    private static final String HYDRATE_DAILY_SCHEDULE = "hydrate_daily_schedule";
    private Context context;

    public HydrateDatabase(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create hydrate log table
        database.execSQL(CREATE_TABLE_HYDRATE_LOG);

        // Create hydrate target table
        database.execSQL(CREATE_TABLE_HYDRATE_TARGETS);

        // Create hydrate cups table
        createCupTable(database);

        // Create hydrate daily schedule table
        createDailySchedule(database);

        // Create don not disturb table
        createDND(database);


    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.d(getClass().getSimpleName(), "upgrade called");
        if (oldVersion == 1) {
            // Add new table for targets
            database.execSQL(CREATE_TABLE_HYDRATE_TARGETS);
            createCupTable(database);
            // Create hydrate daily schedule table
            createDailySchedule(database);

            // Create don not disturb table
            createDND(database);
        } else if (oldVersion == 2) {
            createCupTable(database);
            // Create hydrate daily schedule table
            createDailySchedule(database);

            // Create don not disturb table
            createDND(database);
        } else if (oldVersion == 3) {
            // Create hydrate daily schedule table
            createDailySchedule(database);

            // Create don not disturb table
            createDND(database);
        } else if (oldVersion == 4) {
            //Update Daily Schedule
            updateDailyScheduleWithTarget(database);
        }
    }

    private void createCupTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_HYDRATE_CUPS);
        ContentValues values;
        for (String cup : cups_ml) {
            values = new ContentValues();
            values.put(COLUMN_QUANTITY, cup);
            database.insert(HYDRATE_CUPS, null, values);
        }
    }

    private void createDailySchedule(SQLiteDatabase database) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        database.execSQL(CREATE_TABLE_HYDRATE_DAILY_SCHEDULE);
        ContentValues values;
        for (int day = 0; day < 7; day++) {
            values = new ContentValues();
            values.put(DAY, day);
            values.put(
                    REMINDER_START_TIME,
                    preferences.getInt(Constants.REMINDER_START_HOUR,
                            Constants.DEFAULT_HOUR_START)
                            * 60
                            + preferences.getInt(Constants.REMINDER_START_MIN,
                            Constants.DEFAULT_MINUTE_START));
            values.put(
                    REMINDER_END_TIME,
                    preferences.getInt(Constants.REMINDER_END_HOUR,
                            Constants.DEFAULT_HOUR_END)
                            * 60
                            + preferences.getInt(Constants.REMINDER_END_MIN,
                            Constants.DEFAULT_MINUTE_END));

            values.put(REMINDER_INTERVAL, Integer.parseInt(preferences
                    .getString(
                            context.getString(R.string.key_reminder_interval),
                            Constants.DEFAULT_REMINDER_INTERVAL)));

            values.put(LUNCH_START, 720);
            values.put(LUNCH_END, 780);

            values.put(DINNER_START, 1200);
            values.put(DINNER_END, 1260);

            //Target newly introduced in version 14
            values.put(COLUMN_TARGET_QUANTITY, Double.valueOf(preferences.getString(context
                    .getString(R.string.key_target), "2.5")) * 1000);
            database.insert(HYDRATE_DAILY_SCHEDULE, null, values);
        }

    }

    private void createDND(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_HYDRATE_DND);
    }

    private void updateDailyScheduleWithTarget(SQLiteDatabase database) {
        String dailyTarget;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String milliliter = context.getString(R.string.milliliter);
        String metric = preferences.getString(context.getString(R.string.key_metric), milliliter);
        ContentValues values = new ContentValues();

        database.execSQL("alter table hydrate_daily_schedule add target_quantity REAL");
        if (metric.equals(milliliter)) {
            dailyTarget = preferences.getString(context
                    .getString(R.string.key_target), "2.5");
            values.put(COLUMN_TARGET_QUANTITY, Double.valueOf(dailyTarget) * 1000);
        } else {
            dailyTarget = preferences.getString(context
                    .getString(R.string.key_target), "85");
            values.put(COLUMN_TARGET_QUANTITY, Double.valueOf(dailyTarget));
        }
        database.update(HYDRATE_DAILY_SCHEDULE, values, null, null);
    }
}
