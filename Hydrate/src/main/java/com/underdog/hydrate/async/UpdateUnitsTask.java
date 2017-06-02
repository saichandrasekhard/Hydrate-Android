package com.underdog.hydrate.async;

import android.app.Activity;
import android.os.AsyncTask;

import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.preference.CupQuantityPreference;
import com.underdog.hydrate.preference.TargetQuantityEditTextPreference;

public class UpdateUnitsTask extends AsyncTask<String, String, Object[]> {

    private String convertTo;
    private Activity context;
    private CupQuantityPreference cupQuantityPreference;
    private TargetQuantityEditTextPreference quantityEditTextPreference;

    public UpdateUnitsTask(String convertTo, Activity context,
                           CupQuantityPreference cupQuantityPreference, TargetQuantityEditTextPreference quantityEditTextPreference) {
        this.convertTo = convertTo;
        this.context = context;
        this.cupQuantityPreference = cupQuantityPreference;
        this.quantityEditTextPreference = quantityEditTextPreference;
    }

    @Override
    protected Object[] doInBackground(String... arg0) {
        if (convertTo.equals(HydrateDatabase.UPDATE_LOG_UNITS_TO_ML)) {
            context.getContentResolver().update(
                    HydrateContentProvider.CONTENT_URI_UPDATE_UNITS, null,
                    HydrateDatabase.UPDATE_LOG_UNITS_TO_ML, null);
        } else if (convertTo.equals(HydrateDatabase.UPDATE_UNITS_TO_OZ_US)) {
            context.getContentResolver().update(
                    HydrateContentProvider.CONTENT_URI_UPDATE_UNITS, null,
                    HydrateDatabase.UPDATE_UNITS_TO_OZ_US, null);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object[] result) {

        // Change units
        cupQuantityPreference.changeUnits();
        quantityEditTextPreference.setValue();
        quantityEditTextPreference.setTitle();
        super.onPostExecute(result);
    }

}
