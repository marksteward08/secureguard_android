package com.nud.secureguardtech.receiver;

import android.content.Context;
import android.content.Intent;


import com.nud.secureguardtech.services.FMDServerLocationUploadService;
import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.data.ConfigSMSRec;
import com.nud.secureguardtech.ui.onboarding.UpdateboardingModernCryptoActivity;

public class AppUpdatedReceiver extends SuperReceiver {

    public static final String APP_UPDATED = "android.intent.action.MY_PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (intent.getAction().equals(APP_UPDATED)) {
            Logger.logSession("AppUpdate", "restarted");
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
            settings.updateSettings();

            UpdateboardingModernCryptoActivity.notifyAboutCryptoRefreshIfRequired(context);

            if (ch.getSettings().checkAccountExists()) {
                FMDServerLocationUploadService.scheduleJob(context, 0);
            }
        }
    }

}
