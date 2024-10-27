package com.nud.secureguardtech.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nud.secureguardtech.ui.CrashedActivity;

import java.util.Calendar;

import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.io.json.JSONLog;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.LogData;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;

public class Logger implements Thread.UncaughtExceptionHandler{

    private static boolean DEBUG;
    private static LogData log;
    private static Context context;
    private static StringBuilder logText;

    public static void init(Thread t, Context con){
        DEBUG = false;
        log = JSONFactory.convertJSONLog(IO.read(JSONLog.class, IO.logFileName));
        context = con;
        logText = new StringBuilder();
        Logger logger = new Logger();
        t.setUncaughtExceptionHandler(logger);
        log.cleanUp();
        writeLog();
    }

    public static void setDebuggingMode(boolean debug){
        DEBUG = debug;
    }

    public static void log(String title, String msg){
        logText.append(title).append(" - ").append(msg);
        writeLog();
        if(DEBUG){
            Log.d(title, msg);
        }
    }

    public static void logSession(String title,String msg){
        if(!logText.toString().isEmpty()) {
            logText.append("\n");
        }
        logText.append(title).append(" - ").append(msg);
        if(DEBUG){
            Log.d(title, msg);
        }
    }

    public static void writeLog(){
        if(!logText.toString().isEmpty()) {
            log.add(Calendar.getInstance().getTimeInMillis(), logText.toString());
            logText = new StringBuilder();
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        log(t.getName(), createNiceCrashLog(e));
        Settings Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        Settings.set(Settings.SET_APP_CRASHED_LOG_ENTRY, log.size()-1);
        Intent crash = new Intent(context, CrashedActivity.class);
        context.startActivity(crash);
    }

    public String createNiceCrashLog(Throwable e){
        StackTraceElement[] arr = e.getStackTrace();
        final StringBuffer report = new StringBuffer(e.toString());
        final String newLine= "\n";
        report.append(newLine);
        report.append(newLine);
        report.append("--------- Stack trace ---------\n\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
        }
        report.append(newLine);
        report.append("--------- Cause ---------\n\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(newLine);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append(arr[i].toString());
                report.append(newLine);
            }
        }
        report.append(newLine);
        report.append(newLine);
        report.append("--------- Device ---------\n\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(newLine);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(newLine);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(newLine);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(newLine);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(newLine);
        report.append(newLine);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK);
        report.append(newLine);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(newLine);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(newLine);
        report.append("FMD-Version: ");
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(
                    context.getPackageName(), 0);
            report.append(info.versionName);
        } catch (PackageManager.NameNotFoundException nameNotFoundException) {
            report.append("-");
            nameNotFoundException.printStackTrace();
        }
        report.append(newLine);

        return report.toString();
    }

}
