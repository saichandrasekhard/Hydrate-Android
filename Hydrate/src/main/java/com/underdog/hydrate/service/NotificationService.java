package com.underdog.hydrate.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.underdog.hydrate.MainActivity;
import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.Log;

public class NotificationService extends IntentService {

    private static final String tag = "NotificationService";

    public NotificationService() {
        super("Notification Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(tag, "Handling intent by setting notification");

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr;
        String notificationMessage = getString(R.string.default_notification_message);
        String userName;
        SharedPreferences preferences;
        String defaultUserName = getString(R.string.user_name_default);
        Intent resultIntent;
        PendingIntent pendingIntent;
        Intent drinkIntent;
        Intent snoozeIntent;
        String quantity;
        String metric;
        String milliliter;

        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        milliliter = getString(R.string.milliliter);
        userName = preferences.getString(getString(R.string.key_user_name),
                null);
        metric = preferences.getString(getString(R.string.key_metric),
                milliliter);

        if (metric.equals(milliliter)) {
            quantity = preferences.getString(
                    getString(R.string.key_glass_quantity), "250")
                    + " "
                    + getString(R.string.ml);
        } else {
            quantity = preferences.getString(
                    getString(R.string.key_glass_quantity), "8.45")
                    + " "
                    + getString(R.string.oz);

        }

        if (userName != null) {
            notificationMessage = notificationMessage.replace(defaultUserName,
                    userName);
        }

        resultIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up snooze and drink events
        drinkIntent = new Intent(this, NotificationActionService.class);
        drinkIntent.setAction(Constants.NOTIFICATION_ACTION_DRINK);
        PendingIntent piDismiss = PendingIntent.getService(this, 0,
                drinkIntent, 0);

        snoozeIntent = new Intent(this, NotificationActionService.class);
        snoozeIntent.setAction(Constants.NOTIFICATION_ACTION_SNOOZE);
        PendingIntent piSnooze = PendingIntent.getService(this, 0,
                snoozeIntent, 0);

        Uri ringtoneUri = Uri.parse(preferences.getString(getString(R.string.key_notification_sound), getString(R.string.default_notification_sound)));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.drawable.ic_stat_notify_glass)
                .setColor(getResources().getColor(R.color.blue2))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setSound(ringtoneUri)
                .addAction(R.drawable.ic_stat_notify_glass,
                        quantity, piDismiss)
                .addAction(R.drawable.ic_statusbar_remind_later,
                        getString(R.string.remind_later), piSnooze);
        mBuilder.setContentIntent(pendingIntent);

        // Gets an instance of the NotificationManager service
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(Constants.NOTIFICATION_ID, mBuilder.build());

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);

    }
}
