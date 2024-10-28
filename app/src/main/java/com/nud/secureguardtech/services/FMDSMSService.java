package com.nud.secureguardtech.services;

import static com.nud.secureguardtech.data.io.IO.context;

import android.app.admin.DevicePolicyManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.RequiresApi;

import com.nud.secureguardtech.data.ConfigSMSRec;
import com.nud.secureguardtech.sender.SMS;
import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.utils.Notifications;
import com.nud.secureguardtech.utils.Permission;

import java.util.Calendar;

import com.nud.secureguardtech.R;

import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.io.json.JSONWhiteList;
import com.nud.secureguardtech.logic.ComponentHandler;
import com.nud.secureguardtech.logic.MessageHandler;

public class FMDSMSService extends JobService {

    private static final int JOB_ID = 107;

    private static final String DESTINATION = "dest";
    private static final String MESSAGE = "msg";
    private static final String TIME = "time";

    private WhiteList whiteList;
    private ComponentHandler ch;
    private ConfigSMSRec config;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context, String destination, String message, Long time) {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(DESTINATION, destination);
        bundle.putString(MESSAGE, message);
        bundle.putLong(TIME, time);

        ComponentName serviceComponent = new ComponentName(context, FMDSMSService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent)
                .setExtras(bundle);
        builder.setMinimumLatency(0);
        builder.setOverrideDeadline(0);

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    public boolean onStartJob(JobParameters params) {
        context = this;
        Logger.init(Thread.currentThread(), this);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        if (config.get(ConfigSMSRec.CONF_LAST_USAGE) == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            config.set(ConfigSMSRec.CONF_LAST_USAGE, cal.getTimeInMillis());
        }
        Notifications.init(this, false);
        Permission.initValues(this);
        ch = new ComponentHandler(settings, this, this, params);
        String receiver = params.getExtras().getString(DESTINATION);
        String msg = params.getExtras().getString(MESSAGE);
        Long time = params.getExtras().getLong(TIME);
        ch.setSender(new SMS(receiver));
        boolean inWhitelist = false;
        String executedCommand = "";
        for (int iwl = 0; iwl < whiteList.size(); iwl++) {
            if (PhoneNumberUtils.compare(whiteList.get(iwl).getNumber(), receiver)) {
                Logger.logSession("Usage", receiver + " used FMD");
                executedCommand = ch.getMessageHandler().handle(msg, this);
                inWhitelist = true;
            }
        }
        if ((Boolean) ch.getSettings().get(Settings.SET_ACCESS_VIA_PIN) && !((String)ch.getSettings().get(Settings.SET_PIN)).isEmpty()) {
            String tempContact = (String) config.get(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT);
            if (!inWhitelist && tempContact != null && PhoneNumberUtils.compare(tempContact, receiver)) {
                Logger.logSession("Usage", receiver + " used FMD");
                executedCommand = ch.getMessageHandler().handle(msg, this);
                inWhitelist = true;
            }
            if (!inWhitelist && ch.getMessageHandler().checkForPin(msg)) {
                Logger.logSession("Usage", receiver + " used the Pin");
                ch.getSender().sendNow(getString(R.string.MH_Pin_Accepted));
                Notifications.notify(this, "SECUREGUARD ALERT!", "This device is being monitored by: " + receiver, Notifications.CHANNEL_PIN);
                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, receiver);
                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, time);

                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                devicePolicyManager.lockNow();


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TempContactExpiredService.scheduleJob(this, ch.getSender());
                }
            }
        }
        return executedCommand.equals(MessageHandler.COM_LOCATE);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
