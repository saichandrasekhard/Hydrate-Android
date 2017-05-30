package com.underdog.hydrate.preference;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class FeedBackAndBugReportPreference extends Preference {

	public FeedBackAndBugReportPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		final String key = this.getKey();

		setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
						Uri.fromParts("mailto", Constants.DEV_EMAIL, null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, key);
				emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(
						Intent.createChooser(emailIntent, getContext()
								.getString(R.string.select_app)));
				return true;
			}
		});

	}

}
