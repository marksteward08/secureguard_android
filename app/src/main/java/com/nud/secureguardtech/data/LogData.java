package com.nud.secureguardtech.data;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;

public class LogData extends LinkedList<LogEntry> {

    public void add(long time, String text) {
        this.add(new LogEntry(time, text));
        if(this.size() > 100){
            this.remove(0);
        }
        IO.write(JSONFactory.convertLogData(this), IO.logFileName);
    }

    public void cleanUp(){
        while(this.size() > 100){
            this.remove(0);
        }
        IO.write(JSONFactory.convertLogData(this), IO.logFileName);
    }

    public List<String> getDates(){
        List<String> dates = new LinkedList<>();
        for(LogEntry logEntry : this){
            Date date = new Date(logEntry.getTime());
            dates.add(date.toString());
        }
        return dates;
    }

}
