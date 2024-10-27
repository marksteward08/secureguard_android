package com.nud.secureguardtech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nud.secureguardtech.logic.SimCheckHandler;
import com.nud.secureguardtech.utils.Logger;

import com.nud.secureguardtech.R;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.io.json.JSONWhiteList;
import com.nud.secureguardtech.receiver.PushReceiver;
import com.nud.secureguardtech.services.FMDServerLocationUploadService;
import com.nud.secureguardtech.ui.onboarding.UpdateboardingModernCryptoActivity;
import com.nud.secureguardtech.ui.settings.FMDConfigActivity;
import com.nud.secureguardtech.ui.settings.SettingsActivity;
import com.nud.secureguardtech.utils.Notifications;
import com.nud.secureguardtech.utils.Permission;
import host.stjin.expandablecardview.ExpandableCardView;

// Inject checkers
import com.nud.secureguardtech.logic.AirplaneCheckHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewFMDCommandName;
    private TextView textViewWhiteListCount;
    private TextView textViewCORE;
    private TextView textViewGPS;
    private TextView textViewDND;
    private TextView textViewDeviceAdmin;
    private TextView textViewWriteSecureSettings;
    private TextView textViewOverlay;
    private TextView textViewNotification;
    private TextView textViewCamera;
    private TextView textViewBatteryOptimization;
    private TextView textViewServerServiceEnabled;
    private TextView textViewServerRegistered;
    private TextView textViewPush;
    private Button buttonOpenWhitelist;
    private Button buttonOpenSettings;
    private ExpandableCardView expandableCardViewPermissions;

    private Settings settings;

    SimCheckHandler simCheckHandler = new SimCheckHandler(this);
    AirplaneCheckHandler airplaneCheckHandler = new AirplaneCheckHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PushReceiver.registerWithUnifiedPush(this);

        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        Notifications.init(this, false);
        Permission.initValues(this);

        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        if (((Integer) settings.get(Settings.SET_APP_CRASHED_LOG_ENTRY)) != -1) {
            Intent intent = new Intent(this, CrashedActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        settings.updateSettings();
        if (!settings.isIntroductionPassed() || !Permission.CORE) {
            Intent intent = new Intent(this, IntroductionActivity.class);
            startActivity(intent);
            finish();
            return;

        }
        if (!(Boolean) settings.get(Settings.SET_UPDATEBOARDING_MODERN_CRYPTO_COMPLETED)) {
            Intent intent = new Intent(this, UpdateboardingModernCryptoActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (settings.checkAccountExists()) {
            FMDServerLocationUploadService.scheduleJob(this, 0);
        }


        findAllViewsById();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permission.initValues(this);
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        updateViews();
    }

    public void findAllViewsById() {
        textViewFMDCommandName = findViewById(R.id.textViewFMDCommandName);
        textViewWhiteListCount = findViewById(R.id.textViewWhiteListCount);
        textViewCORE = findViewById(R.id.textViewCORE);
        textViewGPS = findViewById(R.id.textViewGPS);
        textViewDND = findViewById(R.id.textViewDND);
        textViewDeviceAdmin = findViewById(R.id.textViewDeviceAdmin);
        textViewWriteSecureSettings = findViewById(R.id.textViewWriteSecureSettings);
        textViewOverlay = findViewById(R.id.textViewOverlay);
        textViewNotification = findViewById(R.id.textViewNotification);
        buttonOpenWhitelist = findViewById(R.id.buttonOpenWhiteList);
        buttonOpenSettings = findViewById(R.id.buttonOpenSettings);
        buttonOpenWhitelist.setOnClickListener(this);
        buttonOpenSettings.setOnClickListener(this);
        textViewServerServiceEnabled = findViewById(R.id.textViewServerEnabled);
        textViewServerRegistered = findViewById(R.id.textViewRegisteredOnServer);
        textViewCamera = findViewById(R.id.textViewCamera);
        textViewBatteryOptimization = findViewById(R.id.textviewBatteryOptimization);
//        expandableCardViewPermissions = findViewById(R.id.expandableCardViewPermissions);
//        textViewPush = findViewById(R.id.textViewPushAvailable);
    }

    public void updateViews() {
        WhiteList whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        textViewFMDCommandName.setText((String) settings.get(Settings.SET_FMD_COMMAND));
        textViewWhiteListCount.setText(Integer.valueOf(whiteList.size()).toString());


        int colorEnabled;
        int colorDisabled;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        } else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }

        ForegroundColorSpan whitelistCountColor;
        if (whiteList.size() > 0) {
            whitelistCountColor = new ForegroundColorSpan(colorEnabled);
        } else {
            whitelistCountColor = new ForegroundColorSpan(colorDisabled);
        }
        String whitelistPrefix = getString(R.string.Settings_WhiteList) + ": ";
        String whitelistAll = whitelistPrefix + whiteList.size();
        Spannable spannable = new SpannableString(whitelistAll);
        spannable.setSpan(whitelistCountColor, whitelistPrefix.length(), whitelistAll.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewWhiteListCount.setText(spannable, TextView.BufferType.SPANNABLE);

//        if (Permission.CORE) {
//            textViewCORE.setText(getString(R.string.Enabled));
//            textViewCORE.setTextColor(colorEnabled);
//        } else {
//            textViewCORE.setText(getString(R.string.Disabled));
//            textViewCORE.setTextColor(colorDisabled);
//        }
//        if (Permission.GPS) {
//            textViewGPS.setText(getString(R.string.Enabled));
//            textViewGPS.setTextColor(colorEnabled);
//        } else {
//            textViewGPS.setText(getString(R.string.Disabled));
//            textViewGPS.setTextColor(colorDisabled);
//        }
//        if (Permission.DND) {
//            textViewDND.setText(getString(R.string.Enabled));
//            textViewDND.setTextColor(colorEnabled);
//        } else {
//            textViewDND.setText(getString(R.string.Disabled));
//            textViewDND.setTextColor(colorDisabled);
//        }
//        if (Permission.DEVICE_ADMIN) {
//            textViewDeviceAdmin.setText(getString(R.string.Enabled));
//            textViewDeviceAdmin.setTextColor(colorEnabled);
//        } else {
//            textViewDeviceAdmin.setText(getString(R.string.Disabled));
//            textViewDeviceAdmin.setTextColor(colorDisabled);
//        }
//        if (Permission.WRITE_SECURE_SETTINGS) {
//            textViewWriteSecureSettings.setText(getString(R.string.Enabled));
//            textViewWriteSecureSettings.setTextColor(colorEnabled);
//        } else {
//            textViewWriteSecureSettings.setText(getString(R.string.Disabled));
//            textViewWriteSecureSettings.setTextColor(colorDisabled);
//        }
//        if (Permission.OVERLAY) {
//            textViewOverlay.setText(getString(R.string.Enabled));
//            textViewOverlay.setTextColor(colorEnabled);
//        } else {
//            textViewOverlay.setText(getString(R.string.Disabled));
//            textViewOverlay.setTextColor(colorDisabled);
//        }
//        if (Permission.NOTIFICATION_ACCESS) {
//            textViewNotification.setText(getString(R.string.Enabled));
//            textViewNotification.setTextColor(colorEnabled);
//        } else {
//            textViewNotification.setText(getString(R.string.Disabled));
//            textViewNotification.setTextColor(colorDisabled);
//        }
//        if(Permission.CAMERA){
//            textViewCamera.setText(getString(R.string.Enabled));
//            textViewCamera.setTextColor(colorEnabled);
//        } else {
//            textViewCamera.setText(getString(R.string.Disabled));
//            textViewCamera.setTextColor(colorDisabled);
//        }
//        if(Permission.BATTERY_OPTIMIZATION){
//            textViewBatteryOptimization.setText(R.string.Enabled);
//            textViewBatteryOptimization.setTextColor(colorEnabled);
//        } else {
//            textViewBatteryOptimization.setText(R.string.Disabled);
//            textViewBatteryOptimization.setTextColor(colorDisabled);
//        }
//        expandableCardViewPermissions.setTitle(-1, getString(R.string.Granted) + " " + Permission.ENABLED_PERMISSIONS + "/" + Permission.AVAILABLE_PERMISSIONS);
//
        if (settings.checkAccountExists()) {
            textViewServerServiceEnabled.setText(getString(R.string.Enabled));
            textViewServerServiceEnabled.setTextColor(colorEnabled);
        } else{
            textViewServerServiceEnabled.setText(getString(R.string.Disabled));
            textViewServerServiceEnabled.setTextColor(colorDisabled);
        }

        if (settings.checkAccountExists()) {
            textViewServerRegistered.setText(getString(R.string.registered));
            textViewServerRegistered.setTextColor(colorEnabled);
        }else{
            textViewServerRegistered.setText(getString(R.string.not_registered));
            textViewServerRegistered.setTextColor(colorDisabled);
        }

//        if (PushReceiver.isRegisteredWithUnifiedPush(this)) {
//            textViewPush.setText(R.string.Available);
//            textViewPush.setTextColor(colorEnabled);
//        } else {
//            textViewPush.setTextColor(colorDisabled);
//            textViewPush.setText(R.string.NOT_AVAILABLE);
//        }

        simCheckHandler.startLoop();
        airplaneCheckHandler.registerAirplaneModeReceiver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuItemSettings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v == buttonOpenWhitelist){
            //Intent whiteListActivity = new Intent(this, WhiteListActivity.class);
            //startActivity(whiteListActivity);

            Intent configActivity = new Intent(this, FMDConfigActivity.class);
            startActivity(configActivity);
        }
        if(v == buttonOpenSettings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        airplaneCheckHandler.unregisterAirplaneModeReceiver(this);
    }
}
