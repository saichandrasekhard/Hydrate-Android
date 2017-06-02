package com.underdog.hydrate.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.Toast;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.fragments.DayScheduleDialog;
import com.underdog.hydrate.util.Utility;

public class TargetQuantityEditTextPreference extends Preference {
    SharedPreferences defaultPreferences = PreferenceManager
            .getDefaultSharedPreferences(getContext());

    private String metric;

    public TargetQuantityEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTitle();
        setValue();

        setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                DayScheduleDialog dayScheduleDialog = new DayScheduleDialog();
                Bundle bundle;
                final AppCompatActivity activity = (AppCompatActivity) getContext();

                bundle = new Bundle();
                bundle.putString(Constants.JUST_ANOTHER_KEY, getKey());
                dayScheduleDialog.setArguments(bundle);
                dayScheduleDialog.show(activity.getSupportFragmentManager()
                        .beginTransaction(), DayScheduleDialog.class
                        .getSimpleName());
                return false;
            }
        });
    }

    public void setValue() {
        Cursor targetCursor;
        String value;
        targetCursor = getContext().getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{Utility.getInstance().getToday() + ""}, null);
        targetCursor.moveToFirst();
        value = targetCursor.getDouble(0) + "";
        setSummary(value);
    }

    public void setSummary(String value) {
        metric = defaultPreferences.getString(
                getContext().getString(R.string.key_metric), getContext()
                        .getString(R.string.milliliter));
        if (metric.equals(getContext().getString(R.string.milliliter))) {
            if (value == null || value.equals("")) {
                value = getContext().getString(R.string.defaultTarget);
                Toast.makeText(getContext(),
                        getContext().getString(R.string.validQuantity),
                        Toast.LENGTH_SHORT).show();
            } else {
                value = String.valueOf(Double.valueOf(value) / 1000d);
            }
            super.setSummary(value + " " + getContext().getString(R.string.liters));
        } else {
            if (value == null || value.equals("")) {
                value = getContext().getString(R.string.defaultTargetOz);
                Toast.makeText(getContext(),
                        getContext().getString(R.string.validQuantity),
                        Toast.LENGTH_SHORT).show();
            }
            super.setSummary(value + " " + getContext().getString(R.string.oz));
        }
    }

    public void setTitle() {
        metric = defaultPreferences.getString(
                getContext().getString(R.string.key_metric), getContext()
                        .getString(R.string.milliliter));
        if (metric.equals(getContext().getString(R.string.milliliter))) {
            super.setTitle(R.string.targetQuantityTitleMl);
        } else {
            super.setTitle(R.string.targetQuantityTitleOz);
        }
    }

}
