package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Log;

public class DayScheduleDialog extends DialogFragment {

    String key;
    AdapterView.OnItemClickListener onItemClickListenerForRST = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long rowId) {
            TextView textView = (TextView) view.findViewById(R.id.time_view);
            final String[] parts = textView.getText().toString().split(":");
            final int oldHours = Integer.parseInt(parts[0]);
            final int oldMins = Integer.parseInt(parts[1]);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.OLD_HOUR, oldHours);
            bundle.putInt(Constants.OLD_MINS, oldMins);
            bundle.putString(Constants.JUST_ANOTHER_KEY,
                    HydrateDatabase.REMINDER_START_TIME);

            FragmentManager fragmentManager = getActivity()
                    .getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
            fragmentTransaction.addToBackStack(null);
            ReminderStartEndEditDialog dialog = new ReminderStartEndEditDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentTransaction,
                    ReminderStartEndEditDialog.class.getSimpleName());

        }
    };
    AdapterView.OnItemClickListener onItemClickListenerForRET = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long rowId) {
            TextView textView = (TextView) view.findViewById(R.id.time_view);
            final String[] parts = textView.getText().toString().split(":");
            final int oldHours = Integer.parseInt(parts[0]);
            final int oldMins = Integer.parseInt(parts[1]);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.OLD_HOUR, oldHours);
            bundle.putInt(Constants.OLD_MINS, oldMins);
            bundle.putString(Constants.JUST_ANOTHER_KEY,
                    HydrateDatabase.REMINDER_END_TIME);

            FragmentManager fragmentManager = getActivity()
                    .getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.addToBackStack("myBack");
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
            ReminderStartEndEditDialog dialog = new ReminderStartEndEditDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentTransaction,
                    ReminderStartEndEditDialog.class.getSimpleName());

        }
    };
    AdapterView.OnItemClickListener onItemClickListenerForLunch = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long rowId) {
            TextView textView = (TextView) view.findViewById(R.id.time_view);
            String[] times = textView.getText().toString().split("-");
            String[] startTime = times[0].split(":");
            String[] endTime = times[1].split(":");
            final int oldHours = Integer.parseInt(startTime[0]);
            final int oldMins = Integer.parseInt(startTime[1].trim());
            final int oldEndHours = Integer.parseInt(endTime[0].trim());
            final int oldEndMins = Integer.parseInt(endTime[1]);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.OLD_HOUR, oldHours);
            bundle.putInt(Constants.OLD_MINS, oldMins);
            bundle.putInt(Constants.OLD_END_HOUR, oldEndHours);
            bundle.putInt(Constants.OLD_END_MINS, oldEndMins);
            bundle.putString(Constants.JUST_ANOTHER_KEY,
                    HydrateDatabase.LUNCH_START);

            FragmentManager fragmentManager = getActivity()
                    .getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.addToBackStack("myBack");
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
            LunchDinnerTimeDialog dialog = new LunchDinnerTimeDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentTransaction,
                    LunchDinnerTimeDialog.class.getSimpleName());

        }
    };
    AdapterView.OnItemClickListener onItemClickListenerForDinner = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long rowId) {
            TextView textView = (TextView) view.findViewById(R.id.time_view);
            String[] times = textView.getText().toString().split("-");
            String[] startTime = times[0].split(":");
            String[] endTime = times[1].split(":");
            final int oldHours = Integer.parseInt(startTime[0]);
            final int oldMins = Integer.parseInt(startTime[1].trim());
            final int oldEndHours = Integer.parseInt(endTime[0].trim());
            final int oldEndMins = Integer.parseInt(endTime[1]);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.OLD_HOUR, oldHours);
            bundle.putInt(Constants.OLD_MINS, oldMins);
            bundle.putInt(Constants.OLD_END_HOUR, oldEndHours);
            bundle.putInt(Constants.OLD_END_MINS, oldEndMins);
            bundle.putString(Constants.JUST_ANOTHER_KEY,
                    HydrateDatabase.DINNER_START);

            FragmentManager fragmentManager = getActivity()
                    .getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.addToBackStack("myBack");
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
            LunchDinnerTimeDialog dialog = new LunchDinnerTimeDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentTransaction,
                    LunchDinnerTimeDialog.class.getSimpleName());

        }
    };
    AdapterView.OnItemClickListener onItemClickListenerForInt = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long rowId) {
            TextView textView = (TextView) view.findViewById(R.id.time_view);
            final long time = Long.valueOf(textView.getText().toString()
                    .split("\\s+")[0]);
            final int oldHours = (int) (time / 60);
            final int oldMins = (int) (time % 60);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.OLD_HOUR, oldHours);
            bundle.putInt(Constants.OLD_MINS, oldMins);
            bundle.putString(Constants.JUST_ANOTHER_KEY,
                    HydrateDatabase.REMINDER_INTERVAL);

            FragmentManager fragmentManager = getActivity()
                    .getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.addToBackStack("myBack");
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
            ReminderStartEndEditDialog dialog = new ReminderStartEndEditDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentTransaction,
                    ReminderStartEndEditDialog.class.getSimpleName());

        }
    };

    AdapterView.OnItemClickListener onItemClickListenerForTarget = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long rowId) {
            TextView textView = (TextView) view.findViewById(R.id.time_view);
            final double targetQuantity = Double.valueOf(textView.getText().toString()
                    .split("\\s+")[0]);
            Bundle bundle = new Bundle();
            bundle.putDouble(Constants.OLD_TARGET, targetQuantity);
            bundle.putString(Constants.JUST_ANOTHER_KEY,
                    HydrateDatabase.COLUMN_TARGET_QUANTITY);

            FragmentManager fragmentManager = getActivity()
                    .getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.addToBackStack("myBack");
            fragmentTransaction
                    .remove(fragmentManager
                            .findFragmentByTag(DayScheduleDialog.class
                                    .getSimpleName()));
            TargetQuantityEditDialog dialog = new TargetQuantityEditDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentTransaction,
                    TargetQuantityEditDialog.class.getSimpleName());

        }
    };
    SimpleCursorAdapter.ViewBinder startEndBinder = new SimpleCursorAdapter.ViewBinder() {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            int dayId;
            long time;
            int hours;
            int mins;
            TextView textView;
            if (columnIndex == 1) {
                dayId = cursor.getInt(columnIndex);
                textView = (TextView) view;
                switch (dayId) {

                    case 0:
                        textView.setText(R.string.monday);
                        break;
                    case 1:
                        textView.setText(R.string.tuesday);
                        break;
                    case 2:
                        textView.setText(R.string.wednesday);
                        break;
                    case 3:
                        textView.setText(R.string.thursday);
                        break;
                    case 4:
                        textView.setText(R.string.friday);
                        break;
                    case 5:
                        textView.setText(R.string.saturday);
                        break;
                    case 6:
                        textView.setText(R.string.sunday);
                        break;

                    default:
                        break;
                }
            } else if (columnIndex == 2) {
                time = cursor.getLong(columnIndex);
                hours = (int) (time / 60);
                mins = (int) (time % 60);
                ((TextView) view).setText(hours + ":"
                        + ((mins < 10) ? "0" + mins : mins));
            }
            return true;
        }
    };
    SimpleCursorAdapter.ViewBinder intervalBinder = new SimpleCursorAdapter.ViewBinder() {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            int dayId;
            long time;
            TextView textView;
            if (columnIndex == 1) {
                dayId = cursor.getInt(columnIndex);
                textView = (TextView) view;
                switch (dayId) {

                    case 0:
                        textView.setText(R.string.monday);
                        break;
                    case 1:
                        textView.setText(R.string.tuesday);
                        break;
                    case 2:
                        textView.setText(R.string.wednesday);
                        break;
                    case 3:
                        textView.setText(R.string.thursday);
                        break;
                    case 4:
                        textView.setText(R.string.friday);
                        break;
                    case 5:
                        textView.setText(R.string.saturday);
                        break;
                    case 6:
                        textView.setText(R.string.sunday);
                        break;

                    default:
                        break;
                }
            } else if (columnIndex == 2) {
                time = cursor.getLong(columnIndex);
                ((TextView) view).setText(time + " "
                        + getActivity().getString(R.string.mins));
            }
            return true;
        }
    };
    SimpleCursorAdapter.ViewBinder lunchDinnerBinder = new SimpleCursorAdapter.ViewBinder() {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            int dayId;
            long timeStart;
            long timeEnd;
            int hoursStart;
            int minsStart;
            int hoursEnd;
            int minsEnd;
            TextView textView;
            if (columnIndex == 1) {
                dayId = cursor.getInt(columnIndex);
                textView = (TextView) view;
                switch (dayId) {

                    case 0:
                        textView.setText(R.string.monday);
                        break;
                    case 1:
                        textView.setText(R.string.tuesday);
                        break;
                    case 2:
                        textView.setText(R.string.wednesday);
                        break;
                    case 3:
                        textView.setText(R.string.thursday);
                        break;
                    case 4:
                        textView.setText(R.string.friday);
                        break;
                    case 5:
                        textView.setText(R.string.saturday);
                        break;
                    case 6:
                        textView.setText(R.string.sunday);
                        break;

                    default:
                        break;
                }
            } else if (columnIndex == 2) {
                timeStart = cursor.getLong(columnIndex);
                hoursStart = (int) (timeStart / 60);
                minsStart = (int) (timeStart % 60);
                timeEnd = cursor.getLong(columnIndex + 1);
                hoursEnd = (int) (timeEnd / 60);
                minsEnd = (int) (timeEnd % 60);
                ((TextView) view).setText(hoursStart + ":"
                        + ((minsStart < 10) ? "0" + minsStart : minsStart)
                        + " - " + hoursEnd + ":"
                        + ((minsEnd < 10) ? "0" + minsEnd : minsEnd));
            }
            return true;
        }
    };

    SimpleCursorAdapter.ViewBinder targetBinder = new SimpleCursorAdapter.ViewBinder() {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            int dayId;

            TextView textView;
            if (columnIndex == 1) {
                dayId = cursor.getInt(columnIndex);
                textView = (TextView) view;
                switch (dayId) {

                    case 0:
                        textView.setText(R.string.monday);
                        break;
                    case 1:
                        textView.setText(R.string.tuesday);
                        break;
                    case 2:
                        textView.setText(R.string.wednesday);
                        break;
                    case 3:
                        textView.setText(R.string.thursday);
                        break;
                    case 4:
                        textView.setText(R.string.friday);
                        break;
                    case 5:
                        textView.setText(R.string.saturday);
                        break;
                    case 6:
                        textView.setText(R.string.sunday);
                        break;

                    default:
                        break;
                }
            } else if (columnIndex == 2) {
                double target = cursor.getDouble(columnIndex);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String metric = sharedPreferences.getString(getActivity().getString(R.string.key_metric)
                        , getActivity().getString(R.string.milliliter));
                if (metric.equals(getActivity().getString(R.string.milliliter))) {
                    target /= 1000;
                    target *= 100;
                    target = Math.round(target);
                    target /= 100;
                    ((TextView) view).setText(target + " " + getActivity().getString(R.string.l));
                } else {
                    ((TextView) view).setText(target + " " + getActivity().getString(R.string.oz));
                }
            }
            return true;
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);
        key = this.getArguments().getString(Constants.JUST_ANOTHER_KEY);
        Cursor cursor;
        SimpleCursorAdapter cursorAdapter = null;
        ListView listView = new ListView(getActivity());
        builder.setTitle(R.string.select_edit);
        builder.setView(listView);

        if (getActivity().getString(R.string.key_reminder_start_time).equals(
                key)) {
            cursor = getActivity().getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.ROW_ID + " as _id ",
                            HydrateDatabase.DAY,
                            HydrateDatabase.REMINDER_START_TIME}, null, null,
                    null);
            Log.d(getClass().getSimpleName(),
                    "cursor size - " + cursor.getCount());
            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.day_schedule, cursor, new String[]{
                    HydrateDatabase.DAY,
                    HydrateDatabase.REMINDER_START_TIME}, new int[]{
                    R.id.day_name, R.id.time_view},
                    CursorAdapter.NO_SELECTION);
            listView.setOnItemClickListener(onItemClickListenerForRST);
            cursorAdapter.setViewBinder(startEndBinder);
        } else if (getActivity().getString(R.string.key_reminder_end_time)
                .equals(key)) {
            cursor = getActivity().getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.ROW_ID + " as _id ",
                            HydrateDatabase.DAY,
                            HydrateDatabase.REMINDER_END_TIME}, null, null,
                    null);
            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.day_schedule, cursor, new String[]{
                    HydrateDatabase.DAY,
                    HydrateDatabase.REMINDER_END_TIME}, new int[]{
                    R.id.day_name, R.id.time_view},
                    CursorAdapter.NO_SELECTION);
            listView.setOnItemClickListener(onItemClickListenerForRET);
            cursorAdapter.setViewBinder(startEndBinder);
        } else if (getActivity().getString(R.string.key_reminder_interval)
                .equals(key)) {
            cursor = getActivity().getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.ROW_ID + " as _id ",
                            HydrateDatabase.DAY,
                            HydrateDatabase.REMINDER_INTERVAL}, null, null,
                    null);
            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.day_schedule, cursor, new String[]{
                    HydrateDatabase.DAY,
                    HydrateDatabase.REMINDER_INTERVAL}, new int[]{
                    R.id.day_name, R.id.time_view},
                    CursorAdapter.NO_SELECTION);
            cursorAdapter.setViewBinder(intervalBinder);
            listView.setOnItemClickListener(onItemClickListenerForInt);
        } else if (getActivity().getString(R.string.key_lunch).equals(key)) {
            cursor = getActivity().getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.ROW_ID + " as _id ",
                            HydrateDatabase.DAY, HydrateDatabase.LUNCH_START,
                            HydrateDatabase.LUNCH_END}, null, null, null);
            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.day_schedule, cursor, new String[]{
                    HydrateDatabase.DAY, HydrateDatabase.LUNCH_START},
                    new int[]{R.id.day_name, R.id.time_view},
                    CursorAdapter.NO_SELECTION);
            cursorAdapter.setViewBinder(lunchDinnerBinder);
            listView.setOnItemClickListener(onItemClickListenerForLunch);
        } else if (getActivity().getString(R.string.key_dinner).equals(key)) {
            cursor = getActivity().getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.ROW_ID + " as _id ",
                            HydrateDatabase.DAY, HydrateDatabase.DINNER_START,
                            HydrateDatabase.DINNER_END}, null, null, null);
            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.day_schedule, cursor,
                    new String[]{HydrateDatabase.DAY,
                            HydrateDatabase.DINNER_START}, new int[]{
                    R.id.day_name, R.id.time_view},
                    CursorAdapter.NO_SELECTION);
            cursorAdapter.setViewBinder(lunchDinnerBinder);
            listView.setOnItemClickListener(onItemClickListenerForDinner);
        } else if (getActivity().getString(R.string.key_target).equals(key)) {
            cursor = getActivity().getContentResolver().query(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                    new String[]{HydrateDatabase.ROW_ID + " as _id ",
                            HydrateDatabase.DAY,
                            HydrateDatabase.COLUMN_TARGET_QUANTITY}, null, null,
                    null);
            Log.d(getClass().getSimpleName(),
                    "cursor size - " + cursor.getCount());
            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.day_schedule, cursor, new String[]{
                    HydrateDatabase.DAY,
                    HydrateDatabase.COLUMN_TARGET_QUANTITY}, new int[]{
                    R.id.day_name, R.id.time_view},
                    CursorAdapter.NO_SELECTION);
            listView.setOnItemClickListener(onItemClickListenerForTarget);
            cursorAdapter.setViewBinder(targetBinder);
        }

        listView.setAdapter(cursorAdapter);

        return builder.create();
    }
}
