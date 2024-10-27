package com.nud.secureguardtech.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;

import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.utils.Notifications;
import com.nud.secureguardtech.utils.Permission;

import java.util.Calendar;

import com.nud.secureguardtech.data.ConfigSMSRec;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.io.json.JSONWhiteList;
import com.nud.secureguardtech.logic.ComponentHandler;

abstract class SuperReceiver extends BroadcastReceiver {

    protected WhiteList whiteList;
    protected ConfigSMSRec config;
    protected Settings settings;

    protected ComponentHandler ch;

    protected void init(Context context) {
        IO.context = context;
        Logger.init(Thread.currentThread(), context);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        if (config.get(ConfigSMSRec.CONF_LAST_USAGE) == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            config.set(ConfigSMSRec.CONF_LAST_USAGE, cal.getTimeInMillis());
        }
        Notifications.init(context, false);
        Permission.initValues(context);
        ch = new ComponentHandler(settings, context, null, null);
    }

}
