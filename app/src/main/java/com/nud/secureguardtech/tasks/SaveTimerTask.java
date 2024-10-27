package com.nud.secureguardtech.tasks;

import java.util.TimerTask;

import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;

public class SaveTimerTask extends TimerTask {

    private Settings Settings;
    private WhiteList whiteList;

    public SaveTimerTask(Settings Settings) {
        this.Settings = Settings;
    }

    public SaveTimerTask(WhiteList whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public void run() {
        write();
    }

    public void write(){
        if (Settings != null) {
            IO.write(JSONFactory.convertSettings(Settings), IO.settingsFileName);
        } else if (Settings != null) {
            IO.write(JSONFactory.convertWhiteList(whiteList), IO.whiteListFileName);
        }
    }
}
