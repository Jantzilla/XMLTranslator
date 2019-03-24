package com.jantzapps.jantz.xmltranslatorfree.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.jantzapps.jantz.xmltranslatorfree.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static android.R.string.ok;

/**
 * Created by jantz on 7/10/2017.
 */

public class MultiSelectionSpinner extends android.support.v7.widget.AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener
{
    SharedPreferences sharedPreferences;
    String[] _items = null;
    public boolean[] mSelection = null;

    ArrayAdapter<String> simple_adapter;
    public boolean singleChoice;
    public int selectedIndex;
    int otherIndex;
    OnItemSelected onItemSelected;

    public interface OnItemSelected {
       void onItemSelectedListener();
    }

    public MultiSelectionSpinner(Context context)
    {
        super(context);

        simple_adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        onItemSelected = (OnItemSelected) context;
        super.setAdapter(simple_adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simple_adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        onItemSelected = (OnItemSelected) context;
        super.setAdapter(simple_adapter);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (mSelection != null && which < mSelection.length) {
            mSelection[which] = isChecked;
            sharedPreferences.edit().putBoolean(String.valueOf(which), isChecked).apply();

            simple_adapter.clear();
            simple_adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        sharedPreferences.edit().putInt("index", which).apply();
        selectedIndex = which;
        onItemSelected.onItemSelectedListener();
        super.onClick(dialog, which);
    }

    @Override
    public boolean performClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if(singleChoice) {
            builder.setSingleChoiceItems(_items, selectedIndex, this);
        } else {
            builder.setMultiChoiceItems(_items, mSelection, this);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    validateInputs();
                }
            });

            builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearChoices();
                    performClick();
                }
            });

            builder.setNegativeButton("Select All", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectAll();
                    performClick();
                }
            });
        }


        builder.show();
        return true;
    }

    private void clearChoices() {
        for(int i = 0; i < mSelection.length; i++) {
            if(mSelection[i]) {
                mSelection[i] = false;
                sharedPreferences.edit().putBoolean(String.valueOf(i), false).apply();
            }
        }
        validateInputs();
    }

    private void selectAll() {
        for(int i = 0; i < mSelection.length; i++) {
                mSelection[i] = true;
                sharedPreferences.edit().putBoolean(String.valueOf(i), true).apply();
        }
        validateInputs();
    }

    public void validateInputs() {
        if(noChoiceSelected()) {
            mSelection[0] = true;
            sharedPreferences.edit().putBoolean(String.valueOf(0), true).apply();
        }
        resolveLoopedTranslation();
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    private void resolveLoopedTranslation() {
        for(int i = 0; i < mSelection.length; i++) {
            if(mSelection[i] && i == sharedPreferences.getInt("index", 0)) {
                mSelection[i] = false;
                sharedPreferences.edit().putBoolean(String.valueOf(i), false).apply();
            }
        }

        if(noChoiceSelected()) {
            if(sharedPreferences.getInt("index", 0) < mSelection.length - 1) {
                mSelection[sharedPreferences.getInt("index", 0) + 1] = true;
                sharedPreferences.edit().putBoolean(String.valueOf(sharedPreferences.getInt("index", 0) + 1), true).apply();
            } else {
                mSelection[sharedPreferences.getInt("index", 0) - 1] = true;
                sharedPreferences.edit().putBoolean(String.valueOf(sharedPreferences.getInt("index", 0) - 1), true).apply();
            }
        }
    }

    private boolean noChoiceSelected() {
        for(boolean selection : mSelection) {
            if(selection)
                return false;
        }
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(String[] items) {
        _items = items;
        mSelection = new boolean[_items.length];
        simple_adapter.clear();
        simple_adapter.add(_items[0]);
        Arrays.fill(mSelection, false);
    }

    public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        mSelection = new boolean[_items.length];
        simple_adapter.clear();
        simple_adapter.add(_items[0]);
        Arrays.fill(mSelection, false);
    }

    public void setSelection(String[] selection) {
        for (String cell : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    mSelection[j] = true;
                }
            }
        }
    }

    public void setSelection(List<String> selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    mSelection[j] = true;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int index) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = true;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }

        if(singleChoice)
            selectedIndex = index;

        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int[] selectedIndicies) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        for (int index : selectedIndicies) {
            if (index >= 0 && index < mSelection.length) {
                mSelection[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public List<String> getSelectedStrings() {
        List<String> selection = new LinkedList<String>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(_items[i]);
            }
        }
        return selection;
    }

    public List<Integer> getSelectedIndicies() {
        List<Integer> selection = new LinkedList<Integer>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }

    public String getSelectedItemsAsString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(",");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }
}