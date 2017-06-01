package com.underdog.hydrate.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.underdog.hydrate.R;
import com.underdog.hydrate.async.ApiClientAsyncTask;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.Log;
import com.underdog.hydrate.util.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DriveRestoreDialog extends DialogFragment implements
        ConnectionCallbacks, OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final String TAG = DriveRestoreDialog.class.toString();
    private int filesRestored = 0;
    private Utility utility;
    private GoogleApiClient googleApiClient;
    private boolean mClearDefaultAccount = false;

    public Utility getUtility() {
        if (utility == null) {
            utility = Utility.getInstance();
        }
        return utility;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");

        filesRestored = 0;
        if (googleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and
            // connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
        }
        // Connect the client. Once connected, the camera is launched.
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.progress_layout, null);
        builder.setView(view);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(),
                        REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        } else {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.getErrorDialog(getActivity(), connectionResult.getErrorCode(), 0).show();
//            GooglePlayServicesUtil.getErrorDialog(
//                    connectionResult.getErrorCode(), getActivity(), 0).show();
            this.dismiss();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.d(TAG, "GoogleApiClient connected");
        if (mClearDefaultAccount) {
            mClearDefaultAccount = false;
            googleApiClient.clearDefaultAccountAndReconnect();
            return;
        }
        DriveFolder driveFolder = Drive.DriveApi
                .getAppFolder(getGoogleApiClient());
        Query query = new Query.Builder()
                .addFilter(
                        Filters.eq(SearchableField.TITLE,
                                Constants.BACKUP_DB_FILE_NAME)).build();

        driveFolder.queryChildren(getGoogleApiClient(), query)
                .setResultCallback(
                        new ResultCallback<DriveApi.MetadataBufferResult>() {

                            @Override
                            public void onResult(MetadataBufferResult result) {
                                int count = result.getMetadataBuffer()
                                        .getCount();
                                Log.d(TAG, "Count of db file - " + count);
                                DriveId driveId;
                                MetadataBuffer metadataBuffer = result
                                        .getMetadataBuffer();
                                if (count == 1) {
                                    // Already present
                                    Log.d(TAG, "DB File already present");
                                    Iterator<Metadata> iterator = metadataBuffer
                                            .iterator();
                                    Metadata metadata = iterator.next();
                                    driveId = metadata.getDriveId();
                                    new RetrieveDriveFileContentsAsyncTask(
                                            getActivity(), metadata.getTitle())
                                            .execute(driveId);
                                } else {
                                    filesRestored++;
                                    dismissDialog(R.string.db_file_not_present);
                                }
                                metadataBuffer.release();
                            }

                        });

        query = new Query.Builder().addFilter(
                Filters.eq(SearchableField.TITLE,
                        Constants.BACKUP_SETTINGS_FILE_NAME)).build();

        driveFolder.queryChildren(getGoogleApiClient(), query)
                .setResultCallback(
                        new ResultCallback<DriveApi.MetadataBufferResult>() {

                            @Override
                            public void onResult(MetadataBufferResult result) {
                                int count = result.getMetadataBuffer()
                                        .getCount();
                                Log.d(TAG, "Count of settings file - " + count);
                                DriveId driveId;
                                MetadataBuffer metadataBuffer = result
                                        .getMetadataBuffer();
                                if (count == 1) {
                                    // Already present
                                    Log.d(TAG, "Settings File already present");
                                    Iterator<Metadata> iterator = metadataBuffer
                                            .iterator();
                                    Metadata metadata = iterator.next();
                                    driveId = metadata.getDriveId();
                                    new RetrieveDriveFileContentsAsyncTask(
                                            getActivity(), metadata.getTitle())
                                            .execute(driveId);
                                } else if (count == 0) {
                                    filesRestored++;
                                    dismissDialog(R.string.settings_file_not_present);
                                }
                                metadataBuffer.release();
                            }

                        });
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION
                && resultCode == Activity.RESULT_OK) {
            googleApiClient.connect();
        }
    }

    private void dismissDialog(Throwable tr) {
        if (tr != null)
            Log.e(TAG, "Exception occured", tr);
        showMessage(R.string.problem_connect_gdrive);
        dismiss();
    }

    private void dismissDialog(int message) {
        filesRestored++;
        Log.d(TAG, "" + message + filesRestored);
        if (filesRestored == 2) {
            showMessage(message);
            dismiss();
        }
    }

    private void showMessage(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    final private class RetrieveDriveFileContentsAsyncTask extends
            ApiClientAsyncTask<DriveId, Boolean, Boolean> {

        private String fileName;
        private Context context;

        public RetrieveDriveFileContentsAsyncTask(Context context,
                                                  String fileName) {
            super(context);
            this.fileName = fileName;
            this.context = context;
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveId... params) {
//            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(),
//                    params[0]);
            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult contentsResult = file.open(
                    getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                    .await();
            if (!contentsResult.getStatus().isSuccess()) {
                return false;
            }
            InputStream inputStream = contentsResult.getDriveContents()
                    .getInputStream();

            // Check fileName and write it
            if (fileName.equals(Constants.BACKUP_DB_FILE_NAME)) {
                byte[] content;
                byte[] buffer = new byte[2048];
                boolean success;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    while (inputStream.read(buffer) != -1) {
                        baos.write(buffer);
                    }
                    content = baos.toByteArray();
                    contentsResult.getDriveContents().discard(getGoogleApiClient());
                    success = getUtility().replaceDb(content);
                    if (success) {
                        HydrateContentProvider hydrateContentProvider = (HydrateContentProvider) context
                                .getContentResolver()
                                .acquireContentProviderClient(
                                        HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS)
                                .getLocalContentProvider();
                        hydrateContentProvider.resetDatabase();
                        startReminders();
                        notifyLoaders();

                    }
                    return success;
                } catch (Exception e) {
                    Log.e(TAG, "IOException while reading from the stream", e);
                    return false;
                }
            } else if (fileName.equals(Constants.BACKUP_SETTINGS_FILE_NAME)) {
                ObjectInputStream ois;
                SharedPreferences.Editor prefEdit = PreferenceManager
                        .getDefaultSharedPreferences(context).edit();
                Map<String, ?> entries;
                try {
                    ois = new ObjectInputStream(inputStream);

                    entries = (Map<String, ?>) ois.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    Log.e(getClass().getSimpleName(), "Exception - ", e);
                    return false;
                }
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
                contentsResult.getDriveContents().discard(getGoogleApiClient());
                return true;
            }
            contentsResult.getDriveContents().discard(getGoogleApiClient());
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result == null || !result) {
                showMessage(R.string.problem_connect_gdrive);
                dismissDialog(null);
                return;
            }
            dismissDialog(R.string.restore_complete);
        }

        private void notifyLoaders() {
            context.getContentResolver().notifyChange(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS, null);
            context.getContentResolver().notifyChange(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS, null);
            context.getContentResolver().notifyChange(
                    HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET, null);
        }

        private void startReminders() {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            boolean reminders = preferences.getBoolean(
                    context.getString(R.string.key_reminders_status), false);
            if (reminders)
                new AlarmReceiver().setNextAlarm(context);
        }
    }
}
