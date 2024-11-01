package com.nud.secureguardtech.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nud.secureguardtech.utils.Permission;

import com.nud.secureguardtech.R;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;

public class IntroductionActivity extends AppCompatActivity implements View.OnClickListener {

    public static String POS_KEY = "pos";

    private TextView textViewInfoText;

    private Button buttonRoot;
    private Button buttonShizuku;
    private Button buttonNext;
    private Button buttonGivePermission;
    private int position = 0;
    private Settings Settings;

    private int colorEnabled;
    private int colorDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            position = bundle.getInt(POS_KEY);
        }
        IO.context = this;
        Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        textViewInfoText = findViewById(R.id.textViewInfoText);
        buttonRoot = findViewById(R.id.buttonPermViaRoot);
        buttonRoot.setOnClickListener(this);
        buttonShizuku = findViewById(R.id.buttonPermViaShizuku);
        buttonShizuku.setOnClickListener(this);
        buttonGivePermission = findViewById(R.id.buttonGivePermission);
        buttonGivePermission.setOnClickListener(this);
        buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        }else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }

        updateViews();

    }

    public void updateViews() {
        switch (position) {
            case 0:
                buttonGivePermission.setText(getString(R.string.about_wiki));
                if ((Integer) Settings.get(Settings.SET_INTRODUCTION_VERSION) > 0) {
                    textViewInfoText.setText(getString(R.string.Introduction_UpdatePermission));
                } else {
                    textViewInfoText.setText(getString(R.string.Introduction_Introduction));
                }
                break;
            case 1: // SMS Permission
                buttonGivePermission.setText(getString(R.string.Introduction_Give_Permission));
                textViewInfoText.setText(getString(R.string.Permission_SMS));
                if (Permission.checkSMSPermission(this)) {
                    buttonNext.setEnabled(true);
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonNext.setEnabled(false);
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 2: //Contacts Permission
                textViewInfoText.setText(getString(R.string.Permission_CONTACTS));
                if (Permission.checkContactsPermission(this)) {
                    buttonNext.setEnabled(true);
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonNext.setEnabled(false);
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 3: // GPS Permission
                textViewInfoText.setText(getString(R.string.Permission_GPS));
                if (Permission.checkGPSBackgroundPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                if(Permission.checkGPSForegroundPermission(this) && !Permission.checkGPSBackgroundPermission(this)){
                    Permission.requestGPSBackgroundPermission(this);
                }
                break;
            case 4: // DND Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textViewInfoText.setText(getString(R.string.Permission_DND));
                    if (Permission.checkDNDPermission(this)) {
                        buttonGivePermission.setBackgroundColor(colorEnabled);
                    } else {
                        buttonGivePermission.setBackgroundColor(colorDisabled);
                    }
                } else {
                    position++;
                    updateViews();
                }
                break;
            case 5: // DeviceAdmin Permission
                textViewInfoText.setText(getString(R.string.Permission_DEVICE_ADMIN));
                if (Permission.checkDeviceAdminPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 6: // Overlay Permission
                textViewInfoText.setText(getString(R.string.Permission_OVERLAY));
                if (Permission.checkOverlayPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 7: // Write Secure Settings Permission
                if(Permission.isShizukuRunning()){
                    buttonShizuku.setVisibility(View.VISIBLE);
                    if(Permission.checkShizukuPermission() && !Permission.checkWriteSecurePermission(this)){
                        Permission.requestWriteSecureSettingsPermissionViaShizuku(this);
                    }
                }
                buttonRoot.setVisibility(View.VISIBLE);
                textViewInfoText.setText(getString(R.string.Permission_WRITE_SECURE_SETTINGS));
                if (Permission.checkWriteSecurePermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 8: // Notificaton Permission
                buttonShizuku.setVisibility(View.INVISIBLE);
                buttonRoot.setVisibility(View.INVISIBLE);
                textViewInfoText.setText(getString(R.string.Permission_Notification));
                if (Permission.checkNotificationAccessPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 9: // Camera Permission
                textViewInfoText.setText(getString(R.string.Permission_Camera));
                if (Permission.checkCameraPermissions(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 10:    // Battery Optimization Permission
                textViewInfoText.setText(R.string.Permission_IGNORE_BATTERY_OPTIMIZATION);
                if(Permission.checkBatteryOptimizationPermission(this)){
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 11:    // Post Notification Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    textViewInfoText.setText(R.string.Permission_POST_NOTIFICATIONS);
                    if (Permission.checkPostNotificationsPermissions(this)) {
                        buttonNext.setEnabled(true);
                        buttonGivePermission.setBackgroundColor(colorEnabled);
                    } else {
                        buttonNext.setEnabled(false);
                        buttonGivePermission.setBackgroundColor(colorDisabled);
                    }
                } else {
                    position++;
                    updateViews();
                }
                break;
            case 12:
                Settings.setIntroductionPassed();
                Intent myIntent = new Intent(this, MainActivity.class);
                finish();
                startActivity(myIntent);
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonGivePermission) {
            switch (position) {
                case 0:
                    Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://national-u.edu.ph/nu-dasmarinas/"));
                    startActivity(helpIntent);
                    break;
                case 1:
                    Permission.requestSMSPermission(this);
                    break;
                case 2:
                    Permission.requestContactPermission(this);
                    break;
                case 3:
                    Permission.requestGPSForegroundPermission(this);
                    break;
                case 4:
                    Permission.requestDNDPermission(this);
                    break;
                case 5:
                    Permission.requestDeviceAdminPermission(this);
                    break;
                case 6:
                    Permission.requestOverlayPermission(this);
                    break;
                case 7:
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/Nulide/findmydevice/-/wikis/PERMISSION-WRITE_SECURE_SETTINGS"));
                    startActivity(intent);
                    updateViews();
                    break;
                case 8:
                    Permission.requestNotificationPermission(this);
                    break;
                case 9:
                    Permission.requestCameraPermission(this);
                    break;
                case 10:
                    Permission.requestBatteryOptimizationPermission(this);
                    break;
                case 11:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Permission.requestPostNotificationsPermission(this);
                    }
                    break;
            }
        } else if (v == buttonNext) {
            position++;
            updateViews();
        } else if (v == buttonShizuku){
            if(Permission.checkShizukuPermission()){
                Permission.requestWriteSecureSettingsPermissionViaShizuku(this);
            }else {
                Permission.requestShizukuPermission();
            }
        } else if (v == buttonRoot) {
            Permission.requestWriteSecureSettingsPermissionViaRoot(this);
            updateViews();
        }
    }
}