package com.uxxu.konashi.inspector.android

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.ToggleButton

import com.uxxu.konashi.lib.Konashi
import com.uxxu.konashi.lib.KonashiListener
import com.uxxu.konashi.lib.KonashiManager

import java.util.ArrayList

import info.izumin.android.bletia.BletiaException

/**
 * Created by kiryu on 7/27/15.
 */
class PioFragment : Fragment() {

    private var mKonashiManager: KonashiManager? = null

    private var mTableLayout: TableLayout? = null
    private val mRows = ArrayList<PioTableRow>()

    private val mKonashiListener = object : KonashiListener {
        override fun onConnect(manager: KonashiManager) {}

        override fun onDisconnect(manager: KonashiManager) {}

        override fun onError(manager: KonashiManager, e: BletiaException) {}

        override fun onUpdatePioOutput(manager: KonashiManager, value: Int) {
            mRows[0].setInputValue(value)
        }

        override fun onUpdateUartRx(manager: KonashiManager, value: ByteArray) {}

        override fun onUpdateBatteryLevel(manager: KonashiManager, level: Int) {}

        override fun onUpdateSpiMiso(manager: KonashiManager, value: ByteArray) {}

        override fun onFindNoDevice(manager: KonashiManager) {}

        override fun onConnectOtherDevice(manager: KonashiManager) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.title_pio)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pio, container, false)
        mTableLayout = view.findViewById<View>(R.id.tableLayout) as TableLayout
        mTableLayout!!.addView(Utils.HeaderTableRowBuilder(activity!!)
                .column(getString(R.string.title_pin), 1f)
                .column(getString(R.string.title_mode), 2f)
                .column(getString(R.string.title_output), 2f)
                .column(getString(R.string.title_input), 1f)
                .column(getString(R.string.title_pullup), 1f)
                .build())
        for (pinNumber in Utils.PIO_PINS) {
            val row = PioTableRow.createWithPinNumber(activity!!, pinNumber)
            mTableLayout!!.addView(row)
            mRows.add(row)
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mKonashiManager = Konashi.getManager()
        mKonashiManager!!.addListener(mKonashiListener)
    }

    override fun onDestroy() {
        if (mKonashiManager!!.isReady) {
            Thread(Runnable {
                for (pinNumber in Utils.PIO_PINS) {
                    mKonashiManager!!.pinMode(pinNumber, Konashi.INPUT)
                }
            }).start()
        }
        mKonashiManager!!.removeListener(mKonashiListener)
        super.onDestroy()
    }

    class PioTableRow(context: Context) : TableRow(context) {

        private val mPinTextView: TextView
        private val mIoToggleButton: ToggleButton
        lateinit var mOutputToggleButton: ToggleButton
        lateinit var mInputTextView: TextView
        private val mPullupCheckBox: CheckBox
        private val mKonashiManager = Konashi.getManager()
        private var mPinNumber: Int = 0

        init {

            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)

            mPinTextView = TextView(context)
            mPinTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            addView(mPinTextView, Utils.createTableRowLayoutParamsWithWeight(1f))

            mIoToggleButton = ToggleButton(context)
            mIoToggleButton.textOff = context.getString(R.string.title_input).toUpperCase()
            mIoToggleButton.textOn = context.getString(R.string.title_output).toUpperCase()
            mIoToggleButton.text = mIoToggleButton.textOff
            mIoToggleButton.setOnCheckedChangeListener { compoundButton, b ->
                val mode = if (b) Konashi.OUTPUT else Konashi.INPUT
                when (mode) {
                    Konashi.OUTPUT -> {
                        mOutputToggleButton.isEnabled = true
                        mInputTextView.isEnabled = false
                    }
                    Konashi.INPUT -> {
                        mOutputToggleButton.isEnabled = false
                        mInputTextView.isEnabled = true
                    }
                }
                mKonashiManager.pinMode(mPinNumber, mode)
            }
            addView(mIoToggleButton, Utils.createTableRowLayoutParamsWithWeight(2f))

            mOutputToggleButton = ToggleButton(context)
            mOutputToggleButton.textOff = context.getString(R.string.title_low)
            mOutputToggleButton.textOn = context.getString(R.string.title_high)
            mOutputToggleButton.text = mOutputToggleButton.textOff
            mOutputToggleButton.isEnabled = false
            mOutputToggleButton.setOnCheckedChangeListener { compoundButton, b -> mKonashiManager.digitalWrite(mPinNumber, if (b) Konashi.HIGH else Konashi.LOW) }
            addView(mOutputToggleButton, Utils.createTableRowLayoutParamsWithWeight(2f))

            mInputTextView = TextView(context)
            mInputTextView.text = context.getString(R.string.title_low)
            mInputTextView.gravity = Gravity.CENTER
            addView(mInputTextView, Utils.createTableRowLayoutParamsWithWeight(1f))

            mPullupCheckBox = CheckBox(context)
            mPullupCheckBox.gravity = Gravity.CENTER
            mPullupCheckBox.setOnCheckedChangeListener { compoundButton, b -> mKonashiManager.pinPullup(mPinNumber, if (b) Konashi.PULLUP else Konashi.NO_PULLS) }
            addView(mPullupCheckBox, Utils.createTableRowLayoutParamsWithWeight(1f))
        }

        fun setPinNumber(pinNumber: Int) {
            this.mPinNumber = pinNumber
            mPinTextView.text = pinNumber.toString()
        }

        fun setInputValue(value: Int) {
            val context = context
            mInputTextView.text = if (value == Konashi.HIGH)
                context.getString(R.string.title_high)
            else
                context.getString(R.string.title_low)
        }

        companion object {

            fun createWithPinNumber(context: Context, pinNumber: Int): PioTableRow {
                val row = PioTableRow(context)
                row.setPinNumber(pinNumber)
                return row
            }
        }
    }
}
