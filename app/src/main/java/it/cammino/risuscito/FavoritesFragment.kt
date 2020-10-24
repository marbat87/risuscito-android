package it.cammino.risuscito

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.databinding.ActivityFavouritesBinding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.LocaleManager
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.viewmodels.FavoritesViewModel
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Collator

class FavoritesFragment : Fragment() {
    private val mFavoritesViewModel: FavoritesViewModel by viewModels()
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val activityViewModel: MainActivityViewModel by viewModels({ requireActivity() })
    private val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private var selectExtension: SelectExtension<SimpleItem>? = null
    private var actionModeOk: Boolean = false
    private var mMainActivity: MainActivity? = null
    private var mLastClickTime: Long = 0

    private var _binding: ActivityFavouritesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity = requireActivity() as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_favourites)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableBottombar(false)
        mMainActivity?.enableFab(false)

        if (!PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Utility.PREFERITI_OPEN, false)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(Utility.PREFERITI_OPEN, true) }
            Handler(Looper.getMainLooper()).postDelayed(250) {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                        .show()
            }
        }

        setHasOptionsMenu(true)
        subscribeUiFavorites()

        cantoAdapter.onPreClickListener = { _: View?, _: IAdapter<SimpleItem>, _: SimpleItem, position: Int ->
            var consume = false
            if (mMainActivity?.actionMode != null) {
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY_SELECTION) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    cantoAdapter
                            .getAdapterItem(position)
                            .isSelected = !cantoAdapter.getAdapterItem(position).isSelected
                    cantoAdapter.notifyAdapterItemChanged(position)
                    if (selectExtension?.selectedItems?.size == 0)
                        mMainActivity?.actionMode?.finish()
                    else
                        updateActionModeTitle()
                }
                consume = true
            }
            consume
        }

        cantoAdapter.onClickListener =
                { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                    var consume = false
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        // lancia l'activity che visualizza il canto passando il parametro creato
                        val intent = Intent(activity, PaginaRenderActivity::class.java)
                        intent.putExtras(bundleOf(Utility.PAGINA to item.source?.getText(requireContext()), Utility.ID_CANTO to item.id))
                        activityViewModel.mLUtils.startActivityWithTransition(intent)
                        consume = true
                    }
                    consume
                }

        cantoAdapter.onPreLongClickListener =
                { _: View?, _: IAdapter<SimpleItem>, _: SimpleItem, position: Int ->
                    if (mMainActivity?.actionMode == null) {
                        if (!activityViewModel.isOnTablet)
                            mMainActivity?.expandToolbar()
                        cantoAdapter.getAdapterItem(position).isSelected = true
                        cantoAdapter.notifyAdapterItemChanged(position)
                        startCab()
                    }
                    true
                }

        selectExtension = SelectExtension(cantoAdapter)
        selectExtension?.isSelectable = true
        selectExtension?.multiSelect = true
        selectExtension?.selectOnLongClick = true
        selectExtension?.deleteAllSelectedItems()

        cantoAdapter.setHasStableIds(true)

        binding.favouritesList.adapter = cantoAdapter
        val llm = if (activityViewModel.isGridLayout)
            GridLayoutManager(context, if (activityViewModel.hasThreeColumns) 3 else 2)
        else
            LinearLayoutManager(context)
        binding.favouritesList.layoutManager = llm
        val insetDivider = DividerItemDecoration(requireContext(), llm.orientation)
        ContextCompat.getDrawable(requireContext(), R.drawable.material_inset_divider)?.let { insetDivider.setDrawable(it) }
        binding.favouritesList.addItemDecoration(insetDivider)
        binding.favouritesList.itemAnimator = SlideRightAlphaAnimator()

    }

    override fun onDestroy() {
        mMainActivity?.actionMode?.finish()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        IconicsMenuInflaterUtil.inflate(
                requireActivity().menuInflater, requireContext(), R.menu.clean_list_menu, menu)
        menu.findItem(R.id.list_reset).isVisible = cantoAdapter.adapterItemCount > 0
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.list_reset -> {
                mMainActivity?.let {
                    SimpleDialogFragment.show(SimpleDialogFragment.Builder(it, FAVORITES_RESET)
                            .title(R.string.dialog_reset_favorites_title)
                            .content(R.string.dialog_reset_favorites_desc)
                            .positiveButton(R.string.clear_confirm)
                            .negativeButton(R.string.cancel),
                            it.supportFragmentManager)
                }
                return true
            }
            R.id.action_help -> {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                        .show()
                return true
            }
        }
        return false
    }

    private fun removeFavorites() {
        ListeUtils.removeFavoritesWithUndo(this, selectExtension?.selectedItems)
    }

    private fun startCab() {
        val callback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
                IconicsMenuInflaterUtil.inflate(
                        requireActivity().menuInflater, requireContext(), R.menu.menu_delete, menu)
                Log.d(TAG, "MaterialCab onCreate")
                actionModeOk = false
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                Log.d(TAG, "MaterialCab onSelection: ${item?.itemId}")
                return if (item?.itemId == R.id.action_remove_item) {
                    removeFavorites()
                    actionModeOk = true
                    mMainActivity?.actionMode?.finish()
                    true
                } else
                    false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
                if (!actionModeOk) {
                    try {
                        selectExtension?.deselect()
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
                mMainActivity?.destroyActionMode()
            }

        }

        mMainActivity?.createActionMode(callback)
        updateActionModeTitle()
    }

    private fun updateActionModeTitle() {
        val itemSelectedCount = selectExtension?.selectedItems?.size ?: 0
        mMainActivity?.updateActionModeTitle(resources.getQuantityString(R.plurals.item_selected, itemSelectedCount, itemSelectedCount))
    }


    private fun subscribeUiFavorites() {
        mFavoritesViewModel.mFavoritesResult?.observe(viewLifecycleOwner) { canti ->
            cantoAdapter.set(canti.sortedWith(compareBy(Collator.getInstance(LocaleManager.getSystemLocale(resources))) { it.title?.getText(requireContext()) }))
            binding.noFavourites.isInvisible = cantoAdapter.adapterItemCount > 0
            binding.favouritesList.isInvisible = cantoAdapter.adapterItemCount == 0
            if (cantoAdapter.adapterItemCount == 0)
                mMainActivity?.expandToolbar()
            activity?.invalidateOptionsMenu()
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            FAVORITES_RESET -> {
                                simpleDialogViewModel.handled = true
                                val mDao = RisuscitoDatabase.getInstance(requireContext()).favoritesDao()
                                lifecycleScope.launch(Dispatchers.IO) { mDao.resetFavorites() }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = FavoritesFragment::class.java.canonicalName
        private const val FAVORITES_RESET = "FAVORITES_RESET"
    }

}
