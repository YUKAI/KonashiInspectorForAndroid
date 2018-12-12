package com.uxxu.konashi.inspector.android

import android.support.v4.app.Fragment
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
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

import java.util.Arrays

import info.izumin.android.bletia.BletiaException
import kotlin.experimental.and

/**
 * Created by e10dokup on 12/16/15
 */
class SpiFragment : Fragment() {

    private var mKonashiManager: KonashiManager? = null

    private var mSpiSwitch: Switch? = null
    private var mSpiSpeedSpinner: Spinner? = null
    private var mSpiDataEditText: EditText? = null
    private var mSpiDataSendButton: Button? = null
    private var mSpiResultEditText: EditText? = null
    private var mSpiResultClearButton: Button? = null

    private var mValue: ByteArray? = null

    private val mKonashiListener = object : KonashiListener {
        override fun onConnect(manager: KonashiManager) {}

        override fun onDisconnect(manager: KonashiManager) {}

        override fun onError(manager: KonashiManager, e: BletiaException) {}

        override fun onUpdatePioOutput(manager: KonashiManager, value: Int) {}

        override fun onUpdateUartRx(manager: KonashiManager, value: ByteArray) {}

        override fun onUpdateSpiMiso(manager: KonashiManager, value: ByteArray) {
            mValue = value
            mSpiResultEditText!!.append(String(mValue!!))
        }

        override fun onUpdateBatteryLevel(manager: KonashiManager, level: Int) {}

        override fun onFindNoDevice(manager: KonashiManager) {}

        override fun onConnectOtherDevice(manager: KonashiManager) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.title_spi)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_spi, container, false)

        initSpiViews(view)

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

    private fun initSpiViews(parent: View) {
        mSpiSpeedSpinner = parent.findViewById<View>(R.id.spiSpeedSpinner) as Spinner
        mSpiSpeedSpinner!!.setSelection(1)
        mSpiSpeedSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                resetSpi()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        mSpiSwitch = parent.findViewById<View>(R.id.spiSwitch) as Switch
        mSpiSwitch!!.setOnCheckedChangeListener { compoundButton, b -> resetSpi() }

        mSpiDataEditText = parent.findViewById<View>(R.id.spiDataEditText) as EditText

        mSpiDataSendButton = parent.findViewById<View>(R.id.spiDataSendButton) as Button
        mSpiDataSendButton!!.setOnClickListener {
            try {
                mKonashiManager!!.digitalWrite(Konashi.PIO2, Konashi.LOW)
                        .then(DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void> { mKonashiManager!!.spiWrite(mSpiDataEditText!!.text.toString().toByteArray()) })
                        .then(DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void> { mKonashiManager!!.digitalWrite(Konashi.PIO2, Konashi.HIGH) })
                        .then(DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void> { mKonashiManager!!.spiRead() })
                        .then { result ->
                            val data = ByteArray(result.value.size)
                            for (i in 0 until result.value.size) {
                                val temp = result.value[i] and 0xff.toByte()
                                data[i] = temp
                            }
                            mSpiResultEditText!!.setText(Arrays.toString(data))
                        }
                        .fail { result -> Toast.makeText(activity, result.message, Toast.LENGTH_SHORT).show() }
            } catch (e: NullPointerException) {
                // TODO: hotfix for https://github.com/YUKAI/konashi-android-sdk/issues/170
                noticeForNoSpiDevices()
            }
        }

        mSpiResultEditText = parent.findViewById<View>(R.id.spiResultEditText) as EditText

        mSpiResultClearButton = parent.findViewById<View>(R.id.spiResultClearButton) as Button
        mSpiResultClearButton!!.setOnClickListener { mSpiResultEditText!!.setText("") }
        setEnableSpiViews(false)
    }

    private fun noticeForNoSpiDevices() {
        activity!!.runOnUiThread { Toast.makeText(activity, getString(R.string.message_spi_notSupported), Toast.LENGTH_SHORT).show() }
    }

    private fun resetSpi() {
        if (!mKonashiManager!!.isReady) {
            return
        }
        if (mSpiSwitch!!.isChecked) {
            val i = mSpiSpeedSpinner!!.selectedItemPosition
            val labels = resources.getStringArray(R.array.spiSpeedLabels)
            val label = labels[i]
            val speed = Utils.spiLabelToValue(activity!!, label)
            try {
                mKonashiManager!!.spiConfig(Konashi.SPI_MODE_ENABLE_CPOL0_CPHA0,
                        Konashi.SPI_BIT_ORDER_LITTLE_ENDIAN, speed)
                        .then(DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void> { mKonashiManager!!.pinMode(Konashi.PIO2, Konashi.OUTPUT) })
                        .then(DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void> {
                            setEnableSpiViews(true)
                            mKonashiManager!!.digitalWrite(Konashi.PIO2, Konashi.HIGH)
                        })
                        .fail { result -> Toast.makeText(activity, result.message, Toast.LENGTH_SHORT).show() }
            } catch (e: NullPointerException) {
                // TODO: hotfix for https://github.com/YUKAI/konashi-android-sdk/issues/170
                noticeForNoSpiDevices()
            }

        } else {
            try {
                mKonashiManager!!.spiConfig(Konashi.SPI_MODE_DISABLE, Konashi.SPI_BIT_ORDER_LITTLE_ENDIAN, Konashi.SPI_SPEED_1M)
                        .then { setEnableSpiViews(false) }
            } catch (e: NullPointerException) {
                // TODO: hotfix for https://github.com/YUKAI/konashi-android-sdk/issues/170
                noticeForNoSpiDevices()
            }

        }
    }


    private fun setEnableSpiViews(enable: Boolean) {
        mSpiSpeedSpinner!!.isEnabled = enable
        mSpiDataEditText!!.isEnabled = enable
        mSpiDataSendButton!!.isEnabled = enable
    }
}
