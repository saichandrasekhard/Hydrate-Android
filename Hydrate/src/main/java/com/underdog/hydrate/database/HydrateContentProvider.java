package com.underdog.hydrate.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.underdog.hydrate.util.Log;

import java.util.Arrays;
import java.util.HashMap;

public class HydrateContentProvider extends ContentProvider {

    public static final String HYDRATE_DAILY_SCHEDULE = "hydrate_daily_schedule";
    private static final String tag = "HydrateContentProvider";
    private static final String HYDRATE_LOGS = "hydrate_log";
    private static final String HYDRATE_TARGET = "hydrate_target";
    private static final String HYDRATE_DND = "hydrate_dnd";
    private static final String HYDRATE_CUPS = "hydrate_cups";
    private static final String HYDRATE_LOGS_UPDATE_UNITS = "hydrate_log_update_units";
    private static final String HYDRATE_LOGS_DELETE_LAST = "hydrate_log_delete_last";

    private static final String PROVIDER_NAME = "com.underdog.hydrate.provider";

    public static final Uri CONTENT_URI_HYDRATE_LOGS = Uri.parse("content://"
            + PROVIDER_NAME + "/" + HYDRATE_LOGS);
    public static final Uri CONTENT_URI_HYDRATE_TARGET = Uri.parse("content://"
            + PROVIDER_NAME + "/" + HYDRATE_TARGET);
    public static final Uri CONTENT_URI_HYDRATE_CUPS = Uri.parse("content://"
            + PROVIDER_NAME + "/" + HYDRATE_CUPS);
    public static final Uri CONTENT_URI_HYDRATE_DELETE_LAST = Uri
            .parse("content://" + PROVIDER_NAME + "/"
                    + HYDRATE_LOGS_DELETE_LAST);
    public static final Uri CONTENT_URI_UPDATE_UNITS = Uri.parse("content://"
            + PROVIDER_NAME + "/" + HYDRATE_LOGS_UPDATE_UNITS);
    public static final Uri CONTENT_URI_HYDRATE_DAILY_SCHEDULE = Uri
            .parse("content://" + PROVIDER_NAME + "/" + HYDRATE_DAILY_SCHEDULE);
    public static final Uri CONTENT_URI_HYDRATE_DND = Uri.parse("content://"
            + PROVIDER_NAME + "/" + HYDRATE_DND);
    private static final int HYDRATE_LOGS_INT = 1;
    private static final int HYDRATE_LOGS_DELETE_LAST_INT = 2;
    private static final int HYDRATE_LOGS_UPDATE_UNITS_INT = 3;
    private static final int HYDRATE_TARGET_INT = 4;
    private static final int HYDRATE_CUPS_INT = 5;
    private static final int HYDRATE_DAILY_SCHEDULE_INT = 6;
    private static final int HYDRATE_DND_INT = 7;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_LOGS, HYDRATE_LOGS_INT);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_LOGS_DELETE_LAST,
                HYDRATE_LOGS_DELETE_LAST_INT);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_LOGS_UPDATE_UNITS,
                HYDRATE_LOGS_UPDATE_UNITS_INT);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_TARGET, HYDRATE_TARGET_INT);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_CUPS, HYDRATE_CUPS_INT);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_DAILY_SCHEDULE,
                HYDRATE_DAILY_SCHEDULE_INT);
        uriMatcher.addURI(PROVIDER_NAME, HYDRATE_DND, HYDRATE_DND_INT);

    }

    private HydrateDatabase database;

    @Override
    public String getType(Uri arg0) {
        return null;
    }

    @Override
    public boolean onCreate() {
        database = new HydrateDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteQueryBuilder queryBuilder;
        SQLiteDatabase sqlDB;
        HashMap<String, String> projectionMap;
        if (uriMatcher.match(uri) == HYDRATE_LOGS_INT) {
            queryBuilder = new SQLiteQueryBuilder();
            projectionMap = new HashMap<>();
            projectionMap.put("_id", "rowId as _id");
            projectionMap.put(HydrateDatabase.COLUMN_QUANTITY,
                    HydrateDatabase.COLUMN_QUANTITY);
            projectionMap.put(HydrateDatabase.COLUMN_TIMESTAMP,
                    HydrateDatabase.COLUMN_TIMESTAMP);

            // Add _id by default
            projection = Arrays.copyOf(projection, projection.length + 1);
            projection[projection.length - 1] = "_id";

            queryBuilder.setTables(HydrateDatabase.HYDRATE_LOG);
            queryBuilder.setProjectionMap(projectionMap);
            cursor = queryBuilder
                    .query(database.getWritableDatabase(), projection,
                            selection, selectionArgs, null, null, sortOrder);

            cursor.setNotificationUri(getContext().getContentResolver(),
                    CONTENT_URI_HYDRATE_LOGS);
            return cursor;
        } else if (uriMatcher.match(uri) == HYDRATE_TARGET_INT) {
            Log.d(tag, "Enter hydrate_target cp");
            sqlDB = database.getWritableDatabase();
            cursor = sqlDB.query(HYDRATE_TARGET, projection, selection,
                    selectionArgs, null, null, sortOrder,uri.getQueryParameter(HydrateDatabase.LIMIT));
            cursor.setNotificationUri(getContext().getContentResolver(),
                    CONTENT_URI_HYDRATE_TARGET);
            return cursor;
        } else if (uriMatcher.match(uri) == HYDRATE_CUPS_INT) {
            sqlDB = database.getWritableDatabase();
            cursor = sqlDB.query(HYDRATE_CUPS, projection, selection,
                    selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(),
                    CONTENT_URI_HYDRATE_CUPS);
            return cursor;
        } else if (uriMatcher.match(uri) == HYDRATE_DAILY_SCHEDULE_INT) {
            sqlDB = database.getWritableDatabase();
            cursor = sqlDB.query(HYDRATE_DAILY_SCHEDULE, projection, selection,
                    selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(),
                    CONTENT_URI_HYDRATE_DAILY_SCHEDULE);
            return cursor;
        } else if (uriMatcher.match(uri) == HYDRATE_DND_INT) {
            sqlDB = database.getWritableDatabase();
            cursor = sqlDB.query(HYDRATE_DND, projection, selection,
                    selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(),
                    CONTENT_URI_HYDRATE_DND);
            return cursor;
        }
        return null;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;

        if (uriMatcher.match(uri) == HYDRATE_LOGS_INT) {
            id = sqlDB.insert(HydrateDatabase.HYDRATE_LOG, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(HYDRATE_LOGS + "/" + id);
        } else if (uriMatcher.match(uri) == HYDRATE_TARGET_INT) {
            id = sqlDB.insert(HYDRATE_TARGET, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(HYDRATE_TARGET + "/" + id);
        } else if (uriMatcher.match(uri) == HYDRATE_DND_INT) {
            id = sqlDB.insert(HYDRATE_DND, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(HYDRATE_DND + "/" + id);
        } else {
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause,
                      String[] whereArgs) {
        SQLiteDatabase sqLiteDatabase;
        if (uriMatcher.match(uri) == HYDRATE_LOGS_INT) {
            sqLiteDatabase = database.getWritableDatabase();
            sqLiteDatabase.update(HydrateDatabase.HYDRATE_LOG, values,
                    whereClause, whereArgs);

            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_LOGS, null);

        } else if (uriMatcher.match(uri) == HYDRATE_TARGET_INT) {
            sqLiteDatabase = database.getWritableDatabase();
            sqLiteDatabase.update(HYDRATE_TARGET, values, whereClause,
                    whereArgs);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_TARGET, null);
        } else if (uriMatcher.match(uri) == HYDRATE_CUPS_INT) {
            sqLiteDatabase = database.getWritableDatabase();
            sqLiteDatabase.update(HYDRATE_CUPS, values, whereClause, whereArgs);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_CUPS, null);
        } else if (uriMatcher.match(uri) == HYDRATE_LOGS_UPDATE_UNITS_INT) {
            sqLiteDatabase = database.getWritableDatabase();
            if (whereClause.equals(HydrateDatabase.UPDATE_LOG_UNITS_TO_ML)) {
                Log.d(tag, "converting to ML");
                delete(CONTENT_URI_HYDRATE_CUPS, null, null);
                for (String cup : HydrateDatabase.cups_ml) {
                    values = new ContentValues();
                    values.put(HydrateDatabase.COLUMN_QUANTITY, cup);
                    sqLiteDatabase.insert(HYDRATE_CUPS, null, values);
                }

                sqLiteDatabase.execSQL(HydrateDatabase.UPDATE_LOG_UNITS_TO_ML);
                sqLiteDatabase
                        .execSQL(HydrateDatabase.UPDATE_TARGET_UNITS_TO_ML);
                sqLiteDatabase
                        .execSQL(HydrateDatabase.UPDATE_DS_TARGET_UNITS_TO_ML);

            } else if (whereClause
                    .equals(HydrateDatabase.UPDATE_UNITS_TO_OZ_US)) {
                Log.d(tag, "converting to Us oz");
                delete(CONTENT_URI_HYDRATE_CUPS, null, null);
                for (String cup : HydrateDatabase.cups_oz) {
                    values = new ContentValues();
                    values.put(HydrateDatabase.COLUMN_QUANTITY, cup);
                    sqLiteDatabase.insert(HYDRATE_CUPS, null, values);
                }
                sqLiteDatabase.execSQL(HydrateDatabase.UPDATE_UNITS_TO_OZ_US);
                sqLiteDatabase
                        .execSQL(HydrateDatabase.UPDATE_TARGET_UNITS_TO_OZ_US);
                sqLiteDatabase
                        .execSQL(HydrateDatabase.UPDATE_DS_TARGET_UNITS_TO_OZ_US);

            }
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_LOGS, null);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_TARGET, null);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_CUPS, null);
        } else if (uriMatcher.match(uri) == HYDRATE_DAILY_SCHEDULE_INT) {
            sqLiteDatabase = database.getWritableDatabase();
            sqLiteDatabase.update(HYDRATE_DAILY_SCHEDULE, values, whereClause,
                    whereArgs);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_DAILY_SCHEDULE, null);
        } else if (uriMatcher.match(uri) == HYDRATE_DND_INT) {
            sqLiteDatabase = database.getWritableDatabase();
            sqLiteDatabase.update(HYDRATE_DND, values, whereClause, whereArgs);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_DND, null);
        }
        return 0;
    }

    @Override
    public int delete(Uri uri, String where, String[] selectionArgs) {
        SQLiteDatabase sqlDB;
        if (uriMatcher.match(uri) == HYDRATE_LOGS_INT) {
            sqlDB = database.getWritableDatabase();
            sqlDB.delete(HYDRATE_LOGS, where, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        } else if (uriMatcher.match(uri) == HYDRATE_TARGET_INT) {
            sqlDB = database.getWritableDatabase();
            sqlDB.delete(HYDRATE_TARGET, where, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        } else if (uriMatcher.match(uri) == HYDRATE_LOGS_DELETE_LAST_INT) {
            sqlDB = database.getWritableDatabase();
            sqlDB.execSQL(HydrateDatabase.DELETE_LAST);
            getContext().getContentResolver().notifyChange(
                    CONTENT_URI_HYDRATE_LOGS, null);
        } else if (uriMatcher.match(uri) == HYDRATE_CUPS_INT) {
            sqlDB = database.getWritableDatabase();
            sqlDB.delete(HYDRATE_CUPS, where, selectionArgs);
//            getContext().getContentResolver().notifyChange(uri, null);
        } else if (uriMatcher.match(uri) == HYDRATE_DND_INT) {
            sqlDB = database.getWritableDatabase();
            sqlDB.delete(HYDRATE_DND, where, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    public void resetDatabase() {
        database.close();
        database = new HydrateDatabase(getContext());
    }

}
