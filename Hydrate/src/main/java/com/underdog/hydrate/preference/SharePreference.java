package com.underdog.hydrate.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.underdog.hydrate.util.Utility;

public class SharePreference extends Preference {

    public SharePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utility.getInstance().launchShareActivity(getContext());
                return true;
            }
        });
    }

}
