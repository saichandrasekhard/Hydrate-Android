package com.underdog.hydrate.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;

public class BackupAndRestore {
    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    /**
     * Next available request code.
     */
    protected static final int NEXT_AVAILABLE_REQUEST_CODE = 2;
    Context context;
    private GoogleApiClient mGoogleApiClient;

    public BackupAndRestore(Context context) {
        this.context = context;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void backupDBToSD() {
        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String backupDBFolder = "Hydrate";
                String backupDBPath = backupDBFolder + File.separator
                        + "hydrate_back.db";
                File currentDB = new File(data, Constants.CURRENT_DB_PATH);
                File backupDB = new File(sd, backupDBFolder);
                backupDB.mkdirs();
                backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(
                            context,
                            context.getString(R.string.db_backed_upto)
                                    + backupDB.getCanonicalPath(),
                            Toast.LENGTH_SHORT).show();
                }
                Log.d(this.getClass().toString(), "After writing");
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage(), e);
            Toast.makeText(context, R.string.failure, Toast.LENGTH_LONG).show();
        }
    }

    public void restoreDBFromSD() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String backupDBPath = Constants.BACKUP_FOLDER + File.separator
                        + Constants.BACKUP_DB_FILE_NAME;
                File currentDB = new File(data, Constants.CURRENT_DB_PATH);
                File backupDB;
                backupDB = new File(sd, backupDBPath);
                if (!backupDB.exists()) {
                    Toast.makeText(context, R.string.db_file_not_present,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (currentDB.exists()) {
                    FileChannel dst = new FileOutputStream(currentDB)
                            .getChannel();
                    FileChannel src = new FileInputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(context, R.string.restore_complete,
                            Toast.LENGTH_SHORT).show();

                }
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage(), e);
            Toast.makeText(context, R.string.failure, Toast.LENGTH_LONG).show();
        }
    }

    public void backUpSettingsToSD() {
        Map<String, ?> settings;
        File sd;
        String backupDBPath;
        File backupFile;
        ObjectOutputStream output = null;
        try {
            settings = PreferenceManager.getDefaultSharedPreferences(context)
                    .getAll();

            sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                backupDBPath = Constants.BACKUP_FOLDER + File.separator
                        + Constants.BACKUP_SETTINGS_FILE_NAME;
                backupFile = new File(sd, Constants.BACKUP_FOLDER);
                backupFile.mkdirs();
                backupFile = new File(sd, backupDBPath);

                output = new ObjectOutputStream(
                        new FileOutputStream(backupFile));
                output.writeObject(settings);
                Toast.makeText(
                        context,
                        context.getString(R.string.settings_backed_upto)
                                + backupFile.getCanonicalPath(),
                        Toast.LENGTH_SHORT).show();

                Log.d(this.getClass().toString(), "After writing");
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage(), e);
            Toast.makeText(context, R.string.failure, Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void restoreSettingsFromSD() {
        SharedPreferences preferences;
        String backupDBPath;
        File backupFolder;
        ObjectInputStream input = null;
        Editor prefEdit;
        File sd;
        try {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);

            sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                backupDBPath = Constants.BACKUP_FOLDER + File.separator
                        + Constants.BACKUP_SETTINGS_FILE_NAME;
                backupFolder = new File(sd, backupDBPath);
                if (!backupFolder.exists()) {
                    Toast.makeText(context, R.string.settings_file_not_present,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                input = new ObjectInputStream(new FileInputStream(backupFolder));
                prefEdit = preferences.edit();
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Entry<String, ?> entry : entries.entrySet()) {
                    Object value = entry.getValue();
                    String key = entry.getKey();

                    if (value instanceof Boolean)
                        prefEdit.putBoolean(key,
                                ((Boolean) value).booleanValue());
                    else if (value instanceof Float)
                        prefEdit.putFloat(key, ((Float) value).floatValue());
                    else if (value instanceof Integer)
                        prefEdit.putInt(key, ((Integer) value).intValue());
                    else if (value instanceof Long)
                        prefEdit.putLong(key, ((Long) value).longValue());
                    else if (value instanceof String)
                        prefEdit.putString(key, ((String) value));
                }
                prefEdit.apply();
                Toast.makeText(
                        context,
                        R.string.restore_complete
                                + backupFolder.getCanonicalPath(),
                        Toast.LENGTH_SHORT).show();

                Log.d(this.getClass().toString(), "After writing");
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage(), e);
            Toast.makeText(context, R.string.failure, Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
