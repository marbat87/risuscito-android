package it.cammino.risuscito

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.OnLongClickListener
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeDragCallback
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.InputTextDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SwipeableItem
import it.cammino.risuscito.ui.SwipeDismissTouchListener
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.viewmodels.CreaListaViewModel
import kotlinx.android.synthetic.main.activity_crea_lista.*
import kotlinx.android.synthetic.main.hint_layout.*
import java.util.*
import kotlin.collections.ArrayList

class CreaListaActivity : ThemeableActivity(), InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback, ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    private var mViewModel: CreaListaViewModel? = null
    private var celebrazione: ListaPersonalizzata? = null
    private var titoloLista: String? = null
    private var modifica: Boolean = false
    private var idModifica: Int = 0
    private var nomiCanti: ArrayList<String>? = null
    private var mAdapter: FastItemAdapter<SwipeableItem>? = null
    private var elementi: ArrayList<SwipeableItem>? = null
    // drag & drop
    private var touchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_lista)

        mViewModel = ViewModelProviders.of(this).get(CreaListaViewModel::class.java)

        risuscito_toolbar!!.setBackgroundColor(themeUtils!!.primaryColor())
        setSupportActionBar(risuscito_toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tabletToolbarBackground?.setBackgroundColor(themeUtils!!.primaryColor())
        action_title_bar.setBackgroundColor(themeUtils!!.primaryColor())

        val leaveBehindDrawable = IconicsDrawable(this@CreaListaActivity)
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(android.R.color.white)
                .sizeDp(24)
                .paddingDp(2)

        val touchCallback = SimpleSwipeDragCallback(
                this,
                this,
                leaveBehindDrawable,
                ItemTouchHelper.LEFT,
                ContextCompat.getColor(this, R.color.md_red_900))
                .withBackgroundSwipeRight(ContextCompat.getColor(this, R.color.md_red_900))
                .withLeaveBehindSwipeRight(leaveBehindDrawable)
        touchCallback.setIsDragEnabled(false)

        touchHelper = ItemTouchHelper(
                touchCallback) // Create ItemTouchHelper and pass with parameter the SimpleDragCallback

        val mLongClickListener = OnLongClickListener<SwipeableItem> { _, _, item, i ->
            Log.d(TAG, "onItemLongClick: " + i)
            mViewModel!!.positionToRename = i
            InputTextDialogFragment.Builder(
                    this@CreaListaActivity, this@CreaListaActivity, "RENAME")
                    .title(R.string.posizione_rename)
                    .prefill(item.name.text.toString())
                    .positiveButton(R.string.aggiungi_rename)
                    .negativeButton(android.R.string.cancel)
                    .show()
            false
        }

        mAdapter = FastItemAdapter()
        elementi = ArrayList()
        mAdapter!!.add(elementi)
        mAdapter!!.withOnLongClickListener(mLongClickListener)

        val llm = LinearLayoutManager(this@CreaListaActivity)
        recycler_view!!.layoutManager = llm

        recycler_view!!.adapter = mAdapter
        recycler_view!!.setHasFixedSize(true) // Size of RV will not change

        val insetDivider = DividerItemDecoration(this@CreaListaActivity, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(
                        this@CreaListaActivity, R.drawable.preference_list_divider_material)!!)
        recycler_view!!.addItemDecoration(insetDivider)

        touchHelper!!.attachToRecyclerView(recycler_view) // Attach ItemTouchHelper to RecyclerView

        SearchTask().execute(savedInstanceState)

        val icon = IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(4)
        fab_crea_lista.setImageDrawable(icon)

        textTitleDescription.requestFocus()

        var iFragment = InputTextDialogFragment.findVisible(this@CreaListaActivity, "RENAME")
        if (iFragment != null) iFragment.setmCallback(this@CreaListaActivity)
        iFragment = InputTextDialogFragment.findVisible(this@CreaListaActivity, "ADD_POSITION")
        if (iFragment != null) iFragment.setmCallback(this@CreaListaActivity)
        val fragment = SimpleDialogFragment.findVisible(this@CreaListaActivity, "SAVE_LIST")
        fragment?.setmCallback(this@CreaListaActivity)

        hint_text.setText(R.string.showcase_rename_desc)
        hint_text.append(System.getProperty("line.separator"))
        hint_text.append(getString(R.string.showcase_delete_desc))
        ViewCompat.setElevation(question_mark, 1f)
        main_hint_layout.setOnTouchListener(
                SwipeDismissTouchListener(
                        main_hint_layout, null,
                        object : SwipeDismissTouchListener.DismissCallbacks {
                            override fun canDismiss(token: Any?): Boolean {
                                return true
                            }

                            override fun onDismiss(view: View, token: Any?) {
                                main_hint_layout.visibility = View.GONE
//                                val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit()
//                                prefEditor.putBoolean(Utility.INTRO_CREALISTA_2, true)
//                                prefEditor.apply()
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA_2, true) }
                            }
                        }))

        textfieldTitle.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        collapsingToolbarLayout.title = s
                        mViewModel!!.tempTitle = s.toString()
                    }
                }
        )

        fab_crea_lista.setOnClickListener {
            InputTextDialogFragment.Builder(
                    this@CreaListaActivity, this@CreaListaActivity, "ADD_POSITION")
                    .title(R.string.posizione_add_desc)
                    .positiveButton(R.string.aggiungi_confirm)
                    .negativeButton(android.R.string.cancel)
                    .show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        IconicsMenuInflaterUtil.inflate(
                menuInflater, this@CreaListaActivity, R.menu.crea_lista_menu, menu)
        super.onCreateOptionsMenu(menu)
        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity)
        Log.d(
                TAG,
                "onCreateOptionsMenu - INTRO_CREALISTA: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false))
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false)) {
            val handler = Handler()
            handler.postDelayed(
                    {
                        // Do something after 5s = 5000ms
                        playIntro()
                    },
                    1500)
        }
        if (mAdapter!!.adapterItems == null
                || mAdapter!!.adapterItems.size == 0
                || mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false))
            main_hint_layout.visibility = View.GONE
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                playIntro()
                if (mAdapter!!.adapterItems != null && mAdapter!!.adapterItems.size > 0)
                    main_hint_layout.visibility = View.VISIBLE
                return true
            }
            R.id.action_save_list -> {
                Thread(
                        Runnable {
                            if (saveList()) {
                                setResult(Activity.RESULT_OK)
                                finish()
                                overridePendingTransition(0, R.anim.slide_out_bottom)
                            }
                        })
                        .start()
                return true
            }
            android.R.id.home -> {
                if (mAdapter!!.adapterItems.size > 0) {
                    SimpleDialogFragment.Builder(
                            this@CreaListaActivity, this@CreaListaActivity, "SAVE_LIST")
                            .title(R.string.save_list_title)
                            .content(R.string.save_list_question)
                            .positiveButton(R.string.confirm)
                            .negativeButton(R.string.dismiss)
                            .neutralButton(android.R.string.cancel)
                            .show()
                    return true
                } else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    overridePendingTransition(0, R.anim.slide_out_bottom)
                }
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        if (mAdapter!!.adapterItems.size > 0) {
            SimpleDialogFragment.Builder(this@CreaListaActivity, this@CreaListaActivity, "SAVE_LIST")
                    .title(R.string.save_list_title)
                    .content(R.string.save_list_question)
                    .positiveButton(R.string.confirm)
                    .negativeButton(R.string.dismiss)
                    .neutralButton(android.R.string.cancel)
                    .show()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
            overridePendingTransition(0, R.anim.slide_out_bottom)
        }
    }

    private fun saveList(): Boolean {
        celebrazione = ListaPersonalizzata()

        if (textfieldTitle.text != null && !textfieldTitle.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true)) {
            titoloLista = textfieldTitle.text.toString()
        } else {
            val toast = Toast.makeText(
                    this@CreaListaActivity, getString(R.string.no_title_edited), Toast.LENGTH_SHORT)
            toast.show()
        }

        celebrazione!!.name = titoloLista
        var mElement: SwipeableItem
        Log.d(TAG, "saveList - elementi.size(): " + mAdapter!!.adapterItems.size)
        for (i in 0 until mAdapter!!.adapterItems.size) {
            mElement = mAdapter!!.getItem(i)
            if (celebrazione!!.addPosizione(mElement.name.text.toString()) == -2) {
                Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.lista_pers_piena,
                        Snackbar.LENGTH_SHORT)
                        .show()
                return false
            }
        }

        if (celebrazione!!.getNomePosizione(0).equals("", ignoreCase = true)) {
            Snackbar.make(
                    findViewById(android.R.id.content), R.string.lista_pers_vuota, Snackbar.LENGTH_SHORT)
                    .show()
            return false
        }

        if (modifica) {
            for (i in 0 until mAdapter!!.adapterItems.size) {
                celebrazione!!.addCanto(nomiCanti!![i], i)
            }
        }

        val mDao = RisuscitoDatabase.getInstance(this@CreaListaActivity).listePersDao()
        val listaToUpdate = ListaPers()
        listaToUpdate.lista = celebrazione
        listaToUpdate.titolo = titoloLista
        if (modifica) {
            listaToUpdate.id = idModifica
            mDao.updateLista(listaToUpdate)
        } else
            mDao.insertLista(listaToUpdate)

        return true
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)
        mViewModel!!.dataDrag = mAdapter!!.adapterItems as java.util.ArrayList<SwipeableItem>
        if (modifica) mViewModel!!.data = nomiCanti
    }

    override fun onPositive(tag: String, dialog: MaterialDialog) {
        Log.d(TAG, "onPositive: " + tag)
        when (tag) {
            "RENAME" -> {
                val mEditText = dialog.inputEditText
                val mElement = mAdapter!!.adapterItems[mViewModel!!.positionToRename]
                mElement.withName(mEditText?.text?.toString() ?: "NULL")
                mAdapter!!.notifyAdapterItemChanged(mViewModel!!.positionToRename)
            }
            "ADD_POSITION" -> {
                noElementsAdded.visibility = View.GONE
                val mEditText = dialog.inputEditText
                if (modifica) nomiCanti!!.add("")
                if (mAdapter!!.adapterItemCount == 0) {
                    elementi!!.clear()
                    elementi!!.add(
                            SwipeableItem()
                                    .withName(mEditText?.text?.toString() ?: "NULL")
                                    .withTouchHelper(touchHelper!!)
                                    .withIdentifier(Utility.random(0, 5000).toLong()))
                    mAdapter!!.add(elementi)
                    mAdapter!!.notifyItemInserted(0)
                } else {
                    val mSize = mAdapter!!.adapterItemCount
                    mAdapter!!
                            .adapterItems
                            .add(
                                    SwipeableItem()
                                            .withName(mEditText?.text?.toString() ?: "NULL")
                                            .withTouchHelper(touchHelper!!)
                                            .withIdentifier(Utility.random(0, 5000).toLong()))
                    mAdapter!!.notifyAdapterItemInserted(mSize)
                }
                Log.d(TAG, "onPositive - elementi.size(): " + mAdapter!!.adapterItems.size)
                val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity)
                Log.d(
                        TAG,
                        "onCreateOptionsMenu - INTRO_CREALISTA_2: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false))
                if (!mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false)) {
                    main_hint_layout.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onNegative(tag: String, dialog: MaterialDialog) {}

    override fun onNeutral(tag: String, dialog: MaterialDialog) {}

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: " + tag)
        when (tag) {
            "SAVE_LIST" -> Thread(
                    Runnable {
                        if (saveList()) {
                            setResult(Activity.RESULT_OK)
                            finish()
                            overridePendingTransition(0, R.anim.slide_out_bottom)
                        }
                    })
                    .start()
        }
    }

    override fun onNegative(tag: String) {
        Log.d(TAG, "onNegative: " + tag)
        when (tag) {
            "SAVE_LIST" -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                overridePendingTransition(0, R.anim.slide_out_bottom)
            }
        }
    }

    override fun onNeutral(tag: String) {}

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        if (modifica) Collections.swap(nomiCanti!!, oldPosition, newPosition) // change canto
        Collections.swap(mAdapter!!.adapterItems, oldPosition, newPosition) // change position
        mAdapter!!.notifyAdapterItemMoved(oldPosition, newPosition)
        return true
    }

    override fun itemTouchDropped(i: Int, i1: Int) {}

    override fun itemSwiped(position: Int, direction: Int) {
        // -- Option 1: Direct action --
        // do something when swiped such as: select, remove, update, ...:
        // A) fastItemAdapter.select(position);
        // B) fastItemAdapter.remove(position);
        // C) update item, set "read" if an email etc

        // -- Option 2: Delayed action --
        val item = mAdapter!!.getItem(position)
        item.setSwipedDirection(direction)

        // This can vary depending on direction but remove & archive simulated here both results in
        // removal from list
        val removeRunnable = Runnable {
            item.setSwipedAction(Runnable {})
            val mPosition = mAdapter!!.getAdapterPosition(item)
            if (mPosition != RecyclerView.NO_POSITION) {
                // this sample uses a filter. If a filter is used we should use the methods provided
                // by the filter (to make sure filter and normal state is updated)
                mAdapter!!.adapterItems.removeAt(mPosition)
                mAdapter!!.notifyAdapterItemRemoved(mPosition)
                if (modifica) nomiCanti!!.removeAt(mPosition)
                if (mAdapter!!.adapterItemCount == 0) {
                    noElementsAdded.visibility = View.VISIBLE
                    main_hint_layout.visibility = View.GONE
                }
            }
        }
        recycler_view!!.postDelayed(removeRunnable, 2000)

        item.setSwipedAction(Runnable {
            recycler_view!!.removeCallbacks(removeRunnable)
            item.setSwipedDirection(0)
            val mPosition = mAdapter!!.getAdapterPosition(item)
            if (mPosition != RecyclerView.NO_POSITION)
                mAdapter!!.notifyItemChanged(mPosition)
        })

        mAdapter!!.notifyItemChanged(position)
    }

    private fun playIntro() {
        fab_crea_lista.show()
        TapTargetSequence(this@CreaListaActivity)
                .continueOnCancel(true)
                .targets(
                        TapTarget.forView(
                                fab_crea_lista,
                                getString(R.string.add_position),
                                getString(R.string.showcase_add_pos_desc))
                                // All options below are optional
                                .outerCircleColorInt(
                                        themeUtils!!.primaryColor()) // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(
                                        Typeface.createFromAsset(
                                                resources.assets,
                                                "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .tintTarget(false)
                                .id(1),
                        TapTarget.forToolbarMenuItem(
                                risuscito_toolbar!!,
                                R.id.action_save_list,
                                getString(R.string.list_save_exit),
                                getString(R.string.showcase_saveexit_desc))
                                // All options below are optional
                                .outerCircleColorInt(
                                        themeUtils!!.primaryColor()) // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(
                                        Typeface.createFromAsset(
                                                resources.assets,
                                                "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .id(2),
                        TapTarget.forToolbarMenuItem(
                                risuscito_toolbar!!,
                                R.id.action_help,
                                getString(R.string.showcase_end_title),
                                getString(R.string.showcase_help_general))
                                // All options below are optional
                                .outerCircleColorInt(
                                        themeUtils!!.primaryColor()) // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(
                                        Typeface.createFromAsset(
                                                resources.assets,
                                                "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .id(3))
                .listener(
                        object : TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                            override fun onSequenceFinish() {
                                Log.d(TAG, "onSequenceFinish: ")
//                                val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit()
//                                prefEditor.putBoolean(Utility.INTRO_CREALISTA, true)
//                                prefEditor.apply()
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA, true) }
                            }

                            override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {}

                            override fun onSequenceCanceled(tapTarget: TapTarget) {
                                Log.d(TAG, "onSequenceCanceled: ")
//                                val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit()
//                                prefEditor.putBoolean(Utility.INTRO_CREALISTA, true)
//                                prefEditor.apply()
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA, true) }
                            }
                        })
                .start()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SearchTask : AsyncTask<Bundle, Void, Int>() {

        override fun doInBackground(vararg savedInstanceState: Bundle): Int? {

            val bundle = this@CreaListaActivity.intent.extras
            modifica = bundle != null && bundle.getBoolean("modifica")

            if (modifica) {
                idModifica = bundle!!.getInt("idDaModif")
                val mDao = RisuscitoDatabase.getInstance(this@CreaListaActivity).listePersDao()
                val lista = mDao.getListById(idModifica)
                titoloLista = lista?.titolo
                celebrazione = lista?.lista
            } else
                titoloLista = bundle?.getString("titolo")

            if (mViewModel!!.dataDrag != null) {
                elementi = mViewModel!!.dataDrag
                Log.d(TAG, "doInBackground: elementi size " + if (elementi != null) elementi!!.size else 0)
                for (elemento in elementi!!) elemento.withTouchHelper(touchHelper!!)
            } else {
                elementi = ArrayList()
                if (modifica) {
                    for (i in 0 until celebrazione!!.numPosizioni) {
                        elementi!!.add(
                                SwipeableItem()
                                        .withName(celebrazione!!.getNomePosizione(i))
                                        .withTouchHelper(touchHelper!!)
                                        .withIdentifier(Utility.random(0, 5000).toLong()))
                    }
                }
            }

            Log.d(TAG, "doInBackground: modifica " + modifica)
            if (modifica) {
                if (mViewModel!!.data != null) {
                    nomiCanti = mViewModel!!.data
                    Log.d(
                            TAG, "doInBackground: nomiCanti size " + if (nomiCanti != null) nomiCanti!!.size else 0)
                } else {
                    nomiCanti = ArrayList()
                    if (modifica) {
                        for (i in 0 until celebrazione!!.numPosizioni) {
                            //		        		Log.i("CANTO", celebrazione.getCantoPosizione(i));
                            nomiCanti!!.add(celebrazione!!.getCantoPosizione(i))
                        }
                    }
                }
            }

            mAdapter!!.notifyDataSetChanged()

            if (mViewModel!!.tempTitle.isEmpty()) {
                textfieldTitle.setText(titoloLista)
                collapsingToolbarLayout.title = titoloLista
            } else {
                textfieldTitle.setText(mViewModel!!.tempTitle)
                collapsingToolbarLayout.title = mViewModel!!.tempTitle
            }

            return 0
        }

        override fun onPreExecute() {
            super.onPreExecute()
            mAdapter!!.clear()
        }

        override fun onPostExecute(result: Int?) {
            mAdapter!!.add(elementi)
            if (elementi!!.size > 0) noElementsAdded.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = CreaListaActivity::class.java.canonicalName
    }
}
