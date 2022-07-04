package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import it.cammino.risuscito.ui.activity.MainActivity

open class AccountMenuFragment : Fragment() {

    protected var mMainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? MainActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainActivity?.actionMode?.finish()
        mMainActivity?.activitySearchView?.closeSearch()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainActivity?.let {
            it.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    it.updateProfileImage()
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return false
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
    }
}
