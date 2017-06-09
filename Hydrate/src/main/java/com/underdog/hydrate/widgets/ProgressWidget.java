package com.underdog.hydrate.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;

import com.underdog.hydrate.R;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Utility;

import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 */
public class ProgressWidget extends AppWidgetProvider implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Loader for Drink summary
    private static final int DRINK_SUMMARY_LOADER_ID = 2;
    SimpleCursorAdapter cursorAdapter;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.progress_widget);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String metric = preferences.getString(context.getString(R.string.key_metric),
                context.getString(R.string.milliliter));
        String milliliter = context.getString(R.string.milliliter);

        String[] selectionArgs = Utility.getInstance()
                .getSelectionArgsForDay(Calendar.getInstance().getTimeInMillis());

        Cursor consumedCursor = context.getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                HydrateDatabase.FROM_TO_TIME,
                selectionArgs, null);
        consumedCursor.moveToFirst();
        int count = consumedCursor.getInt(0);
        double quantityConsumed = consumedCursor.getFloat(1);
        consumedCursor.close();

        Cursor targetCursor = context.getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{Utility.getInstance().getToday() + ""}, null);
        targetCursor.moveToFirst();
        double target = targetCursor.getDouble(0);
        targetCursor.close();

        String dailyTarget = null;
        if (metric.equals(milliliter)) {
            target /= 1000;
            target *= 100;
            target = Math.round(target);
            target /= 100;
            dailyTarget = String.valueOf(target);

            quantityConsumed = quantityConsumed / 1000;
            quantityConsumed *= 100;
            quantityConsumed = Math.round(quantityConsumed);
            quantityConsumed /= 100;
            views.setTextViewText(R.id.water_quantity_unit, context.getString(R.string.liters));
        } else {
            target = Math.round(target);
            dailyTarget = String.valueOf((int) target);

            quantityConsumed = Math.round(quantityConsumed);
            views.setTextViewText(R.id.water_quantity_unit, context.getString(R.string.oz));
        }

        int color = -1;
        if (quantityConsumed < (target * .75)) {
            color = ContextCompat.getColor(context, R.color.danger);
        } else if (quantityConsumed < target) {
            color = ContextCompat.getColor(context, R.color.safe);
        } else {
            color = ContextCompat.getColor(context, R.color.success);
        }

        int lighterColor = Color.argb(Color.alpha(color) / 5, Color.red(color), Color.green(color), Color.blue(color));

        int to = (int) ((quantityConsumed / target) * 100);

        views.setProgressBar(R.id.waterProgress, 100, to, false);

        views.setTextViewText(R.id.water_target, dailyTarget);
        views.setTextViewText(R.id.water_quantity_status, quantityConsumed + "");
        views.setTextViewText(R.id.water_status, count + "");
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

