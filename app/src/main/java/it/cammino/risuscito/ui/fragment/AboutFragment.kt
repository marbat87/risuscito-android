package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.google.android.material.transition.MaterialSharedAxis
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.activity.ChangelogActivity
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.shareThisApp
import it.cammino.risuscito.utils.extension.slideInRight


class AboutFragment : MaterialAboutFragment() {

    private var mMainActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? MainActivity
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

        mMainActivity?.setupToolbarTitle(R.string.title_activity_about)
        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)
    }

    override fun getMaterialAboutList(activityContext: Context?): MaterialAboutList? {
        val builder = MaterialAboutList.Builder()

        context?.let { ctx ->

            val infoCard = MaterialAboutCard.Builder().outline(false)
                .addItem(
                    MaterialAboutTitleItem.Builder().text(getString(R.string.app_name))
                        .icon(R.drawable.ic_launcher_144dp).build()
                )
                .addItem(
                    ConvenienceBuilder.createVersionActionItem(
                        ctx,
                        ContextCompat.getDrawable(ctx, R.drawable.info_24px),
                        getString(R.string.version),
                        true
                    )
                )
                .addItem(
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
                )
                .build()

            val authorCard = MaterialAboutCard.Builder().outline(false).title("Author")
                .addItem(
                    MaterialAboutTitleItem.Builder().text("Marbat87").desc("Italy")
                        .icon(R.drawable.ic_brand_icon).build()
                )
                .addItem(
                    MaterialAboutActionItem.Builder().text(R.string.email)
                        .subText("marbat87@outlook.it").icon(R.drawable.mail_24px)
                        .setOnClickAction {
                            try {
                                ctx.startActivity(
                                    ShareCompat.IntentBuilder(ctx).setType("text/html")

                                        .setSubject(getString(R.string.app_name))
                                        .addEmailTo("marbat87@outlook.it")
                                        .intent
                                )
                            } catch (e: Exception) {
                                // No activity to handle intent
                                Toast.makeText(
                                    ctx,
                                    R.string.mal_activity_exception,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }.build()
                ).build()

            val miscCard = MaterialAboutCard.Builder().outline(false)
                .addItem(
                    MaterialAboutActionItem.Builder().text(R.string.changelog)
                        .icon(R.drawable.list_alt_24px)
                        .setOnClickAction {
                            activity?.let {
                                val intent = Intent(it, ChangelogActivity::class.java)
                                if (OSUtils.isObySamsung()) {
                                    startActivity(intent)
                                    it.slideInRight()
                                } else {
                                    val bundle =
                                        ActivityOptionsCompat.makeSceneTransitionAnimation(it)
                                            .toBundle()
                                    startActivity(
                                        intent,
                                        bundle
                                    )
                                }
                            }
                        }.build()
                )
                .addItem(
                    ConvenienceBuilder.createRateActionItem(
                        ctx,
                        ContextCompat.getDrawable(ctx, R.drawable.star_24px),
                        getString(R.string.rate_five_stars),
                        null
                    )
                )
                .addItem(
                    MaterialAboutActionItem.Builder().text(R.string.share_app)
                        .icon(R.drawable.share_24px)
                        .setOnClickAction {
                            ctx.startActivity(
                                ctx.shareThisApp(getString(R.string.app_name))
                            )
                        }.build()
                )
                .addItem(
                    MaterialAboutActionItem.Builder().text(R.string.update_app)
                        .icon(R.drawable.file_download_24px)
                        .setOnClickAction(ConvenienceBuilder.createRateOnClickAction(ctx))
                        .build()
                )
                .build()

            builder.addCard(infoCard)
            builder.addCard(authorCard)
            builder.addCard(miscCard)

        }

        return builder.build()
    }

}
