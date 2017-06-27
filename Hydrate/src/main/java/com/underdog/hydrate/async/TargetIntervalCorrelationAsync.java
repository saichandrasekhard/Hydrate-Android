package com.underdog.hydrate.async;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.underdog.hydrate.R;
import com.underdog.hydrate.SettingsActivity;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.preference.TargetQuantityEditTextPreference;
import com.underdog.hydrate.preference.TimePreference;
import com.underdog.hydrate.util.Log;

import java.util.ArrayList;

/**
 * To auto adjust Target and Reminder Interval
 */
public class TargetIntervalCorrelationAsync extends AsyncTask {

    Context activity;
    ArrayList<Integer> daysSelected;

    public TargetIntervalCorrelationAsync(Context activity, ArrayList<Integer> daysSelected) {
        this.activity = activity;
        this.daysSelected = daysSelected;
    }

    @Override
    protected Object doInBackground(Object[] strings) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String milliliter = activity.getString(R.string.milliliter);
        String metric = preferences.getString(activity.getString(R.string.key_metric), milliliter);
        String defaultCup;
        ContentResolver contentResolver = activity.getContentResolver();
        Cursor cursor;
        int timeAlive;
        int interval;
        ContentValues contentValues;
        double target;
        if (metric.equals(milliliter)) {
            defaultCup = preferences.getString(
                    activity.getString(R.string.key_glass_quantity), "250");
        } else {
            defaultCup = preferences.getString(
                    activity.getString(R.string.key_glass_quantity), "8.45");
        }

        if (!((strings[0]).equals(HydrateDatabase.REMINDER_INTERVAL))) {
            for (int day : daysSelected) {
                //Get Start and end Times
                cursor = contentResolver.query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY,
                                "((" + HydrateDatabase.REMINDER_END_TIME + "-" + HydrateDatabase.REMINDER_START_TIME + ")" +
                                        "-((" + HydrateDatabase.LUNCH_END + "-" + HydrateDatabase.LUNCH_START + ")" +
                                        "+(" + HydrateDatabase.DINNER_END + "-" + HydrateDatabase.DINNER_START + "))) AS time_alive"
                        },
                        HydrateDatabase.DAY + "=?", new String[]{String.valueOf(day)}, null);
                cursor.moveToFirst();
                target = cursor.getDouble(0);
                timeAlive = cursor.getInt(1);

                interval = (int) (timeAlive * (Double.valueOf(defaultCup) / target));
                contentValues = new ContentValues();
                contentValues.put(HydrateDatabase.REMINDER_INTERVAL, interval);
                contentResolver.update(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        contentValues, HydrateDatabase.DAY + "=?", new String[]{String.valueOf(day)});
            }
            ((Activity) activity).runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, R.string.auto_adjust_interval, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            for (int day : daysSelected) {
                //Get Start and end Times
                cursor = contentResolver.query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        new String[]{HydrateDatabase.REMINDER_INTERVAL,
                                "((" + HydrateDatabase.REMINDER_END_TIME + "-" + HydrateDatabase.REMINDER_START_TIME + ")" +
                                        "-((" + HydrateDatabase.LUNCH_END + "-" + HydrateDatabase.LUNCH_START + ")" +
                                        "+(" + HydrateDatabase.DINNER_END + "-" + HydrateDatabase.DINNER_START + "))) AS time_alive"
                        },
                        HydrateDatabase.DAY + "=?", new String[]{String.valueOf(day)}, null);
                cursor.moveToFirst();
                interval = cursor.getInt(0);
                timeAlive = cursor.getInt(1);

                target = (Math.round((double) timeAlive / (double) interval) * (Double.valueOf(defaultCup)));
                Log.d(getClass().getSimpleName(), "calculated target - " + target);
                contentValues = new ContentValues();
                contentValues.put(HydrateDatabase.COLUMN_TARGET_QUANTITY, target);
                contentResolver.update(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        contentValues, HydrateDatabase.DAY + "=?", new String[]{String.valueOf(day)});
            }
            ((Activity) activity).runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, R.string.auto_adjust_target, Toast.LENGTH_SHORT).show();
                }
            });
        }
        HydrateDAO.getInstance().syncTargets(activity);
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        try {
            Activity settingsActivity = (SettingsActivity) activity;
            SettingsActivity.PlaceholderFragment placeholderFragment = (SettingsActivity.PlaceholderFragment) settingsActivity.getFragmentManager()
                    .findFragmentById(R.id.settings_container);
            TimePreference timePreference = (TimePreference) placeholderFragment
                    .findPreference(settingsActivity.getString(R.string.key_reminder_interval));
            timePreference.setSummary();
            TargetQuantityEditTextPreference targetPreference = (TargetQuantityEditTextPreference) placeholderFragment
                    .findPreference(settingsActivity.getString(R.string.key_target));
            targetPreference.setValue();
        } catch (ClassCastException cce) {
            Log.e(getClass().getSimpleName(), "Exception occurred - ", cce);
        }
        super.onPostExecute(o);
    }
}
