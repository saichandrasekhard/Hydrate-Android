package com.underdog.hydrate.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.Toast;

import com.underdog.hydrate.R;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.fragments.DriveBackupDialog;
import com.underdog.hydrate.fragments.DriveRestoreDialog;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.BackupAndRestore;
import com.underdog.hydrate.util.Log;

public class BackupPreference extends Preference {

    private final String backToSD = getContext().getString(
            R.string.key_backup_sd);
    private final String backToGDrive = getContext().getString(
            R.string.key_backup_gdrive);
    private final String restoreFromSD = getContext().getString(
            R.string.key_restore_sd);
    private final String restoreFromGDrive = getContext().getString(
            R.string.key_restore_gdrive);
    private final String clearDb = getContext()
            .getString(R.string.key_clear_db);

    public BackupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        final String key = this.getKey();

        this.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent;
                final BackupAndRestore backupAndRestore = new BackupAndRestore(
                        getContext());
                if (key.equals(backToSD)) {
                    backupAndRestore.backUpSettingsToSD();
                    backupAndRestore.backupDBToSD();
                    return true;
                } else if (key.equals(restoreFromSD)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            (Activity) getContext(), R.style.customAlert);
                    builder.setTitle(R.string.restore_warning);
                    builder.setPositiveButton(R.string.yes,
                            new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    backupAndRestore.restoreSettingsFromSD();
                                    backupAndRestore.restoreDBFromSD();

                                    HydrateContentProvider hydrateContentProvider = (HydrateContentProvider) getContext()
                                            .getContentResolver()
                                            .acquireContentProviderClient(
                                                    HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS)
                                            .getLocalContentProvider();
                                    hydrateContentProvider.resetDatabase();
                                    startReminders();
                                    notifyLoaders();
                                    dialog.cancel();
                                }
                            });
                    builder.setNegativeButton(R.string.no,
                            new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                    // notifyLoaders();
                    return true;
                } else if (key.equals(backToGDrive)) {
                    DriveBackupDialog backupDialog = new DriveBackupDialog();
                    backupDialog.show(((AppCompatActivity) getContext())
                                    .getSupportFragmentManager(),
                            DriveBackupDialog.class.getSimpleName());
                } else if (key.equals(restoreFromGDrive)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            (Activity) getContext(), R.style.customAlert);
                    builder.setTitle(R.string.restore_warning);
                    builder.setPositiveButton(R.string.yes,
                            new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    DriveRestoreDialog restoreDialog = new DriveRestoreDialog();
                                    restoreDialog.show(
                                            ((AppCompatActivity) getContext())
                                                    .getSupportFragmentManager()
                                            ,
                                            DriveRestoreDialog.class
                                                    .getSimpleName());

                                    HydrateContentProvider hydrateContentProvider = (HydrateContentProvider) getContext()
                                            .getContentResolver()
                                            .acquireContentProviderClient(
                                                    HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS)
                                            .getLocalContentProvider();
                                    hydrateContentProvider.resetDatabase();
                                    startReminders();
                                    notifyLoaders();
                                    dialog.cancel();
                                }
                            });
                    builder.setNegativeButton(R.string.no,
                            new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                } else if (key.equals(clearDb)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            (Activity) getContext(), R.style.customAlert);
                    builder.setTitle(R.string.clear_db_warning);
                    builder.setPositiveButton(R.string.yes,
                            new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    try {
                                        getContext()
                                                .getContentResolver()
                                                .delete(HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS,
                                                        null, null);
                                        getContext()
                                                .getContentResolver()
                                                .delete(HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET,
                                                        null, null);
                                        notifyLoaders();
                                        Toast.makeText(getContext(),
                                                R.string.clear_db_success,
                                                Toast.LENGTH_LONG).show();

                                    } catch (Exception e) {
                                        Toast.makeText(getContext(),
                                                R.string.failure,
                                                Toast.LENGTH_LONG).show();
                                        Log.e(this.getClass().toString(),
                                                "Error clearing DB", e);
                                    }
                                    dialog.cancel();
                                }
                            });
                    builder.setNegativeButton(R.string.no,
                            new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                    return true;
                }
                return false;
            }
        });

    }

    private void notifyLoaders() {
        getContext().getContentResolver().notifyChange(
                HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, null);
        getContext().getContentResolver().notifyChange(
                HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS, null);
        getContext().getContentResolver().notifyChange(
                HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET, null);
    }

    private void startReminders() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        boolean reminders = preferences.getBoolean(
                getContext().getString(R.string.key_reminders_status), false);
        if (reminders)
            new AlarmReceiver().setNextAlarm(getContext());
    }

}
