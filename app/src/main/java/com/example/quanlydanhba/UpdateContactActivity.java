package com.example.quanlydanhba;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class UpdateContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle bundle = getIntent().getExtras();
        String oldName = bundle.getString("oldName");
        String oldPhoneNumber = bundle.getString("oldPhoneNumber");
        TextView tvOldPhoneNumber = findViewById(R.id.tv_old_phone_number);
        TextView tvOldName = findViewById(R.id.tv_old_name);
        tvOldPhoneNumber.setText(oldPhoneNumber);
        tvOldName.setText(oldName);

        TextView tvNewPhoneNumber = findViewById(R.id.tv_new_phone_number);
        TextView tvNewName = findViewById(R.id.tv_new_name);

        Button btnUpdateContact = findViewById(R.id.btn_update_contact);
        btnUpdateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPhoneNumber = tvNewPhoneNumber.getText().toString();
                String newName = tvNewName.getText().toString();
                handleUpdateContact(new Contact(newPhoneNumber, newName), oldPhoneNumber, oldName);
            }
        });
    }

    private void handleUpdateContact(Contact updatingContact, String oldPhoneNumber, String oldName) {
        if (updatingContact.getName().isEmpty() && updatingContact.getPhoneNumber().isEmpty()) {
            Toast.makeText(this, "Bạn chưa điền thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 0);
        }
        else {
            boolean isSuccessful = false;
            ArrayList<String> contactIds = getAllConactIds();

            for (String contactId : contactIds) {
                if (updateContact(updatingContact.getName(), updatingContact.getPhoneNumber(), contactId, oldPhoneNumber, oldName)) {
                    isSuccessful = true;
                }
            }

            if (isSuccessful) {
                Toast.makeText(this, "Chỉnh sửa thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                Toast.makeText(this, "Chỉnh sửa không thành công", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean updateContact(String name, String number ,String ContactId, String oldPhoneNumber, String oldName) {
        boolean success = true;
        
        try {
            name = name.trim();
            number = number.trim();

            ContentResolver contentResolver  = getContentResolver();

            String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";

            String[] nameParams = new String[]{ContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, oldName};
            String[] numberParams = new String[]{ContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, oldPhoneNumber};

            ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<android.content.ContentProviderOperation>();

            if (!name.isEmpty()) {
                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where,nameParams)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build());
            }

            if (!number.isEmpty()) {

                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where,numberParams)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                        .build());
            }
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public ArrayList<String> getAllConactIds() {
        ArrayList<String> contactList = new ArrayList<String>();

        Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, null, null, null, "display_name ASC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    contactList.add(""+_id);
                }
                while(cursor.moveToNext());
            }
        }
        return contactList;
    }
}