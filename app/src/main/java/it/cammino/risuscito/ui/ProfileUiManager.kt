package it.cammino.risuscito.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.dialog.ProfileDialogFragment
import it.cammino.risuscito.utils.Utility

/**
 * Manages the display of the user's profile picture and the sign-in button in the UI.
 *
 * This class handles the visibility of the profile image and sign-in button based on the user's
 * sign-in status. It also loads the profile picture from a URL, sets up click listeners, and
 * manages the display of the ProfileDialogFragment.
 */
class ProfileUiManager(
    private val context: Context,
    private val supportFragmentManager: FragmentManager,
    private val profileItemActionView: View?, // Assuming this is the actionView from the profileItem
//    private val mViewModel: MainActivityViewModel, // Replace YourViewModel with your actual ViewModel
    private val signIn: (Boolean) -> Unit // Function to initiate sign-in
) {

    private val profileImage: ShapeableImageView? by lazy {
        profileItemActionView?.findViewById(R.id.profile_icon)
    }
    private val signInButton: Button? by lazy {
        profileItemActionView?.findViewById(R.id.sign_in_button)
    }

    private var profilePhotoUrl: String = ""
    private var profileNameStr: String = ""
    private var profileEmailStr: String = ""

    /**
     * Updates the profile image and sign-in button visibility in the UI.
     *
     * - **Visibility Control:**
     *   - Shows/hides the profile image and sign-in button based on the user's
     *     sign-in status (stored in SharedPreferences).
     *   - If the user is signed in, the profile image is visible, and the sign-in
     *     button is hidden.
     *   - If the user is not signed in, the profile image is hidden, and the
     *     sign-in button is visible.
     * - **Profile Image Handling:**
     *   - If `profilePhotoUrl` is empty, sets a default "account circle" image
     *     and removes any existing background.
     *   - If `profilePhotoUrl` is not empty, loads the image from the URL using
     *     Picasso, with the "account circle" image as a placeholder.
     *   - Sets a selectable borderless background for the profile image.
     * - **Click Listeners:**
     *   - Sets a click listener on the profile image to show the
     *     `ProfileDialogFragment`. This dialog displays detailed user information.
     *   - Sets a click listener on the sign-in button to initiate the sign-in
     *     process and sets a shared preference to indicate a sign in is requested.
     */
    fun updateProfileUi(
        newProfilePhotoUrl: String, newProfileName: String, newProfileEmail: String
    ) {
        profilePhotoUrl = newProfilePhotoUrl
        profileNameStr = newProfileName
        profileEmailStr = newProfileEmail

        val isSignedIn = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(Utility.SIGNED_IN, false)

        profileImage?.isVisible = isSignedIn
        signInButton?.isGone = isSignedIn

        updateProfileImage()
        setupClickListeners()
    }

    private fun updateProfileImage() {
        if (profilePhotoUrl.isEmpty()) {
            setDefaultProfileImage()
        } else {
            loadProfileImageFromUrl()
        }
    }

    private fun setDefaultProfileImage() {
        profileImage?.apply {
            setImageResource(R.drawable.account_circle_56px)
            background = null
        }
    }

    private fun loadProfileImageFromUrl() {
        val placeholder = AppCompatResources.getDrawable(context, R.drawable.account_circle_56px)
        val selectableBackground = getSelectableItemBackgroundBorderless()

        profileImage?.apply {
            Picasso.get().load(profilePhotoUrl)
                .placeholder(placeholder ?: return@apply) // Handle null placeholder
                .into(this)
            background = selectableBackground
        }
    }

    private fun setupClickListeners() {
        profileImage?.setOnClickListener {
            showProfileDialog()
        }

        signInButton?.setOnClickListener {
            handleSignInButtonClick()
        }
    }

    fun showProfileDialog() {
        ProfileDialogFragment.show(
            ProfileDialogFragment.Builder(PROFILE_DIALOG).apply {
                profileName = profileNameStr
                profileEmail = profileEmailStr
                profileImageSrc = profilePhotoUrl
            }, supportFragmentManager
        )
    }

    private fun handleSignInButtonClick() {
        signIn(false)
    }

    private fun getSelectableItemBackgroundBorderless(): Drawable? {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            androidx.appcompat.R.attr.selectableItemBackgroundBorderless, typedValue, true
        )
        return AppCompatResources.getDrawable(context, typedValue.resourceId)
    }

    companion object {
        private const val PROFILE_DIALOG = "PROFILE_DIALOG"
    }

}

// Example usage (in your Activity or Fragment):
// Assuming you have access to these variables
// val profileItemActionView: View? = ...
// val mViewModel: YourViewModel = ...
// val signIn: (Boolean) -> Unit = ...
// val profileUiManager = ProfileUiManager(this, supportFragmentManager, profileItemActionView, mViewModel, signIn)
// profileUiManager.updateProfileUi(profilePhotoUrl, profileNameStr, profileEmailStr)