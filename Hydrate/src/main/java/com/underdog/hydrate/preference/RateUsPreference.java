package com.underdog.hydrate.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class RateUsPreference extends Preference {

	public RateUsPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://play.google.com/store/apps/details?id="
								+ getContext().getPackageName()));
				getContext().startActivity(intent);
				return false;
			}
		});
	}

}
