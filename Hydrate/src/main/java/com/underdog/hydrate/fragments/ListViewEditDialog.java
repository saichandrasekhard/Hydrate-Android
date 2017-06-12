package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Dialog Fragment that pops up when user clicks on item in the all events list
 * view
 *
 * @author Sekhar
 */
public class ListViewEditDialog extends DialogFragment {

    private static final String tag = "ListViewEditDialog";
    private Bundle inputExtras;

    /*
     * (non-Javadoc)
     *
     * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText editQuantity;
        final TextView unitView;
        final TimePicker timePicker;
        final Calendar calendar;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
                Constants.DATE_TIME_FORMAT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ThreeButtonDialog);
        final View view = getActivity().getLayoutInflater().inflate(
                R.layout.edit_dialog, null);
        inputExtras = getArguments();
        String quantity = inputExtras.getString(Constants.QUANTITY);
        String tokens[] = quantity.split("\\s+");
        StringBuffer unit = new StringBuffer();
        String time = inputExtras.getString(Constants.TIME);
        String date = inputExtras.getString(Constants.DATE);

        // Set the dialog title
        builder.setTitle(R.string.edit);
        builder.setView(view);

        // Set the quantity
        editQuantity = (EditText) view.findViewById(R.id.editQuantity);
        editQuantity.setText(tokens[0]);

        // Set the units
        unitView = (TextView) view.findViewById(R.id.editUnit);
        for (int i = 1; i < tokens.length; i++) {
            unit.append(tokens[i]).append(" ");
        }
        unit.deleteCharAt(unit.length() - 1);
        unitView.setText(unit);

        // To set the time, Parse the date string and instantiate calendar
        // object
        calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateTimeFormat.parse(new StringBuffer(date)
                    .append(" ").append(time).toString()));
        } catch (ParseException e) {
            Log.e(tag, "Error parsing date - ", e);
        }

        // Set time
        timePicker = (TimePicker) view.findViewById(R.id.editTime);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        builder.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface,
                                        int arg1) {
                        double quantity;
                        long rowId;

                        // Get rowId
                        rowId = inputExtras.getLong(HydrateDatabase.ROW_ID);

                        // Get the updated quantity
                        String editable = editQuantity.getText().toString();
                        if (editable == null || editable.equals("")) {
                            Toast.makeText(
                                    getActivity(),
                                    getActivity().getString(
                                            R.string.validQuantity),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        quantity = Double.valueOf(editable);

                        // Get the updated Time
                        calendar.set(Calendar.HOUR_OF_DAY,
                                timePicker.getCurrentHour());
                        calendar.set(Calendar.MINUTE,
                                timePicker.getCurrentMinute());

                        // Use the calendar object as input along with
                        // quantity to update the DB
                        HydrateDAO.getHydrateDAO().updateEvent(rowId,
                                calendar.getTimeInMillis(), quantity, getContext());

                        dialogInterface.dismiss();

                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface,
                                        int arg1) {
                        dialogInterface.cancel();
                    }
                });

        builder.setNeutralButton(R.string.delete,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface,
                                        int arg1) {
                        long rowId;

                        // Get rowId
                        rowId = inputExtras.getLong(HydrateDatabase.ROW_ID);

                        HydrateDAO.getHydrateDAO().deleteWaterById(rowId, getContext());
                        dialogInterface.dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
//        Button positive= ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
//        positive.setTextColor(ContextCompat.getColor(getContext(),R.color.success));
    }
}