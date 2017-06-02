package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;

import java.util.ArrayList;

/**
 * Class that lets you edit the target
 */
public class TargetQuantityEditDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final String key = bundle.getString(Constants.JUST_ANOTHER_KEY);
        final double oldTarget = bundle.getDouble(Constants.OLD_TARGET);
        final String whereCondition = key + "=?";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String metric = preferences.getString(getString(R.string.key_metric), getString(R.string.milliliter));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.customAlert);
        final LinearLayout editTarget = (LinearLayout) getActivity().getLayoutInflater().inflate(
                R.layout.edit_target, null);
        final EditText editText = (EditText) editTarget.findViewById(R.id.editTargetView);
        editText.setText(String.valueOf(oldTarget));
        if (metric.equals(getString(R.string.milliliter))) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setTitle(R.string.targetQuantityTitleMl);
        } else {
            builder.setTitle(R.string.targetQuantityTitleOz);
        }
        builder.setView(editTarget);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show day selection dialog
                        String value = editText.getText().toString();
                        if (value != null && !value.equals("")) {
                            DaySelectionDialog daySelectionDialog = new DaySelectionDialog();
                            Bundle args = new Bundle();
                            double oldValue = oldTarget;
                            if (metric.equals(getString(R.string.milliliter)))
                                oldValue = (Double) oldValue * 1000;
                            Cursor cursor = getActivity()
                                    .getContentResolver()
                                    .query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                                            new String[]{HydrateDatabase.DAY},
                                            whereCondition,
                                            new String[]{String.valueOf(oldValue),
                                            }, null);
                            ArrayList<Integer> daysSelected = new ArrayList<>();
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                daysSelected.add(cursor.getInt(0));
                                cursor.moveToNext();
                            }

                            args.putString(Constants.JUST_ANOTHER_KEY, key);
                            //Using the variable NEW_START_TIME to reuse the Day Selection Dialog
                            if (metric.equals(getString(R.string.milliliter)))
                                value = String.valueOf(Double.valueOf(value) * 1000);
                            args.putString(Constants.NEW_START_TIME, value);
                            args.putIntegerArrayList(Constants.DAYS_SELECTED,
                                    daysSelected);
                            FragmentManager fragmentManager = getActivity()
                                    .getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager
                                    .beginTransaction();
                            fragmentTransaction.remove(fragmentManager
                                    .findFragmentByTag(TargetQuantityEditDialog.class
                                            .getSimpleName()));
                            fragmentTransaction.addToBackStack("myBack");
                            daySelectionDialog.setArguments(args);
                            daySelectionDialog.show(fragmentTransaction,
                                    DaySelectionDialog.class.getSimpleName());
                            getActivity().getContentResolver().notifyChange(
                                    HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, null);
                        } else {
                            dialog.cancel();
                            Toast.makeText(getActivity(), R.string.validQuantity, Toast.LENGTH_SHORT).show();
                        }
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
