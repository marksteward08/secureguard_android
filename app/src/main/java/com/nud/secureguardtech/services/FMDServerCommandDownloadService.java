package com.nud.secureguardtech.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.nud.secureguardtech.net.FMDServerApiRepoSpec;
import com.nud.secureguardtech.net.FMDServerApiRepository;
import com.nud.secureguardtech.sender.FooSender;
import com.nud.secureguardtech.sender.Sender;
import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.utils.Notifications;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.logic.ComponentHandler;

/**
 * Downloads the latest command and executes it
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class FMDServerCommandDownloadService extends JobService {

    private String TAG = FMDServerCommandDownloadService.class.getSimpleName();

    private static final int JOB_ID = 109;
    private Settings settings;
    private JobParameters params;

    @Override
    public boolean onStartJob(JobParameters params) {
        IO.context = this;
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        this.params = params;

        FMDServerApiRepository fmdServerRepo = FMDServerApiRepository.Companion.getInstance(new FMDServerApiRepoSpec(this));
        fmdServerRepo.getCommand(this::onResponse, error -> {
            error.printStackTrace();
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJobNow(Context context) {
        ComponentName serviceComponent = new ComponentName(context, FMDServerCommandDownloadService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setMinimumLatency(0);
        builder.setOverrideDeadline(1000);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    private void onResponse(String remoteCommand) {
        Log.i(TAG, "Received remote command '" + remoteCommand + "'");
        if (remoteCommand.equals("")) {
            return;
        }
        if (remoteCommand.startsWith("423")) {
            Notifications.init(this, false);
            Notifications.notify(this, "Serveraccess", "Somebody tried three times in a row to log in the server. Access is locked for 10 minutes", Notifications.CHANNEL_SERVER);
            return;
        }
        Sender sender = new FooSender();
        Logger.init(Thread.currentThread(), this);
        ComponentHandler ch = new ComponentHandler(settings, this, this, params);
        ch.setSender(sender);
        ch.getLocationHandler().setSendToServer(true);
        ch.getMessageHandler().setSilent(true);
        String fmdCommand = (String) settings.get(Settings.SET_FMD_COMMAND);

        ch.getMessageHandler().handle(fmdCommand + " " + remoteCommand, this);
    }
}
