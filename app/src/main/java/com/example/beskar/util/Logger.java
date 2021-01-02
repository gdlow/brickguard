package com.example.beskar.util;

import android.util.Log;

import com.example.beskar.Beskar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static StringBuffer buffer = null;

    public static void init() {
        if (buffer != null) {
            buffer.setLength(0);
        } else {
            buffer = new StringBuffer();
        }
    }

    public static void shutdown() {
        buffer = null;
    }

    public static String getLog() {
        return buffer.toString();
    }

    public static void error(String message) {
        send("[ERROR] " + message);
    }

    public static void warning(String message) {
        send("[WARNING] " + message);
    }

    public static void info(String message) {
        send("[INFO] " + message);
    }

    public static void debug(String message) {
        send("[DEBUG] " + message);
    }

    public static void logException(Throwable e) {
        error(getExceptionMessage(e));
    }

    public static String getExceptionMessage(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private static int getLogSizeLimit() {
        return Integer.parseInt(Beskar.getPrefs().getString("settings_log_size", "10000"));
    }

    private static boolean checkBufferSize() {
        int limit = getLogSizeLimit();
        if (limit == 0) {//DISABLED!
            return false;
        }
        if (limit == -1) {//N0 limit
            return true;
        }
        if (buffer.length() > limit) {//LET's clean it up!
            buffer.setLength(limit);
        }
        return true;
    }

    private static void send(String message) {
        try {
            if (checkBufferSize()) {
                String fileDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date());
                buffer.insert(0, "\n").insert(0, message).insert(0, fileDateFormat);
            }
            Log.d("Daedalus", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
