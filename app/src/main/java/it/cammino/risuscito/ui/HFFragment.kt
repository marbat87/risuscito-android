package it.cammino.risuscito.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

/**
 * This class include workaround for fix issues in Android behaviour:
 * 1. correct set user visible hint after resumed or created view
 * 2. correct set child menu visibility
 */

open class HFFragment : Fragment() {

    private var mViewCreated = false
    private var mIsVisibleToUser = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewCreated = true
        if (savedInstanceState != null && savedInstanceState.containsKey("android:user_visible_hint")) {
            super.setUserVisibleHint(mIsVisibleToUser)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        mIsVisibleToUser = isVisibleToUser
        super.setUserVisibleHint(isVisibleToUser)
        if (mViewCreated)
            setChildMenuVisibility(mIsVisibleToUser)
    }

    override fun getUserVisibleHint(): Boolean {
        return mIsVisibleToUser
    }

    private fun setChildMenuVisibility(visible: Boolean) {
        val childFm = childFragmentManager
        @Suppress("UNNECESSARY_SAFE_CALL")
        if (childFm?.fragments == null)
            return
        for (f in childFm.fragments)
            f?.setMenuVisibility(visible)
    }
}