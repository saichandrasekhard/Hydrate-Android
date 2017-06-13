package com.underdog.hydrate;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.kobakei.ratethisapp.RateThisApp;
import com.underdog.hydrate.animation.ProgressBarAnimation;
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
import com.underdog.hydrate.widgets.OnSwipeTouchListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String tag = "MainActivity";
    private AlarmReceiver alarmReceiver;
    private NavigationView navigationView;

    /**
     * @return the alarmReceiver
     */
    public AlarmReceiver getAlarmReceiver() {
        if (alarmReceiver == null) {
            alarmReceiver = new AlarmReceiver();
        }
        return alarmReceiver;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!preferences.contains(Constants.FIRST_RUN)) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().add(R.id.main_container, new HomeScreenFragment()).commit();

        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this);
        // If the condition is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
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

        Log.d(tag, "Activity - onStart()");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        userName = preferences.getString(
                this.getString(R.string.key_user_name), "User");
        View headerView = navigationView.getHeaderView(0);
        welcomeMessage = (TextView) headerView.findViewById(R.id.hello_user);
        welcomeMessage
                .setText(userName);

        metricView = (TextView) findViewById(R.id.water_quantity_unit);
        metric = preferences.getString(this.getString(R.string.key_metric), ml);
        if (metric.equals(ml)) {
            metricView.setText(this.getString(R.string.liters));
        } else {
            Log.i(tag, "Called");
            metricView.setText(this.getString(R.string.oz));
        }

        if (!preferences.contains(Constants.FIRST_RUN)) {
            //Means first run
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
                Log.d(tag,
                        "External storage permission has already been granted.");
                //Show restore dialog
                if (Utility.getInstance().isBackupAvailable()) {
                    // Show dialog asking to restore
                    RestoreDialog restoreDialog = new RestoreDialog();
                    restoreDialog.show(getSupportFragmentManager(),
                            "Restore dialog");
                }
            }

            getAlarmReceiver().resetAlarms(this.getApplicationContext());

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

//        for (int i = 0; i < menu.size(); i++) {
//            Drawable drawable = menu.getItem(i).getIcon();
//            if (drawable != null) {
//                drawable.mutate();
//                drawable.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
//            }
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

//        if (id == R.id.menu_calendar) {
//
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Handle navigation view item clicks here.
                int id = item.getItemId();
                Intent intent = null;
                if (id == R.id.nav_trends) {
                    // Handle the camera action
                    intent = new Intent(getApplicationContext(), SummaryActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_silent_mode) {
                    InstantMuteDialog instantMuteDialog = new InstantMuteDialog();
                    instantMuteDialog.show(getSupportFragmentManager(), "Instant Mute");
                } else if (id == R.id.nav_edit_cups) {
                    intent = new Intent(getApplicationContext(), EditCupsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_backup) {
                    //Ask for write external permission
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // External storage permission has not been granted.
                        requestStoragePermission(Constants.REQUEST_WRITE_EXTERNAL_OVERFLOW);

                    } else {
                        //Already have storage permission
                        intent = new Intent(getApplicationContext(), BackupActivity.class);
                        startActivity(intent);
                    }
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_love) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://play.google.com/store/apps/details?id="
                                    + getPackageName()));
                    startActivity(intent);
                } else if (id == R.id.nav_share) {
                    Utility.getInstance().launchShareActivity(getApplicationContext());
                } else if (id == R.id.nav_send) {
                    Utility.getInstance().launchFeedbackActivity(getApplicationContext(), getResources().getString(R.string.key_feedback));
                }
            }
        }, 200);

        return true;
    }

    /**
     * Increase the count of water based on button click
     *
     * @param view
     */
    public void increaseWater(final View view) {
        SharedPreferences sharedPreferences;
        boolean reminderStatus;
        String quantity;
        boolean targetAchieved;
        TargetAchievedDialog targetAchievedDialog;

        view.animate().alpha(0.5f).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.animate().alpha(1f).setDuration(250);
            }
        }).start();

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.waterdrop);
        mediaPlayer.start();

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
        targetAchieved = Utility.getInstance().isTargetAchieved(
                (TextView) findViewById(R.id.water_quantity_status),
                (TextView) findViewById(R.id.water_target), quantity,
                getApplicationContext());

        // Save the water in DB
        HydrateDAO.getHydrateDAO().addWater(System.currentTimeMillis(), quantity, this);
        notificationManager.cancel(Constants.NOTIFICATION_ID);

        if (targetAchieved) {
            // Show dialog
            targetAchievedDialog = new TargetAchievedDialog();
            targetAchievedDialog.show(getSupportFragmentManager(),
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
        HydrateDAO.getHydrateDAO().deleteWater(this);
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

            MainActivity.RequestPermissionDialog requestPermissionDialog = new MainActivity.RequestPermissionDialog();
            Bundle args = new Bundle();
            args.putInt("code", code);
            requestPermissionDialog.setArguments(args);
            requestPermissionDialog.show(getFragmentManager(), "Request Permission");
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
                Toast.makeText(this, R.string.permission_storage_available, Toast.LENGTH_SHORT).show();

                //Show restore dialog
                if (requestCode == Constants.REQUEST_WRITE_EXTERNAL_STARTUP && Utility.getInstance().isBackupAvailable()) {
                    // Show dialog asking to restore
                    RestoreDialog restoreDialog = new RestoreDialog();
                    restoreDialog.show(getSupportFragmentManager(),
                            "Restore dialog");
                } else if (requestCode == Constants.REQUEST_WRITE_EXTERNAL_OVERFLOW) {
                    Intent intent = new Intent(this, BackupActivity.class);
                    startActivity(intent);
                }
            } else {
                Log.i(tag, "external storage permission was NOT granted.");
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
            LoaderManager.LoaderCallbacks<Cursor> {

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
        private TextView dateView;
        private ListView listView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_constraint, container,
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
            String date;
            dateView = (TextView) getActivity().findViewById(
                    R.id.dateView);
            if (savedInstanceState != null) {
                date = savedInstanceState.getString(Constants.DATE);
            } else {
                date = Utility.getInstance().getDate(
                        System.currentTimeMillis());
            }
            dateView.setText(date);

            final ImageButton previous;
            final ImageButton next;
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Constants.DATE_FORMAT);
            final ImageButton calendarButton = (ImageButton) getActivity()
                    .findViewById(R.id.menu_calendar);
            listView = (ListView) getActivity().findViewById(R.id.dayLog);
            final float listViewX = listView.getX();

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

                    listView.animate().translationX(listViewX + 1000f).scaleX(0.5f).scaleY(0.5f).setDuration(100).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            listView.animate().translationX(listViewX - 1000f).setDuration(0).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listView.animate().translationX(listViewX).scaleX(1f).scaleY(1f).setDuration(300);
                                }
                            }).start();
                        }
                    }).start();

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

                    listView.animate().translationX(listViewX - 1000f).scaleX(0.5f).scaleY(0.5f).setDuration(100).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            listView.animate().translationX(listViewX + 1000f).setDuration(0).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listView.animate().translationX(listViewX).scaleX(1f).scaleY(1f).setDuration(300);
                                }
                            }).start();
                        }
                    }).start();

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

            View view = getActivity().findViewById(R.id.main_fragment);
            OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener(getContext()) {
                @Override
                public void onSwipeLeft() {
                    next.callOnClick();
                }

                @Override
                public void onSwipeRight() {
                    previous.callOnClick();
                }
            };
            view.setOnTouchListener(swipeTouchListener);
            listView.setOnTouchListener(swipeTouchListener);
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.d(tag, "HomeScreenFragment - onStart()");
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
                    MainActivity.HomeScreenFragment.this);
            getLoaderManager().restartLoader(DRINK_SUMMARY_LOADER_ID, null,
                    MainActivity.HomeScreenFragment.this);
        }

        /**
         * Method to initiate a list view with all events for the day
         */
        private void setDrinkEvents() {
            ListView listView;

            int[] to = {R.id._id, R.id.quantityConsumed, R.id.time};

            MainActivity activity = (MainActivity) getActivity();
            final FragmentManager fragmentManager = activity.getSupportFragmentManager();

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
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                            fragmentManager,
                            "allEventsEditDialog");
                }
            });
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = HydrateContentProvider.CONTENT_URI_HYDRATE_LOGS;
            String[] selectionArgs = null;
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
            boolean dateChanged = true;

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
            String currentDate = Utility.getInstance()
                    .getDate(System.currentTimeMillis());
            if (date.equals(currentDate)) {
                targetCursor = getActivity().getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                        new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                        new String[]{Utility.getInstance().getToday() + ""}, null);
                targetCursor.moveToFirst();
                target = targetCursor.getDouble(0);
                targetCursor.close();
                dateChanged = false;
            } else {
                // Get it from database providing the date
                targetCursor = getActivity()
                        .getContentResolver()
                        .query(HydrateContentProvider.CONTENT_URI_HYDRATE_TARGET,
                                new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY},
                                HydrateDatabase.COLUMN_DATE + "=?",
                                new String[]{Utility.getInstance().getSqliteDate(
                                        Utility.getInstance()
                                                .getTimeInMillis(date))},
                                null);
                if (targetCursor.getCount() > 0) {
                    targetCursor.moveToFirst();
                    target = targetCursor.getDouble(0);

                } else {
                    targetCursor = getActivity().getContentResolver().query(HydrateContentProvider.CONTENT_URI_HYDRATE_DAILY_SCHEDULE,
                            new String[]{HydrateDatabase.COLUMN_TARGET_QUANTITY}, HydrateDatabase.DAY + "=?",
                            new String[]{Utility.getInstance().getToday() + ""}, null);
                    targetCursor.moveToFirst();
                    target = targetCursor.getDouble(0);
                    targetCursor.close();
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
                setProgressIndicators(textView, quantityConsumed, target, dateChanged);
            } else {
                quantityConsumed = Math.round(quantityConsumed);
                textView.setText(String.valueOf((int) quantityConsumed));
                setProgressIndicators(textView, quantityConsumed, target, dateChanged);
            }

            // Set water cup count
            textView = (TextView) getActivity().findViewById(R.id.water_status);
            textView.setText(String.valueOf(count));

        }

        private void setProgressIndicators(TextView textView, double consumed,
                                           double target, boolean dateChanged) {
            int color = -1;
            if (consumed < (target * .75)) {
                color = ContextCompat.getColor(getActivity(), R.color.danger);
            } else if (consumed < target) {
                color = ContextCompat.getColor(getActivity(), R.color.safe);
            } else {
                color = ContextCompat.getColor(getActivity(), R.color.success);
            }

            int lighterColor = Color.argb(Color.alpha(color) / 5, Color.red(color), Color.green(color), Color.blue(color));

            int to = (int) ((consumed / target) * 100);
            MaterialProgressBar progressBar = (MaterialProgressBar) getActivity().findViewById(R.id.waterProgress);
            int from = progressBar.getProgress();
            if (dateChanged)
                from = 0;
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(lighterColor));


            ProgressBarAnimation animation = new ProgressBarAnimation(progressBar, from, to);
            animation.setDuration(300);
            progressBar.startAnimation(animation);
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
