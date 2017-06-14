package com.underdog.hydrate.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import com.underdog.hydrate.R;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.BackupAndRestore;
import com.underdog.hydrate.util.Log;

public class SchedulerService extends IntentService {

    private static final String tag = "SchedulerService";

    public SchedulerService() {
        super("SchedulerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences;
        AlarmReceiver alarmReceiver;
        boolean reminders;
        HydrateDAO dao;
        boolean autoBackup;
        BackupAndRestore backupAndRestore;

        Log.d(tag, "Serving the reset intent");

        // 1.Reset reminder alarm to start time
        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        reminders = preferences.getBoolean(
                getString(R.string.key_reminders_status), false);
        if (reminders) {
            alarmReceiver = new AlarmReceiver();
            alarmReceiver.setNextAlarm(getApplicationContext());
        }

        // Add entry for daily target consumption
        HydrateDAO.getInstance().updateTargetStatus(this);

        // Auto backup to SD Card
        autoBackup = preferences.getBoolean(
                getString(R.string.key_auto_backup), false);
        if (autoBackup && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            backupAndRestore = new BackupAndRestore(getApplicationContext());
            backupAndRestore.backUpSettingsToSD();
            backupAndRestore.backupDBToSD();
        }

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

}
