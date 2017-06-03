package com.underdog.hydrate;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.underdog.hydrate.constants.Constants;

/**
 * Class that launches a blank screen activity to post a notification to share the daily achievement
 * with others
 */
public class BlankShareActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent shareIntent;
		SharedPreferences preferences;
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int notificationId = Constants.NOTIFICATION_TARGET_ACHIEVED_ID;
		Intent activityChooser;

		shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT,
				getShareMessageForTarget(preferences));
		shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		activityChooser = Intent.createChooser(shareIntent,
				getString(R.string.select_app));
		activityChooser.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(activityChooser);

		notificationManager.cancel(notificationId);
		finish();
	}

    /**
     * Method that forms the message content for sharing
     * @param preferences
     * @return
     */
	public String getShareMessageForTarget(SharedPreferences preferences) {
		String targetReachedMessage = getString(R.string.share_message_target_reached);
		String milliliter = getString(R.string.milliliter);
		String metric = preferences.getString(getString(R.string.key_metric),
				milliliter);
		String targetQuantity = preferences.getString(
				getString(R.string.key_target), null);
		targetReachedMessage = targetReachedMessage.replace("$$",
				targetQuantity);
		if (metric.equals(milliliter)) {
			targetReachedMessage += " " + getString(R.string.liters);
		} else {
			targetReachedMessage += " " + getString(R.string.oz);
		}
		targetReachedMessage += "\n \n" + getString(R.string.checkout);
		final String finalMessage = targetReachedMessage;
		return finalMessage;
	}
}
