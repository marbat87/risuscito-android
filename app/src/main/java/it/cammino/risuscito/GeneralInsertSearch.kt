package it.cammino.risuscito

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_insert_search.*
import kotlinx.android.synthetic.main.risuscito_toolbar_noelevation.*

class GeneralInsertSearch : ThemeableActivity() {

    private var fromAdd: Int = 0
    private var idLista: Int = 0
    private var listPosition: Int = 0
    //    var isOnTablet: Boolean = false
//        private set
    var hasThreeColumns: Boolean = false
        private set
    var isGridLayout: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_search)

        risuscito_toolbar.setBackgroundColor(themeUtils!!.primaryColor())
        risuscito_toolbar.title = getString(R.string.title_activity_inserisci_titolo)
        setSupportActionBar(risuscito_toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val bundle = this@GeneralInsertSearch.intent.extras
        fromAdd = bundle!!.getInt("fromAdd")
        idLista = bundle.getInt("idLista")
        listPosition = bundle.getInt("position")

        view_pager.adapter = SectionsPagerAdapter(supportFragmentManager)

        val mLUtils = LUtils.getInstance(this@GeneralInsertSearch)
//        isOnTablet = mLUtils.isOnTablet
        hasThreeColumns = mLUtils.hasThreeColumns
        isGridLayout = mLUtils.isGridLayout
        if (mLUtils.isOnTablet)
            tabletToolbarBackground?.setBackgroundColor(themeUtils!!.primaryColor())
        else
            material_tabs.setBackgroundColor(themeUtils!!.primaryColor())
        material_tabs.setupWithViewPager(view_pager)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(CustomLists.RESULT_CANCELED)
                finish()
                Animatoo.animateShrink(this@GeneralInsertSearch)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        setResult(CustomLists.RESULT_CANCELED)
        finish()
        Animatoo.animateShrink(this@GeneralInsertSearch)
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
            val l = getSystemLocalWrapper(resources.configuration)
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