package com.underdog.hydrate.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.underdog.hydrate.util.Utility;

public class FeedBackAndBugReportPreference extends Preference {

    public FeedBackAndBugReportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        final String key = this.getKey();

        setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utility.getInstance().launchFeedbackActivity(getContext(), key);
                return true;
            }
        });

    }

}
