package com.nud.secureguardtech.ui.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nud.secureguardtech.ui.helper.WhiteListViewAdapter;

import java.util.LinkedList;
import java.util.List;

import com.nud.secureguardtech.R;
import com.nud.secureguardtech.data.Contact;
import com.nud.secureguardtech.data.Settings;
import com.nud.secureguardtech.data.WhiteList;
import com.nud.secureguardtech.data.io.IO;
import com.nud.secureguardtech.data.io.JSONFactory;
import com.nud.secureguardtech.data.io.json.JSONMap;
import com.nud.secureguardtech.data.io.json.JSONWhiteList;

public class WhiteListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private WhiteList whiteList;
    private Settings Settings;

    private ListView listWhiteList;
    private WhiteListViewAdapter whiteListAdapter;
    private TextView textWhitelistEmpty;
    private Button buttonAddContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);

        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        listWhiteList = findViewById(R.id.listWhiteList);
        whiteListAdapter = new WhiteListViewAdapter(this, whiteList);
        listWhiteList.setAdapter(whiteListAdapter);
        listWhiteList.setOnItemClickListener(this);
        registerForContextMenu(listWhiteList);

        whiteListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (whiteList.isEmpty()) {
                    textWhitelistEmpty.setVisibility(View.VISIBLE);
                } else {
                    textWhitelistEmpty.setVisibility(View.GONE);
                }
            }
        });

        textWhitelistEmpty = findViewById(R.id.whitelistEmpty);
        if (whiteList.isEmpty()) {
            textWhitelistEmpty.setVisibility(View.VISIBLE);
        }

        buttonAddContact = findViewById(R.id.buttonAddContact);
        buttonAddContact.setOnClickListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(getString(R.string.WhiteList_Select_Action));
        menu.add(0, v.getId(), 0, getString(R.string.Delete));
    }

    @Override
    public void onClick(View v) {
        if (v == buttonAddContact) {
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

            try {
                startActivityForResult(intent, 1);
            }catch(ActivityNotFoundException e) {
                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                try {
                    startActivityForResult(intent, 1);
                }catch(ActivityNotFoundException e2){
                    Toast.makeText(this, getString(R.string.not_possible), Toast.LENGTH_LONG);
                }

            }


        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        if (item.getTitle() == getString(R.string.Delete)) {
            whiteList.remove(index);
            whiteListAdapter.notifyDataSetChanged();
        } else {
            return false;
        }
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (1):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    String[] projection = new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    };
                    Cursor c = managedQuery(contactData, projection, null, null, null);
                    List<Contact> contacts = new LinkedList<>();
                    List<String> numbers = new LinkedList<>();
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME));
                        String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        contacts.add(new Contact(name, phoneNumber));
                        numbers.add(phoneNumber);

                        while (c.moveToNext()) {
                            String cNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String cName = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                            if (!cNumber.isEmpty()) {
                                contacts.add(new Contact(cName, cNumber));
                                numbers.add(cNumber);
                            }
                        }
                    }

                    if(numbers.size() == 1){
                        addContactToWiteList(contacts.get(0));
                    }else{
                        final List<Contact> finalContacts = contacts;
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.WhiteList_Select_Number));
                        String[] numbersArray = numbers.toArray(new String[numbers.size()]);
                        builder.setItems(numbersArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addContactToWiteList(finalContacts.get(which));
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + reqCode);
        }
    }

    private void addContactToWiteList(Contact contact){
        if(contact != null) {
            if (!whiteList.checkForDuplicates(contact)) {
                whiteList.add(contact);
                whiteListAdapter.notifyDataSetChanged();
                if (!(Boolean) Settings.get(Settings.SET_FIRST_TIME_CONTACT_ADDED)) {
                    new AlertDialog.Builder(this)
                            .setMessage(this.getString(R.string.Alert_First_Time_contact_added))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Settings.set(Settings.SET_FIRST_TIME_CONTACT_ADDED, true);
                                }
                            })
                            .show();
                }
            } else {
                Toast toast = Toast.makeText(this, getString(R.string.Toast_Duplicate_contact), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

}