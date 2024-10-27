package com.nud.secureguardtech.services;

import android.content.Context;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.nud.secureguardtech.sender.FooSender;
import com.nud.secureguardtech.sender.NotificationReply;
import com.nud.secureguardtech.sender.Sender;
import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.utils.Notifications;
import com.nud.secureguardtech.utils.Permission;

import java.util.Calendar;
import java.util.Date;

import com.nud.secureguardtech.data.ConfigSMSRec;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.io.json.JSONWhiteList;
import com.nud.secureguardtech.logic.ComponentHandler;

public class ThirdPartyAccessService extends NotificationListenerService {

    protected WhiteList whiteList;
    protected ConfigSMSRec config;

    protected ComponentHandler ch;
    protected  String DEFAULT_SMS_PACKAGE_NAME = "";
    protected void init(Context context) {
        IO.context = context;
        Logger.init(Thread.currentThread(), context);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        if (config.get(ConfigSMSRec.CONF_LAST_USAGE) == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            config.set(ConfigSMSRec.CONF_LAST_USAGE, cal.getTimeInMillis());
        }
        Notifications.init(context, false);
        Permission.initValues(context);
        ch = new ComponentHandler(settings, context, null, null);

        DEFAULT_SMS_PACKAGE_NAME = Telephony.Sms.getDefaultSmsPackage(context);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        init(this);
        CharSequence msgCS = sbn.getNotification().extras.getCharSequence("android.text");
        if(sbn.getPackageName().equals(DEFAULT_SMS_PACKAGE_NAME)){
            return;
        }
        if(msgCS != null) {
            NotificationReply sender = new NotificationReply(this, sbn);
            if(sender.canSend()) {
                ch.setSender(sender);
                String msg = msgCS.toString();
                String msgLower = msg.toLowerCase();
                String fmdcommand = (String) ch.getSettings().get(Settings.SET_FMD_COMMAND);
                if (msgLower.contains(fmdcommand)) {
                    msg = ch.getMessageHandler().checkAndRemovePin(msg);
                    if (msg != null) {
                        ch.getMessageHandler().handle(msg, this);
                        cancelNotification(sbn.getKey());
                    }
                }
            }
            if((Boolean)ch.getSettings().get(Settings.SET_FMD_LOW_BAT_SEND)) {
                if (sbn.getPackageName().equals("com.android.systemui")) {
                    if (sbn.getTag().equals("low_battery")) {
                        Long lastTime = (Long) config.get(ConfigSMSRec.CONF_TEMP_BAT_CHECK);
                        Long nowTime = new Date().getTime();
                        config.set(ConfigSMSRec.CONF_TEMP_BAT_CHECK, nowTime);
                        if (lastTime == null || lastTime+60000 < nowTime) {
                            Sender dummySender = new FooSender();
                            Logger.log("BatteryWarning", "Low Battery detected: sending message.");
                            ch.setSender(dummySender);
                            String fmdcommand = (String) ch.getSettings().get(Settings.SET_FMD_COMMAND);
                            ch.getMessageHandler().handle(fmdcommand + " locate", this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }



}
