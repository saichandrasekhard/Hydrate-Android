package com.underdog.hydrate.preference;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.fragments.DayScheduleDialog;
import com.underdog.hydrate.util.Utility;

public class TimePreference extends Preference {
    public final String key = this.getKey();
    private final SharedPreferences preferences = PreferenceManager
			.getDefaultSharedPreferences(getContext());

	public TimePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		boolean reminders = preferences.getBoolean(
				getContext().getString(R.string.reminders_status), true);
		if (!(key.equals(getContext().getString(R.string.key_lunch)) || key
				.equals(getContext().getString(R.string.key_dinner)))) {
			this.setEnabled(reminders);
		}
		setSummary();

		setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				DayScheduleDialog dayScheduleDialog = new DayScheduleDialog();
				Bundle bundle;
                AppCompatActivity activity = (AppCompatActivity) getContext();

				bundle = new Bundle();
				bundle.putString(Constants.JUST_ANOTHER_KEY, key);
				dayScheduleDialog.setArguments(bundle);
                dayScheduleDialog.show(activity.getSupportFragmentManager()
                        .beginTransaction(), DayScheduleDialog.class
						.getSimpleName());
				return false;
			}
		});
	}

	public void setSummary() {
		ContentResolver contentResolver = getContext().getContentResolver();
		Cursor cursor;
		int hours;
		int mins;
		int hoursEnd;
		int minsEnd;
		long time;
		long timeEnd;
		if (key.equals(getContext().getString(R.string.key_reminder_start_time))) {
			cursor = contentResolver.query(
					HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
					new String[] { HydrateDatabase.REMINDER_START_TIME },
					HydrateDatabase.DAY + "=?",
					new String[] { String.valueOf(Utility.getInstance().getToday()) },
					null);
			cursor.moveToFirst();
			time = cursor.getLong(0);
			hours = (int) (time / 60);
			mins = (int) (time % 60);
			cursor.close();
			setSummary(hours + ":" + ((mins < 10) ? "0" + mins : mins));
		} else if (key.equals(getContext().getString(
				R.string.key_reminder_end_time))) {
			cursor = contentResolver.query(
					HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
					new String[] { HydrateDatabase.REMINDER_END_TIME },
					HydrateDatabase.DAY + "=?",
					new String[] { String.valueOf(Utility.getInstance().getToday()) },
					null);
			cursor.moveToFirst();
			time = cursor.getLong(0);
			hours = (int) (time / 60);
			mins = (int) (time % 60);
			cursor.close();
			setSummary(hours + ":" + ((mins < 10) ? "0" + mins : mins));
		} else if (key.equals(getContext().getString(
				R.string.key_reminder_interval))) {
			cursor = contentResolver.query(
					HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
					new String[] { HydrateDatabase.REMINDER_INTERVAL },
					HydrateDatabase.DAY + "=?",
					new String[] { String.valueOf(Utility.getInstance().getToday()) },
					null);
			cursor.moveToFirst();
			time = cursor.getLong(0);
			cursor.close();
			setSummary(time + " " + getContext().getString(R.string.mins));
		} else if (key.equals(getContext().getString(R.string.key_lunch))) {
			cursor = contentResolver.query(
					HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
					new String[] { HydrateDatabase.LUNCH_START,
							HydrateDatabase.LUNCH_END }, HydrateDatabase.DAY
							+ "=?",
					new String[] { String.valueOf(Utility.getInstance().getToday()) },
					null);
			cursor.moveToFirst();
			time = cursor.getLong(0);
			timeEnd = cursor.getLong(1);
			hours = (int) (time / 60);
			mins = (int) (time % 60);
			hoursEnd = (int) (timeEnd / 60);
			minsEnd = (int) (timeEnd % 60);
			cursor.close();
			setSummary(hours + ":" + ((mins < 10) ? "0" + mins : mins) + " - "
					+ hoursEnd + ":"
					+ ((minsEnd < 10) ? "0" + minsEnd : minsEnd));

		} else if (key.equals(getContext().getString(R.string.key_dinner))) {
			cursor = contentResolver.query(
					HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
					new String[] { HydrateDatabase.DINNER_START,
							HydrateDatabase.DINNER_END }, HydrateDatabase.DAY
							+ "=?",
					new String[] { String.valueOf(Utility.getInstance().getToday()) },
					null);
			cursor.moveToFirst();
			time = cursor.getLong(0);
			timeEnd = cursor.getLong(1);
			hours = (int) (time / 60);
			mins = (int) (time % 60);
			hoursEnd = (int) (timeEnd / 60);
			minsEnd = (int) (timeEnd % 60);
			cursor.close();
			setSummary(hours + ":" + ((mins < 10) ? "0" + mins : mins) + " - "
					+ hoursEnd + ":"
					+ ((minsEnd < 10) ? "0" + minsEnd : minsEnd));
		}
	}
}
