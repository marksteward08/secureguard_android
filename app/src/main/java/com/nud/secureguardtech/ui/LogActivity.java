package com.nud.secureguardtech.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nud.secureguardtech.R;
import com.nud.secureguardtech.data.LogData;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONLog;

public class LogActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listLog;
    private LogData logData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logData = JSONFactory.convertJSONLog(IO.read(JSONLog.class, IO.logFileName));
        LogData correctedLogData = new LogData();

        if(logData.size() != 0) {
            for (int x = logData.size() - 1; x >= 0; x--) {
                correctedLogData.add(logData.get(x));
            }
        }
        logData = correctedLogData;

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                logData.getDates());

        listLog = findViewById(R.id.listLog);
        listLog.setAdapter(adapter);
        listLog.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Log_Alert_LogData))
                .setMessage(logData.get(position).getText())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}