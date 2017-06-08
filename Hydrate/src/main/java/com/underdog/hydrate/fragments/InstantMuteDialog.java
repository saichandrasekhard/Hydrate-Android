package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TimePicker;

import com.underdog.hydrate.R;
import com.underdog.hydrate.util.Log;

public class InstantMuteDialog extends DialogFragment {

    SharedPreferences preferences;
    private DialogInterface.OnClickListener stopMute = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Set preference value to 0
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(getActivity().getString(R.string.key_instant_mute),
                    0);
            editor.apply();
            // Change icon
        }
    };
    private DialogInterface.OnClickListener enableMute = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            AlertDialog alertDialog = (AlertDialog) dialog;
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity()).edit();
            int hours;
            int mins;
            long duration;
            TimePicker timePicker = (TimePicker) alertDialog
                    .findViewById(R.id.instant_mute_duration);
            hours = timePicker.getCurrentHour();
            mins = timePicker.getCurrentMinute();
            Log.d(getTag(), "hours,mins - " + hours + ":" + mins);
            duration = hours * 3600000l + mins * 60000l;
            Log.d(getTag(), "duration - " + duration);
            editor.putLong(getActivity().getString(R.string.key_instant_mute),
                    System.currentTimeMillis() + duration);
            editor.apply();
            dialog.dismiss();
        }
    };
    private DialogInterface.OnClickListener dismissDialog = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long instantMute;
        preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);
        View view;
        TimePicker timePicker;
        instantMute = preferences.getLong(
                getActivity().getString(R.string.key_instant_mute),
                Long.valueOf(getActivity().getString(
                        R.string.default_instant_mute)));
        if (instantMute > System.currentTimeMillis()) {
            // Cancel mute
            builder.setPositiveButton(R.string.yes, stopMute);
            builder.setNegativeButton(R.string.no, dismissDialog);
            builder.setTitle(R.string.stop_instant_mute_warning);
        } else {
            // Enable mute
            builder.setPositiveButton(R.string.ok, enableMute);
            builder.setNegativeButton(R.string.cancel, dismissDialog);
            builder.setTitle(R.string.instant_mute);

            // Set View
            view = getActivity().getLayoutInflater().inflate(
                    R.layout.instant_mute_dialog, null);
            timePicker = (TimePicker) view
                    .findViewById(R.id.instant_mute_duration);
            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(2);
            timePicker.setCurrentMinute(0);
            builder.setView(view);
        }
        return builder.create();
    }

}
