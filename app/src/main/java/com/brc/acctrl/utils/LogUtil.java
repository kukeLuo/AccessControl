package com.brc.acctrl.utils;

import android.text.TextUtils;
import android.util.Log;

import com.brc.acctrl.BuildConfig;

public class LogUtil {
    private static final String TAG = "ACCESS_CTRL";

    public static void trackLogDebug(String message) {
        d(message);
    }

    public static void d(String message) {
        if (!TextUtils.isEmpty(message)) {
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if (!TextUtils.isEmpty(message)) {
            Log.i(TAG, message);
        }
    }

    public static void e(String message) {
        if (!TextUtils.isEmpty(message)) {
            Log.e(TAG, message);
        }
    }

    public static void w(String message) {
        if (BuildConfig.DEBUG && !TextUtils.isEmpty(message)) {
            Log.w(TAG, message);
        }
    }

}
