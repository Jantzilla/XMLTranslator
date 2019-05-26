package com.jantzapps.jantz.xmltranslatorfree.fragments

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton

import com.jantzapps.jantz.xmltranslatorfree.R

class ConfirmationDialog : androidx.fragment.app.DialogFragment() {

    private var checkBox: CheckBox? = null
    private var button1: Button? = null
    private var button2: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView = inflater.inflate(R.layout.confirmation_dialog, container)
        checkBox = mainView.findViewById<View>(R.id.checkBox) as CheckBox
        button1 = mainView.findViewById<View>(R.id.button1) as Button
        button2 = mainView.findViewById<View>(R.id.button2) as Button
        return mainView
    }

    fun setPositiveButton(clickListener: View.OnClickListener) {
        button1!!.setOnClickListener(clickListener)
    }

    fun setNegativeButton(clickListener: View.OnClickListener) {
        button2!!.setOnClickListener(clickListener)
    }

    fun setOnCheckedChangeListener(checkedChangeListener: CompoundButton.OnCheckedChangeListener) {
        checkBox!!.setOnCheckedChangeListener(checkedChangeListener)
    }
}