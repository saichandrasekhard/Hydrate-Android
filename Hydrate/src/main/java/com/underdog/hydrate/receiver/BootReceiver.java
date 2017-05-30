package com.underdog.hydrate.receiver;

import com.underdog.hydrate.R;
import com.underdog.hydrate.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	AlarmReceiver alarmReceiver = new AlarmReceiver();

	private static final String tag = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(tag, "In BootReceiver");
		SharedPreferences preferences;
		boolean reminderStatus;

		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

			// Start daily reset alarm
			alarmReceiver.resetAlarms(context);

			// Start hydrate reminders (if enabled)
			preferences = PreferenceManager
					.getDefaultSharedPreferences(context);

			reminderStatus = preferences.getBoolean(
					context.getString(R.string.reminders_status), false);

			Log.d(tag, "Reminders are enabled - " + reminderStatus);

			// If reminders are enabled, set reminder alarms
			//A bug here.Everytime phone restarts, the alarms will change time.MINOR BUG
			if (reminderStatus) {
				alarmReceiver.setNextAlarm(context);
			}
			Log.d(tag, "Boot receiver job done");
		}
	}

}
