package com.nud.secureguardtech.data.io;

import com.nud.secureguardtech.data.ConfigSMSRec;
import com.nud.secureguardtech.data.Contact;
import com.nud.secureguardtech.data.LogData;
import com.nud.secureguardtech.data.LogEntry;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.json.JSONContact;
import com.nud.secureguardtech.data.io.json.JSONLog;
import com.nud.secureguardtech.data.io.json.JSONLogEntry;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.io.json.JSONWhiteList;

public class JSONFactory {

    public static Settings convertJSONSettings(JSONMap jsonSettings) {
        Settings Settings = new Settings();
        if(jsonSettings != null) {
            Settings.putAll(jsonSettings);
        }
        return Settings;
    }

    public static JSONMap convertSettings(Settings Settings) {
        JSONMap jsonSettings = new JSONMap();
        jsonSettings.putAll(Settings);
        return jsonSettings;
    }

    public static Contact convertJSONContact(JSONContact jsonContact) {
        return new Contact(jsonContact.getName(), jsonContact.getNumber());
    }

    public static JSONContact convertContact(Contact contact) {
        JSONContact jsonContact = new JSONContact();
        jsonContact.setName(contact.getName());
        jsonContact.setNumber(contact.getNumber());
        return jsonContact;
    }

    public static WhiteList convertJSONWhiteList(JSONWhiteList jsonWhiteList) {
        WhiteList whiteList = new WhiteList();
        if(jsonWhiteList != null) {
            for (JSONContact jsonContact : jsonWhiteList) {
                whiteList.superAdd(convertJSONContact(jsonContact));
            }
        }
        return whiteList;
    }

    public static JSONWhiteList convertWhiteList(WhiteList whiteList) {
        JSONWhiteList jsonWhiteList = new JSONWhiteList();
        for (Contact c : whiteList) {
            jsonWhiteList.add(convertContact(c));
        }
        return jsonWhiteList;
    }

    public static ConfigSMSRec convertJSONConfig(JSONMap jsonSettings) {
        ConfigSMSRec temp = new ConfigSMSRec();
        if(jsonSettings != null) {
            temp.putAll(jsonSettings);
        }
        return temp;
    }

    public static JSONMap convertTempConfigSMSRec(ConfigSMSRec configSMSRec) {
        JSONMap jsonSettings = new JSONMap();
        jsonSettings.putAll(configSMSRec);
        return jsonSettings;
    }

    public static LogEntry convertJSONLogEntry(JSONLogEntry jsonLogEntry){
        return new LogEntry(jsonLogEntry.getTime(), jsonLogEntry.getText());
    }

    public static JSONLogEntry convertLogEntry(LogEntry logEntry){
        return new JSONLogEntry(logEntry.getTime(), logEntry.getText());
    }

    public static LogData convertJSONLog(JSONLog jsonLog) {
        LogData temp = new LogData();
        if(jsonLog != null) {
            for(JSONLogEntry jsonLogEntry : jsonLog){
                temp.add(convertJSONLogEntry(jsonLogEntry));
            }
        }
        return temp;
    }

    public static JSONLog convertLogData(LogData logData) {
        JSONLog jsonLog = new JSONLog();
        for(LogEntry logEntry : logData){
            jsonLog.add(convertLogEntry(logEntry));
        }
        return jsonLog;
    }
}
