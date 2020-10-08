package it.cammino.risuscito

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.binding.BindingViewHolder
import com.mikepenz.fastadapter.binding.listeners.addLongClickListener
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback
import com.mikepenz.fastadapter.swipe_drag.SimpleSwipeDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.ActivityCreaListaBinding
import it.cammino.risuscito.databinding.SwipeableItemBinding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.InputTextDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SwipeableItem
import it.cammino.risuscito.items.swipeableItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.ui.SwipeDismissTouchListener
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.CreaListaViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreaListaActivity : ThemeableActivity(), ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    private val mCreaListaViewModel: CreaListaViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply {
            putInt(ID_DA_MODIF, intent.extras?.getInt(ID_DA_MODIF, 0) ?: 0)
        })
    }
    private val inputdialogViewModel: InputTextDialogFragment.DialogViewModel by viewModels()
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels()

    private var modifica: Boolean = false
    private var mAdapter: FastItemAdapter<SwipeableItem> = FastItemAdapter()
    private var mRegularFont: Typeface? = null

    // drag & drop
    private var mTouchHelper: ItemTouchHelper? = null

    private lateinit var binding: ActivityCreaListaBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreaListaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        modifica = intent.extras?.getBoolean(EDIT_EXISTING_LIST) == true

        mRegularFont = ResourcesCompat.getFont(this, R.font.googlesans_regular)

        setSupportActionBar(binding.risuscitoToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val leaveBehindDrawable = IconicsDrawable(this, CommunityMaterial.Icon.cmd_delete_sweep).apply {
            colorInt = MaterialColors.getColor(this@CreaListaActivity, R.attr.colorOnPrimary, TAG)
            sizeDp = 24
            paddingDp = 2
        }
        val touchCallback = SimpleSwipeDragCallback(
                this,
                this,
                leaveBehindDrawable,
                ItemTouchHelper.LEFT,
                MaterialColors.getColor(this, R.attr.colorPrimary, TAG))
                .withBackgroundSwipeRight(MaterialColors.getColor(this, R.attr.colorPrimary, TAG))
                .withLeaveBehindSwipeRight(leaveBehindDrawable)
        touchCallback.setIsDragEnabled(false)
        touchCallback.notifyAllDrops = true

        mTouchHelper = ItemTouchHelper(touchCallback) // Create ItemTouchHelper and pass with parameter the SimpleDragCallback

        mAdapter.addLongClickListener<SwipeableItemBinding, SwipeableItem>({ binding -> binding.swipeableText1 }) { _, position, _, item ->
            Log.d(TAG, "onItemLongClick: $position")
            mCreaListaViewModel.positionToRename = position
            InputTextDialogFragment.show(InputTextDialogFragment.Builder(
                    this, RENAME)
                    .title(R.string.posizione_rename)
                    .prefill(item.name?.getText(this).toString())
                    .positiveButton(R.string.aggiungi_rename)
                    .negativeButton(R.string.cancel), supportFragmentManager)
            true
        }

        val llm = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = llm

        binding.recyclerView.adapter = mAdapter

        val insetDivider = DividerItemDecoration(this, llm.orientation)
        insetDivider.setDrawable(
                ContextCompat.getDrawable(
                        this, R.drawable.preference_list_divider_material)!!)
        binding.recyclerView.addItemDecoration(insetDivider)

        mTouchHelper?.attachToRecyclerView(binding.recyclerView) // Attach ItemTouchHelper to RecyclerView

        val icon = IconicsDrawable(this, CommunityMaterial.Icon2.cmd_plus).apply {
            colorInt = Color.WHITE
            sizeDp = 24
            paddingDp = 4
        }
        binding.fabCreaLista.setImageDrawable(icon)

        binding.textTitleDescription.requestFocus()

        binding.mainHintLayout.hintText.setText(R.string.showcase_rename_desc)
        binding.mainHintLayout.hintText.append(System.getProperty("line.separator"))
        binding.mainHintLayout.hintText.append(getString(R.string.showcase_delete_desc))
        ViewCompat.setElevation(binding.mainHintLayout.questionMark, 1f)
        binding.mainHintLayout.mainHintLayout.setOnTouchListener(
                SwipeDismissTouchListener(
                        binding.mainHintLayout.mainHintLayout, null,
                        object : SwipeDismissTouchListener.DismissCallbacks {
                            override fun canDismiss(token: Any?): Boolean {
                                return true
                            }

                            override fun onDismiss(view: View, token: Any?) {
                                binding.mainHintLayout.mainHintLayout.isVisible = false
                                PreferenceManager.getDefaultSharedPreferences(this@CreaListaActivity).edit { putBoolean(Utility.INTRO_CREALISTA_2, true) }
                            }
                        }))

        binding.textFieldTitle.doOnTextChanged { s: CharSequence?, _: Int, _: Int, _: Int ->
            binding.collapsingToolbarLayout.title = s
            mCreaListaViewModel.tempTitle = s.toString()
        }

        if (ThemeUtils.isDarkMode(this)) {
            val elevatedSurfaceColor = ElevationOverlayProvider(this).compositeOverlayWithThemeSurfaceColorIfNeeded(resources.getDimension(R.dimen.design_appbar_elevation))
            binding.collapsingToolbarLayout.setContentScrimColor(elevatedSurfaceColor)
            binding.appBarLayout.background = ColorDrawable(elevatedSurfaceColor)
        }

        binding.fabCreaLista.setOnClickListener {
            InputTextDialogFragment.show(InputTextDialogFragment.Builder(
                    this, ADD_POSITION)
                    .title(R.string.posizione_add_desc)
                    .positiveButton(R.string.aggiungi_confirm)
                    .negativeButton(R.string.cancel), supportFragmentManager)
        }

        if (modifica)
            subscribeUiChanges()
        else {
            if (mCreaListaViewModel.tempTitle.isEmpty())
                mCreaListaViewModel.tempTitle = intent.extras?.getString(LIST_TITLE) ?: ""
            binding.textFieldTitle.setText(mCreaListaViewModel.tempTitle)
            binding.collapsingToolbarLayout.title = mCreaListaViewModel.tempTitle
            if (mCreaListaViewModel.elementi == null)
                mCreaListaViewModel.elementi = ArrayList()
            mCreaListaViewModel.elementi?.let { mAdapter.set(it) }
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        inputdialogViewModel.state.observe(this) {
            Log.d(TAG, "inputdialogViewModel state $it")
            if (!inputdialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (inputdialogViewModel.mTag) {
                            RENAME -> {
                                inputdialogViewModel.handled = true
                                val mElement = mAdapter.adapterItems[mCreaListaViewModel.positionToRename]
                                mElement.setName = inputdialogViewModel.outputText
                                mAdapter.notifyAdapterItemChanged(mCreaListaViewModel.positionToRename)
                            }
                            ADD_POSITION -> {
                                inputdialogViewModel.handled = true
                                binding.noElementsAdded.isVisible = false
                                mAdapter.add(swipeableItem {
                                    identifier = Utility.random(0, 5000).toLong()
                                    touchHelper = mTouchHelper
                                    setName = inputdialogViewModel.outputText
                                })
                                Log.d(TAG, "onPositive - elementi.size(): " + mAdapter.adapterItems.size)
                                val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                                Log.d(
                                        TAG,
                                        "onCreateOptionsMenu - INTRO_CREALISTA_2: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false))
                                binding.mainHintLayout.mainHintLayout.isVisible = !mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false)
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        inputdialogViewModel.handled = true
                    }
                }
            }
        }

        simpleDialogViewModel.state.observe(this) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            SAVE_LIST -> {
                                simpleDialogViewModel.handled = true
                                lifecycleScope.launch { saveList() }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        when (simpleDialogViewModel.mTag) {
                            SAVE_LIST -> {
                                simpleDialogViewModel.handled = true
                                setResult(RESULT_CANCELED)
                                finish()
                                Animatoo.animateSlideDown(this)
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mCreaListaViewModel.elementi = mAdapter.itemAdapter.adapterItems as? ArrayList<SwipeableItem>
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
            Handler(Looper.getMainLooper()).postDelayed(1500) {
                playIntro()
            }
        }
        binding.mainHintLayout.mainHintLayout.isVisible = mAdapter.adapterItems.isNotEmpty() && !mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                playIntro()
                binding.mainHintLayout.mainHintLayout.isVisible = mAdapter.adapterItems.isNotEmpty()
                return true
            }
            R.id.action_save_list -> {
                lifecycleScope.launch { saveList() }
                return true
            }
            android.R.id.home -> {
                if (mAdapter.adapterItems.isNotEmpty()) {
                    SimpleDialogFragment.show(SimpleDialogFragment.Builder(
                            this, SAVE_LIST)
                            .title(R.string.save_list_title)
                            .content(R.string.save_list_question)
                            .positiveButton(R.string.save_exit_confirm)
                            .negativeButton(R.string.discard_exit_confirm),
                            supportFragmentManager)
                    return true
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                    Animatoo.animateSlideDown(this)
                }
                return true
            }
        }
        return false
    }

    private suspend fun saveList() {
        val mDao = RisuscitoDatabase.getInstance(this).listePersDao()

        var result = 0
        val celebrazione = ListaPersonalizzata()

        if (binding.textFieldTitle.text.toString().isNotBlank()) {
            celebrazione.name = binding.textFieldTitle.text.toString()
        } else {
            result += 100
            celebrazione.name = if (modifica) withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.getListById(mCreaListaViewModel.idModifica)?.titolo }
                    ?: DEFAULT_TITLE else intent.extras?.getString(LIST_TITLE)
                    ?: DEFAULT_TITLE
        }

        Log.d(TAG, "saveList - elementi.size(): " + mAdapter.adapterItems.size)
        for (i in mAdapter.adapterItems.indices) {
            mAdapter.getItem(i)?.let {
                if (celebrazione.addPosizione(it.name?.getText(this).toString()) == -2) {
                    Snackbar.make(
                            binding.mainContent,
                            R.string.lista_pers_piena,
                            Snackbar.LENGTH_SHORT)
                            .show()
                    return
                }
                celebrazione.addCanto(it.idCanto, i)
            }
        }

        if (celebrazione.getNomePosizione(0).equals("", ignoreCase = true)) {
            Snackbar.make(
                    binding.mainContent, R.string.lista_pers_vuota,
                    Snackbar.LENGTH_SHORT)
                    .show()
            return
        }

        Log.d(TAG, "saveList - $celebrazione")

        val listaToUpdate = ListaPers()
        listaToUpdate.lista = celebrazione
        listaToUpdate.titolo = celebrazione.name
        if (modifica) {
            listaToUpdate.id = mCreaListaViewModel.idModifica
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.updateLista(listaToUpdate) }
        } else
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.insertLista(listaToUpdate) }

        if (result == 100)
            Toast.makeText(this, getString(R.string.no_title_edited), Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
        Animatoo.animateSlideDown(this)
    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        if (mAdapter.adapterItems.isNotEmpty()) {
            SimpleDialogFragment.show(SimpleDialogFragment.Builder(this, SAVE_LIST)
                    .title(R.string.save_list_title)
                    .content(R.string.save_list_question)
                    .positiveButton(R.string.save_exit_confirm)
                    .negativeButton(R.string.discard_exit_confirm),
                    supportFragmentManager)
        } else {
            setResult(RESULT_CANCELED)
            finish()
            Animatoo.animateSlideDown(this)
        }
    }

    override fun itemTouchStartDrag(viewHolder: RecyclerView.ViewHolder) {
        @Suppress("UNCHECKED_CAST")
        (viewHolder as? BindingViewHolder<SwipeableItemBinding>)?.binding?.cardContainer?.isDragged = true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        @Suppress("UNCHECKED_CAST")
        val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(newPosition) as? BindingViewHolder<SwipeableItemBinding>?
        viewHolder?.binding?.cardContainer?.isDragged = false
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(mAdapter.itemAdapter, oldPosition, newPosition)
        return true
    }

    override fun itemSwiped(position: Int, direction: Int) {
        val item = mAdapter.getItem(position) ?: return

        mAdapter.remove(position)
        binding.noElementsAdded.isVisible = mAdapter.adapterItemCount == 0
        if (mAdapter.adapterItemCount == 0) binding.mainHintLayout.mainHintLayout.isVisible = false

        Snackbar.make(binding.mainContent, getString(R.string.generic_removed, item.name?.getText(this@CreaListaActivity)), Snackbar.LENGTH_SHORT)
                .setAction(getString(R.string.cancel).toUpperCase(getSystemLocale(resources))) {
                    item.swipedDirection = 0
                    mAdapter.add(position, item)
                    if (position != RecyclerView.NO_POSITION)
                        mAdapter.notifyItemChanged(position)
                    binding.noElementsAdded.isVisible = mAdapter.adapterItemCount == 0
                    if (mAdapter.adapterItemCount == 0) binding.mainHintLayout.mainHintLayout.isVisible = false
                }.show()
    }

    private fun playIntro() {
        binding.fabCreaLista.show()
        val colorOnPrimary = MaterialColors.getColor(this, R.attr.colorOnPrimary, TAG)
        TapTargetSequence(this)
                .continueOnCancel(true)
                .targets(
                        TapTarget.forView(
                                binding.fabCreaLista,
                                getString(R.string.add_position),
                                getString(R.string.showcase_add_pos_desc))
                                // All options below are optional
                                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColorInt(colorOnPrimary)
                                .textColorInt(colorOnPrimary)
                                .tintTarget(false)
                                .id(1),
                        TapTarget.forToolbarMenuItem(
                                binding.risuscitoToolbar,
                                R.id.action_save_list,
                                getString(R.string.list_save_exit),
                                getString(R.string.showcase_saveexit_desc))
                                // All options below are optional
                                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColorInt(colorOnPrimary)
                                .textColorInt(colorOnPrimary)
                                .id(2),
                        TapTarget.forToolbarMenuItem(
                                binding.risuscitoToolbar,
                                R.id.action_help,
                                getString(R.string.showcase_end_title),
                                getString(R.string.showcase_help_general))
                                // All options below are optional
                                .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
                                .textTypeface(mRegularFont) // Specify a typeface for the text
                                .titleTextColorInt(colorOnPrimary)
                                .textColorInt(colorOnPrimary)
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
        mCreaListaViewModel.listaResult?.observe(this) { listaPers ->

            val celebrazione = listaPers.lista

            mCreaListaViewModel.elementi?.let {
                Log.d(TAG, "Lista già valorizzata")
                for (elemento in it) elemento.touchHelper = mTouchHelper
            } ?: run {
                Log.d(TAG, "Lista nulla")
                mCreaListaViewModel.elementi = ArrayList()
                celebrazione?.let {
                    for (i in 0 until it.numPosizioni) {
                        mCreaListaViewModel.elementi?.add(
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

            mCreaListaViewModel.elementi?.let { mAdapter.set(it) }
            if (mCreaListaViewModel.tempTitle.isEmpty())
                mCreaListaViewModel.tempTitle = listaPers.titolo ?: DEFAULT_TITLE
            binding.textFieldTitle.setText(mCreaListaViewModel.tempTitle)
            binding.collapsingToolbarLayout.title = mCreaListaViewModel.tempTitle
            binding.noElementsAdded.isVisible = mAdapter.adapterItemCount == 0
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
