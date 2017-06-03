package com.underdog.hydrate;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.underdog.hydrate.async.UpdateUnitsTask;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.fragments.DayScheduleDialog;
import com.underdog.hydrate.fragments.DaySelectionDialog;
import com.underdog.hydrate.fragments.DaySelectionDialog.ScheduleUpdated;
import com.underdog.hydrate.fragments.LunchDinnerTimeDialog;
import com.underdog.hydrate.fragments.ReminderStartEndEditDialog;
import com.underdog.hydrate.fragments.TargetQuantityEditDialog;
import com.underdog.hydrate.preference.CupQuantityPreference;
import com.underdog.hydrate.preference.SummaryListPreference;
import com.underdog.hydrate.preference.TargetQuantityEditTextPreference;
import com.underdog.hydrate.preference.TimePreference;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.Log;

public class SettingsActivity extends AppCompatActivity implements ScheduleUpdated {

    private static final String tag = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Replace the view by settings fragment that has been defined as an inner class below
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new PlaceholderFragment())
                    .commit();
        }

    }

    @Override
    public void destroyBackStack(String key) {
        // Remove stuff from backstack
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        if (fragmentManager.findFragmentByTag(DaySelectionDialog.class
                .getSimpleName()) != null) {
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DaySelectionDialog.class
                                    .getSimpleName()));
        }
        if (fragmentManager.findFragmentByTag(ReminderStartEndEditDialog.class
                .getSimpleName()) != null) {
            fragmentTransaction.remove(fragmentManager
                    .findFragmentByTag(ReminderStartEndEditDialog.class
                            .getSimpleName()));
        }
        if (fragmentManager.findFragmentByTag(LunchDinnerTimeDialog.class
                .getSimpleName()) != null) {
            fragmentTransaction.remove(fragmentManager
                    .findFragmentByTag(LunchDinnerTimeDialog.class
                            .getSimpleName()));
        }
        if (fragmentManager.findFragmentByTag(DayScheduleDialog.class
                .getSimpleName()) != null) {
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
        }
        if (fragmentManager.findFragmentByTag(TargetQuantityEditDialog.class
                .getSimpleName()) != null) {
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(TargetQuantityEditDialog.class
                                    .getSimpleName()));
        }
        fragmentManager.popBackStack(null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();

        // Update summary
        TimePreference preference = null;
        TargetQuantityEditTextPreference targetPreference;
        PlaceholderFragment placeholderFragment = (PlaceholderFragment) getFragmentManager()
                .findFragmentById(R.id.settings_container);
        if (placeholderFragment != null) {
            if (key.equals(HydrateDatabase.REMINDER_START_TIME)) {
                preference = (TimePreference) placeholderFragment
                        .findPreference(getString(R.string.key_reminder_start_time));
            } else if (key.equals(HydrateDatabase.REMINDER_END_TIME)) {
                preference = (TimePreference) placeholderFragment
                        .findPreference(getString(R.string.key_reminder_end_time));
            } else if (key.equals(HydrateDatabase.REMINDER_INTERVAL)) {
                preference = (TimePreference) placeholderFragment
                        .findPreference(getString(R.string.key_reminder_interval));
            } else if (key.equals(HydrateDatabase.LUNCH_START)) {
                preference = (TimePreference) placeholderFragment
                        .findPreference(getString(R.string.key_lunch));
            } else if (key.equals(HydrateDatabase.DINNER_START)) {
                preference = (TimePreference) placeholderFragment
                        .findPreference(getString(R.string.key_dinner));
            } else if (key.equals(HydrateDatabase.COLUMN_TARGET_QUANTITY)) {
                targetPreference = (TargetQuantityEditTextPreference) placeholderFragment
                        .findPreference(getString(R.string.key_target));
                targetPreference.setValue();
            }

            if (preference != null) {
                preference.setSummary();
                preference.getOnPreferenceClickListener().onPreferenceClick(
                        preference);
            }
        }
    }

    /**
     * A placeholder fragment containing the settings view.
     */
    public static class PlaceholderFragment extends PreferenceFragment
            implements OnSharedPreferenceChangeListener {

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d(tag, "onCreate PlaceHolderFragment");
            super.onCreate(savedInstanceState);

            //Add view from settings xml
            addPreferencesFromResource(R.xml.settings);
        }

        /**
         * Method that listens to preference changes in the settings screen
         * @param preferences
         * @param key
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences,
                                              String key) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(this.getActivity());

            Preference timePicker;
            boolean reminderStatus;
            Preference reminderInterval;
            EditTextPreference userName;
            AlarmReceiver alarmReceiver;
            UpdateUnitsTask updateUnitsTask = null;
            Preference snoozeInterval;
            SummaryListPreference metric;
            CupQuantityPreference cupQuantityPreference;
            TargetQuantityEditTextPreference editTextPreference;

            //If reminders are enabled or disabled
            if (key.equals(this.getString(R.string.key_reminders_status))) {

                reminderStatus = sharedPref.getBoolean(
                        this.getString(R.string.key_reminders_status), false);

                Log.d(tag, "Reminders enabled- " + reminderStatus);

                //Enable/disable the reminder start and end time widgets
                timePicker = findPreference(getString(R.string.key_reminder_start_time));
                timePicker.setEnabled(reminderStatus);

                timePicker = findPreference(getString(R.string.key_reminder_end_time));
                timePicker.setEnabled(reminderStatus);

                //Enable/disable reminder interval setting
                reminderInterval = findPreference(getString(R.string.key_reminder_interval));
                reminderInterval.setEnabled(reminderStatus);

                //Enable/disable reminder later interval setting
                snoozeInterval = findPreference(getString(R.string.key_reminder_snooze_interval));
                snoozeInterval.setEnabled(reminderStatus);

                //Enable/disable the alarms based on the setting
                alarmReceiver = new AlarmReceiver();
                if (reminderStatus) {
                    // Start reminders
                    alarmReceiver.setNextAlarm(getActivity());
                } else {
                    // Stop reminders
                    alarmReceiver.cancelAlarm(getActivity());
                }

            } else if (key.equals(this.getString(R.string.key_user_name))) {
                //Setting user name
                userName = (EditTextPreference) findPreference(key);
                userName.setSummary(userName.getText());
            } else if (key.equals(this.getString(R.string.key_metric))) {

                //Change in metrics
                metric = (SummaryListPreference) findPreference(key);
                editTextPreference = (TargetQuantityEditTextPreference) findPreference(getString(R.string.key_target));
                cupQuantityPreference = (CupQuantityPreference) findPreference(getString(R.string.key_glass_quantity));

                if (metric.getValue().equals(
                        this.getString(R.string.milliliter))) {
                    //Update settings to handle Milliliters
                    updateUnitsTask = new UpdateUnitsTask(
                            HydrateDatabase.UPDATE_LOG_UNITS_TO_ML,
                            this.getActivity(), cupQuantityPreference, editTextPreference);
                } else {
                    //Update settings to handle US oz
                    updateUnitsTask = new UpdateUnitsTask(
                            HydrateDatabase.UPDATE_UNITS_TO_OZ_US,
                            this.getActivity(), cupQuantityPreference, editTextPreference);
                }
                updateUnitsTask.execute("");

            } else if (key.equals(this.getString(R.string.key_glass_quantity))) {
                //Change default quantity summary based on the value chosen
                cupQuantityPreference = (CupQuantityPreference) findPreference(key);
                cupQuantityPreference.setSummary();
            }

            // Changing start time should be handled here

        }

        @Override
        public void onResume() {
            super.onResume();

            //Resume the listener
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();

            //Pause the listener
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

}
