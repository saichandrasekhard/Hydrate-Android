package com.underdog.hydrate.async;

import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;

import com.underdog.hydrate.R;
import com.underdog.hydrate.adapter.SummaryArrayAdapter;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Utility;

public class SummaryAsyncTask extends AsyncTask<String, String, Object[]> {
    private Activity context;

    public SummaryAsyncTask(Activity context) {
        this.context = context;
    }

    @Override
    protected Object[] doInBackground(String... arg0) {
        Object[] values;
        HashMap<String, String> last3Values;
        HashMap<String, String> lastWeekValues;
        HashMap<String, String> lastFifteenValues;
        HashMap<String, String> last30Values;
        HashMap<String, String> last90Values;
        HashMap<String, String> last180Values;
        HashMap<String, String> last365Values;
        double quantity;
        int sinceFirstUse;
        int divider;
        Cursor cursor;

        ContentResolver contentResolver = context.getContentResolver();

        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                new String[]{"min(timestamp) as min"}, null, null, null);
        cursor.moveToFirst();
        sinceFirstUse = Utility.getInstance().getDaysSince(cursor.getLong(cursor
                .getColumnIndex("min")));
        cursor.close();

        // Get values for last 3 days
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(3), null);

        last3Values = new HashMap<>();
        last3Values.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.last3));

        cursor.moveToFirst();
        divider = 3 > sinceFirstUse ? sinceFirstUse : 3;
        last3Values.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        last3Values.put(Constants.SUMMARY_QUANTITY, String.valueOf(quantity));

        cursor.close();

        // Get values for last week
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(7), null);

        lastWeekValues = new HashMap<>();
        lastWeekValues.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.lastWeek));

        cursor.moveToFirst();
        divider = 7 > sinceFirstUse ? sinceFirstUse : 7;
        lastWeekValues.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        lastWeekValues
                .put(Constants.SUMMARY_QUANTITY, String.valueOf(quantity));

        cursor.close();

        // Get values for last 15 days
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(15), null);

        lastFifteenValues = new HashMap<>();
        lastFifteenValues.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.lastFifteen));

        cursor.moveToFirst();
        divider = 15 > sinceFirstUse ? sinceFirstUse : 15;

        lastFifteenValues.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        lastFifteenValues.put(Constants.SUMMARY_QUANTITY,
                String.valueOf(quantity));

        cursor.close();

        // Get values for last 30 days
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(30), null);

        last30Values = new HashMap<>();
        last30Values.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.lastMonth));

        cursor.moveToFirst();
        divider = 30 > sinceFirstUse ? sinceFirstUse : 30;
        last30Values.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        last30Values.put(Constants.SUMMARY_QUANTITY, String.valueOf(quantity));

        cursor.close();

        // Get values for last 90 days
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(90), null);

        last90Values = new HashMap<>();
        last90Values.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.last90));

        cursor.moveToFirst();
        divider = 90 > sinceFirstUse ? sinceFirstUse : 90;
        last90Values.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        last90Values.put(Constants.SUMMARY_QUANTITY, String.valueOf(quantity));

        cursor.close();

        // Get values for last 180 days
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(180), null);

        last180Values = new HashMap<>();
        last180Values.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.last180));

        cursor.moveToFirst();
        divider = 180 > sinceFirstUse ? sinceFirstUse : 180;
        last180Values.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        last180Values.put(Constants.SUMMARY_QUANTITY, String.valueOf(quantity));

        cursor.close();

        // Get values for last 1 year
        cursor = contentResolver.query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME, getSelectionArgs(360), null);

        last365Values = new HashMap<>();
        last365Values.put(Constants.SUMMARY_HEADING,
                context.getString(R.string.last360));

        cursor.moveToFirst();
        divider = 360 > sinceFirstUse ? sinceFirstUse : 360;
        last365Values.put(Constants.SUMMARY_CUPS,
                String.valueOf(cursor.getInt(0) / divider));

        quantity = cursor.getDouble(1);
        quantity /= divider;
        last365Values.put(Constants.SUMMARY_QUANTITY, String.valueOf(quantity));

        cursor.close();

        values = new Object[7];
        values[0] = last3Values;
        values[1] = lastWeekValues;
        values[2] = lastFifteenValues;
        values[3] = last30Values;
        values[4] = last90Values;
        values[5] = last180Values;
        values[6] = last365Values;

        // put regularity values into the maps
        last3Values.put(Constants.REGULARITY,
                String.valueOf(getRegularity(3, sinceFirstUse)));
        lastWeekValues.put(Constants.REGULARITY,
                String.valueOf(getRegularity(7, sinceFirstUse)));
        lastFifteenValues.put(Constants.REGULARITY,
                String.valueOf(getRegularity(15, sinceFirstUse)));
        last30Values.put(Constants.REGULARITY,
                String.valueOf(getRegularity(30, sinceFirstUse)));
        last90Values.put(Constants.REGULARITY,
                String.valueOf(getRegularity(90, sinceFirstUse)));
        last180Values.put(Constants.REGULARITY,
                String.valueOf(getRegularity(180, sinceFirstUse)));
        last365Values.put(Constants.REGULARITY,
                String.valueOf(getRegularity(360, sinceFirstUse)));

        return values;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        ListView summaryListView;
        SummaryArrayAdapter summaryArrayAdapter;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Activity activity = context;

        summaryListView = (ListView) activity
                .findViewById(R.id.summaryListView);
        summaryListView.setClickable(false);

        summaryArrayAdapter = new SummaryArrayAdapter(activity,
                R.layout.summary_list_view, result, preferences.getString(
                context.getString(R.string.key_metric),
                context.getString(R.string.milliliter)));

        // Assign the array adaptor for the list view
        summaryListView.setAdapter(summaryArrayAdapter);
        super.onPostExecute(result);
    }

    private String[] getSelectionArgs(long period) {
        String[] selectionArgs;
        Calendar calendar;
        long toTime = System.currentTimeMillis();
        long fromTime;
        fromTime = getFromTime(period, toTime);
        selectionArgs = new String[2];
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(fromTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        selectionArgs[0] = String.valueOf(calendar.getTimeInMillis());
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(toTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        selectionArgs[1] = String.valueOf(calendar.getTimeInMillis());

        return selectionArgs;
    }

    private long getFromTime(long period, long toTime) {
        long fromTime = toTime;
        fromTime -= (period * 86400000l);
        return fromTime;
    }

    private double getRegularity(long period, int sinceFirstUse) {
        double percentage;
        Cursor cursor;
        int targetReachedCount;
        long divider;

        cursor = context.getContentResolver().query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET,
                new String[]{"count(*) AS count"},
                "date >= ? and reached=1",
                new String[]{Utility.getInstance().getSqliteDate(getFromTime(period,
                        System.currentTimeMillis()))}, null);

        cursor.moveToFirst();
        targetReachedCount = cursor.getInt(0);
        if (targetReachedCount < period) {
            divider = period > sinceFirstUse ? sinceFirstUse : period;
            percentage = ((targetReachedCount * 100d) / divider);
        } else {
            percentage = 100;
        }
        return percentage;
    }

}
