package com.uxxu.konashi.inspector.android

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

import com.uxxu.konashi.lib.Konashi
import com.uxxu.konashi.lib.KonashiListener
import com.uxxu.konashi.lib.KonashiManager

import org.jdeferred.DoneCallback
import org.jdeferred.DonePipe
import org.jdeferred.Promise

import info.izumin.android.bletia.BletiaException

/**
 * Created by kiryu on 7/27/15.
 */
class KonashiInfoFragment : Fragment(), KonashiListener {

    private var mKonashiManager: KonashiManager? = null

    private var mNameTextView: TextView? = null
    private var mFirmTextView: TextView? = null
    private var mRssiTextView: TextView? = null
    private var mRssiProgressBar: ProgressBar? = null
    private var mBatteryTextView: TextView? = null
    private var mBatteryProgressBar: ProgressBar? = null
    private var mReloadButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.title_konashiInfo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_konashi_info, container, false)

        mNameTextView = view.findViewById<View>(R.id.nameTextView) as TextView

        mFirmTextView = view.findViewById<View>(R.id.firmTextView) as TextView

        mRssiTextView = view.findViewById<View>(R.id.rssiTextView) as TextView
        mRssiProgressBar = view.findViewById<View>(R.id.rssiProgressBar) as ProgressBar

        mBatteryTextView = view.findViewById<View>(R.id.batteryTextView) as TextView
        mBatteryProgressBar = view.findViewById<View>(R.id.batteryProgressBar) as ProgressBar

        mReloadButton = view.findViewById<View>(R.id.reloadButton) as Button
        mReloadButton!!.setOnClickListener { reload() }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mKonashiManager = Konashi.getManager()
        mKonashiManager!!.addListener(this)
        reload()
    }

    override fun onDestroy() {
        mKonashiManager!!.removeListener(this)
        super.onDestroy()
    }

    private fun reload() {
        if (!mKonashiManager!!.isReady) {
            return
        }

        mNameTextView!!.text = mKonashiManager!!.peripheralName

        mKonashiManager!!.softwareRevision
                .then(DonePipe<String, Int, BletiaException, Void> { result ->
                    mFirmTextView!!.text = String.format("Firmware: %s", result)
                    mKonashiManager!!.batteryLevel
                })
                .then(DonePipe<Int, Int, BletiaException, Void> { result ->
                    mBatteryTextView!!.text = String.format("%d%%", result)
                    mBatteryProgressBar!!.progress = result!!
                    mKonashiManager!!.signalStrength
                })
                .then(DonePipe<Int, Void, BletiaException, Void> { result ->
                    mRssiTextView!!.text = String.format("%ddb", result)
                    mRssiProgressBar!!.progress = Math.abs(result!!)
                    null
                })
    }

    override fun onConnect(manager: KonashiManager) {
        Handler().postDelayed({ reload() }, 1000)
    }

    override fun onDisconnect(manager: KonashiManager) {

    }

    override fun onError(manager: KonashiManager, e: BletiaException) {

    }

    override fun onUpdatePioOutput(manager: KonashiManager, value: Int) {

    }

    override fun onUpdateUartRx(manager: KonashiManager, value: ByteArray) {

    }

    override fun onUpdateSpiMiso(manager: KonashiManager, value: ByteArray) {

    }

    override fun onUpdateBatteryLevel(manager: KonashiManager, level: Int) {

    }

    override fun onFindNoDevice(manager: KonashiManager) {

    }

    override fun onConnectOtherDevice(manager: KonashiManager) {

    }
}
