package com.underdog.hydrate.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.util.DateUtil;
import com.underdog.hydrate.util.Log;

public class HydrateDAO {

    private static HydrateDAO hydrateDAO;
    private final String TAG = "HydrateDAO";

    private HydrateDAO() {
    }

    private HydrateDAO(Context context) {

    }

    public static HydrateDAO getInstance() {
        if (hydrateDAO == null) {
            synchronized (HydrateDAO.class) {
                if (hydrateDAO == null) {
                    hydrateDAO = new HydrateDAO();
                }
            }
        }
        return hydrateDAO;
    }


    /**
     * Log a new entry in database
     *
     * @param timestamp time at which user drank water
     * @param quantity  quantity of water consumed
     */
    public void addWater(long timestamp, String quantity, Context context) {
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
    public void addDefaultWater(Context context) {
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
    public void deleteWater(Context context) {
        context.getContentResolver().delete(
                HydrateContentProvider.CONTENT_URI_HYDRATE_DELETE_LAST, null,
                null);
    }

    public void deleteWaterById(long rowId, Context context) {
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
    public void updateEvent(long rowId, long timeStamp, double quantity, Context context) {
        ContentValues values = new ContentValues();
        values.put(HydrateDatabase.COLUMN_QUANTITY, quantity);
        values.put(HydrateDatabase.COLUMN_TIMESTAMP, timeStamp);
        context.getContentResolver().update(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, values,
                HydrateDatabase.ROW_ID + "=?",
                new String[]{String.valueOf(rowId)});
    }

    public long checkDnd(long reminderTime, Context context) {
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
        DateUtil utility;

        if (lunchAndDinner) {
            utility = DateUtil.getInstance();
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
            cursor.close();
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

    public double getTargetFromTargetTable(Context context, long timeInMillis) {
        double target = -1;
        Cursor targetCursor = context
                .getContentResolver()
                .query(HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET,
                        new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY},
                        HydrateDatabase.COLUMN_DATE + "=?",
                        new String[]{DateUtil.getInstance().getSqliteDate(
                                timeInMillis)},
                        null);
        if (targetCursor != null && targetCursor.getCount() > 0) {
            targetCursor.moveToFirst();
            target = targetCursor.getDouble(0);
            targetCursor.close();
        }
        return target;
    }

    public double getTargetFromDS(Context context, long timeInMillis) {
        double target = -1;
        Cursor targetCursor = context.getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{DateUtil.getInstance().getDay(timeInMillis) + ""}, null);
        if (targetCursor != null && targetCursor.getCount() > 0) {
            targetCursor.moveToFirst();
            target = targetCursor.getDouble(0);
            targetCursor.close();
        }
        return target;
    }

    public double getTargetForDay(Context context, long timeInMillis) {
        double target = getTargetFromTargetTable(context, timeInMillis);
        if (target == -1) {
            target = getTargetFromDS(context, timeInMillis);
        }
        return target;
    }

    /**
     * Returns today's target
     *
     * @return
     */
    public double getTodayTarget(Context context) {
        return getTargetFromDS(context, System.currentTimeMillis());
    }


    /**
     * Update the target status at the end of the day
     */
    public void updateTargetStatus(Context context) {
        long timestamp = System.currentTimeMillis();
        String[] selectionArgs;
        double todaysConsumption;
        double dailyTarget;

        ContentValues values;

        // Subtract 24hours to make sure we are calculating for the same day
        // since this method gets called at 11:59:59 PM in inexact fashion
        timestamp -= 86400000l;
        selectionArgs = DateUtil.getInstance().getSelectionArgsForDay(timestamp);

        Cursor cursor = context.getContentResolver().query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                new String[]{"sum(quantity) AS sum"},
                HydrateDatabase.FROM_TO_TIME, selectionArgs, null);

        cursor.moveToFirst();
        todaysConsumption = cursor.getDouble(0);
        cursor.close();
        Log.d(this.getClass().toString(), "consumption - " + todaysConsumption);
        Cursor targetCursor = context.getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{DateUtil.getInstance().getDay(timestamp) + ""}, null);
        targetCursor.moveToFirst();
        dailyTarget = targetCursor.getDouble(0);
        targetCursor.close();

        values = new ContentValues();
        if (todaysConsumption >= dailyTarget) {
            values.put(HydrateDatabase.COLUMN_REACHED, 1);
        } else {
            values.put(HydrateDatabase.COLUMN_REACHED, 0);
        }
        values.put(HydrateDatabase.COLUMN_TARGET_QUANTITY, dailyTarget);
        values.put(HydrateDatabase.COLUMN_CONSUMED_QUANTITY, todaysConsumption);
        values.put(HydrateDatabase.COLUMN_DATE,
                DateUtil.getInstance().getSqliteDate(timestamp));
        context.getContentResolver().insert(
                HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET, values);
    }

    public void syncTargets(Context context) {
        String lastEntryDate = getLastTargetTableEntry(context);
        String todaysDate = DateUtil.getInstance().getSqliteDate(System.currentTimeMillis());

        if (lastEntryDate.equals(todaysDate)) {
            // Already entry made, just update consumed status for today
        } else {
            // Starting from the lastEntryDate, keep making entries in target table for each day until today
            long lastEntryDateInMillis = DateUtil.getInstance().getTimeFromSqliteDate(lastEntryDate);
            long todayDateInMillis = DateUtil.getInstance().getTimeFromSqliteDate(todaysDate);
            long startAt = lastEntryDateInMillis;
            while (startAt < todayDateInMillis) {

                Log.i(TAG, "startAt - " + startAt + ":::" + todayDateInMillis);
                startAt += Constants.DAY_HOURS_LONG;
            }
        }
    }

    public String getLastTargetTableEntry(Context context) {
        String date = null;
        Uri uri = HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET.buildUpon().appendQueryParameter(HydrateDatabase.LIMIT, "1").build();
        Cursor cursor = context.getContentResolver().query(uri, new String[]{HydrateDatabase.COLUMN_DATE}, null, null, " target_id DESC ");
        cursor.moveToFirst();
        date = cursor.getString(0);
        Log.d(TAG, "date - " + date);
        cursor.close();
        return date;
    }

    public boolean applyInitialSetupChanges(double target, int startHour, int startMin, int endHour, int endMin, int interval, boolean isML, Context context) {
        HydrateDatabase hydrateDatabase;
        SQLiteDatabase database = null;
        ContentValues contentValues = null;
        try {
            hydrateDatabase = new HydrateDatabase(context);
            database = hydrateDatabase.getWritableDatabase();
            database.beginTransaction();
            contentValues = new ContentValues();
            contentValues.put(HydrateDatabase.COLUMN_TARGET_QUANTITY, target);
            contentValues.put(HydrateDatabase.REMINDER_START_TIME, startHour * 60 + startMin);
            contentValues.put(HydrateDatabase.REMINDER_END_TIME, endHour * 60 + endMin);
            contentValues.put(HydrateDatabase.REMINDER_INTERVAL, interval);
            database.update(HydrateContentProvider.HYDRATE_DAILY_SCHEDULE, contentValues, null, null);
            database.delete(HydrateDatabase.HYDRATE_CUPS, null, null);
            if (isML) {
                for (String cup : HydrateDatabase.cups_ml) {
                    ContentValues values = new ContentValues();
                    values.put(HydrateDatabase.COLUMN_QUANTITY, cup);
                    database.insert(HydrateDatabase.HYDRATE_CUPS, null, values);
                }
            } else {
                for (String cup : HydrateDatabase.cups_oz) {
                    ContentValues values = new ContentValues();
                    values.put(HydrateDatabase.COLUMN_QUANTITY, cup);
                    database.insert(HydrateDatabase.HYDRATE_CUPS, null, values);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Exception - ", e);
            return false;
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
        }
        return true;
    }
}
