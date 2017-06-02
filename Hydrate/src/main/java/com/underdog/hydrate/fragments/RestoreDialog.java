package com.underdog.hydrate.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.underdog.hydrate.R;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.BackupAndRestore;

public class RestoreDialog extends DialogFragment {

    AlarmReceiver alarmReceiver;

    /**
     * @return the alarmReceiver
     */
    public AlarmReceiver getAlarmReceiver() {
        if (alarmReceiver == null) {
            alarmReceiver = new AlarmReceiver();
        }
        return alarmReceiver;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);
        builder.setTitle(R.string.restore_dialog_title);
        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BackupAndRestore backupAndRestore = new BackupAndRestore(
                                getActivity());
                        backupAndRestore.restoreSettingsFromSD();
                        backupAndRestore.restoreDBFromSD();
                        notifyLoaders();
                        getAlarmReceiver().resetAlarms(getActivity());
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private void notifyLoaders() {
        getActivity().getContentResolver().notifyChange(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, null);
        getActivity().getContentResolver().notifyChange(
                HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS, null);
        getActivity().getContentResolver().notifyChange(
                HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET, null);
    }
}
