package com.uxxu.konashi.inspector.android

import android.app.Activity
import android.support.v4.app.Fragment
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the [
 * design guidelines](https://developer.android.com/design/patterns/navigation-drawer.html#Interaction) for a complete explanation of the behaviors implemented here.
 */
class NavigationDrawerFragment : Fragment() {

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private var mCallbacks: NavigationDrawerCallbacks? = null

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerListView: ListView? = null
    private var mFragmentContainerView: View? = null

    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState: Boolean = false
    private var mUserLearnedDrawer: Boolean = false

    val isDrawerOpen: Boolean
        get() = mDrawerLayout != null && mDrawerLayout!!.isDrawerOpen(mFragmentContainerView!!)

    private val actionBar: ActionBar?
        get() = (activity as AppCompatActivity).supportActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
            mFromSavedInstanceState = true
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mDrawerListView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false) as ListView
        mDrawerListView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> selectItem(position) }
        mDrawerListView!!.adapter = ArrayAdapter(
                actionBar!!.themedContext,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                arrayOf(getString(R.string.title_konashiInfo), getString(R.string.title_pio), getString(R.string.title_pwm), getString(R.string.title_aio), getString(R.string.title_communication), getString(R.string.title_spi)))
        mDrawerListView!!.setItemChecked(mCurrentSelectedPosition, true)
        return mDrawerListView
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout) {
        mFragmentContainerView = activity!!.findViewById(fragmentId)
        mDrawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        // set up the drawer's list view with items and click listener

        val actionBar = actionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = object : ActionBarDrawerToggle(
                activity, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.action_open, /* "open drawer" description for accessibility */
                R.string.action_close /* "close drawer" description for accessibility */
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (!isAdded) {
                    return
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true
                    val sp = PreferenceManager
                            .getDefaultSharedPreferences(activity)
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                }

                (activity as AppCompatActivity).supportInvalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!isAdded) {
                    return
                }

                (activity as AppCompatActivity).supportInvalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }

        }

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout!!.openDrawer(mFragmentContainerView!!)
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout!!.post { mDrawerToggle!!.syncState() }

        mDrawerLayout!!.setDrawerListener(mDrawerToggle)
    }

    private fun selectItem(position: Int) {
        mCurrentSelectedPosition = position
        if (mDrawerListView != null) {
            mDrawerListView!!.setItemChecked(position, true)
        }
        if (mDrawerLayout != null) {
            mDrawerLayout!!.closeDrawer(mFragmentContainerView!!)
        }
        if (mCallbacks != null) {
            mCallbacks!!.onNavigationDrawerItemSelected(position)
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mCallbacks = activity as NavigationDrawerCallbacks
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NavigationDrawerCallbacks.")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    fun open() {
        if (mDrawerLayout != null && mFragmentContainerView != null) {
            mDrawerLayout!!.openDrawer(mFragmentContainerView!!)
        }
    }

    fun close() {
        if (mDrawerLayout != null && mFragmentContainerView != null) {
            mDrawerLayout!!.closeDrawer(mFragmentContainerView!!)
        }
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        fun onNavigationDrawerItemSelected(position: Int)
    }

    companion object {

        /**
         * Remember the position of the selected item.
         */
        private val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

        /**
         * Per the design guidelines, you should show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }
}
