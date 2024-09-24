package de.nulide.findmydevice.ui;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.sender.FooSender;
import de.nulide.findmydevice.sender.SMS;
import de.nulide.findmydevice.sender.Sender;

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