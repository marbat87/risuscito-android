package it.cammino.risuscito

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.MenuItem
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_insert_search.*
import kotlinx.android.synthetic.main.risuscito_toolbar_noelevation.*

class GeneralInsertSearch : ThemeableActivity() {

    private var fromAdd: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0
    var isOnTablet: Boolean = false
        private set
    var hasThreeColumns: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_search)

        main_toolbarTitle.setText(R.string.title_activity_inserisci_titolo)
        risuscito_toolbar.setBackgroundColor(themeUtils!!.primaryColor())
        setSupportActionBar(risuscito_toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val bundle = this@GeneralInsertSearch.intent.extras
        fromAdd = bundle!!.getInt("fromAdd")
        idLista = bundle.getInt("idLista")
        listPosition = bundle.getInt("position")

        view_pager.adapter = SectionsPagerAdapter(supportFragmentManager)

        val mLUtils = LUtils.getInstance(this@GeneralInsertSearch)
        isOnTablet = mLUtils.isOnTablet
        hasThreeColumns = mLUtils.hasThreeColumns
        if (isOnTablet)
            tabletToolbarBackground?.setBackgroundColor(themeUtils!!.primaryColor())
        else
            material_tabs.setBackgroundColor(themeUtils!!.primaryColor())
        material_tabs.setupWithViewPager(view_pager)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                overridePendingTransition(0, R.anim.slide_out_right)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(0, R.anim.slide_out_right)
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    val bundle = Bundle()
                    bundle.putInt("fromAdd", fromAdd)
                    bundle.putInt("idLista", idLista)
                    bundle.putInt("position", listPosition)
                    val insertVeloceFrag = InsertVeloceFragment()
                    insertVeloceFrag.arguments = bundle
                    return insertVeloceFrag
                }
                1 -> {
                    val bundle1 = Bundle()
                    bundle1.putInt("fromAdd", fromAdd)
                    bundle1.putInt("idLista", idLista)
                    bundle1.putInt("position", listPosition)
                    val insertAvanzataFrag = InsertAvanzataFragment()
                    insertAvanzataFrag.arguments = bundle1
                    return insertAvanzataFrag
                }
                else -> {
                    val bundle2 = Bundle()
                    bundle2.putInt("fromAdd", fromAdd)
                    bundle2.putInt("idLista", idLista)
                    bundle2.putInt("position", listPosition)
                    val insertVeloceFrag2 = InsertVeloceFragment()
                    insertVeloceFrag2.arguments = bundle2
                    return insertVeloceFrag2
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = ThemeableActivity.getSystemLocalWrapper(resources.configuration)
            return when (position) {
                0 -> getString(R.string.fast_search_title).toUpperCase(l)
                1 -> getString(R.string.advanced_search_title).toUpperCase(l)
                else -> ""
            }
        }
    }

    companion object {
        private val TAG = GeneralInsertSearch::class.java.canonicalName
    }

}