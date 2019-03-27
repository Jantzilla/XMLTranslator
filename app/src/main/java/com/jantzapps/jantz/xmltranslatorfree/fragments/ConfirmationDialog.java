package com.jantzapps.jantz.xmltranslatorfree.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jantzapps.jantz.xmltranslatorfree.R;

public class ConfirmationDialog extends DialogFragment {

    public CheckBox checkBox;
    private Button button1;
    private Button button2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.confirmation_dialog, container);
        checkBox = (CheckBox) mainView.findViewById(R.id.checkBox);
        button1 = (Button) mainView.findViewById(R.id.button1);
        button2 = (Button) mainView.findViewById(R.id.button2);
        return mainView;
    }

    public void setPositiveButton(View.OnClickListener clickListener) {
        button1.setOnClickListener(clickListener);
    }

    public void setNegativeButton(View.OnClickListener clickListener) {
        button2.setOnClickListener(clickListener);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        checkBox.setOnCheckedChangeListener(checkedChangeListener);
    }
}