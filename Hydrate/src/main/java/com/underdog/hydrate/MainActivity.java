package com.underdog.hydrate;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.database.HydrateContentProvider;
import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.database.HydrateDatabase;
import com.underdog.hydrate.fragments.InstantMuteDialog;
import com.underdog.hydrate.fragments.ListViewEditDialog;
import com.underdog.hydrate.fragments.RestoreDialog;
import com.underdog.hydrate.fragments.TargetAchievedDialog;
import com.underdog.hydrate.receiver.AlarmReceiver;
import com.underdog.hydrate.util.Log;
import com.underdog.hydrate.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String tag = "MainActivity";
    AlarmReceiver alarmReceiver;
    private HydrateDAO dao;
    private Utility utility;
    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

    public Utility getUtility() {
        if (utility == null) {
            utility = Utility.getInstance();
        }
        return utility;
    }

    /**
     * @return the alarmReceiver
     */
    public AlarmReceiver getAlarmReceiver() {
        if (alarmReceiver == null) {
            alarmReceiver = new AlarmReceiver();
        }
        return alarmReceiver;
    }

    /**
     * @return the dao
     */
    public HydrateDAO getDao() {
        if (dao == null) {
            dao = new HydrateDAO(getApplicationContext());
        }
        return dao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_container);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.main_container, new HomeScreenFragment())
                    .commit();
        }
    }

    protected void onStart() {
        super.onStart();
        SharedPreferences preferences;
        String userName;
        TextView welcomeMessage;
        TextView metricView;
        String metric;
        SharedPreferences.Editor editor;
        String ml = this.getString(R.string.milliliter);

        Log.i(tag, "Called");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        userName = preferences.getString(
                this.getString(R.string.key_user_name), "User");

        welcomeMessage = (TextView) findViewById(R.id.hello_user);
        welcomeMessage
                .setText(getString(R.string.hello) + " " + userName + ",");

        metricView = (TextView) findViewById(R.id.water_quantity_unit);
        metric = preferences.getString(this.getString(R.string.key_metric), ml);
        if (metric.equals(ml)) {
            metricView.setText(this.getString(R.string.liters));
        } else {
            Log.i(tag, "Called");
            metricView.setText(this.getString(R.string.oz));
        }

        if (!preferences.contains(Constants.FIRST_RUN)) {
            editor = preferences.edit();

            // Make first run as false
            editor.putBoolean(Constants.FIRST_RUN, false);
            editor.apply();

            //Ask for write external permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // External storage permission has not been granted.
                requestStoragePermission(Constants.REQUEST_WRITE_EXTERNAL_STARTUP);

            } else {

                // External storage permissions is already available.
                Log.i(tag,
                        "External storage permission has already been granted.");
                //Show restore dialog
                if (getUtility().isBackupAvailable()) {
                    // Show dialog asking to restore
                    RestoreDialog restoreDialog = new RestoreDialog();
                    restoreDialog.show(getFragmentManager().beginTransaction(),
                            "Restore dialog");
                }
            }

            getAlarmReceiver().resetAlarms(this.getApplicationContext());

        } /*else if (Constants.googlePlay) {
            // Monitor launch times and interval from installation
            RateThisApp.onStart(this);
            // If the criteria is satisfied, "Rate this app" dialog will be
            // shown
            RateThisApp.showRateDialogIfNeeded(this);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.i(tag, "Settings button clicked");
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.summary) {
            Log.i(tag, "Summary menu button clicked");
            intent = new Intent(this, SummaryActivity.class);
            startActivity(intent);
        } else if (id == R.id.backup) {
            //Ask for write external permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // External storage permission has not been granted.
                requestStoragePermission(Constants.REQUEST_WRITE_EXTERNAL_OVERFLOW);

            } else {
                //Already have storage permission
                intent = new Intent(this, BackupActivity.class);
                startActivity(intent);
            }
        } else if (id == R.id.customizeCups) {
            intent = new Intent(this, EditCupsActivity.class);
            startActivity(intent);
        } else if (id == R.id.instant_mute) {
            InstantMuteDialog instantMuteDialog = new InstantMuteDialog();
            instantMuteDialog.show(getFragmentManager().beginTransaction(),
                    "Instant Mute");
        } else if (id == R.id.rate_hydrate) {
            intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://play.google.com/store/apps/details?id="
                            + getPackageName()));
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Increase the count of water based on button click
     *
     * @param view
     */
    public void increaseWater(View view) {
        SharedPreferences sharedPreferences;
        boolean reminderStatus;
        String quantity;
        boolean targetAchieved;
        TargetAchievedDialog targetAchievedDialog;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        switch (view.getId()) {
            case R.id.add_water1:
                quantity = ((TextView) findViewById(R.id.addWaterText1)).getText()
                        .toString().split("\\s+")[0];

                break;

            case R.id.add_water2:
                quantity = ((TextView) findViewById(R.id.addWaterText2)).getText()
                        .toString().split("\\s+")[0];

                break;

            case R.id.add_water3:
                quantity = ((TextView) findViewById(R.id.addWaterText3)).getText()
                        .toString().split("\\s+")[0];

                break;

            case R.id.add_water4:
                quantity = ((TextView) findViewById(R.id.addWaterText4)).getText()
                        .toString().split("\\s+")[0];

                break;

            case R.id.add_water5:
                quantity = ((TextView) findViewById(R.id.addWaterText5)).getText()
                        .toString().split("\\s+")[0];

                break;

            default:
                quantity = sharedPreferences
                        .getString(getString(R.string.key_glass_quantity),
                                String.valueOf(250));
                break;
        }
        // check if target is achieved
        targetAchieved = getUtility().isTargetAchieved(
                (TextView) findViewById(R.id.water_quantity_status),
                (TextView) findViewById(R.id.water_target), quantity,
                getApplicationContext());

        // Save the water in DB
        getDao().addWater(System.currentTimeMillis(), quantity);
        notificationManager.cancel(Constants.NOTIFICATION_ID);

        if (targetAchieved) {
            // Show dialog
            targetAchievedDialog = new TargetAchievedDialog();
            targetAchievedDialog.show(getFragmentManager().beginTransaction(),
                    "targetAchievedFragmentation");
        }

        reminderStatus = sharedPreferences.getBoolean(
                this.getString(R.string.reminders_status), false);

        if (reminderStatus) {

            // Set a reminder
            Log.i(tag, "Water increased.Setting reminder");
            getAlarmReceiver().setNextAlarm(this);
        }
    }

    /**
     * Decrease the count of water based on button click.
     * Not used
     *
     * @param view
     */
    public void decreaseWater(View view) {
        getDao().deleteWater();
    }


    /**
     * Requests the storage permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestStoragePermission(final int code) {
        Log.i(tag, "Storage permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(storage_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(tag,
                    "Displaying storage permission rationale to provide additional context.");

            RequestPermissionDialog requestPermissionDialog = new RequestPermissionDialog();
            Bundle args = new Bundle();
            args.putInt("code", code);
            requestPermissionDialog.setArguments(args);
            requestPermissionDialog.show(getFragmentManager().beginTransaction(), "Request Permission");
        } else {
            Log.i(tag, "No need to display rationale. Asking for permission");
            // Storage permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code);
        }
        // END_INCLUDE(storage_permission_request)
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == Constants.REQUEST_WRITE_EXTERNAL_STARTUP || requestCode == Constants.REQUEST_WRITE_EXTERNAL_OVERFLOW) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for external storage.
            Log.i(tag, "Received response for external storage permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // storage permission has been granted, preview can be displayed
                Log.i(tag, "external storage permission has now been granted. Showing preview.");
//                Snackbar.make(mLayout, R.string.permission_storage_available,
//                        Snackbar.LENGTH_SHORT).show();
                Toast.makeText(this, R.string.permission_storage_available, Toast.LENGTH_SHORT).show();

                //Show restore dialog
                if (requestCode == Constants.REQUEST_WRITE_EXTERNAL_STARTUP && getUtility().isBackupAvailable()) {
                    // Show dialog asking to restore
                    RestoreDialog restoreDialog = new RestoreDialog();
                    restoreDialog.show(getFragmentManager().beginTransaction(),
                            "Restore dialog");
                } else if (requestCode == Constants.REQUEST_WRITE_EXTERNAL_OVERFLOW) {
                    Intent intent = new Intent(this, BackupActivity.class);
                    startActivity(intent);
                }
            } else {
                Log.i(tag, "external storage permission was NOT granted.");
//                Snackbar.make(mLayout, ,
//                        Snackbar.LENGTH_SHORT).show();
                Toast.makeText(this, R.string.permission_storage_not_granted, Toast.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        }
    }

    /**
     * The main view that contains drink summary and drink/un-drink buttons
     *
     * @author Sekhar
     */
    public static class HomeScreenFragment extends Fragment implements
            LoaderCallbacks<Cursor> {

        // Loader for List View loader
        private static final int LIST_VIEW_LOADER_ID = 1;

        // Loader for Drink summary
        private static final int DRINK_SUMMARY_LOADER_ID = 2;

        private static final int CUPS_LOADER_ID = 3;
        SimpleCursorAdapter cursorAdapter;
        /**
         * The view to show the ad.
         */
        private AdView adView;
        private DatePickerDialog datePickerDialog;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.app.Fragment#onCreate(android.os.Bundle)
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // setRetainInstance(true);
            super.onCreate(savedInstanceState);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle)
         */
        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(Constants.DATE, ((TextView) getActivity()
                    .findViewById(R.id.dateView)).getText().toString());
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Log.i(tag, "On ActivityCreated - " + getActivity().getExternalFilesDir(null));
            ImageButton previous;
            ImageButton next;
            String date;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Constants.DATE_FORMAT);
            final TextView dateView = (TextView) getActivity().findViewById(
                    R.id.dateView);
            final ImageButton calendarButton = (ImageButton) getActivity()
                    .findViewById(R.id.calenderButton);

            if (savedInstanceState != null) {
                date = savedInstanceState.getString(Constants.DATE);
            } else {
                date = ((MainActivity) getActivity()).getUtility().getDate(
                        System.currentTimeMillis());
            }
            dateView.setText(date);

            calendarButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    datePickerDialog = new DatePickerDialog(getActivity(), R.style.customAlert,
                            null, calendar.get(Calendar.YEAR), calendar
                            .get(Calendar.MONTH), calendar
                            .get(Calendar.DATE));
                    datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                            getString(R.string.ok),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    DatePicker datePicker = ((DatePickerDialog) dialog)
                                            .getDatePicker();
                                    String dateText;
                                    TextView dateView = (TextView) getActivity()
                                            .findViewById(R.id.dateView);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                                            Constants.DATE_FORMAT);
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(datePicker.getYear(),
                                            datePicker.getMonth(),
                                            datePicker.getDayOfMonth());
                                    dateText = dateFormat.format(calendar
                                            .getTime());
                                    dateView.setText(dateText);

                                    restartLoaders();
                                    dialog.dismiss();
                                    Log.i(tag, "date set");
                                }
                            });
                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                            getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            });
                    datePickerDialog.show();
                }
            });

            // Set button listeners for list view previous and next
            previous = (ImageButton) getActivity().findViewById(R.id.prev);
            previous.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    long timestamp = 0;
                    String dateText;
                    try {
                        timestamp = dateFormat.parse(
                                dateView.getText().toString()).getTime();
                    } catch (ParseException e) {
                        Log.e(tag, "Parse exception occurred", e);
                    }
                    dateText = dateFormat.format(new Date(timestamp
                            - Constants.DAY_HOURS_LONG));
                    // Change the date in date view
                    dateView.setText(dateText);

                    restartLoaders();
                }
            });

            next = (ImageButton) getActivity().findViewById(R.id.next);
            next.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    long timestamp = 0;
                    String dateText;

                    try {
                        timestamp = dateFormat.parse(
                                dateView.getText().toString()).getTime();
                    } catch (ParseException e) {
                        Log.e(tag, "Parse exception occurred", e);
                    }

                    dateText = dateFormat.format(new Date(timestamp
                            + Constants.DAY_HOURS_LONG));
                    // Change the date in date view
                    dateView.setText(dateText);

                    restartLoaders();
                }
            });

            /*edit = (ImageButton) getActivity().findViewById(R.id.editCups);
            edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(getActivity(),
                            EditCupsActivity.class);
                    startActivity(intent);
                }
            });*/

            // Initiate a loader for List view population
            getLoaderManager().initLoader(LIST_VIEW_LOADER_ID, null, this);

            // Initiate a loader for sumsmary population
            getLoaderManager().initLoader(DRINK_SUMMARY_LOADER_ID, null, this);

            // Initiate a loader for summary population
            getLoaderManager().initLoader(CUPS_LOADER_ID, null, this);

            // Set today's drink events in list view
            setDrinkEvents();

            // Create an ad.
            adView = new AdView(this.getActivity());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(Constants.AD_UNIT_ID);
            adView.setVisibility(View.VISIBLE);

            // Add the AdView to the view hierarchy. The view will have no size
            // until the ad is loaded.
            LinearLayout layout = (LinearLayout) getActivity().findViewById(
                    R.id.adLayoutMain);
            layout.addView(adView);

            // Create an ad request. Check logcat output for the hashed device
            // ID to
            // get test ads on a physical device.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("B0E58BFA678C367F782946106E3FDB62").build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);

        }

        @Override
        public void onStop() {
            if (datePickerDialog != null) {
                datePickerDialog.dismiss();
            }
            super.onStop();
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
            Log.i(tag, "Destroyed");
        }

        /**
         * Method to restart the loaders
         */
        private void restartLoaders() {
            getLoaderManager().restartLoader(LIST_VIEW_LOADER_ID, null,
                    HomeScreenFragment.this);
            getLoaderManager().restartLoader(DRINK_SUMMARY_LOADER_ID, null,
                    HomeScreenFragment.this);
        }

        /**
         * Method to initiate a list view with all events for the day
         */
        private void setDrinkEvents() {
            ListView listView;

            int[] to = {R.id._id, R.id.quantityConsumed, R.id.time};

            MainActivity activity = (MainActivity) getActivity();

            listView = (ListView) activity.findViewById(R.id.dayLog);
            cursorAdapter = new SimpleCursorAdapter(activity,
                    R.layout.today_log_layout, null,
                    HydrateDatabase.EVENT_COLUMNS, to,
                    CursorAdapter.NO_SELECTION);

            // Customize the contents before populating the list view
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

                @Override
                public boolean setViewValue(View view, Cursor cursor, int column) {
                    TextView textView;
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            Constants.TIME_FORMAT);
                    String value = null;
                    SharedPreferences preferences;
                    String metric;
                    if (column == 2) {
                        textView = (TextView) view;
                        value = dateFormat.format(new Date(cursor
                                .getLong(column)));
                        textView.setText(value);
                        return true;
                    } else if (column == 1) {
                        textView = (TextView) view;
                        preferences = PreferenceManager
                                .getDefaultSharedPreferences(getActivity()
                                        .getApplicationContext());
                        metric = preferences.getString(
                                getString(R.string.key_metric),
                                getString(R.string.milliliter));
                        if (metric.equals(getString(R.string.milliliter))) {
                            value = ((int) cursor.getDouble(column)) + " "
                                    + getString(R.string.ml);
                        } else {
                            value = ((int) cursor.getDouble(column)) + " "
                                    + getString(R.string.oz);
                        }

                        textView.setText(value);
                        return true;
                    }
                    return false;
                }
            });

            // Set the cursor adapter to the listView
            listView.setAdapter(cursorAdapter);

            // Set listView dialog
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long rowId) {
                    Bundle inputExtras;
                    RelativeLayout layout = (RelativeLayout) view;
                    inputExtras = new Bundle();
                    TextView textView = (TextView) layout.getChildAt(2);
                    TextView dateView;

                    // Set quantity
                    inputExtras.putString(Constants.QUANTITY, textView
                            .getText().toString());

                    // Set time
                    textView = (TextView) layout.getChildAt(1);
                    inputExtras.putString(Constants.TIME, textView.getText()
                            .toString());

                    // Set Date
                    dateView = (TextView) getActivity().findViewById(
                            R.id.dateView);
                    inputExtras.putString(Constants.DATE, dateView.getText()
                            .toString());

                    // Set rowId
                    inputExtras.putLong(HydrateDatabase.ROW_ID, rowId);

                    ListViewEditDialog dialogFragment = new ListViewEditDialog();
                    dialogFragment.setArguments(inputExtras);
                    dialogFragment.show(
                            getFragmentManager().beginTransaction(),
                            "allEventsEditDialog");
                }
            });
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS;
            String[] selectionArgs = null;
            TextView dateView = (TextView) getActivity().findViewById(
                    R.id.dateView);
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Constants.DATE_FORMAT);

            switch (id) {
                case LIST_VIEW_LOADER_ID:
                    // Form the start and end time for results
                    try {
                        selectionArgs = Utility.getInstance()
                                .getSelectionArgsForDay(dateFormat.parse(
                                        dateView.getText().toString()).getTime());
                    } catch (ParseException e) {
                        Log.e(tag, "Parse exeption occurred", e);
                    }
                    return new CursorLoader(getActivity().getApplicationContext(),
                            uri, HydrateDatabase.EVENT_COLUMNS,
                            HydrateDatabase.FROM_TO_TIME, selectionArgs,
                            HydrateDatabase.COLUMN_TIMESTAMP + " DESC");
                case DRINK_SUMMARY_LOADER_ID:
                    // Form the start and end time for results
                    try {
                        selectionArgs = Utility.getInstance()
                                .getSelectionArgsForDay(dateFormat.parse(
                                        dateView.getText().toString()).getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return new CursorLoader(getActivity().getApplicationContext(),
                            uri, HydrateDatabase.SUMMARY_TOTAL_COLUMNS,
                            HydrateDatabase.FROM_TO_TIME, selectionArgs, null);

                case CUPS_LOADER_ID:
                    return new CursorLoader(getActivity().getApplicationContext(),
                            HydrateContentProvider.CONTENT_URI_HYDRATE_CUPS,
                            new String[]{HydrateDatabase.COLUMN_QUANTITY,
                                    HydrateDatabase.ROW_ID + " AS _id"}, null,
                            null, null);
                default:

                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            switch (loader.getId()) {
                case LIST_VIEW_LOADER_ID:
                    Log.i(tag, "OnLoadFinished for list view");
                    cursorAdapter.swapCursor(cursor);
                    break;

                case DRINK_SUMMARY_LOADER_ID:
                    Log.i(tag, "OnLoadFinished for Summary");
                    setWaterSummary(cursor);
                    break;

                case CUPS_LOADER_ID:
                    setCups(cursor);
                default:
                    break;
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            switch (loader.getId()) {
                case LIST_VIEW_LOADER_ID:
                    cursorAdapter.swapCursor(null);
                    break;

                default:
                    break;
            }

        }

        /**
         * Method to set water details in main fragment
         */
        private void setWaterSummary(Cursor cursor) {
            TextView textView;
            double quantityConsumed;
            int count;
            SharedPreferences preferences;
            String metric;
            double target;
            TextView targetTextView;
            String date = ((TextView) getActivity().findViewById(R.id.dateView))
                    .getText().toString();
            Cursor targetCursor;
            String dailyTarget;
            String milliliter = getActivity().getString(R.string.milliliter);

            cursor.moveToFirst();
            count = cursor.getInt(0);
            quantityConsumed = cursor.getFloat(1);

            preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity()
                            .getApplicationContext());
            metric = preferences.getString(this.getString(R.string.key_metric),
                    this.getString(R.string.milliliter));

            targetTextView = (TextView) getActivity().findViewById(
                    R.id.water_target);

            // Get today's date
            String currentDate = ((MainActivity) getActivity()).getUtility()
                    .getDate(System.currentTimeMillis());
            if (date.equals(currentDate)) {
                targetCursor = getActivity().getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                        new String[]{((MainActivity) getActivity()).getUtility().getToday() + ""}, null);
                targetCursor.moveToFirst();
                target = targetCursor.getDouble(0);
                targetCursor.close();
            } else {
                // Get it from database providing the date
                targetCursor = getActivity()
                        .getContentResolver()
                        .query(HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET,
                                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY},
                                HydrateDatabase.COLUMN_DATE + "=?",
                                new String[]{((MainActivity) getActivity())
                                        .getUtility().getSqliteDate(
                                        ((MainActivity) getActivity())
                                                .getUtility()
                                                .getTimeInMillis(date))},
                                null);
                if (targetCursor.getCount() > 0) {
                    targetCursor.moveToFirst();
                    target = targetCursor.getDouble(0);

                } else {
                    targetCursor = getActivity().getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                            new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                            new String[]{((MainActivity) getActivity()).getUtility().getToday() + ""}, null);
                    targetCursor.moveToFirst();
                    target = targetCursor.getDouble(0);
                }

            }
            if (metric.equals(milliliter)) {
                target /= 1000;
                target *= 100;
                target = Math.round(target);
                target /= 100;
                dailyTarget = String.valueOf(target);
            } else {
                target = Math.round(target);
                dailyTarget = String.valueOf((int) target);

            }
            targetTextView.setText(dailyTarget);

            // Set water quantity
            textView = (TextView) getActivity().findViewById(
                    R.id.water_quantity_status);
            if (metric.equals(this.getString(R.string.milliliter))) {
                quantityConsumed = quantityConsumed / 1000;
                quantityConsumed *= 100;
                quantityConsumed = Math.round(quantityConsumed);
                quantityConsumed /= 100;
                textView.setText(String.valueOf(quantityConsumed));
                setWaterStatusColor(textView, quantityConsumed, target);
            } else {
                quantityConsumed = Math.round(quantityConsumed);
                textView.setText(String.valueOf((int) quantityConsumed));
                setWaterStatusColor(textView, quantityConsumed, target);
            }

            // Set water cup count
            textView = (TextView) getActivity().findViewById(R.id.water_status);
            textView.setText(String.valueOf(count));

        }

        private void setWaterStatusColor(TextView textView, double consumed,
                                         double target) {
            if (consumed < (target * .75)) {
                textView.setTextColor(getActivity().getResources().getColor(
                        android.R.color.holo_red_dark));
            } else if (consumed < target) {
                textView.setTextColor(getActivity().getResources().getColor(
                        android.R.color.holo_orange_dark));
            } else {
                textView.setTextColor(getActivity().getResources().getColor(
                        android.R.color.holo_green_dark));
            }
        }

        private void setCups(Cursor cursor) {
            TextView textView;
            String milliliter = getActivity().getString(R.string.milliliter);
            String ml = getActivity().getString(R.string.ml);
            String oz = getActivity().getString(R.string.oz);
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String metric = preferences.getString(
                    getActivity().getString(R.string.key_metric), milliliter);

            cursor.moveToFirst();
            textView = (TextView) getActivity()
                    .findViewById(R.id.addWaterText1);
            if (metric.equals(milliliter)) {
                textView.setText(cursor.getInt(0) + " " + ml);
            } else {
                textView.setText(cursor.getInt(0) + " " + oz);
            }

            cursor.moveToNext();
            textView = (TextView) getActivity()
                    .findViewById(R.id.addWaterText2);
            if (metric.equals(milliliter)) {
                textView.setText(cursor.getInt(0) + " " + ml);
            } else {
                textView.setText(cursor.getInt(0) + " " + oz);
            }

            cursor.moveToNext();
            textView = (TextView) getActivity()
                    .findViewById(R.id.addWaterText3);
            if (metric.equals(milliliter)) {
                textView.setText(cursor.getInt(0) + " " + ml);
            } else {
                textView.setText(cursor.getInt(0) + " " + oz);
            }

            cursor.moveToNext();
            textView = (TextView) getActivity()
                    .findViewById(R.id.addWaterText4);
            if (metric.equals(milliliter)) {
                textView.setText(cursor.getInt(0) + " " + ml);
            } else {
                textView.setText(cursor.getInt(0) + " " + oz);
            }

            cursor.moveToNext();
            textView = (TextView) getActivity()
                    .findViewById(R.id.addWaterText5);
            if (metric.equals(milliliter)) {
                textView.setText(cursor.getInt(0) + " " + ml);
            } else {
                textView.setText(cursor.getInt(0) + " " + oz);
            }

        }
    }

    public static class RequestPermissionDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Bundle args = getArguments();
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setMessage(R.string.permission_storage_rationale);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int code = args.getInt("code");
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            code);
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            return builder.create();
        }
    }

}
