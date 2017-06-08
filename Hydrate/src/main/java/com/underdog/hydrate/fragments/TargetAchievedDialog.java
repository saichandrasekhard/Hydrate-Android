package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.underdog.hydrate.R;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.util.Utility;

public class TargetAchievedDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.customAlert);

        String title = getString(R.string.congrats);
        String milliliter = getString(R.string.milliliter);
        String metric = preferences.getString(getString(R.string.key_metric),
                milliliter);
        String userName = preferences.getString(
                getString(R.string.key_user_name), null);
        Cursor targetCursor = getActivity().getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                new String[]{Utility.getInstance().getToday() + ""}, null);
        targetCursor.moveToFirst();
        String targetQuantity = targetCursor.getDouble(0) + "";
        String targetReachedMessage = getString(R.string.share_message_target_reached);
        targetReachedMessage = targetReachedMessage.replace("$$",
                targetQuantity);
        if (metric.equals(milliliter)) {
            targetReachedMessage += " " + getString(R.string.liters);
        } else {
            targetReachedMessage += " " + getString(R.string.oz);
        }
        targetReachedMessage += "\n" + getString(R.string.checkout);
        final String finalMessage = targetReachedMessage;

        builder.setNeutralButton(R.string.cancel, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.share, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, finalMessage);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.select_app)));
                } catch (android.content.ActivityNotFoundException ex) {
                    // (handle error)
                }
                dialog.dismiss();
            }
        });

        if (userName != null && !userName.equals("")) {
            title += " " + userName + "!";
        } else {
            title += "!";
        }

        builder.setTitle(title);
        builder.setView(getActivity().getLayoutInflater().inflate(
                R.layout.target_achieved_dialog, null));
        return builder.create();
    }
}
