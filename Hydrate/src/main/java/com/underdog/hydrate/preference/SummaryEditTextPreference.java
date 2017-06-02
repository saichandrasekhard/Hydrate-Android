package com.underdog.hydrate.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.underdog.hydrate.R;

public class SummaryEditTextPreference extends EditTextPreference {

	public SummaryEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		SharedPreferences defaultPreferences;
		String value;

		defaultPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		value = defaultPreferences.getString(
				context.getString(R.string.key_user_name), null);
		if (value != null) {
			this.setSummary(value);
		}

	}

}
