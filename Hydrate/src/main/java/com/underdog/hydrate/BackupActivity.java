package com.underdog.hydrate;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity that loads the backup/restore screen
 */
public class BackupActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.backup_container, new PlaceholderFragment())
					.commit();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends PreferenceFragment {
		

		public PlaceholderFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.backup);
		}
	}
}
