package com.underdog.hydrate;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.underdog.hydrate.async.SummaryAsyncTask;
import com.underdog.hydrate.constants.Constants;

public class SummaryActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_summary);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SummaryFragment()).commit();
		}
	}

	/**
	 * Fragment to display the summary of app usage
	 */
	public static class SummaryFragment extends Fragment {

		/** The view to show the ad. */
		private AdView adView;

        public SummaryFragment() {
        }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_summary,
					container, false);
		}

		/**
		 * Once activity is created,this method is called
		 */
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			SummaryAsyncTask summaryAsyncTask;

			summaryAsyncTask = new SummaryAsyncTask(getActivity());
			summaryAsyncTask.execute("");

			// Create an ad.
			adView = new AdView(this.getActivity());
			adView.setAdSize(AdSize.BANNER);
			adView.setAdUnitId(Constants.AD_UNIT_ID);
			adView.setVisibility(View.VISIBLE);

			// Add the AdView to the view hierarchy. The view will have no size
			// until the ad is loaded.
			LinearLayout layout = (LinearLayout) getActivity().findViewById(
					R.id.adLayoutSummary);
			layout.addView(adView);

			// Create an ad request. Check logcat output for the hashed device
			// ID to
			// get test ads on a physical device.
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("B0E58BFA678C367F782946106E3FDB62").build();

			// Start loading the ad in the background.
			adView.loadAd(adRequest);

			super.onActivityCreated(savedInstanceState);
		}

		@Override
		public void onResume() {
			super.onResume();
			if (adView != null) {
				adView.resume();
			}
		}

		@Override
		public void onPause() {
			if (adView != null) {
				adView.pause();
			}
			super.onPause();
		}

		public void onDestroy() {
			// Destroy the AdView.
			if (adView != null) {
				adView.destroy();
			}
			super.onDestroy();
		}
	}

}
