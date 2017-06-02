package com.underdog.hydrate.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.underdog.hydrate.R;
import com.underdog.hydrate.util.Log;
import com.underdog.hydrate.util.Utility;

import java.util.List;
import java.util.Map;

public class HydrateDAO {
    private Context context;

    public HydrateDAO(Context context) {
        this.context = context;
    }

    /**
     * Log a new entry in database
     *
     * @param timestamp time at which user drank water
     * @param quantity  quantity of water consumed
     */
    public void addWater(long timestamp, String quantity) {
        ContentValues values;
        ContentResolver contentResolver = context.getContentResolver();

        values = new ContentValues();
        values.put(HydrateDatabase.COLUMN_QUANTITY, Double.valueOf(quantity));
        values.put(HydrateDatabase.COLUMN_TIMESTAMP, timestamp);

        contentResolver.insert(HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                values);
    }

    /**
     * Log a new entry in database with default quantity
     */
    public void addDefaultWater() {
        ContentValues values;
        SharedPreferences preferences;
        double defaultQuantity;
        String metric;
        String milliliter = context.getString(R.string.milliliter);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        metric = preferences.getString(context.getString(R.string.key_metric),
                milliliter);
        if (metric.equals(milliliter)) {
            defaultQuantity = Double.valueOf(preferences.getString(
                    context.getString(R.string.glass_quantity), "250"));
        } else {
            defaultQuantity = Double.valueOf(preferences.getString(
                    context.getString(R.string.glass_quantity), "8.45"));
        }

        values = new ContentValues();
        values.put(HydrateDatabase.COLUMN_QUANTITY, defaultQuantity);
        values.put(HydrateDatabase.COLUMN_TIMESTAMP, System.currentTimeMillis());

        context.getContentResolver().insert(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, values);

    }

    /**
     * Method to delete the last logged
     */
    public void deleteWater() {
        context.getContentResolver().delete(
                HydrateContentProvider.CONTENT_URI_HYDRATE_DELETE_LAST, null,
                null);
    }

    public void deleteWaterById(long rowId) {
        context.getContentResolver().delete(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.ROW_ID + "=?",
                new String[]{String.valueOf(rowId)});
    }

    /**
     * @param rowId
     * @param timeStamp
     * @param quantity
     */
    public void updateEvent(long rowId, long timeStamp, double quantity) {
        ContentValues values = new ContentValues();
        values.put(HydrateDatabase.COLUMN_QUANTITY, quantity);
        values.put(HydrateDatabase.COLUMN_TIMESTAMP, timeStamp);
        context.getContentResolver().update(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, values,
                HydrateDatabase.ROW_ID + "=?",
                new String[]{String.valueOf(rowId)});
    }

    /**
     * Update the target status at the end of the day
     */
    public void updateTargetStatus() {
        long timestamp = System.currentTimeMillis();
        String[] selectionArgs;
        double todaysConsumption;
        double dailyTarget;

        ContentValues values;

        // Subtract 24hours to make sure we are calculating for the same day
        // since this method gets called at 11:59:59 PM in inexact fashion
        timestamp -= 86400000l;
        selectionArgs = Utility.getInstance().getSelectionArgsForDay(timestamp);

        Cursor cursor = context.getContentResolver().query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                new String[]{"sum(quantity) AS sum"},
                HydrateDatabase.FROM_TO_TIME, selectionArgs, null);

        cursor.moveToFirst();
        todaysConsumption = cursor.getDouble(0);
        Log.d(this.getClass().toString(), "consumption - " + todaysConsumption);
        Cursor targetCursor = context.getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{Utility.getInstance().getDay(timestamp) + ""}, null);
        targetCursor.moveToFirst();
        dailyTarget = targetCursor.getDouble(0);

        values = new ContentValues();
        if (todaysConsumption >= dailyTarget) {
            values.put(HydrateDatabase.COLUMN_REACHED, 1);
        } else {
            values.put(HydrateDatabase.COLUMN_REACHED, 0);
        }
        values.put(HydrateDatabase.COLUMN_TARGET_QUANTITY, dailyTarget);
        values.put(HydrateDatabase.COLUMN_CONSUMED_QUANTITY, todaysConsumption);
        values.put(HydrateDatabase.COLUMN_DATE,
                Utility.getInstance().getSqliteDate(timestamp));
        context.getContentResolver().insert(
                HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET, values);
    }

    public long checkDnd(long reminderTime) {
        long lunchStart;
        long lunchEnd;
        long dinnerStart;
        long dinnerEnd;
        long prefixTime = 30 * 60000;
        Cursor cursor;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean lunchAndDinner = preferences.getBoolean(
                context.getString(R.string.key_mute_lunch_dinner), true);
        Utility utility;

        if (lunchAndDinner) {
            utility = Utility.getInstance();
            cursor = context.getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.LUNCH_START,
                            HydrateDatabase.LUNCH_END,
                            HydrateDatabase.DINNER_START,
                            HydrateDatabase.DINNER_END},
                    HydrateDatabase.DAY + "=?",
                    new String[]{String.valueOf(utility.getToday())}, null);
            cursor.moveToFirst();
            lunchStart = cursor.getLong(0);
            lunchEnd = cursor.getLong(1);
            dinnerStart = cursor.getLong(2);
            dinnerEnd = cursor.getLong(3);
            lunchStart = utility.subGivenTimeInDay(lunchStart);
            lunchEnd = utility.subGivenTimeInDay(lunchEnd);
            dinnerStart = utility.subGivenTimeInDay(dinnerStart);
            dinnerEnd = utility.subGivenTimeInDay(dinnerEnd);

            if (reminderTime >= lunchStart - prefixTime
                    && reminderTime <= lunchEnd + prefixTime) {
                return lunchEnd + prefixTime;
            } else if (reminderTime >= dinnerStart - prefixTime
                    && reminderTime <= dinnerEnd + prefixTime) {
                return dinnerEnd + prefixTime;
            }
        }

        // Check DND also here
        return reminderTime;
    }

    /**
     * Returns today's target
     * @return
     */
    public double getTodayTarget(){
        double target;
        Cursor targetCursor = context.getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{Utility.getInstance().getToday() + ""}, null);
        targetCursor.moveToFirst();
        target = targetCursor.getDouble(0);
        return target;
    }

    public List<Map<String, String>> getTargetStatus(int noOfDays) {
        List<Map<String, String>> targets = null;


        return targets;
    }
}
