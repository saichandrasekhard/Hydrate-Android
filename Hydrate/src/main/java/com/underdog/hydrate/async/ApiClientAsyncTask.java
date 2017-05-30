// Copyright 2013 Google Inc. All Rights Reserved.

package com.underdog.hydrate.async;

import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

/**
 * An AsyncTask that maintains a connected client.
 */
public abstract class ApiClientAsyncTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	private GoogleApiClient mClient;
	private Context context;

	public ApiClientAsyncTask(Context context) {
		this.context = context;
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
				.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER);
		mClient = builder.build();
	}

	@Override
	protected final Result doInBackground(Params... params) {
		Log.d("TAG", "in background");
		final CountDownLatch latch = new CountDownLatch(1);
		mClient.registerConnectionCallbacks(new ConnectionCallbacks() {
			@Override
			public void onConnectionSuspended(int cause) {
			}

			@Override
			public void onConnected(Bundle arg0) {
				Log.d("TAG", "connected");
				latch.countDown();
			}
		});
		mClient.registerConnectionFailedListener(new OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult connectionResult) {
				Log.d("TAG",
						"connection failed - "
								+ connectionResult.getErrorCode());
				GooglePlayServicesUtil.getErrorDialog(
						connectionResult.getErrorCode(), (Activity) context, 0)
						.show();
				latch.countDown();
			}
		});
		mClient.connect();
		try {
			latch.await();
		} catch (InterruptedException e) {
			return null;
		}
		if (!mClient.isConnected()) {
			Log.d("TAG", "Not connected");
			return null;
		}
		try {
			com.underdog.hydrate.util.Log.d("TAG", "calling doInBackground");
			return (Result) doInBackgroundConnected(params);
		} finally {
			mClient.disconnect();
		}
	}

	/**
	 * Override this method to perform a computation on a background thread,
	 * while the client is connected.
	 */
	protected abstract Result doInBackgroundConnected(Params... params);

	/**
	 * Gets the GoogleApliClient owned by this async task.
	 */
	protected GoogleApiClient getGoogleApiClient() {
		return mClient;
	}
}
