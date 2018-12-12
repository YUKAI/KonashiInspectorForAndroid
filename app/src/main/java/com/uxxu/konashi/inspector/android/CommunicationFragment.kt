package com.uxxu.konashi.inspector.android

import android.support.v4.app.Fragment
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast

import com.uxxu.konashi.lib.Konashi
import com.uxxu.konashi.lib.KonashiListener
import com.uxxu.konashi.lib.KonashiManager

import org.jdeferred.DoneCallback
import org.jdeferred.DonePipe
import org.jdeferred.FailCallback
import org.jdeferred.Promise

import info.izumin.android.bletia.BletiaException

/**
 * Created by kiryu on 7/27/15.
 */
class CommunicationFragment : Fragment() {

    private var mKonashiManager: KonashiManager? = null

    private var mUartSwitch: Switch? = null
    private var mUartBaudrateSpinner: Spinner? = null
    private var mUartDataEditText: EditText? = null
    private var mUartDataSendButton: Button? = null
    private var mUartResultEditText: EditText? = null
    private var mUartResultClearButton: Button? = null

    private var mI2cSwitch: Switch? = null
    private var mI2cBaudrateSpinner: Spinner? = null
    private var mI2cDataEditText: EditText? = null
    private var mI2cDataSendButton: Button? = null
    private var mI2cResultEditText: EditText? = null
    private var mI2cResultReadButton: Button? = null
    private var mI2cResultClearButton: Button? = null

    private var mValue: ByteArray? = null

    private val mKonashiListener = object : KonashiListener {
        override fun onConnect(manager: KonashiManager) {}
        override fun onDisconnect(manager: KonashiManager) {}
        override fun onError(manager: KonashiManager, e: BletiaException) {}
        override fun onUpdatePioOutput(manager: KonashiManager, value: Int) {}
        override fun onUpdateSpiMiso(manager: KonashiManager, value: ByteArray) {}

        override fun onUpdateUartRx(manager: KonashiManager, value: ByteArray) {
            mValue = value
            mUartResultEditText!!.append(String(mValue!!))
        }

        override fun onUpdateBatteryLevel(manager: KonashiManager, level: Int) {}

        override fun onFindNoDevice(manager: KonashiManager) {}

        override fun onConnectOtherDevice(manager: KonashiManager) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.title_communication)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_communication, container, false)

        initUartViews(view)
        initI2cViews(view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mKonashiManager = Konashi.getManager()
        mKonashiManager!!.addListener(mKonashiListener)
    }

    override fun onDestroy() {
        mKonashiManager!!.removeListener(mKonashiListener)
        super.onDestroy()
    }

    private fun initUartViews(parent: View) {
        mUartBaudrateSpinner = parent.findViewById<View>(R.id.uartBaudrateSpinner) as Spinner
        mUartBaudrateSpinner!!.setSelection(1)
        mUartBaudrateSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                resetUart()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        mUartSwitch = parent.findViewById<View>(R.id.uartSwitch) as Switch
        mUartSwitch!!.setOnCheckedChangeListener { compoundButton, b -> resetUart() }

        mUartDataEditText = parent.findViewById<View>(R.id.uartDataEditText) as EditText

        mUartDataSendButton = parent.findViewById<View>(R.id.uartDataSendButton) as Button
        mUartDataSendButton!!.setOnClickListener {
            mKonashiManager!!.uartWrite(mUartDataEditText!!.text.toString().toByteArray())
                    .then { }
                    .fail { result -> Toast.makeText(activity, result.message, Toast.LENGTH_SHORT).show() }
        }

        mUartResultEditText = parent.findViewById<View>(R.id.uartResultEditText) as EditText

        mUartResultClearButton = parent.findViewById<View>(R.id.uartResultClearButton) as Button
        mUartResultClearButton!!.setOnClickListener { mUartResultEditText!!.setText("") }
        setEnableUartViews(false)
    }

    private fun initI2cViews(parent: View) {
        mI2cBaudrateSpinner = parent.findViewById<View>(R.id.i2cBaudrateSpinner) as Spinner
        mI2cBaudrateSpinner!!.setSelection(0)
        mI2cBaudrateSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                resetI2c()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        mI2cSwitch = parent.findViewById<View>(R.id.i2cSwitch) as Switch
        mI2cSwitch!!.setOnCheckedChangeListener { compoundButton, b -> resetI2c() }

        mI2cDataEditText = parent.findViewById<View>(R.id.i2cDataEditText) as EditText

        mI2cDataSendButton = parent.findViewById<View>(R.id.i2cDataSendButton) as Button
        mI2cDataSendButton!!.setOnClickListener {
            val value = mI2cDataEditText!!.text.toString().trim { it <= ' ' }.toByteArray()
            mKonashiManager!!.i2cMode(Konashi.I2C_ENABLE_100K)
                    .then(mKonashiManager!!.i2cStartConditionPipe())
                    .then(mKonashiManager!!.i2cWritePipe(value.size, value, I2C_ADDRESS))
                    .then(mKonashiManager!!.i2cStopConditionPipe())
                    .fail { result -> Toast.makeText(activity, result.message, Toast.LENGTH_SHORT).show() }
        }

        mI2cResultEditText = parent.findViewById<View>(R.id.i2cResultEditText) as EditText

        mI2cResultReadButton = parent.findViewById<View>(R.id.i2cResultReadButton) as Button
        mI2cResultReadButton!!.setOnClickListener {
            mKonashiManager!!.i2cStartCondition()
                    .then(mKonashiManager!!.i2cReadPipe(Konashi.I2C_DATA_MAX_LENGTH, I2C_ADDRESS))
                    .then { result ->
                        val builder = StringBuilder()
                        for (b in result) {
                            builder.append(b.toInt()).append(",")
                        }
                        mI2cResultEditText!!.setText(builder.toString().substring(0, builder.length - 1))
                    }
                    .then(mKonashiManager!!.i2cStopConditionPipe())
                    .fail { result -> Toast.makeText(activity, result.message, Toast.LENGTH_SHORT).show() }
        }

        mI2cResultClearButton = parent.findViewById<View>(R.id.i2cResultClearButton) as Button
        mI2cResultClearButton!!.setOnClickListener { mI2cResultEditText!!.setText("") }
        setEnableI2cViews(false)
    }

    private fun resetUart() {
        if (!mKonashiManager!!.isReady) {
            return
        }
        if (mUartSwitch!!.isChecked) {
            mKonashiManager!!.uartMode(Konashi.UART_ENABLE)
                    .then(DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void> {
                        val i = mUartBaudrateSpinner!!.selectedItemPosition
                        val labels = resources.getStringArray(R.array.uartBaudrateLabels)
                        val label = labels[i]
                        mKonashiManager!!.uartBaudrate(Utils.uartLabelToValue(activity!!, label))
                    })
                    .then { setEnableUartViews(true) }
        } else {
            mKonashiManager!!.uartMode(Konashi.UART_DISABLE)
                    .then { setEnableUartViews(false) }
        }
    }

    private fun resetI2c() {
        if (!mKonashiManager!!.isReady) {
            return
        }
        if (mI2cSwitch!!.isChecked) {
            val i = mI2cBaudrateSpinner!!.selectedItemPosition
            if (i == 0) {
                mKonashiManager!!.i2cMode(Konashi.I2C_ENABLE_100K)
                        .then { setEnableI2cViews(true) }
                        .fail { result -> Log.d("i2cMode", result.message) }
            } else {
                mKonashiManager!!.i2cMode(Konashi.I2C_ENABLE_400K)
                        .then { setEnableI2cViews(true) }
            }
        } else {
            mKonashiManager!!.i2cMode(Konashi.I2C_DISABLE)
                    .then { setEnableI2cViews(false) }
        }
    }

    private fun setEnableUartViews(enable: Boolean) {
        mUartBaudrateSpinner!!.isEnabled = enable
        mUartDataEditText!!.isEnabled = enable
        mUartDataSendButton!!.isEnabled = enable
    }

    private fun setEnableI2cViews(enable: Boolean) {
        mI2cBaudrateSpinner!!.isEnabled = enable
        mI2cDataEditText!!.isEnabled = enable
        mI2cDataSendButton!!.isEnabled = enable
        mI2cResultReadButton!!.isEnabled = enable
    }

    companion object {

        private val I2C_ADDRESS: Byte = 0x01f
    }
}
