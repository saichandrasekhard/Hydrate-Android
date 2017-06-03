package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.underdog.hydrate.R;
import com.underdog.hydrate.async.TargetIntervalCorrelationAsync;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Log;

import java.util.ArrayList;

public class EditCupsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity().getApplicationContext();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String milliliter = context.getString(R.string.milliliter);
        final String metric = preferences.getString(
                context.getString(R.string.key_metric), milliliter);
        EditText editText;
        TextView textView;

        final String oldQuantity = getArguments().getString(Constants.QUANTITY)
                .split("\\s+")[0];
        final long rowId = getArguments().getLong(HydrateDatabase.ROW_ID);
        final long position = getArguments().getLong(Constants.POSITION);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.customAlert);
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_edit_cups, null);

        // Set values and metric in the view
        textView = (TextView) view.findViewById(R.id.editCupDialogMetric);
        editText = (EditText) view.findViewById(R.id.editCupText);
        editText.setText(oldQuantity);
        if (metric.equals(milliliter)) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            textView.setText(R.string.ml);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            textView.setText(R.string.oz);
        }

        builder.setView(view);
        builder.setTitle(R.string.edit);

        //Set buttons and their actions
        builder.setPositiveButton(R.string.update, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int arg1) {
                ContentResolver contentResolver = getActivity()
                        .getContentResolver();
                double oldValueDouble = Double.valueOf(oldQuantity);
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor;
                String editCupText;
                double defaultQuantity = Double.valueOf(preferences.getString(
                        getActivity().getString(R.string.key_glass_quantity),
                        getActivity().getString(R.string.default_glass_ml)));
                ContentValues values;
                double newQuantity;
                AlertDialog cupsDialog = (AlertDialog) dialogInterface;
                editCupText = ((TextView) cupsDialog
                        .findViewById(R.id.editCupText)).getText().toString();
                if (editCupText.equals("")) {
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.validQuantity),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                newQuantity = Double.valueOf((editCupText));

                if (newQuantity > HydrateDAO.getHydrateDAO().getTodayTarget(getContext())) {
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.validQuantity),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (oldValueDouble != newQuantity) {

                    //Update in DB
                    values = new ContentValues();
                    values.put(HydrateDatabase.COLUMN_QUANTITY, newQuantity);
                    contentResolver.update(
                            HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS,
                            values, HydrateDatabase.ROW_ID + "=?",
                            new String[]{String.valueOf(rowId)});

                    // Update default cup value in preferences
                    if (defaultQuantity == oldValueDouble) {
                        editor = preferences.edit();
                        Log.d(EditCupsDialog.class.toString(),
                                "default Value - " + defaultQuantity + ","
                                        + oldValueDouble);
                        editor.putString(
                                getActivity().getString(
                                        R.string.key_glass_quantity),
                                metric.equals(milliliter) ? String
                                        .valueOf((int) newQuantity) : String
                                        .valueOf(newQuantity));
                        editor.apply();

                        //Adjust targets for all days as default cup quantity have changed
                        ArrayList<Integer> daysSelected = new ArrayList<>();
                        for (int i = 0; i < 7; i++) {
                            daysSelected.add(i);
                        }
                        new TargetIntervalCorrelationAsync(getActivity(), daysSelected).execute(HydrateDatabase.COLUMN_TARGET_QUANTITY);
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int arg1) {
                dialogInterface.cancel();
            }
        });

        return builder.create();
    }
}
