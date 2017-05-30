package com.underdog.hydrate.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.underdog.hydrate.BlankShareActivity;
import com.underdog.hydrate.MainActivity;
import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.util.Log;

public class OtherNotificationService extends IntentService {

    public OtherNotificationService() {
        super("OtherNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notificationId = intent.getIntExtra(Constants.KEY_NOTIFICATION_ID,
                Constants.NOTIFICATION_TARGET_ACHIEVED_ID);
        NotificationManager notificationManager;
        NotificationCompat.Builder builder;
        PendingIntent mainActivityPendingIntent;
        Intent mainActivityIntent;
        PendingIntent shareActivity;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String userName = preferences.getString(
                getString(R.string.key_user_name), null);

        if (notificationId == Constants.NOTIFICATION_TARGET_ACHIEVED_ID) {
            Log.d(this.getClass().getSimpleName(),
                    "In otherNotification service");
            String notificationMessage = getString(R.string.target_achieved_message);

            if (userName != null && !userName.equals("")) {
                notificationMessage = notificationMessage.replace(
                        getString(R.string.user_name_default), userName);
            }

            mainActivityIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            mainActivityPendingIntent = PendingIntent.getActivity(this, 1,
                    mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            shareActivity = PendingIntent.getActivity(this, 0, new Intent(this,
                            BlankShareActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                    | Intent.FLAG_ACTIVITY_NO_HISTORY),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Uri ringtoneUri = Uri.parse(preferences.getString(getString(R.string.key_notification_sound), getString(R.string.default_notification_sound)));

            builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_notify_goal)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(notificationMessage)
                    .setColor(getResources().getColor(R.color.white))
                    .setAutoCancel(true)
                    .setSound(ringtoneUri)
                    .setContentIntent(mainActivityPendingIntent)
                    .addAction(android.R.drawable.ic_menu_share,
                            getString(R.string.share), shareActivity)
                    .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText(notificationMessage));

            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, builder.build());
        }
        Log.d(getClass().getSimpleName(), "Notified");
    }

}
