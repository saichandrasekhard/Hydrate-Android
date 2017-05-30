package com.underdog.hydrate.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;

public class LunchDinnerTimeDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String whereCondition = null;
        final String key = bundle.getString(Constants.JUST_ANOTHER_KEY);
        final int oldStartHours = bundle.getInt(Constants.OLD_HOUR);
        final int oldStartMins = bundle.getInt(Constants.OLD_MINS);
        final int oldEndHours = bundle.getInt(Constants.OLD_END_HOUR);
        final int oldEndMins = bundle.getInt(Constants.OLD_END_MINS);
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.lunch_dinner_timepick, null);
        final TimePicker startTimePicker = (TimePicker) view
                .findViewById(R.id.startTime);
        startTimePicker.setCurrentHour(oldStartHours);
        startTimePicker.setCurrentMinute(oldStartMins);
        startTimePicker.setIs24HourView(true);
        final TimePicker endTimePicker = (TimePicker) view
                .findViewById(R.id.endTime);
        endTimePicker.setCurrentHour(oldEndHours);
        endTimePicker.setCurrentMinute(oldEndMins);
        endTimePicker.setIs24HourView(true);

        AlertDialog.Builder builder = new Builder(getActivity(),R.style.customAlert);
        builder.setView(view);
        if (key.equals(HydrateDatabase.LUNCH_START)) {
            builder.setTitle(R.string.lunch);
            whereCondition = key + "=? AND " + HydrateDatabase.LUNCH_END + "=?";
        } else if (key.equals(HydrateDatabase.DINNER_START)) {
            builder.setTitle(R.string.dinner);
            whereCondition = key + "=? AND " + HydrateDatabase.DINNER_END
                    + "=?";
        }
        final String whereCondFinal = whereCondition;
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show day selection dialog
                        DaySelectionDialog daySelectionDialog = new DaySelectionDialog();
                        Bundle args = new Bundle();

                        Cursor cursor = getActivity()
                                .getContentResolver()
                                .query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                                        new String[]{HydrateDatabase.DAY},
                                        whereCondFinal,
                                        new String[]{
                                                String.valueOf(oldStartHours
                                                        * 60 + oldStartMins),
                                                String.valueOf(oldEndHours * 60
                                                        + oldEndMins)}, null);
                        ArrayList<Integer> daysSelected = new ArrayList<>();
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            daysSelected.add(cursor.getInt(0));
                            cursor.moveToNext();
                        }

                        args.putString(Constants.JUST_ANOTHER_KEY, key);
                        args.putString(Constants.NEW_START_TIME,
                                startTimePicker.getCurrentHour() * 60
                                        + startTimePicker.getCurrentMinute() + "");
                        args.putInt(Constants.NEW_END_TIME,
                                endTimePicker.getCurrentHour() * 60
                                        + endTimePicker.getCurrentMinute());
                        args.putIntegerArrayList(Constants.DAYS_SELECTED,
                                daysSelected);
                        FragmentManager fragmentManager = getActivity()
                                .getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.remove(fragmentManager
                                .findFragmentByTag(LunchDinnerTimeDialog.class
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
