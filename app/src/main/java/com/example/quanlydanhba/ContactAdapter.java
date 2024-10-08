package com.example.quanlydanhba;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private List<Contact> contacts;
    private Activity activity;
    private boolean checkBoxVisibility;
//    private List<Boolean> checkList;

    public ContactAdapter(Activity activity, List<Contact> contacts, boolean checkBoxVisibility) {
        this.checkBoxVisibility = checkBoxVisibility;
        this.contacts = contacts;
        this.activity = activity;
    }



    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int i) {
        return contacts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public boolean getCheckBoxVisibility() {
        return checkBoxVisibility;
    }

    public void setCheckBoxVisibility(boolean checkBoxVisibility) {
        this.checkBoxVisibility = checkBoxVisibility;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }



    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.item_contact, null);
        TextView tvPhoneNumber = view.findViewById(R.id.tv_item_phone_number);
        TextView tvName = view.findViewById(R.id.tv_item_name);
        TextView tvOrdinalNumber = view.findViewById(R.id.tv_item_ordinal_number);
        tvPhoneNumber.setText(contacts.get(i).getPhoneNumber());
        tvName.setText(contacts.get(i).getName());
        tvOrdinalNumber.setText(String.valueOf(i + 1));
        CheckBox checkBox = view.findViewById(R.id.check_box);
        if (checkBoxVisibility) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.INVISIBLE);
        }
        return view;
    }
}
