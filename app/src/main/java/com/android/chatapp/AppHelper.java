package com.android.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

public class AppHelper {
    public static final boolean DEBUGGING_MODE = false;
    public static void LaunchActivity(Activity mContext, Class mActivity) {
        Intent mIntent = new Intent(mContext, mActivity);
        mContext.startActivity(mIntent);
        mContext.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    public static void LaunchActivityfinish(Activity mContext, Class<?> target) {
        Intent i = new Intent(mContext, target);
        mContext.startActivity(i);
        mContext.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        mContext.finish();
    }
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    public static void LogCat(String Message) {
        if (DEBUGGING_MODE) {
            if (Message != null) {
                if (!BuildConfig.DEBUG) {
                    return;
                }
                //  Logger.e(Message);
            }
        }
    }
    public static boolean isAndroid6() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    public static String getFileTime(long milliseconds) {
        String TimerString = "";
        String secondsString;
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            TimerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        TimerString = TimerString + minutes + ":" + secondsString;
        return TimerString;
    }
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration;
        totalDuration = (int) totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }
}
