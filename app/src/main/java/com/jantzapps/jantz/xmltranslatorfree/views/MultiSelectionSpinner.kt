package com.jantzapps.jantz.xmltranslatorfree.views

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter

import com.jantzapps.jantz.xmltranslatorfree.R

import java.util.Arrays
import java.util.LinkedList

import android.R.string.ok

/**
 * Created by jantz on 7/10/2017.
 */

class MultiSelectionSpinner : android.support.v7.widget.AppCompatSpinner, DialogInterface.OnMultiChoiceClickListener {
    internal var sharedPreferences: SharedPreferences
    internal var _items: Array<String>? = null
    var mSelection: BooleanArray? = null

    internal var simple_adapter: ArrayAdapter<String>
    var singleChoice: Boolean = false
    var selectedIndex: Int = 0
    internal var otherIndex: Int = 0
    internal var onItemSelected: OnItemSelected

    val selectedStrings: List<String>
        get() {
            val selection = LinkedList<String>()
            for (i in _items!!.indices) {
                if (mSelection!![i]) {
                    selection.add(_items!![i])
                }
            }
            return selection
        }

    val selectedIndicies: List<Int>
        get() {
            val selection = LinkedList<Int>()
            for (i in _items!!.indices) {
                if (mSelection!![i]) {
                    selection.add(i)
                }
            }
            return selection
        }

    val selectedItemsAsString: String
        get() {
            val sb = StringBuilder()
            var foundOne = false

            for (i in _items!!.indices) {
                if (mSelection!![i]) {
                    if (foundOne) {
                        sb.append(",")
                    }
                    foundOne = true
                    sb.append(_items!![i])
                }
            }
            return sb.toString()
        }

    interface OnItemSelected {
        fun onItemSelectedListener()
    }

    constructor(context: Context) : super(context) {

        simple_adapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_item)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        onItemSelected = context as OnItemSelected
        super.setAdapter(simple_adapter)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        simple_adapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_item)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        onItemSelected = context as OnItemSelected
        super.setAdapter(simple_adapter)
    }

    override fun onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) {
        if (mSelection != null && which < mSelection!!.size) {
            mSelection[which] = isChecked
            sharedPreferences.edit().putBoolean(which.toString(), isChecked).apply()

            simple_adapter.clear()
            simple_adapter.add(buildSelectedItemString())
        } else {
            throw IllegalArgumentException(
                    "Argument 'which' is out of bounds.")
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        sharedPreferences.edit().putInt("index", which).apply()
        selectedIndex = which
        onItemSelected.onItemSelectedListener()
        super.onClick(dialog, which)
    }

    override fun performClick(): Boolean {
        val builder = AlertDialog.Builder(context)

        if (singleChoice) {
            builder.setSingleChoiceItems(_items, selectedIndex, this)
        } else {
            builder.setMultiChoiceItems(_items, mSelection, this)
            builder.setPositiveButton(R.string.ok) { arg0, arg1 -> validateInputs() }

            builder.setNeutralButton("Clear") { dialog, which ->
                clearChoices()
                performClick()
            }

            builder.setNegativeButton("Select All") { dialog, which ->
                selectAll()
                performClick()
            }
        }


        builder.show()
        return true
    }

    private fun clearChoices() {
        for (i in mSelection!!.indices) {
            if (mSelection!![i]) {
                mSelection[i] = false
                sharedPreferences.edit().putBoolean(i.toString(), false).apply()
            }
        }
        validateInputs()
    }

    private fun selectAll() {
        for (i in mSelection!!.indices) {
            mSelection[i] = true
            sharedPreferences.edit().putBoolean(i.toString(), true).apply()
        }
        validateInputs()
    }

    fun validateInputs() {
        if (noChoiceSelected()) {
            mSelection[0] = true
            sharedPreferences.edit().putBoolean(0.toString(), true).apply()
        }
        resolveLoopedTranslation()
        simple_adapter.clear()
        simple_adapter.add(buildSelectedItemString())
    }

    private fun resolveLoopedTranslation() {
        for (i in mSelection!!.indices) {
            if (mSelection!![i] && i == sharedPreferences.getInt("index", 0)) {
                mSelection[i] = false
                sharedPreferences.edit().putBoolean(i.toString(), false).apply()
            }
        }

        if (noChoiceSelected()) {
            if (sharedPreferences.getInt("index", 0) < mSelection!!.size - 1) {
                mSelection[sharedPreferences.getInt("index", 0) + 1] = true
                sharedPreferences.edit().putBoolean((sharedPreferences.getInt("index", 0) + 1).toString(), true).apply()
            } else {
                mSelection[sharedPreferences.getInt("index", 0) - 1] = true
                sharedPreferences.edit().putBoolean((sharedPreferences.getInt("index", 0) - 1).toString(), true).apply()
            }
        }
    }

    private fun noChoiceSelected(): Boolean {
        for (selection in mSelection!!) {
            if (selection)
                return false
        }
        return true
    }

    override fun setAdapter(adapter: SpinnerAdapter) {
        throw RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.")
    }

    fun setItems(items: Array<String>) {
        _items = items
        mSelection = BooleanArray(_items!!.size)
        simple_adapter.clear()
        simple_adapter.add(_items!![0])
        Arrays.fill(mSelection, false)
    }

    fun setItems(items: List<String>) {
        _items = items.toTypedArray()
        mSelection = BooleanArray(_items!!.size)
        simple_adapter.clear()
        simple_adapter.add(_items!![0])
        Arrays.fill(mSelection, false)
    }

    fun setSelection(selection: Array<String>) {
        for (cell in selection) {
            for (j in _items!!.indices) {
                if (_items!![j] == cell) {
                    mSelection[j] = true
                }
            }
        }
    }

    fun setSelection(selection: List<String>) {
        for (i in mSelection!!.indices) {
            mSelection[i] = false
        }
        for (sel in selection) {
            for (j in _items!!.indices) {
                if (_items!![j] == sel) {
                    mSelection[j] = true
                }
            }
        }
        simple_adapter.clear()
        simple_adapter.add(buildSelectedItemString())
    }

    override fun setSelection(index: Int) {
        for (i in mSelection!!.indices) {
            mSelection[i] = false
        }
        if (index >= 0 && index < mSelection!!.size) {
            mSelection[index] = true
        } else {
            throw IllegalArgumentException("Index " + index
                    + " is out of bounds.")
        }

        if (singleChoice)
            selectedIndex = index

        simple_adapter.clear()
        simple_adapter.add(buildSelectedItemString())
    }

    fun setSelection(selectedIndicies: IntArray) {
        for (i in mSelection!!.indices) {
            mSelection[i] = false
        }
        for (index in selectedIndicies) {
            if (index >= 0 && index < mSelection!!.size) {
                mSelection[index] = true
            } else {
                throw IllegalArgumentException("Index " + index
                        + " is out of bounds.")
            }
        }
        simple_adapter.clear()
        simple_adapter.add(buildSelectedItemString())
    }

    private fun buildSelectedItemString(): String {
        val sb = StringBuilder()
        var foundOne = false

        for (i in _items!!.indices) {
            if (mSelection!![i]) {
                if (foundOne) {
                    sb.append(", ")
                }
                foundOne = true

                sb.append(_items!![i])
            }
        }
        return sb.toString()
    }
}
