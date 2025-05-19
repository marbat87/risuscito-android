package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.activity.ChangelogActivity
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.utils.extension.shareThisApp
import it.cammino.risuscito.utils.extension.startActivityWithTransition


class AboutFragment : MaterialAboutFragment() {

    private var mMainActivity: ThemeableActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? ThemeableActivity
        Log.d(TAG, "Fragment: ${this::class.java.canonicalName}")
        Firebase.crashlytics.log("Fragment: ${this::class.java}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //solo per tablet
        (mMainActivity as? MainActivity)?.let {
            it.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    it.updateProfileImage()
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return false
                }
            }, viewLifecycleOwner)
            it.setupToolbarTitle(R.string.title_activity_about)
            it.setTabVisible(false)
            it.enableFab(false)
        }
    }

    override fun getMaterialAboutList(activityContext: Context?): MaterialAboutList? {
        val builder = MaterialAboutList.Builder()

        context?.let { ctx ->

            val infoCard = MaterialAboutCard.Builder().outline(false).addItem(
                MaterialAboutTitleItem.Builder().text(getString(R.string.app_name))
                    .icon(R.drawable.ic_launcher_144dp).build()
            ).addItem(
                ConvenienceBuilder.createVersionActionItem(
                    ctx,
                    ContextCompat.getDrawable(ctx, R.drawable.info_24px),
                    getString(R.string.version),
                    true
                )
            ).addItem(
                ConvenienceBuilder.createWebViewDialogItem(
                    ctx,
                    ContextCompat.getDrawable(ctx, R.drawable.policy_24px),
                    getString(R.string.privacy),
                    null,
                    getString(R.string.privacy),
                    "https://marbat87.altervista.org/privacy_policy.html",
                    true,
                    false
                )
            ).build()

            val authorCard = MaterialAboutCard.Builder().outline(false).title("Author").addItem(
                MaterialAboutTitleItem.Builder().text("Marbat87").desc("Italy")
                    .icon(R.drawable.ic_brand_icon).build()
            ).addItem(
                MaterialAboutActionItem.Builder().text(R.string.email)
                    .subText("marbat87@outlook.it").icon(R.drawable.mail_24px)
                    .setOnClickAction {
                        try {
                            ctx.startActivity(
                                ShareCompat.IntentBuilder(ctx).setType("text/html")

                                    .setSubject(getString(R.string.app_name))
                                    .addEmailTo("marbat87@outlook.it").intent
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error:", e)
                            // No activity to handle intent
                            Toast.makeText(
                                ctx, com.danielstone.materialaboutlibrary.R.string.mal_activity_exception, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.build()
            ).build()

            val miscCard = MaterialAboutCard.Builder().outline(false).addItem(
                MaterialAboutActionItem.Builder().text(R.string.changelog)
                    .icon(R.drawable.list_alt_24px).setOnClickAction {
                        activity?.let {
                            it.startActivityWithTransition(
                                Intent(
                                    it, ChangelogActivity::class.java
                                ),
                                com.google.android.material.transition.platform.MaterialSharedAxis.Y
                            )
                        }
                    }.build()
            ).addItem(
                ConvenienceBuilder.createRateActionItem(
                    ctx,
                    ContextCompat.getDrawable(ctx, R.drawable.star_24px),
                    getString(R.string.rate_five_stars),
                    null
                )
            ).addItem(
                MaterialAboutActionItem.Builder().text(R.string.share_app)
                    .icon(R.drawable.share_24px).setOnClickAction {
                        ctx.startActivity(
                            ctx.shareThisApp(getString(R.string.app_name))
                        )
                    }.build()
            ).addItem(
                MaterialAboutActionItem.Builder().text(R.string.update_app)
                    .icon(R.drawable.file_download_24px)
                    .setOnClickAction(ConvenienceBuilder.createRateOnClickAction(ctx)).build()
            ).build()

            builder.addCard(infoCard)
            builder.addCard(authorCard)
            builder.addCard(miscCard)

        }

        return builder.build()
    }

    companion object {
        internal val TAG = AboutFragment::class.java.canonicalName
    }

}
