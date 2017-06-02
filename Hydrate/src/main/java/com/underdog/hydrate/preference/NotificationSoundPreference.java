package com.underdog.hydrate.preference;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.AttributeSet;

import com.underdog.hydrate.R;


public class NotificationSoundPreference extends RingtonePreference {
    public NotificationSoundPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Uri ringtoneUri = Uri.parse(preferences.getString(context.getString(R.string.key_notification_sound), "DEFAULT_NOTIFICATION_URI"));
        if (ringtoneUri != null)
            setSummary(ringtoneUri);
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        super.onSaveRingtone(ringtoneUri);
        setSummary(ringtoneUri);
    }

    private void setSummary(Uri ringtoneUri) {
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
        String name = ringtone.getTitle(getContext());
        setSummary(name);
    }
}
