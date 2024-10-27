package com.nud.secureguardtech.ui;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nud.secureguardtech.R;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.sender.FooSender;
import com.nud.secureguardtech.sender.SMS;
import com.nud.secureguardtech.sender.Sender;

public class LockScreenMessage extends AppCompatActivity {

    public static final String SENDER = "sender";
    public static final String SENDER_TYPE = "type";
    public static final String CUSTOM_TEXT = "ctext";
    private Sender sender;

    private TextView tvLockScreenMessage;

    int usageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_message);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        Bundle bundle = getIntent().getExtras();
        switch(bundle.getString(SENDER_TYPE)){
            case SMS.TYPE:
                    sender = new SMS(bundle.getString(SENDER));
                break;
            default:
                sender = new FooSender();
        }
        Settings Settings;
        IO.context = this;
        Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        tvLockScreenMessage = findViewById(R.id.textViewLockScreenMessage);
        if (bundle.containsKey(CUSTOM_TEXT)) {
            tvLockScreenMessage.setText(bundle.getString(CUSTOM_TEXT));
        } else {
            tvLockScreenMessage.setText((String) Settings.get(Settings.SET_LOCKSCREEN_MESSAGE));
        }
    }

    @Override
    protected void onPause() {

        if (usageIndex != 0) {
            sender.sendNow(getString(R.string.LockScreen_Usage_detectd));
        }
        super.onPause();
        usageIndex = 1;
    }

    @Override
    public void onBackPressed() {
        sender.sendNow(getString(R.string.LockScreen_Backbutton_pressed));
        finish();
    }



}