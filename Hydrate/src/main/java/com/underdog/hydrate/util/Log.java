package com.underdog.hydrate.util;

import com.underdog.hydrate.constants.Constants;

public class Log {

    public static void d(String tag, String msg) {
        if (Constants.debugLogs) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        android.util.Log.e(tag, msg, tr);
    }

}
