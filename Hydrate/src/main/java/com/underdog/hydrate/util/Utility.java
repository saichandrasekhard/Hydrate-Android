package com.underdog.hydrate.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {

    private static Utility utility = null;

    private Utility() {

    }

    public static Utility getInstance() {
        if (utility == null)
            utility = new Utility();
        return utility;
    }

    /**
     * forms where clause values for a given day
     *
     * @param currentTime
     * @return
     */
    public String[] getSelectionArgsForDay(long currentTime) {
        String[] selectionArgs;
        Calendar calendar;
        long fromTime;
        long toTime;

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 59);
        fromTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        toTime = calendar.getTimeInMillis();

        selectionArgs = new String[2];
        selectionArgs[0] = String.valueOf(fromTime);
        selectionArgs[1] = String.valueOf(toTime);

        return selectionArgs;
    }

    public long getTimeInMillis(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT);
        Date dateObj = null;
        try {
            dateObj = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj.getTime();

    }

    public String getSqliteDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(timestamp);
        Log.d(this.getClass().toString(), "date - " + dateFormat.format(date));
        return dateFormat.format(date);
    }

    public String getDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT);
        return dateFormat.format(new Date(timestamp));
    }

    public int getDaysSince(long timestamp) {
        long difference = System.currentTimeMillis() - timestamp;
        difference = difference / 86400000l;
        return (int) (difference == 0 ? 1 : difference);
    }

    public boolean isTargetAchieved(TextView currentView, TextView targetView,
                                    String quantityString, Context context) {
        boolean targetAchieved = false;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String milliliter = context.getString(R.string.milliliter);
        String metric = preferences.getString(
                context.getString(R.string.key_metric), milliliter);
        double current = Double.valueOf(currentView.getText().toString());
        double target = Double.valueOf(targetView.getText().toString());
        double quantity = Double.valueOf(quantityString);

        if (metric.equals(milliliter)) {
            quantity /= 1000;
        }

        if (current < target && current + quantity >= target) {
            targetAchieved = true;
        }

        return targetAchieved;
    }

    public boolean isBackupAvailable() {
        boolean backupAvailable = false;
        File backupSettings = new File(
                Environment.getExternalStorageDirectory(),
                Constants.BACKUP_FOLDER + File.separator
                        + Constants.BACKUP_SETTINGS_FILE_NAME);
        File backupDB = new File(Environment.getExternalStorageDirectory(),
                Constants.BACKUP_FOLDER + File.separator
                        + Constants.BACKUP_DB_FILE_NAME);
        if (backupSettings.exists() && backupDB.exists()) {
            backupAvailable = true;
        }
        return backupAvailable;
    }

    public int getToday() {
        int day_id;
        Calendar calendar = Calendar.getInstance();
        day_id = calendar.get(Calendar.DAY_OF_WEEK);
        day_id -= 2;
        if (day_id == -1)
            day_id = 6;
        return day_id;
    }

    public int getDay(long timestamp) {
        int day_id;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        day_id = calendar.get(Calendar.DAY_OF_WEEK);
        day_id -= 2;
        if (day_id == -1)
            day_id = 6;
        return day_id;
    }

    public long subGivenTimeInDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, (int) (time / 60));
        calendar.set(Calendar.MINUTE, (int) (time % 60));
        return calendar.getTimeInMillis();
    }

    public byte[] getDBFileContent() {
        byte[] fileContent = null;
        String currentDBPath = File.separator + "data" + File.separator
                + "com.underdog.hydrate" + File.separator + "databases"
                + File.separator + "hydrate.db";
        File data = Environment.getDataDirectory();
        File file = new File(data, currentDBPath);
        try {
            FileInputStream src = new FileInputStream(file);
            fileContent = new byte[(int) file.length()];
            src.read(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContent;
    }

    public boolean replaceDb(byte[] content) {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileOutputStream fos = null;
        if (sd.canWrite()) {
            File currentDB = new File(data, Constants.CURRENT_DB_PATH);
            try {
                fos = new FileOutputStream(currentDB);
                fos.write(content);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Exception occurred", e);
                return false;
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(this.getClass().toString(), e.getMessage(), e);
                }
            }
            return true;
        }
        return false;
    }

    public void launchShareActivity(Context context) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.checkout_message)
                        + "\n"
                        + context.getString(R.string.checkout));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hydrate");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(
                    Intent.createChooser(intent, context
                            .getString(R.string.select_app)));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void launchFeedbackActivity(Context context, String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", Constants.DEV_EMAIL, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(
                Intent.createChooser(emailIntent, context
                        .getString(R.string.select_app)));
    }

    public int[] getHoursAndMins(String value) {
        String hourMins[] = value.split(":");
        int hour = Integer.parseInt(hourMins[0]);
        int mins = Integer.parseInt(hourMins[1]);
        return new int[]{hour, mins};
    }

}
