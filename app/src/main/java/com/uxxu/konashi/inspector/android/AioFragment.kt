package com.uxxu.konashi.inspector.android

import android.support.v4.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
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
class AioFragment : Fragment() {

    private var mKonashiManager: KonashiManager? = null

    private var mTableLayout: TableLayout? = null
    private val mRows = ArrayList<AioTableRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.title_aio)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_aio, container, false)

        mTableLayout = view.findViewById<View>(R.id.tableLayout) as TableLayout
        mTableLayout!!.addView(Utils.HeaderTableRowBuilder(activity!!)
                .column(getString(R.string.title_pin), 1f)
                .column(getString(R.string.title_voltage), 5f)
                .column(getString(R.string.action_read), 2f).build())
        for (pinNumber in Utils.AIO_PINS) {
            val row = AioTableRow.createWithPinNumber(activity!!, pinNumber)
            mTableLayout!!.addView(row)
            mRows.add(row)
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mKonashiManager = Konashi.getManager()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class AioTableRow(context: Context) : TableRow(context) {

        private val mPinTextView: TextView
        private val mVoltageTextView: TextView
        private val mVoltageProgressBar: ProgressBar
        private val mReadButton: Button
        private val mKonashiManager = Konashi.getManager()
        private var mPinNumber: Int = 0

        init {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)

            mPinTextView = TextView(context)
            mPinTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            addView(mPinTextView, Utils.createTableRowLayoutParamsWithWeight(1f))

            val voltageWrapper = LinearLayout(context)
            voltageWrapper.orientation = LinearLayout.VERTICAL
            mVoltageTextView = TextView(context)
            mVoltageTextView.gravity = Gravity.CENTER_HORIZONTAL
            voltageWrapper.addView(mVoltageTextView)
            mVoltageProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
            mVoltageProgressBar.max = 100
            voltageWrapper.addView(mVoltageProgressBar)
            addView(voltageWrapper, Utils.createTableRowLayoutParamsWithWeight(5f))

            mReadButton = Button(context)
            mReadButton.text = context.getString(R.string.action_read)
            mReadButton.setOnClickListener {
                mKonashiManager.analogRead(mPinNumber)
                        .then { result -> setVoltage(result!! / 1000.0f) }
            }
            addView(mReadButton, Utils.createTableRowLayoutParamsWithWeight(2f))
        }

        fun setPinNumber(pinNumber: Int) {
            this.mPinNumber = pinNumber
            mPinTextView.text = pinNumber.toString()
        }

        fun setVoltage(value: Float) {
            mVoltageTextView.text = String.format("%.3f V", value)
            mVoltageProgressBar.progress = Math.min(100, Math.round(value / 1.3f * 100f))
        }

        companion object {

            fun createWithPinNumber(context: Context, pinNumber: Int): AioTableRow {
                val row = AioTableRow(context)
                row.setPinNumber(pinNumber)
                return row
            }
        }
    }
}
