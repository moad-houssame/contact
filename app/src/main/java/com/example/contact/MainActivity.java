package com.example.contact;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView lv;
    private ArrayList<String> contactsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = findViewById(R.id.lv);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        lv.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else {
            loadContacts();
        }

        lv.setOnItemClickListener((parent, view, position, id) -> {
            String selected = contactsList.get(position);
            String[] parts = selected.split(" : ");
            if (parts.length < 2) return;
            String name = parts[0];
            String number = parts[1];

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisir une action pour " + name);
            builder.setItems(new CharSequence[]{"Appeler", "Envoyer SMS"}, (dialog, which) -> {
                if (which == 0) { // Appel
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + number));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 200);
                    } else {
                        startActivity(callIntent);
                    }
                } else if (which == 1) { // SMS
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
                    smsIntent.putExtra("sms_body", "Salut les Zéros !");
                    startActivity(smsIntent);
                }
            });
            builder.show();
        });
    }

    private void loadContacts() {
        ContentResolver cr = getContentResolver();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (phones != null) {
            while (phones.moveToNext()) {
                int nameIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (nameIndex >= 0 && numberIndex >= 0) {
                    String name = phones.getString(nameIndex);
                    String number = phones.getString(numberIndex);
                    contactsList.add(name + " : " + number);
                }
            }
            phones.close();
        }

        adapter.notifyDataSetChanged();
    }

    // Demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        }

        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Appel autorisé maintenant — tu peux relancer l’intent si besoin
        }
    }
}
