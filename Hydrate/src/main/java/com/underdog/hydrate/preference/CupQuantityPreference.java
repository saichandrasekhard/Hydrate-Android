package com.underdog.hydrate.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.underdog.hydrate.R;
import com.underdog.hydrate.async.TargetIntervalCorrelationAsync;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Log;

import java.util.ArrayList;

public class CupQuantityPreference extends ListPreference {

    private SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(getContext());

    public CupQuantityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEntriesForList(findIndexOfValue(preferences.getString(getContext()
                .getString(R.string.key_glass_quantity), null)));

    }

    public void changeUnits() {
        int index = findIndexOfValue(preferences.getString(getContext()
                        .getString(R.string.glass_quantity),
                getContext().getString(R.string.default_glass_ml)));
        setEntriesForList(index);
    }

    public void setSummary() {
        String metric = preferences.getString(
                getContext().getString(R.string.key_metric), getContext()
                        .getString(R.string.milliliter));
        String value = preferences.getString(
                getContext().getString(R.string.key_glass_quantity),
                getContext().getString(R.string.default_glass_ml));

        if (metric.equals(getContext().getString(R.string.milliliter))) {
            setSummary(value + "" + getContext().getString(R.string.ml));
        } else {
            setSummary(value + " " + getContext().getString(R.string.oz));
        }
    }

    private void setEntriesForList(int index) {
        Log.d(this.getClass().toString(), index + "");
        Cursor cursor;
        Double[] cups;
        CharSequence[] cupsInChar;

        // Fetch quantities from DB
        cursor = getContext().getContentResolver().query(
                HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS,
                new String[]{HydrateDatabase.COLUMN_QUANTITY}, null, null,
                null);

        cups = new Double[cursor.getCount()];

        for (int counter = 0; counter < cursor.getCount(); counter++) {
            cursor.moveToNext();
            cups[counter] = cursor.getDouble(0);
        }

        // Arrays.sort(cups);
        cupsInChar = new CharSequence[cups.length];
        for (int counter = 0; counter < cups.length; counter++) {
            cupsInChar[counter] = String.valueOf(cups[counter].intValue());
        }
        setEntries(cupsInChar);
        setEntryValues(cupsInChar);
        if (index > -1) {
            setValueIndex(index);
        } else {
            setValueIndex(3);
        }
        setSummary();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            ArrayList<Integer> daysSelected = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                daysSelected.add(i);
            }
            new TargetIntervalCorrelationAsync(getContext(), daysSelected).execute(HydrateDatabase.COLUMN_TARGET_QUANTITY);
        }
    }
}
