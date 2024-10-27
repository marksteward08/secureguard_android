package com.nud.secureguardtech.receiver;

import android.content.Context;
import android.content.Intent;


import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.data.ConfigSMSRec;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.services.FMDServerLocationUploadService;
import com.nud.secureguardtech.ui.onboarding.UpdateboardingModernCryptoActivity;

public class BootReceiver extends SuperReceiver {

    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (intent.getAction().equals(BOOT_COMPLETED)) {
            Logger.logSession("AfterBootTest", "passed");
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
            ch.getSettings().set(Settings.SET_GPS_STATE, 1);

            UpdateboardingModernCryptoActivity.notifyAboutCryptoRefreshIfRequired(context);

            if (ch.getSettings().checkAccountExists()) {
                FMDServerLocationUploadService.scheduleJob(context, 0);
                PushReceiver.registerWithUnifiedPush(context);
            }
        }
    }

}
