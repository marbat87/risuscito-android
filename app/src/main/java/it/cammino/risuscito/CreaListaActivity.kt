package it.cammino.risuscito

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback
import com.mikepenz.fastadapter.swipe_drag.SimpleSwipeDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.mikepenz.iconics.dsl.iconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.InputTextDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SwipeableItem
import it.cammino.risuscito.items.swipeableItem
import it.cammino.risuscito.ui.SwipeDismissTouchListener
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.viewmodels.CreaListaViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.android.synthetic.main.activity_crea_lista.*
import kotlinx.android.synthetic.main.hint_layout.*
import java.util.*
import kotlin.collections.ArrayList

class CreaListaActivity : ThemeableActivity(), InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback, ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    private val mViewModel: CreaListaViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply {
            putInt(ID_DA_MODIF, intent.extras?.getInt(ID_DA_MODIF, 0) ?: 0)
        })
    }

    private var modifica: Boolean = false
    private var mAdapter: FastItemAdapter<SwipeableItem> = FastItemAdapter()
    private var mRegularFont: Typeface? = null
    // drag & drop
    private var mTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_lista)

        modifica = intent.extras?.getBoolean(EDIT_EXISTING_LIST) == true

        mRegularFont = ResourcesCompat.getFont(this, R.font.googlesans_regular)

        setSupportActionBar(risuscito_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        val leaveBehindDrawable = IconicsDrawable(this, CommunityMaterial.Icon.cmd_delete)
//                .colorInt(Color.WHITE)
//                .sizeDp(24)
//                .paddingDp(2)
        val leaveBehindDrawable = iconicsDrawable(CommunityMaterial.Icon.cmd_delete_sweep) {
            color = colorInt(Color.WHITE)
            size = sizeDp(24)
            padding = sizeDp(2)
        }

        val touchCallback = SimpleSwipeDragCallback(
                this,
                this,
                leaveBehindDrawable,
                ItemTouchHelper.LEFT,
                ContextCompat.getColor(this, R.color.md_red_900))
                .withBackgroundSwipeRight(ContextCompat.getColor(this, R.color.md_red_900))
                .withLeaveBehindSwipeRight(leaveBehindDrawable)
        touchCallback.setIsDragEnabled(false)

        mTouchHelper = ItemTouchHelper(touchCallback) // Create ItemTouchHelper and pass with parameter the SimpleDragCallback

        mAdapter.onLongClickListener = { _: View?, _: IAdapter<SwipeableItem>, item: SwipeableItem, position: Int ->
            Log.d(TAG, "onItemLongClick: $position")
            mViewModel.positionToRename = position
            InputTextDialogFragment.Builder(
                    this, this, RENAME)
                    .title(R.string.posizione_rename)
                    .prefill(item.name?.text.toString())
                    .positiveButton(R.string.aggiungi_rename)
                    .negativeButton(R.string.cancel)
                    .show()
            true
        }

        val llm = LinearLayoutManager(this)
        recycler_view?.layoutManager = llm

        recycler_view?.adapter = mAdapter

        val insetDivider = DividerItemDecoration(this, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(
                        this, R.drawable.preference_list_divider_material)!!)
        recycler_view?.addItemDecoration(insetDivider)

        mTouchHelper?.attachToRecyclerView(recycler_view) // Attach ItemTouchHelper to RecyclerView

//        val icon = IconicsDrawable(this, CommunityMaterial.Icon2.cmd_plus)
//                .colorInt(Color.WHITE)
//                .sizeDp(24)
//                .paddingDp(4)
        val icon = iconicsDrawable(CommunityMaterial.Icon2.cmd_plus) {
            color = colorInt(Color.WHITE)
            size = sizeDp(24)
            padding = sizeDp(4)
        }
        fab_crea_lista.setImageDrawable(icon)

        textTitleDescription.requestFocus()

        var iFragment = InputTextDialogFragment.findVisible(this, RENAME)
        iFragment?.setmCallback(this)
        iFragment = InputTextDialogFragment.findVisible(this, ADD_POSITION)
        iFragment?.setmCallback(this)
        val fragment = SimpleDialogFragment.findVisible(this, SAVE_LIST)
        fragment?.setmCallback(this)

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
                                main_hint_layout.isVisible = false
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA_2, true) }
                            }
                        }))

        textfieldTitle.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
            collapsingToolbarLayout.title = s
            mViewModel.tempTitle = s.toString()
        }

        fab_crea_lista.setOnClickListener {
            InputTextDialogFragment.Builder(
                    this, this, ADD_POSITION)
                    .title(R.string.posizione_add_desc)
                    .positiveButton(R.string.aggiungi_confirm)
                    .negativeButton(R.string.cancel)
                    .show()
        }

        if (modifica)
            subscribeUiChanges()
        else {
            if (mViewModel.tempTitle.isEmpty())
                mViewModel.tempTitle = intent.extras?.getString(LIST_TITLE) ?: ""
            textfieldTitle.setText(mViewModel.tempTitle)
            collapsingToolbarLayout.title = mViewModel.tempTitle
            if (mViewModel.elementi == null)
                mViewModel.elementi = ArrayList()
            mViewModel.elementi?.let { mAdapter.set(it) }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mViewModel.elementi = mAdapter.itemAdapter.adapterItems as? ArrayList<SwipeableItem>
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        IconicsMenuInflaterUtil.inflate(
                menuInflater, this, R.menu.crea_lista_menu, menu)
        super.onCreateOptionsMenu(menu)
        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        Log.d(
                TAG,
                "onCreateOptionsMenu - INTRO_CREALISTA: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false))
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false)) {
            Handler().postDelayed(1500) {
                playIntro()
            }
        }
        main_hint_layout.isVisible = mAdapter.adapterItems.isNotEmpty() && !mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                playIntro()
                main_hint_layout.isVisible = mAdapter.adapterItems.isNotEmpty()
                return true
            }
            R.id.action_save_list -> {
                SaveListTask().execute(textfieldTitle.text)
                return true
            }
            android.R.id.home -> {
                if (mAdapter.adapterItems.isNotEmpty()) {
                    SimpleDialogFragment.Builder(
                            this, this, SAVE_LIST)
                            .title(R.string.save_list_title)
                            .content(R.string.save_list_question)
                            .positiveButton(R.string.save_exit_confirm)
                            .negativeButton(R.string.discard_exit_confirm)
                            .show()
                    return true
                } else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    Animatoo.animateSlideDown(this)
                }
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        if (mAdapter.adapterItems.isNotEmpty()) {
            SimpleDialogFragment.Builder(this, this, SAVE_LIST)
                    .title(R.string.save_list_title)
                    .content(R.string.save_list_question)
                    .positiveButton(R.string.save_exit_confirm)
                    .negativeButton(R.string.discard_exit_confirm)
                    .show()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
            Animatoo.animateSlideDown(this)
        }
    }

    override fun onPositive(tag: String, dialog: MaterialDialog) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            RENAME -> {
                val mEditText = dialog.getInputField()
                val mElement = mAdapter.adapterItems[mViewModel.positionToRename]
                mElement.setName = mEditText.text.toString()
                mAdapter.notifyAdapterItemChanged(mViewModel.positionToRename)
            }
            ADD_POSITION -> {
                noElementsAdded.isVisible = false
                val mEditText = dialog.getInputField()
                mAdapter.add(swipeableItem {
                    identifier = Utility.random(0, 5000).toLong()
                    touchHelper = mTouchHelper
                    setName = mEditText.text.toString()
                })
                Log.d(TAG, "onPositive - elementi.size(): " + mAdapter.adapterItems.size)
                val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                Log.d(
                        TAG,
                        "onCreateOptionsMenu - INTRO_CREALISTA_2: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false))
                main_hint_layout.isVisible = !mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false)
            }
        }
    }

    override fun onNegative(tag: String, dialog: MaterialDialog) {
        // no-op
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            SAVE_LIST ->
                SaveListTask().execute(textfieldTitle.text)
        }
    }

    override fun onNegative(tag: String) {
        Log.d(TAG, "onNegative: $tag")
        when (tag) {
            SAVE_LIST -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                Animatoo.animateSlideDown(this)
            }
        }
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(mAdapter.itemAdapter, oldPosition, newPosition)
        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) = Unit

    override fun itemSwiped(position: Int, direction: Int) {
        // -- Option 1: Direct action --
        // do something when swiped such as: select, remove, update, ...:
        // A) fastItemAdapter.select(position);
        // B) fastItemAdapter.remove(position);
        // C) update item, set "read" if an email etc

        // -- Option 2: Delayed action --
        val item = mAdapter.getItem(position) ?: return
        item.swipedDirection = direction

        val deleteHandler = Handler { message ->
            val itemOjb = message.obj as SwipeableItem

            itemOjb.swipedAction = null
            val position12 = mAdapter.getAdapterPosition(itemOjb)
            if (position12 != RecyclerView.NO_POSITION) {
                mAdapter.remove(position12)
                noElementsAdded.isVisible = mAdapter.adapterItemCount == 0
                if (mAdapter.adapterItemCount == 0) main_hint_layout.isVisible = false
            }
            true
        }

        // This can vary depending on direction but remove & archive simulated here both results in
        // removal from list
        val message = Random().nextInt()
        deleteHandler.sendMessageDelayed(Message.obtain().apply { what = message; obj = item }, 2000)

        item.swipedAction = Runnable {
            deleteHandler.removeMessages(message)
            item.swipedDirection = 0
            val mPosition = mAdapter.getAdapterPosition(item)
            if (mPosition != RecyclerView.NO_POSITION)
                mAdapter.notifyItemChanged(mPosition)
        }

        mAdapter.notifyItemChanged(position)
    }

    private fun playIntro() {
        fab_crea_lista.show()
        TapTargetSequence(this)
                .continueOnCancel(true)
                .targets(
                        TapTarget.forView(
                                fab_crea_lista,
                                getString(R.string.add_position),
                                getString(R.string.showcase_add_pos_desc))
                                // All options below are optional
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .tintTarget(false)
                                .id(1),
                        TapTarget.forToolbarMenuItem(
                                risuscito_toolbar,
                                R.id.action_save_list,
                                getString(R.string.list_save_exit),
                                getString(R.string.showcase_saveexit_desc))
                                // All options below are optional
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .id(2),
                        TapTarget.forToolbarMenuItem(
                                risuscito_toolbar,
                                R.id.action_help,
                                getString(R.string.showcase_end_title),
                                getString(R.string.showcase_help_general))
                                // All options below are optional
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .id(3))
                .listener(
                        object : TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
                            override fun onSequenceFinish() {
                                Log.d(TAG, "onSequenceFinish: ")
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA, true) }
                            }

                            override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
                                // no-op
                            }

                            override fun onSequenceCanceled(tapTarget: TapTarget) {
                                Log.d(TAG, "onSequenceCanceled: ")
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA, true) }
                            }
                        })
                .start()
    }

    private fun subscribeUiChanges() {
        mViewModel.listaResult?.observe(this) { listaPers ->

            val celebrazione = listaPers.lista

            mViewModel.elementi?.let {
                Log.d(TAG, "Lista già valorizzata")
                for (elemento in it) elemento.touchHelper = mTouchHelper
            } ?: run {
                Log.d(TAG, "Lista nulla")
                mViewModel.elementi = ArrayList()
                celebrazione?.let {
                    for (i in 0 until it.numPosizioni) {
                        mViewModel.elementi?.add(
                                swipeableItem {
                                    identifier = Utility.random(0, 5000).toLong()
                                    touchHelper = mTouchHelper
                                    setName = it.getNomePosizione(i)
                                    idCanto = it.getCantoPosizione(i)
                                }
                        )
                    }
                }
            }

            mViewModel.elementi?.let { mAdapter.set(it) }
            if (mViewModel.tempTitle.isEmpty())
                mViewModel.tempTitle = listaPers.titolo ?: DEFAULT_TITLE
            textfieldTitle.setText(mViewModel.tempTitle)
            collapsingToolbarLayout.title = mViewModel.tempTitle
            noElementsAdded.isVisible = mAdapter.adapterItemCount == 0
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SaveListTask : AsyncTask<Editable, Void, Int>() {

        override fun doInBackground(vararg titleText: Editable): Int {

            val mDao = RisuscitoDatabase.getInstance(this@CreaListaActivity).listePersDao()

            var result = 0
            val celebrazione = ListaPersonalizzata()

            if (titleText[0].isNotBlank()) {
                celebrazione.name = titleText[0].toString()
            } else {
                result += 100
                celebrazione.name = if (modifica) mDao.getListById(mViewModel.idModifica)?.titolo
                        ?: DEFAULT_TITLE else intent.extras?.getString(LIST_TITLE) ?: DEFAULT_TITLE
            }

            Log.d(TAG, "saveList - elementi.size(): " + mAdapter.adapterItems.size)
            for (i in mAdapter.adapterItems.indices) {
                mAdapter.getItem(i)?.let {
                    if (celebrazione.addPosizione(it.name?.text.toString()) == -2) {
                        return 1
                    }
                    celebrazione.addCanto(it.idCanto, i)
                }
            }

            if (celebrazione.getNomePosizione(0).equals("", ignoreCase = true))
                return 2

            Log.d(TAG, "saveList - $celebrazione")

            val listaToUpdate = ListaPers()
            listaToUpdate.lista = celebrazione
            listaToUpdate.titolo = celebrazione.name
            if (modifica) {
                listaToUpdate.id = mViewModel.idModifica
                mDao.updateLista(listaToUpdate)
            } else
                mDao.insertLista(listaToUpdate)

            return result
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            if (result == 100)
                Toast.makeText(this@CreaListaActivity, getString(R.string.no_title_edited), Toast.LENGTH_SHORT).show()
            when (result) {
                0, 100 -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                    Animatoo.animateSlideDown(this@CreaListaActivity)
                }
                1 ->
                    Snackbar.make(
                            this@CreaListaActivity.main_content,
                            R.string.lista_pers_piena,
                            Snackbar.LENGTH_SHORT)
                            .show()
                2 ->
                    Snackbar.make(
                            this@CreaListaActivity.main_content, R.string.lista_pers_vuota, Snackbar.LENGTH_SHORT)
                            .show()

            }
        }
    }

    companion object {
        private val TAG = CreaListaActivity::class.java.canonicalName
        private const val RENAME = "RENAME"
        private const val ADD_POSITION = "ADD_POSITION"
        private const val SAVE_LIST = "SAVE_LIST"
        const val ID_DA_MODIF = "idDaModif"
        const val LIST_TITLE = "titoloLista"
        const val EDIT_EXISTING_LIST = "modifica"
        const val DEFAULT_TITLE = "NEW LIST"
    }
}
