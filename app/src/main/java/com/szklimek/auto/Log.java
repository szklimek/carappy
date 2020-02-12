package com.szklimek.auto;

public class Log {

    private static final int LOG_VERBOSE = 0;
    private static final int LOG_DEBUG = 1;
    private static final int LOG_INFO = 2;
    private static final int LOG_WARN = 3;
    private static final int LOG_ERROR = 4;

    public static String TAG = "WordsApp";

    public static void i(String message) {
        android.util.Log.i(TAG, getLogMessage(message));
    }

    public static void v(String message) {
        android.util.Log.v(TAG, getLogMessage(message));
    }

    public static void d(String message) {
        android.util.Log.d(TAG, getLogMessage(message));
    }

    public static void w(String message) {
        android.util.Log.w(TAG, getLogMessage(message));
    }

    public static void e(String message) {
        android.util.Log.e(TAG, getLogMessage(message));
    }

    private static String getLogMessage(String message) {
        return "(" + getFileName() + ":" + getLineNumber() + ") "  + getMethodName() + " @" + getCurrentThread() + " " + message;
    }

    private static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[5].getLineNumber();
    }

    private static String getFileName() {
        return Thread.currentThread().getStackTrace()[5].getFileName();
    }

    private static String getMethodName() {
        return Thread.currentThread().getStackTrace()[5].getMethodName();
    }

    private static String getCurrentThread() {
        return Thread.currentThread().getName();
    }
}
