package it.cammino.risuscito

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat.START
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.google.android.gms.common.SignInButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.databinding.ActivityRisuscitoBinding
import it.cammino.risuscito.ui.AccountMenuFragment
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.MainActivityViewModel

class Risuscito : AccountMenuFragment() {

    private val activityViewModel: MainActivityViewModel by viewModels({ requireActivity() })

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mMainActivity?.let {
                Snackbar.make(
                    it.activityMainContent,
                    getString(R.string.permission_ok),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putString(Utility.SAVE_LOCATION, "0") }
            mMainActivity?.let {
                Snackbar.make(
                    it.activityMainContent,
                    getString(R.string.external_storage_denied),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private var _binding: ActivityRisuscitoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityRisuscitoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setupToolbarTitle(R.string.activity_homepage)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        binding.imageView1.setOnClickListener {
            if (!activityViewModel.isOnTablet)
                mMainActivity?.activityDrawer?.openDrawer(START)
        }

        binding.signInButton.setSize(SignInButton.SIZE_WIDE)
        binding.signInButton.isInvisible =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(Utility.SIGNED_IN, false)
        binding.signInButton.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putBoolean(Utility.SIGN_IN_REQUESTED, true) }
            activityViewModel.showSnackbar = true
            mMainActivity?.signIn()
        }

        activityViewModel.signedIn.observe(viewLifecycleOwner) {
            binding.signInButton.isVisible = !it
        }

        Log.d(TAG, "getVersionCodeWrapper(): ${getVersionCodeWrapper()}")

        ChangelogBuilder()
            .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
            .withMinVersionToShow(getVersionCodeWrapper())     // provide a number and the log will only show changelog rows for versions equal or higher than this number
            .withManagedShowOnStart(
                requireContext().getSharedPreferences(
                    "com.michaelflisar.changelog",
                    0
                ).getInt("changelogVersion", -1) != -1
            )  // library will take care to show activity/dialog only if the changelog has new infos and will only show this new infos
            .withTitle(getString(R.string.dialog_change_title)) // provide a custom title if desired, default one is "Changelog <VERSION>"
            .withOkButtonLabel(getString(R.string.ok)) // provide a custom ok button text if desired, default one is "OK"
            .buildAndShowDialog(
                mMainActivity,
                ThemeUtils.isDarkMode(requireContext())
            ) // second parameter defines, if the dialog has a dark or light theme

        if (!LUtils.hasQ())
            checkPermission()

    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "permission granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.external_storage_pref_rationale)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        run {
                            dialog.cancel()
                            requestPermissionLauncher.launch(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun getVersionCodeP(): Int {
        return requireActivity()
            .packageManager
            .getPackageInfo(requireActivity().packageName, 0)
            .longVersionCode.toInt()
    }

    @Suppress("DEPRECATION")
    private fun getVersionCodeLegacy(): Int {
        return requireActivity()
            .packageManager
            .getPackageInfo(requireActivity().packageName, 0)
            .versionCode
    }

    private fun getVersionCodeWrapper(): Int {
        return if (LUtils.hasP())
            getVersionCodeP()
        else
            getVersionCodeLegacy()
    }

    companion object {
        private val TAG = Risuscito::class.java.canonicalName
    }

}