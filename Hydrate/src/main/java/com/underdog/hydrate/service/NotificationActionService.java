package com.underdog.hydrate.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.Log;
import com.underdog.hydrate.util.Utility;

public class NotificationActionService extends IntentService {

    public NotificationActionService() {
        super("Notification action service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action;
        AlarmReceiver alarmReceiver;
        Cursor cursor;
        String metric;
        String milliliter = getString(R.string.milliliter);
        double consumption = 0;
        double target;
        Intent otherNotificationIntent;
        double defaultQuantity;
        SharedPreferences preferences;
        int notificationId = Constants.NOTIFICATION_ID;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        action = intent.getAction();
        alarmReceiver = new AlarmReceiver();
        if (action.equalsIgnoreCase(Constants.NOTIFICATION_ACTION_DRINK)) {
            alarmReceiver.setNextAlarm(this);

            // Check if target achieved and show notification
            cursor = getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                    new String[]{HydrateDatabase.SUM_QUANTITY},
                    HydrateDatabase.FROM_TO_TIME,
                    Utility.getInstance().getSelectionArgsForDay(System
                            .currentTimeMillis()), null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                consumption = cursor.getDouble(0);
                cursor.close();
            }

            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            metric = preferences.getString(getString(R.string.key_metric),
                    milliliter);
            if (metric.equals(milliliter)) {
                defaultQuantity = Double.valueOf(preferences.getString(
                        getString(R.string.key_glass_quantity),
                        getString(R.string.default_glass_ml)));
            } else {
                defaultQuantity = Double.valueOf(preferences.getString(
                        getString(R.string.key_glass_quantity),
                        getString(R.string.default_glass_us_oz)));
            }
            HydrateDAO dao = new HydrateDAO(this);
            dao.addDefaultWater();
            Cursor targetCursor = getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                    new String[]{Utility.getInstance().getToday() + ""}, null);
            targetCursor.moveToFirst();
            target = targetCursor.getDouble(0);

            if (consumption < target && consumption + defaultQuantity >= target) {
                // Show notification
                otherNotificationIntent = new Intent(this,
                        OtherNotificationService.class);
                otherNotificationIntent.putExtra(Constants.KEY_NOTIFICATION_ID,
                        Constants.NOTIFICATION_TARGET_ACHIEVED_ID);
                startService(otherNotificationIntent);
                Log.d(getClass().getSimpleName(),
                        "Called other notification service");
            }
        } else if (action
                .equalsIgnoreCase(Constants.NOTIFICATION_ACTION_SNOOZE)) {
            alarmReceiver.snoozeAlarm(this);
        }

        notificationManager.cancel(notificationId);
    }

}
