package com.example.quanlydanhba;

import static java.lang.System.in;

import android.Manifest;
import androidx.appcompat.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CheckBox checkbox;
    View inflatedView;
    ContactAdapter adapter;
    Button addButton;
    List<Contact> contacts;
    Menu myMenu;
    ListView lvContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_color)));

        contacts = getPhoneContacts();

        adapter = new ContactAdapter(this, contacts, false);
        lvContact = findViewById(R.id.lv_phone);
//        lvContact.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvContact.setAdapter(adapter);

        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, UpdateContactActivity.class);
                intent.putExtra("oldName", contacts.get(i).getName());
                intent.putExtra("oldPhoneNumber", contacts.get(i).getPhoneNumber());
                startActivity(intent);
            }
        });

//        lvContact.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
//                showCheckBoxes();
//                adapter.checkItem(i);
//                adapter.notifyDataSetChanged();
//                return true;
//            }
//        });

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddContactActivity();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        contacts = getPhoneContacts();
        renderContacts(contacts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        myMenu = menu;
        menu.findItem(R.id.option_delete).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.option_select) {
            if (adapter.getCheckBoxVisibility()) {
                hideCheckBoxes();
                addButton.setVisibility(View.VISIBLE);

                new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            item.setTitle("Chọn");
                            myMenu.findItem(R.id.option_delete).setVisible(false);

                        }
                }, 200);

            }
            else {
                showCheckBoxes();
                addButton.setVisibility(View.GONE);
                new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        item.setTitle("Bỏ chọn");
                        myMenu.findItem(R.id.option_delete).setVisible(true);
                    }
                }, 200);
            }

        }
        else if (item.getItemId() == R.id.option_sort_default) {
            adapter.setContacts(contacts);
            adapter.notifyDataSetChanged();
        }
        else if (item.getItemId() == R.id.option_sort_name_asc) {
            List<Contact> ascContacts = new ArrayList<>(contacts);
            ascContacts.sort(Comparator.comparing(Contact::getName));
            renderContacts(ascContacts);
        }
        else if (item.getItemId() == R.id.option_sort_name_desc) {
            List<Contact> descContacts = new ArrayList<>(contacts);
            descContacts.sort(Comparator.comparing(Contact::getName).reversed());
            renderContacts(descContacts);
        }
        else if (item.getItemId() == R.id.option_add) {
            openAddContactActivity();
        }
        else if (item.getItemId() == R.id.option_delete) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 0);
            }
            else {
                List<Contact> selectedContacts = getSelectedContacts();
                if (selectedContacts.size() == 0) {
                    Toast.makeText(this, "Vui lòng chọn danh bạ", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (int i = 0; i < selectedContacts.size(); i++) {
                        deleteContact(selectedContacts.get(i).getName(), selectedContacts.get(i).getPhoneNumber());
                    }
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    myMenu.findItem(R.id.option_select).setTitle("Chọn");
                                    item.setVisible(false);

                                }
                            }, 200);
                    hideCheckBoxes();
                    addButton.setVisibility(View.VISIBLE);
                    contacts = getPhoneContacts();
                    renderContacts(contacts);
                    Toast.makeText(this, "Xóa thành công " + String.valueOf(selectedContacts.size()) + " danh bạ", Toast.LENGTH_SHORT).show();

                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAddContactActivity() {
        Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
        startActivity(intent);
    }

    private void showCheckBoxes() {
        adapter.setCheckBoxVisibility(true);
        adapter.notifyDataSetChanged();
    }

    private void hideCheckBoxes() {
        adapter.setCheckBoxVisibility(false);
        adapter.notifyDataSetChanged();
    }

    public void renderContacts(List<Contact> contacts) {
        adapter.setContacts(contacts);
        adapter.notifyDataSetChanged();
    }

    public boolean deleteContact(String name, String phone) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)).equals(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return false;
    }


    public List<Contact> getSelectedContacts() {
        List<Contact> selectedContacts = new ArrayList<>();
        View view;
        for (int i = 0; i < lvContact.getCount(); i++) {
            view = lvContact.getChildAt(i);
            CheckBox checkBox = view.findViewById(R.id.check_box);
            if (checkBox.isChecked()) {
                selectedContacts.add((Contact) adapter.getItem(i));
            }
        }
        return selectedContacts;

    }

    public List<Contact> getPhoneContacts() {
        List<Contact> contacts = new ArrayList<>();
//        Check the permissions of contacts is allowed or not
//        If not, we will launch the permission dialogue to give the contact read permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
        }
        else {
//        Make a query to request the contact application to share it's contact with us
            ContentResolver contentResolver = getContentResolver();
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//        Receive the data in the form of cursor
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
//            Log.i("CONTACT_PROVIDER_DEMO", "TOTAL # OF CONTACTS: " + String.valueOf(cursor.getCount()));
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contacts.add(new Contact(contactNumber, contactName));
                }
            }
        }
        return contacts;
    }
}