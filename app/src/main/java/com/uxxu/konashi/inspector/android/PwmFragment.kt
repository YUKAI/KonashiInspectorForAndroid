package com.uxxu.konashi.inspector.android

import android.support.v4.app.Fragment
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import com.uxxu.konashi.lib.Konashi
import com.uxxu.konashi.lib.KonashiManager

import org.jdeferred.DoneCallback

import java.util.ArrayList

/**
 * Created by kiryu on 7/27/15.
 */
class PwmFragment : Fragment() {

    private var mKonashiManager: KonashiManager? = null

    private var mOptionPinSpinner: Spinner? = null
    private var mOptionPeriodEditText: EditText? = null
    private var mOptionDutyEditText: EditText? = null
    private var mOptionSubmitButton: Button? = null
    private var mTableLayout: TableLayout? = null
    private val mRows = ArrayList<PwmTableRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.title_pwm)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pwm, container, false)

        mTableLayout = view.findViewById<View>(R.id.tableLayout) as TableLayout
        mTableLayout!!.addView(Utils.HeaderTableRowBuilder(activity!!)
                .column(getString(R.string.title_pin), 1f)
                .column(getString(R.string.title_pwm), 1f)
                .column(getString(R.string.title_duty), 6f)
                .build())
        for (pinNumber in Utils.PWM_PINS) {
            val row = PwmTableRow.createWithPinNumber(activity!!, pinNumber)
            mTableLayout!!.addView(row)
            mRows.add(row)
        }

        initOptionViews(view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mKonashiManager = Konashi.getManager()
    }

    override fun onDestroy() {
        if (mKonashiManager!!.isReady) {
            for (pinNumber in Utils.PWM_PINS) {
                mKonashiManager!!.pwmMode(pinNumber, Konashi.PWM_DISABLE)
            }
        }
        super.onDestroy()
    }

    private fun initOptionViews(parent: View) {
        mOptionPinSpinner = parent.findViewById<View>(R.id.optionPinSpinner) as Spinner
        val pinLabels = ArrayList<String>()
        for (pin in Utils.PWM_PINS) {
            pinLabels.add(pin.toString())
        }
        mOptionPinSpinner!!.adapter = ArrayAdapter(
                activity,
                android.R.layout.simple_spinner_dropdown_item,
                pinLabels)
        mOptionPinSpinner!!.setSelection(0)

        mOptionPeriodEditText = parent.findViewById<View>(R.id.optionPeriodEditText) as EditText

        mOptionDutyEditText = parent.findViewById<View>(R.id.optionDutyEditText) as EditText

        mOptionSubmitButton = parent.findViewById<View>(R.id.optionSubmitButton) as Button
        mOptionSubmitButton!!.setOnClickListener(View.OnClickListener {
            val pinNumber = Integer.valueOf(mOptionPinSpinner!!.selectedItem as String)
            if (pinNumber < 0 || Utils.PWM_PINS.size <= pinNumber) {
                return@OnClickListener
            }
            mRows[pinNumber].setValues(
                    Integer.valueOf(mOptionPeriodEditText!!.text.toString()),
                    Integer.valueOf(mOptionDutyEditText!!.text.toString()))
        })
    }

    class PwmTableRow(context: Context) : TableRow(context) {

        private val mPinTextView: TextView
        private val mPwmSwitch: Switch
        lateinit var mDutySeekBar: SeekBar
        private val mKonashiManager = Konashi.getManager()
        private var mPinNumber: Int = 0

        init {

            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(0, 20, 0, 20)

            mPinTextView = TextView(context)
            mPinTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            addView(mPinTextView, Utils.createTableRowLayoutParamsWithWeight(1f))

            mPwmSwitch = Switch(context)
            mPwmSwitch.setOnCheckedChangeListener { compoundButton, b ->
                val pwmMode = if (b) Konashi.PWM_ENABLE_LED_MODE else Konashi.PWM_DISABLE
                mKonashiManager.pwmMode(mPinNumber, pwmMode)
                        .then { mDutySeekBar.isEnabled = b }
                        .then {
                            if (pwmMode == Konashi.PWM_ENABLE_LED_MODE) {
                                mKonashiManager.pwmLedDrive(mPinNumber, mDutySeekBar.progress.toFloat())
                            }
                        }
            }
            addView(mPwmSwitch, Utils.createTableRowLayoutParamsWithWeight(1f))

            mDutySeekBar = SeekBar(context)
            mDutySeekBar.max = 100
            mDutySeekBar.isEnabled = false
            mDutySeekBar.progress = mDutySeekBar.max / 2
            mDutySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mKonashiManager.pwmLedDrive(mPinNumber, i.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            addView(mDutySeekBar, Utils.createTableRowLayoutParamsWithWeight(6f))
        }

        fun setPinNumber(pinNumber: Int) {
            this.mPinNumber = pinNumber
            mPinTextView.text = pinNumber.toString()
        }

        fun setValues(period: Int, duty: Int) {
            if (mKonashiManager.isReady) {
                mKonashiManager.pwmPeriod(mPinNumber, period)
                        .then { mKonashiManager.pwmDuty(mPinNumber, duty) }
            }
        }

        companion object {

            fun createWithPinNumber(context: Context, pinNumber: Int): PwmTableRow {
                val row = PwmTableRow(context)
                row.setPinNumber(pinNumber)
                return row
            }
        }
    }
}
