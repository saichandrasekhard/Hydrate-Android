package com.underdog.hydrate.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.underdog.hydrate.R;
import com.underdog.hydrate.util.Log;

public class SummaryListPreference extends ListPreference {

	private SharedPreferences preferences = PreferenceManager
			.getDefaultSharedPreferences(getContext());

	private String glassKey = getContext().getString(
			R.string.key_glass_quantity);

	private String metricKey = getContext().getString(R.string.key_metric);

	private String snoozeKey = getContext().getString(
			R.string.key_reminder_snooze_interval);

	private String key;

	private String configuredValue;
	private boolean reminders;

	public SummaryListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		key = this.getKey();

		Log.d("SummaryListPreference", "key - " + key);
		if (key.equals(snoozeKey)) {
			// Get value to set summary
			configuredValue = preferences.getString(snoozeKey, null);
			reminders = preferences.getBoolean(
					getContext().getString(R.string.reminders_status), false);
			this.setEnabled(reminders);
		} else if (key.equals(metricKey)) {
			// Get value to set summary
			configuredValue = preferences.getString(metricKey, null);
		} else if (key.equalsIgnoreCase(glassKey)) {

			configuredValue = preferences.getString(metricKey, null);
			if (configuredValue != null) {
				Log.d(this.getClass().toString(), configuredValue);
				if (configuredValue.equals(context.getString(R.string.us_oz))) {
					setUsOz();
				} else {
					setMl();
				}
			}

		}

		if (configuredValue != null && !key.equals(glassKey)) {
			setSummary();
		}

	}

	@Override
	protected void onDialogClosed(boolean okToSave) {
		super.onDialogClosed(okToSave);
		if (okToSave) {

			if (key.equalsIgnoreCase(glassKey)) {
				configuredValue = preferences.getString(glassKey,
						configuredValue);
				if (preferences.getString(metricKey, null).equals(
						getContext().getString(R.string.milliliter))) {
					configuredValue += "ml";
				} else {
					configuredValue += " US oz";
				}
			} else if (key.equals(snoozeKey)) {
				configuredValue = preferences.getString(snoozeKey,
						configuredValue);
			} else {
				configuredValue = preferences.getString(metricKey,
						configuredValue);
			}
			setSummary();
		}
	}

	private void setSummary() {
		if (key.equals(snoozeKey)) {
			this.setSummary(configuredValue + " "
					+ getContext().getString(R.string.mins));
		} else {
			this.setSummary(configuredValue);
		}
	}

	/**
	 * Method to set glass quantity preference
	 */
	public void setMl() {
		setEntries(R.array.glass_options_ml);
		setEntryValues(R.array.glass_options_ml);
		int index = findIndexOfValue(preferences.getString(glassKey, null));
		setValueIndex(index);
		setSummary(getValue() + "ml");

	}

	/*public void changeToMl() {
		int index = findIndexOfValue(preferences.getString(glassKey, null));

		setEntries(R.array.glass_options_ml);
		setEntryValues(R.array.glass_options_ml);
		setValueIndex(index);
		setSummary(getValue() + "ml");
	}*/

	/**
	 * Method to set glass quantity preference
	 */
	public void setUsOz() {
		setEntries(R.array.glass_options_oz);
		setEntryValues(R.array.glass_options_oz);
		int index = findIndexOfValue(preferences.getString(glassKey, null));
		setValueIndex(index);
		setSummary(getValue() + " US oz");
	}

	/*public void changeToUsOz() {
		Log.d(this.getClass().toString(), preferences.getString(glassKey, null));
		int index = findIndexOfValue(preferences.getString(glassKey, null));
		Log.d(this.getClass().toString(), index + "");
		setEntries(R.array.glass_options_oz);
		setEntryValues(R.array.glass_options_oz);
		setValueIndex(index);
		setSummary(getValue() + " US oz");
	}*/
}
