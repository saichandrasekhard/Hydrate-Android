package com.underdog.hydrate.preference;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import com.underdog.hydrate.R;

public class SharePreference extends Preference {

	public SharePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(
						Intent.EXTRA_TEXT,
						getContext().getString(R.string.checkout_message)
								+ "\n"
								+ getContext().getString(R.string.checkout));
				intent.putExtra(Intent.EXTRA_SUBJECT, "Hydrate");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					getContext().startActivity(
							Intent.createChooser(intent, getContext()
									.getString(R.string.select_app)));
				} catch (android.content.ActivityNotFoundException ex) {
					ex.printStackTrace();
				}
				return true;
			}
		});
	}

}
