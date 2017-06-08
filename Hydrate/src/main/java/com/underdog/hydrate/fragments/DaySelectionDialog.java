package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.underdog.hydrate.R;
import com.underdog.hydrate.adapter.DaySelectionArrayAdapter;
import com.underdog.hydrate.async.TargetIntervalCorrelationAsync;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Log;

import java.util.ArrayList;

public class DaySelectionDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);
        DaySelectionArrayAdapter adapter = new DaySelectionArrayAdapter(
                getActivity(), R.layout.day_check, new Integer[]{0, 1, 2, 3,
                4, 5, 6}, getArguments());
        Bundle args = getArguments();
        final String key = args.getString(Constants.JUST_ANOTHER_KEY);
        final String newTime = args.getString(Constants.NEW_START_TIME);
        Log.d(getClass().getSimpleName(), "newTime - " + newTime);
        final ArrayList<Integer> daysSelected = args
                .getIntegerArrayList(Constants.DAYS_SELECTED);
        final ContentValues contentValues = new ContentValues();
        contentValues.put(key, newTime);

        builder.setTitle(R.string.select_days);
        ListView listView = new ListView(getActivity());
        builder.setView(listView);
        listView.setAdapter(adapter);

        if (key.equals(HydrateDatabase.LUNCH_START)) {
            int newEndTime = args.getInt(Constants.NEW_END_TIME);
            contentValues.put(HydrateDatabase.LUNCH_END, newEndTime);
        } else if (key.equals(HydrateDatabase.DINNER_START)) {
            int newEndTime = args.getInt(Constants.NEW_END_TIME);
            contentValues.put(HydrateDatabase.DINNER_END, newEndTime);
        }
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (daysSelected.size() > 0) {
                            // Execute query
                            String[] selectionArgs = new String[daysSelected
                                    .size()];
                            String questionMarks = "";
                            for (int index = 0; index < daysSelected.size(); index++) {
                                selectionArgs[index] = String
                                        .valueOf(daysSelected.get(index));
                                questionMarks += "?,";
                            }
                            questionMarks = questionMarks.substring(0,
                                    questionMarks.length() - 1);
                            getActivity()
                                    .getContentResolver()
                                    .update(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                                            contentValues,
                                            HydrateDatabase.DAY + " IN ("
                                                    + questionMarks + ")",
                                            selectionArgs);

                            new TargetIntervalCorrelationAsync(getActivity(), daysSelected).execute(key);

                            dialog.dismiss();
                        }
                        ((ScheduleUpdated) getActivity()).destroyBackStack(key);
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        final AlertDialog dialog = builder.create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long rowId) {
                CheckBox checkBox = (CheckBox) view
                        .findViewById(R.id.selected_day);
                Button button;
                if (daysSelected.contains(position)) {
                    daysSelected.remove(((Integer) position));
                    checkBox.setChecked(false);
                } else {
                    daysSelected.add(position);
                    checkBox.setChecked(true);
                }
                button = dialog.getButton(Dialog.BUTTON_POSITIVE);
                if (daysSelected.size() == 0) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }
        });
        return dialog;
    }

    public interface ScheduleUpdated {

        void destroyBackStack(String key);
    }
}
