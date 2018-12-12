package com.uxxu.konashi.inspector.android

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import com.uxxu.konashi.lib.Konashi
import com.uxxu.konashi.lib.KonashiListener
import com.uxxu.konashi.lib.KonashiManager
import com.uxxu.konashi.lib.util.KonashiUtils

import info.izumin.android.bletia.BletiaException
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class MainActivity : AppCompatActivity(), NavigationDrawerFragment.NavigationDrawerCallbacks {

    private var mKonashiManager: KonashiManager? = null

    private var fragmentManager:FragmentManager? = null
    private var mNavigationDrawerFragment: NavigationDrawerFragment? = null
    private var mOverlay: View? = null
    private var mMenu: Menu? = null
    private val mKonashiListener = object : KonashiListener {

        init {
            fragmentManager  = supportFragmentManager
        }
        override fun onConnect(manager: KonashiManager) {
            KonashiUtils.log("onReady")
            refreshActionBarMenu()
            mOverlay!!.visibility = View.GONE

            Toast.makeText(this@MainActivity, getString(R.string.message_connected), Toast.LENGTH_SHORT).show()
        }

        override fun onDisconnect(manager: KonashiManager) {
            KonashiUtils.log("onDisconnected")
            refreshActionBarMenu()
            mOverlay!!.visibility = View.VISIBLE

            Toast.makeText(this@MainActivity, getString(R.string.message_disconnected), Toast.LENGTH_SHORT).show()
        }

        override fun onError(manager: KonashiManager, e: BletiaException) {
            AlertDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.title_error))
                    .setMessage(e.message)
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
                    .show()
        }

        override fun onUpdatePioOutput(manager: KonashiManager, value: Int) {

        }

        override fun onUpdateUartRx(manager: KonashiManager, value: ByteArray) {

        }

        override fun onUpdateBatteryLevel(manager: KonashiManager, level: Int) {

        }

        override fun onUpdateSpiMiso(manager: KonashiManager, value: ByteArray) {

        }

        override fun onFindNoDevice(manager: KonashiManager) {
            KonashiUtils.log("onFindNoDevice")
            refreshActionBarMenu()
            mOverlay!!.visibility = View.VISIBLE

            Toast.makeText(this@MainActivity, getString(R.string.message_noDeviceFound), Toast.LENGTH_SHORT).show()
        }

        override fun onConnectOtherDevice(manager: KonashiManager) {
            KonashiUtils.log("onConnectOtherDevice")
            refreshActionBarMenu()
            mOverlay!!.visibility = View.VISIBLE

            Toast.makeText(this@MainActivity, getString(R.string.message_connectedDeviceWasNotKonashi), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentManager = getSupportFragmentManager()
        Konashi.initialize(applicationContext)
        mKonashiManager = Konashi.getManager()
        mNavigationDrawerFragment = fragmentManager!!.findFragmentById(R.id.navigation_drawer) as NavigationDrawerFragment
        mNavigationDrawerFragment!!.setUp(
                R.id.navigation_drawer,
                findViewById<View>(R.id.drawer_layout) as DrawerLayout)

        mOverlay = findViewById(R.id.overlay)
    }

    override fun onResume() {
        super.onResume()
        mKonashiManager!!.addListener(mKonashiListener)
        mOverlay!!.visibility = if (mKonashiManager!!.isReady) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        mKonashiManager!!.removeListener(mKonashiListener)
        super.onPause()
    }

    override fun onDestroy() {
        if (mKonashiManager != null) {
            Thread(Runnable {
                if (mKonashiManager!!.isConnected) {
                    mKonashiManager!!.reset()
                    mKonashiManager!!.disconnect()
                    mKonashiManager = null
                }
            }).start()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (mNavigationDrawerFragment!!.isDrawerOpen) {
            mNavigationDrawerFragment!!.close()
            return
        }
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.message_confirmExit))
                .setPositiveButton(android.R.string.yes) { dialogInterface, i -> finish() }
                .setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
    }

    override fun onNavigationDrawerItemSelected(position: Int) {
        val focusedView = this.currentFocus
        if (focusedView != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        }
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = KonashiInfoFragment()
            1 -> fragment = PioFragment()
            2 -> fragment = PwmFragment()
            3 -> fragment = AioFragment()
            4 -> fragment = CommunicationFragment()
            5 -> fragment = SpiFragment()
        }
        if (fragment != null && fragmentManager != null) {
            fragmentManager!!
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        }
    }

    fun restoreActionBar() {
        val actionBar = supportActionBar ?: return

        actionBar.setDisplayShowHomeEnabled(false)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu
        menuInflater.inflate(R.menu.main, menu)
        refreshActionBarMenu()
        if (!mNavigationDrawerFragment!!.isDrawerOpen) {
            restoreActionBar()
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (mNavigationDrawerFragment!!.isDrawerOpen) {
                mNavigationDrawerFragment!!.close()
            } else {
                mNavigationDrawerFragment!!.open()
            }
            R.id.action_find_konashi -> {
                findKonashi()
                return true
            }
            R.id.action_disconnect -> {
                mKonashiManager!!.disconnect()
                refreshActionBarMenu()
                mOverlay!!.visibility = View.VISIBLE
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    internal fun findKonashi() {
        mKonashiManager!!.find(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun refreshActionBarMenu() {
        if (mKonashiManager!!.isConnected) {
            mMenu!!.findItem(R.id.action_find_konashi).isVisible = false
            mMenu!!.findItem(R.id.action_disconnect).isVisible = true
        } else {
            mMenu!!.findItem(R.id.action_find_konashi).isVisible = true
            mMenu!!.findItem(R.id.action_disconnect).isVisible = false
        }
    }
}
