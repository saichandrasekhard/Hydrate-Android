package com.underdog.hydrate.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.underdog.hydrate.R;
import com.underdog.hydrate.async.ApiClientAsyncTask;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.util.Log;
import com.underdog.hydrate.util.Utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

public class DriveBackupDialog extends DialogFragment implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = DriveBackupDialog.class.toString();
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private int filesBackedUp = 0;
    final private ResultCallback<DriveFileResult> dbBackupCallBack = new ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create the file");
                dismissDialog(null);
                return;
            }

            dismissDialog();
        }
    };
    final private ResultCallback<DriveFileResult> settingsBackupCallBack = new ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create the file");
                dismissDialog(null);
                return;
            }

            dismissDialog();
        }
    };
    private Utility utility;
    private GoogleApiClient googleApiClient;
    final private ResultCallback<DriveApi.DriveContentsResult> contentsDBCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            MetadataChangeSet changeSet;
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create the file");
                dismissDialog(null);
                return;
            }
            DriveFolder driveFolder = Drive.DriveApi
                    .getAppFolder(getGoogleApiClient());
            Log.d(TAG, "Drive Folder - " + driveFolder);

            try {
                changeSet = new MetadataChangeSet.Builder()
                        .setTitle(Constants.BACKUP_DB_FILE_NAME)
                        .setMimeType("text/plain").build();

                Log.d(this.getClass().getSimpleName(), new String(getUtility()
                        .getDBFileContent()));
                result.getDriveContents().getOutputStream()
                        .write(getUtility().getDBFileContent());
                driveFolder.createFile(getGoogleApiClient(), changeSet,
                        result.getDriveContents()).setResultCallback(
                        dbBackupCallBack);
            } catch (Exception e) {
                Log.e(TAG, "Exception occured", e);
            }
        }
    };
    final private ResultCallback<DriveApi.DriveContentsResult> contentsSettingsCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            MetadataChangeSet changeSet;
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create the file");
                dismissDialog(null);
                return;
            }
            DriveFolder driveFolder = Drive.DriveApi
                    .getAppFolder(getGoogleApiClient());
            Map<String, ?> settings = PreferenceManager
                    .getDefaultSharedPreferences(getActivity()).getAll();
            Log.d(TAG, "Drive Folder - " + driveFolder);

            try {

                changeSet = new MetadataChangeSet.Builder()
                        .setTitle(Constants.BACKUP_SETTINGS_FILE_NAME)
                        .setMimeType("text/plain").build();

                new ObjectOutputStream(result.getDriveContents().getOutputStream())
                        .writeObject(settings);

                driveFolder.createFile(getGoogleApiClient(), changeSet,
                        result.getDriveContents()).setResultCallback(
                        settingsBackupCallBack);

            } catch (Exception e) {
                Log.e(TAG, "Exception occured", e);
            }

        }
    };
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

        filesBackedUp = 0;
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
        //getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new Builder(getActivity(), R.style.customAlert);
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.progress_layout, null);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
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
                                    DriveFile file = driveId.asDriveFile();
                                    file.open(getGoogleApiClient(),
                                            DriveFile.MODE_WRITE_ONLY, null);
                                    new EditContentsAsyncTask(getActivity(),
                                            metadata.getTitle()).execute(file);
                                } else if (count == 0) {
                                    Log.d(TAG, "DB File not present");
                                    // Not present.So create new file
                                    Drive.DriveApi.newDriveContents(
                                            getGoogleApiClient())
                                            .setResultCallback(
                                                    contentsDBCallBack);
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
                                    DriveFile file = driveId.asDriveFile();
                                    file.open(getGoogleApiClient(),
                                            DriveFile.MODE_WRITE_ONLY, null);
                                    new EditContentsAsyncTask(getActivity(),
                                            metadata.getTitle()).execute(file);
                                } else if (count == 0) {
                                    // Not present.So create new file
                                    Log.d(TAG, "Settings File not present");
                                    Drive.DriveApi.newDriveContents(
                                            getGoogleApiClient())
                                            .setResultCallback(
                                                    contentsSettingsCallBack);
                                }
                                metadataBuffer.release();
                            }

                        });
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(),
                        REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(
                    connectionResult.getErrorCode(), getActivity(), 0).show();
            this.dismiss();
        }
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

    private void dismissDialog() {
        filesBackedUp++;
        Log.d(TAG, "Succesfully synced - " + filesBackedUp);
        if (filesBackedUp == 2) {
            showMessage(R.string.backup_complete);
            dismiss();
        }
    }

    private void showMessage(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public class EditContentsAsyncTask extends
            ApiClientAsyncTask<DriveFile, Void, Boolean> {

        private String fileName;
        private Context context;

        public EditContentsAsyncTask(Context context, String fileName) {
            super(context);
            this.fileName = fileName;
            this.context = context;
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];

            try {
                Map<String, ?> settings = PreferenceManager
                        .getDefaultSharedPreferences(context).getAll();
                Log.d(TAG, "fileName - " + fileName);
                DriveApi.DriveContentsResult contentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null)
                        .await();
                if (!contentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents driveContents = contentsResult.getDriveContents();
                OutputStream outputStream = driveContents
                        .getOutputStream();
                Log.d(TAG, "got outputstream");
                if (fileName.equals(Constants.BACKUP_DB_FILE_NAME)) {
                    // Write DB File Content
                    outputStream.write(getUtility().getDBFileContent());
                } else {
                    // Write settings file content
                    new ObjectOutputStream(outputStream).writeObject(settings);
                }
                com.google.android.gms.common.api.Status status = driveContents.commit(getGoogleApiClient(), null).await();
                return status.getStatus().isSuccess();
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream",
                        e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == null || !result) {
                dismissDialog(null);
                return;
            }
            dismissDialog();
        }
    }

}
