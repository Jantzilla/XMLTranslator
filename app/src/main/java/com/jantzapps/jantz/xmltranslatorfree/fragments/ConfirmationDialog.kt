package com.jantzapps.jantz.xmltranslatorfree.fragments

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import butterknife.BindView
import butterknife.ButterKnife

import com.jantzapps.jantz.xmltranslatorfree.R

class ConfirmationDialog : androidx.fragment.app.DialogFragment() {

    @BindView(R.id.checkBox) private lateinit var checkBox: CheckBox
    @BindView(R.id.button1) private lateinit var button1: Button
    @BindView(R.id.button2) private lateinit var button2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView = inflater.inflate(R.layout.confirmation_dialog, container)
        ButterKnife.bind(this, mainView)
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