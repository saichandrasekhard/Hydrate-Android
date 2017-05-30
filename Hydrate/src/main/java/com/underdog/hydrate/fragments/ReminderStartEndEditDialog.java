package com.underdog.hydrate.fragments;

import java.util.ArrayList;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TimePicker;

public class ReminderStartEndEditDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TimePicker timePicker = new TimePicker(getActivity());
        timePicker.setIs24HourView(true);
        Bundle bundle = getArguments();
        final String key = bundle.getString(Constants.JUST_ANOTHER_KEY);
        final int oldHours = bundle.getInt(Constants.OLD_HOUR);
        final int oldMins = bundle.getInt(Constants.OLD_MINS);
        timePicker.setCurrentHour(oldHours);
        timePicker.setCurrentMinute(oldMins);
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);

        if (key.equals(HydrateDatabase.REMINDER_START_TIME)) {
            builder.setTitle(R.string.reminder_start_time);
        } else if (key.equals(HydrateDatabase.REMINDER_END_TIME)) {
            builder.setTitle(R.string.reminder_end_time);
        } else {
            builder.setTitle(R.string.reminder_interval);
        }
        builder.setView(timePicker);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show day selection dialog
                        DaySelectionDialog daySelectionDialog = new DaySelectionDialog();
                        Bundle args = new Bundle();
                        String whereCondition = key + "=?";

                        Cursor cursor = getActivity()
                                .getContentResolver()
                                .query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                                        new String[]{HydrateDatabase.DAY},
                                        whereCondition,
                                        new String[]{String.valueOf(oldHours
                                                * 60 + oldMins)}, null);
                        ArrayList<Integer> daysSelected = new ArrayList<>();
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            daysSelected.add(cursor.getInt(0));
                            cursor.moveToNext();
                        }

                        args.putString(Constants.JUST_ANOTHER_KEY, key);
                        args.putString(
                                Constants.NEW_START_TIME,
                                timePicker.getCurrentHour() * 60
                                        + timePicker.getCurrentMinute() + "");
                        args.putIntegerArrayList(Constants.DAYS_SELECTED,
                                daysSelected);
                        FragmentManager fragmentManager = getActivity()
                                .getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.remove(fragmentManager
                                .findFragmentByTag(ReminderStartEndEditDialog.class
                                        .getSimpleName()));
                        fragmentTransaction.addToBackStack("myBack");
                        daySelectionDialog.setArguments(args);
                        daySelectionDialog.show(fragmentTransaction,
                                DaySelectionDialog.class.getSimpleName());
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
