package com.nud.secureguardtech.logic;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;

import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;

public class SimCheckHandler {
    private final WeakReference<Context> mContextRef;

    private Settings settings;
    private boolean isRunning = false;
    private final int LOOP_DELAY = 1; // in minutes;

    public int phoneSimLinkIndex = 24; // checks if the user acknowledges the new number

    public String linkedNumber = "";

    public SimCheckHandler(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    public void startLoop() {
        if (!isRunning) {
            isRunning = true;
            checkStatus();
        }
    }

    public void stopLoop() {
        isRunning = false;
    }

    private void checkStatus() {
        Context context = mContextRef.get();
        if (context == null) {
            isRunning = false;
            return;
        }

        // Permission is granted, proceed with reading phone state
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        loadConfig();

        if (telephonyManager != null) {
            int simState = telephonyManager.getSimState();

            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    changeSimIndex(0);
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    try {
                        String phoneNumber = telephonyManager.getLine1Number();
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            // Link the sim number to phone
                            if (linkedNumber.equals("")) {
                                showToast("Your phone number: " + phoneNumber + " Your will get notified upon sim change");
                                linkedNumber = phoneNumber;
                            }

                            // if sim # is not equal to linkedNumber and phoneSimLinkIndex is  not equal 1
                            if (!phoneNumber.equals(linkedNumber) && phoneSimLinkIndex != 1) {
                                alertUser(phoneNumber);
                            }
                        }
                    } catch (SecurityException e) {
                        showToast("Security Exception: " + e.getMessage());
                    }
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    changeSimIndex(0);
                    break;
                default:
                    showToast("Unable to determine SIM card state");
                    break;
            }
        }

        if (isRunning) {
            // Post the next execution after the delay
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkStatus();
                }
            }, LOOP_DELAY * 1000 * 60);
        }
    }

    private void alertUser(String capturedNumber) {
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        String phoneNumber = (String) settings.get(Settings.SET_RESET_NUMBER) ;

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = "1235";  // Default value if the number is not found
        }

        String message = "SecureGuard ALERT! A new SIM card has been inserted into your device.\n\nIf this seems suspicious, you can use this number to send your commands.";

        try {
            // Try sending SMS using SIM 1
            SmsManager smsManager1 = SmsManager.getDefault();
            smsManager1.sendTextMessage(phoneNumber, null, message, null, null);

        } catch (Exception e) {
            try {
                Method method = Class.forName("android.telephony.SmsManager")
                        .getDeclaredMethod("getSmsManagerForSubscriptionId", int.class);
                SmsManager smsManager2 = (SmsManager) method.invoke(null, 1); // Use 1 for SIM 2
                smsManager2.sendTextMessage(phoneNumber, null, message, null, null);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showToast(String message) {
        Context context = mContextRef.get();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private void loadConfig() {
        try {
            // Load config file from internal storage
            FileInputStream fis = mContextRef.get().openFileInput("config.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            fis.close();

            // Parse JSON and get the value of phoneSimLinkIndex
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            phoneSimLinkIndex = jsonObject.getInt("phoneSimLinkIndex");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig(String json) {
        try {
            // Save config file to internal storage
            FileOutputStream fos = mContextRef.get().openFileOutput("config.json", Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(json);
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeSimIndex(int newIndex) {
        phoneSimLinkIndex = newIndex;
        try {
            // Load config file from internal storage
            FileInputStream fis = mContextRef.get().openFileInput("config.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            fis.close();

            // Parse JSON, update the value, and save it
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            jsonObject.put("phoneSimLinkIndex", newIndex);
            saveConfig(jsonObject.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
