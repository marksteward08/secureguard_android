package de.nulide.findmydevice.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

public class AirplaneCheckHandler {

    private AirplaneModeReceiver airplaneModeReceiver;

    public void registerAirplaneModeReceiver(Context context) {
        airplaneModeReceiver = new AirplaneModeReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.registerReceiver(airplaneModeReceiver, intentFilter);

        // Check airplane mode status when registering receiver
        checkAndTurnOffAirplaneMode(context);
    }

    public void unregisterAirplaneModeReceiver(Context context) {
        if (airplaneModeReceiver != null) {
            context.unregisterReceiver(airplaneModeReceiver);
            airplaneModeReceiver = null;
        }
    }

    private void checkAndTurnOffAirplaneMode(Context context) {
        boolean isAirplaneModeOn = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (isAirplaneModeOn) {
            // Airplane mode is on, turn it off
            turnOffAirplaneMode(context);
        }
    }

    private void turnOffAirplaneMode(Context context) {
        // Check if the app has permission to write secure settings
        if (Settings.System.canWrite(context)) {
            // Set airplane mode off
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
        } else {
            Toast.makeText(context, "Permission to modify system settings not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private class AirplaneModeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                if (isAirplaneModeOn) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            turnOffAirplaneMode(context);
                        }
                    }, 5000);

                }
            }
        }
    }
}

