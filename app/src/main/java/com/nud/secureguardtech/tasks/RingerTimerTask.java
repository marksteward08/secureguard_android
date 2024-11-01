package com.nud.secureguardtech.tasks;

import android.media.Ringtone;

import java.util.Timer;
import java.util.TimerTask;

import com.nud.secureguardtech.ui.RingerActivity;

public class RingerTimerTask extends TimerTask {

    private final Timer t;
    private final Ringtone r;
    private int I = 0;
    private RingerActivity ringActivity;

    public RingerTimerTask(Timer t, Ringtone r, RingerActivity ringActivity) {
        this.t = t;
        this.r = r;
        this.ringActivity = ringActivity;
    }

    @Override
    public void run() {
        if (I == 15) {
            r.stop();
            t.cancel();
            ringActivity.finish();
        } else {
            I++;
        }
    }

    public void stop(){
        r.stop();
        cancel();
    }
}
