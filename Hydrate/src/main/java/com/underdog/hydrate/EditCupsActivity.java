package com.underdog.hydrate;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.fragments.EditCupsDialog;

/**
 * Activity that allows editing the cup capacities
 */
public class EditCupsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cups);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<Cursor> {

        /**
         * view to show the ad.
         */
        private AdView adView;

        //Unique id for the content loader
        private static final int CUP_LOADER_ID = 1;

        private SimpleCursorAdapter cursorAdapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //Load the relevant fragment
            View rootView = inflater.inflate(R.layout.fragment_edit_cups,
                    container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            //Get the edit cups list view to show the existing capacities
            final ListView listView = (ListView) getActivity().findViewById(
                    R.id.editCupsListView);

            // Create an ad.
            adView = new AdView(this.getActivity());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(Constants.AD_UNIT_ID);
            adView.setVisibility(View.VISIBLE);

            // Add the AdView to the view hierarchy. The view will have no size
            // until the ad is loaded.
            LinearLayout layout = (LinearLayout) getActivity().findViewById(
                    R.id.adLayoutEdit);
            layout.addView(adView);

            // Create an ad request. Check logcat output for the hashed device
            // ID to
            // get test ads on a physical device.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("B0E58BFA678C367F782946106E3FDB62").build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long rowId) {
                    EditCupsDialog editCupsDialog;

                    //Arguments that need to be sent to the edit cups dialog
                    //to show existing values
                    Bundle args = new Bundle();

                    // Get Values
                    args.putCharSequence(Constants.QUANTITY, ((TextView) view
                            .findViewById(R.id.editCupTextView)).getText()
                            .toString());

                    args.putLong(HydrateDatabase.ROW_ID, rowId);

                    args.putLong(Constants.POSITION, position);

                    // Show Dialog
                    editCupsDialog = new EditCupsDialog();
                    editCupsDialog.setArguments(args);
                    editCupsDialog.show(
                            fragmentManager.beginTransaction(),
                            "editCupsDialog");

                }
            });

            //Initiate the loader
            getLoaderManager().initLoader(CUP_LOADER_ID, null, this);

            cursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.edit_cups_listview_layout, null, new String[]{
                    HydrateDatabase.COLUMN_QUANTITY, "_id"},
                    new int[]{R.id.editCupTextView, R.id.editcupsImage},
                    CursorAdapter.NO_SELECTION);

            //View binder to customize how content is displayed
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

                @Override
                public boolean setViewValue(View view, Cursor cursor, int column) {
                    Context context = getActivity().getApplicationContext();
                    int position;

                    if (column == 0) {

                        SharedPreferences preferences = PreferenceManager
                                .getDefaultSharedPreferences(context);
                        String milliliter = context
                                .getString(R.string.milliliter);
                        String metric = preferences.getString(
                                context.getString(R.string.key_metric),
                                milliliter);

                        TextView textView = (TextView) view;
                        if (metric.equals(milliliter)) {

                            textView.setText(String.valueOf(cursor.getInt(0))
                                    + " "
                                    + getActivity().getString(R.string.ml));
                        } else {
                            textView.setText(String.valueOf(cursor.getInt(0))
                                    + " "
                                    + getActivity().getString(R.string.oz));
                        }
                        return true;
                    } else {
                        // Set image
                        position = cursor.getPosition();
                        ImageView imageView = (ImageView) view;
                        if (position == 0) {
                            imageView
                                    .setBackgroundResource(R.drawable.ic_menu_home_glass_1);
                        } else if (position == 1) {
                            imageView
                                    .setBackgroundResource(R.drawable.ic_menu_home_glass_2);
                        } else if (position == 2) {
                            imageView
                                    .setBackgroundResource(R.drawable.ic_menu_home_glass_3);
                        } else if (position == 3) {
                            imageView
                                    .setBackgroundResource(R.drawable.ic_menu_home_glass_4);
                        } else {
                            imageView
                                    .setBackgroundResource(R.drawable.ic_menu_home_glass_5);
                        }
                        return true;
                    }
                }
            });
            listView.setAdapter(cursorAdapter);
        }

        //Called after loader.init
        @Override
        public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == CUP_LOADER_ID) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS,
                        new String[]{HydrateDatabase.COLUMN_QUANTITY,
                                HydrateDatabase.ROW_ID + " AS _id"}, null,
                        null, null);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (loader.getId() == CUP_LOADER_ID) {
                cursorAdapter.swapCursor(cursor);
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (loader.getId() == CUP_LOADER_ID) {
                cursorAdapter.swapCursor(null);
            }
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
