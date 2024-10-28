package com.nud.secureguardtech.logic;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.logic.command.helper.Cell;
import com.nud.secureguardtech.logic.command.helper.GPS;
import com.nud.secureguardtech.logic.command.helper.Network;
import com.nud.secureguardtech.logic.command.helper.Ringer;
import com.nud.secureguardtech.ui.DummyCameraxActivity;
import com.nud.secureguardtech.ui.LockScreenMessage;
import com.nud.secureguardtech.utils.CypherUtils;
import com.nud.secureguardtech.utils.Logger;
import com.nud.secureguardtech.utils.Notifications;
import com.nud.secureguardtech.utils.Permission;
import com.nud.secureguardtech.utils.SecureSettings;

import java.util.Map;

import com.nud.secureguardtech.R;

public class MessageHandler {

    public static final String COM_LOCATE = "locate";
    public static final String COM_RING = "ring";
    public static final String COM_LOCK = "lock";
    public static final String COM_SENTRY = "sentry";
    public static final String COM_DELETE = "delete";
    public static final String COM_STATS = "stats";

    public static final String COM_NEXT = "next";
    public static final String COM_EXPERT_GPS = "gps";
    public static final String COM_EXPERT_SOUND = "sound";
    public static final String COM_EXPERT_CAMERA = "camera";
    public static final String COM_HELP = "help";

    public static final String COM_OK = "ok";


    private ComponentHandler ch;

    private boolean silent = false;

    public MessageHandler(ComponentHandler ch) {
        this.ch = ch;
    }

    SimCheckHandler simCheckHandler = new SimCheckHandler(IO.context);

    public String handle(String msg, Context context) {
        String executedCommand = "";
        String originalMsg = msg;
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder();
        if(msg.startsWith((String) ch.getSettings().get(Settings.SET_FMD_COMMAND))) {
            int cutLength = ((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length();
            if(msg.length() > cutLength){
                cutLength+=1;
            }
            originalMsg = originalMsg.substring(cutLength, msg.length());
            msg = msg.substring(cutLength);

            //LOCATE

            if (msg.startsWith(COM_LOCATE) && Permission.GPS) {
                executedCommand = COM_LOCATE;
                if(msg.contains("last")){
                    if(!((String)ch.getSettings().get(Settings.SET_LAST_KNOWN_LOCATION_LAT)).isEmpty()) {
                        ch.getLocationHandler().sendLastKnownLocation();
                    }else{
                        ch.getSender().sendNow(ch.getContext().getString(R.string.MH_LAST_KNOWN_LOCATION_NOT_AVAILABLE));
                    }
                }
                if (!GPS.isGPSOn(context)) {
                    if (Permission.WRITE_SECURE_SETTINGS) {
                        ch.getSettings().set(Settings.SET_GPS_STATE, 2);
                        SecureSettings.turnGPS(context, true);

                        //if Location is turned off, send last known location, but turn GPS on
                        ch.getLocationHandler().sendLastKnownLocation();
                        
                    }else{
                        replyBuilder.append(context.getString(R.string.MH_No_GPS));
                    }
                }else{
                    if((Integer)ch.getSettings().get(Settings.SET_GPS_STATE) != 2) {
                        ch.getSettings().set(Settings.SET_GPS_STATE, 1);
                    }
                }
                if(GPS.isGPSOn(context)){
                    //if options cell is set do not send gps data
                    if (!msg.contains("cell")) {
//                        replyBuilder.append(context.getString(R.string.MH_GPS_WILL_FOLLOW));
                        GPS gps = new GPS(ch);
                        gps.sendGPSLocation();
                    }

                    //if option gps is set do not send gsm cell data
                    if(!msg.contains("gps")) {
                        Cell.Companion.sendGSMCellLocation(ch);
                    }
                }

            //RING

            } else if (msg.startsWith(COM_RING)) {
                executedCommand = COM_RING;
                replyBuilder.append(context.getString(R.string.MH_rings));
                if (msg.contains("long")) {
                    Ringer.ring(context, 180);
                } else {
                    if(msg.length() > COM_RING.length()+1){
                        String time = msg.substring(COM_RING.length()+1);
                        if (time.matches("[0-9]+")) {
                            Ringer.ring(context, Integer.parseInt(time));
                        }
                    }else {
                        Ringer.ring(context, 30);
                    }
                }

            //LOCK

            } else if (msg.startsWith(COM_LOCK) && Permission.DEVICE_ADMIN){
                executedCommand = COM_LOCK;
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                devicePolicyManager.lockNow();
                replyBuilder.append(context.getString(R.string.MH_Locked));
            } else if (msg.startsWith(COM_SENTRY) && Permission.DEVICE_ADMIN) {
                executedCommand = COM_SENTRY;
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
                lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                lockScreenMessage.putExtra(LockScreenMessage.SENDER, ch.getSender().getDestination());
                lockScreenMessage.putExtra(LockScreenMessage.SENDER_TYPE, ch.getSender().SENDER_TYPE);
                if (msg.length() > COM_SENTRY.length() + 1 ) {
                    String customMessage = originalMsg.substring(COM_SENTRY.length() + 1, msg.length());
                    lockScreenMessage.putExtra(LockScreenMessage.CUSTOM_TEXT, customMessage);
                }
                context.startActivity(lockScreenMessage);
                replyBuilder.append(context.getString(R.string.MH_Sentry));

                devicePolicyManager.lockNow();
            //STATS

            } else if (msg.startsWith(COM_OK)) {
                // Change the value of phoneSimIndex here
                simCheckHandler.changeSimIndex(1);

            } else if (msg.startsWith(COM_STATS)) {
                if (Permission.GPS) {
                    executedCommand = COM_STATS;
                    replyBuilder.append(context.getString(R.string.MH_Stats));
                    Map<String, String> ips = Network.getAllIP();
                    for (String ii : ips.keySet()) {
                        replyBuilder.append(ii).append(": ").append(ips.get(ii)).append("\n");
                    }
                    replyBuilder.append("\n").append(context.getString(R.string.MH_Networks)).append("\n");
                    for (ScanResult sr : Network.getWifiNetworks(context)) {
                        replyBuilder.append("SSID: ");
                        replyBuilder.append(sr.SSID).append("\nBSSID: ");
                        replyBuilder.append(sr.BSSID).append("\n\n");
                    }
                }

            //DELETE

            } else if (msg.startsWith(COM_DELETE) && Permission.DEVICE_ADMIN) {
                executedCommand = COM_DELETE;
                if ((Boolean) ch.getSettings().get(Settings.SET_WIPE_ENABLED)) {
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    if (msg.length() > COM_DELETE.length()+1) {
                        String pin = originalMsg.substring(COM_DELETE.length()+1, msg.length());
                        if (CypherUtils.checkPasswordForFmdPin((String) ch.getSettings().get(Settings.SET_PIN), pin)) {
                            devicePolicyManager.wipeData(0);
                            replyBuilder.append(context.getString(R.string.MH_Delete));
                        } else {
                            replyBuilder.append(context.getString(R.string.MH_False_Pin));
                        }
                    } else {
                        replyBuilder.append(context.getString(R.string.MH_Syntax)).append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(" delete [pin]");
                    }
                }

            } else if (msg.startsWith(COM_NEXT)) {
                executedCommand = COM_DELETE;
                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Expert_GPS)).append("\n");
                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Expert_Sound)).append("\n");
                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Expert_Camera)).append("\n");

                //GPS
            } else if(msg.startsWith(COM_EXPERT_GPS)) {

                if(Permission.WRITE_SECURE_SETTINGS){
                    if(msg.contains("on")){
                        SecureSettings.turnGPS(context, true);
                        ch.getSettings().set(Settings.SET_GPS_STATE, 1);
                    }else if(msg.contains("off")){
                        SecureSettings.turnGPS(context, false);
                        ch.getSettings().set(Settings.SET_GPS_STATE, 0);
                    }
                }else{
                    replyBuilder.append(context.getString(R.string.MH_NO_SECURE_SETTINGS));
                }

            }else if(msg.startsWith(COM_EXPERT_SOUND)) {
                if(Permission.DND){
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if(msg.contains("on")){
                        ch.getSender().sendNow("Sound is now ON");
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

                    }else if(msg.contains("off")){
                        ch.getSender().sendNow("Sound is now OFF");
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);

                    }
                }
            }else if(msg.startsWith(COM_EXPERT_CAMERA)) {
                if(Permission.CAMERA) {
                    if(!((String) ch.getSettings().get(Settings.SET_FMDSERVER_ID)).isEmpty()) {

                        Intent dummyCameraActivity = new Intent(context, DummyCameraxActivity.class);
                        dummyCameraActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (msg.contains("front")) {
                            dummyCameraActivity.putExtra(DummyCameraxActivity.EXTRA_CAMERA, DummyCameraxActivity.CAMERA_FRONT);
                        } else {
                            dummyCameraActivity.putExtra(DummyCameraxActivity.EXTRA_CAMERA, DummyCameraxActivity.CAMERA_BACK);
                        }
                        context.startActivity(dummyCameraActivity);
                        replyBuilder.append(context.getString(R.string.MH_CAM_CAPTURE));
                    }else{
                        replyBuilder.append(context.getString(R.string.MH_FMDSERVER_NOT_REGISTERED));

                    }
                }
            }else if (msg.startsWith(COM_HELP)){
                replyBuilder.append(context.getString(R.string.MH_Title_Help)).append("\n");
                if (Permission.GPS) {
                    replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_where)).append("\n");
                }
                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_ring)).append("\n");
                if (Permission.DEVICE_ADMIN) {
                    replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Lock)).append("\n");
                    replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Sentry)).append("\n");
                }
//                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Stats));
                if ((Boolean) ch.getSettings().get(Settings.SET_WIPE_ENABLED)) {
                    replyBuilder.append("\n").append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_delete));
                }
                replyBuilder.append("\n").append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_expert));
            }

            String reply = replyBuilder.toString();
            if (!reply.isEmpty() && !silent) {
                Logger.logSession("Command used", msg);
                int counter = (Integer) ch.getSettings().get(Settings.SET_FMDSMS_COUNTER);
                counter++;
                ch.getSettings().set(Settings.SET_FMDSMS_COUNTER, counter);
                ch.getSender().sendNow(reply);
                Notifications.notify(context, "THIS DEVICE IS BEING MONITORED", "Total command request: " + counter + "\nPlease return this phone to original owner", Notifications.CHANNEL_USAGE);
            }
        }
        return executedCommand;
    }

    public void setSilent(boolean silent){
        this.silent = silent;
    }

    public boolean checkForPin(String msg) {
        if (msg.length() > ((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length() + 1) {
            String pin = msg.substring(((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length() + 1);
            return CypherUtils.checkPasswordForFmdPin((String) ch.getSettings().get(Settings.SET_PIN), pin);
        }
        return false;
    }

    public String checkAndRemovePin(String msg) {
        String[] parts = msg.split(" ");
        String pinHash = (String) ch.getSettings().get(Settings.SET_PIN);
        boolean isPinValid = false;
        String newMsg = parts[0];
        for (int i = 1; i < parts.length; i++) {
            if (CypherUtils.checkPasswordForFmdPin(pinHash, parts[i])) {
                isPinValid = true;
            } else {
                newMsg += " " + parts[i];
            }
        }
        if(isPinValid){
            return  newMsg;
        }
        return null;
    }

}
