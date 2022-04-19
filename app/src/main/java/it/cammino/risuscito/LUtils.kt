package it.cammino.risuscito

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.ui.Animations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class LUtils private constructor(private val mActivity: Activity) {

    val isOnTablet: Boolean
        get() = mActivity.resources.getBoolean(R.bool.is_tablet)

    val hasThreeColumns: Boolean
        get() = mActivity.resources.getBoolean(R.bool.has_three_columns)

    val isGridLayout: Boolean
        get() = mActivity.resources.getBoolean(R.bool.is_grid_layout)

    val isLandscape: Boolean
        get() = mActivity.resources.getBoolean(R.bool.landscape)

    val isFabExpansionLeft: Boolean
        get() = mActivity.resources.getBoolean(R.bool.fab_orientation_left)

    fun startActivityWithTransition(
        intent: Intent,
        startView: View?
    ) {

        val options = ActivityOptions.makeSceneTransitionAnimation(
            mActivity,
            startView,
            "shared_element_container" // The transition name to be matched in Activity B.
        )

        mActivity.startActivity(intent, options.toBundle())

        val mDao = RisuscitoDatabase.getInstance(mActivity).cronologiaDao()
        val cronologia = Cronologia()
        cronologia.idCanto = intent.extras?.getInt(Utility.ID_CANTO) ?: 0
        (mActivity as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            mDao.insertCronologia(
                cronologia
            )
        }
    }

    fun startActivityWithFadeIn(intent: Intent) {
        mActivity.startActivity(intent)
        Animations.enterZoom(mActivity)
    }

    //ISSUE in API 21
    fun finishAfterTransitionWrapper() {
        if (hasM())
            mActivity.finishAfterTransition()
        else
            mActivity.finish()
    }

    fun setLigthStatusBar(light: Boolean) {
        WindowInsetsControllerCompat(
            mActivity.window,
            mActivity.window.decorView
        ).isAppearanceLightStatusBars = light
        setLighStatusBarFlag(light)
    }

    private fun setLighStatusBarFlag(light: Boolean) {
        if (hasM())
            setLighStatusBarFlagM(light)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setLighStatusBarFlagM(light: Boolean) {
        if (light)
            mActivity
                .window
                .decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    internal fun goFullscreen() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(mActivity.window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//        when {
//            hasR() -> goFullscreenR()
//            else -> goFullscreenLegacy()
//        }
    }

//    private fun goFullscreenR() {
//        WindowCompat.setDecorFitsSystemWindows(mActivity.window, false)
//        WindowInsetsControllerCompat(
//            mActivity.window,
//            mActivity.window.decorView
//        ).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
//            controller.systemBarsBehavior =
//                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
//    }

//    @Suppress("DEPRECATION")
//    private fun goFullscreenLegacy() {
//        mActivity
//            .window
//            .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//    }

    // controlla se l'app deve mantenere lo schermo acceso
    fun checkScreenAwake() {
        val pref = PreferenceManager.getDefaultSharedPreferences(mActivity)
        val screenOn = pref.getBoolean(Utility.SCREEN_ON, false)
        if (screenOn)
            mActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            mActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    internal fun listToXML(lista: ListaPersonalizzata?): Uri? {
        lista?.let {
            try {
                val docFactory = DocumentBuilderFactory.newInstance()
                val docBuilder = docFactory.newDocumentBuilder()

                // root elements
                val doc = docBuilder.newDocument()
                val rootElement = doc.createElement("list")
                rootElement.setAttribute("title", it.name)
                doc.appendChild(rootElement)

                for (i in 0 until it.numPosizioni) {
                    val position = doc.createElement("position")
                    position.setAttribute("name", it.getNomePosizione(i))
                    if (it.getCantoPosizione(i) != "")
                        position.appendChild(doc.createTextNode(it.getCantoPosizione(i)))
                    else
                        position.appendChild(doc.createTextNode("0"))
                    rootElement.appendChild(position)
                }

                val domSource = DOMSource(doc)
                val writer = StringWriter()
                val result = StreamResult(writer)
                val tf = TransformerFactory.newInstance()
                val transformer = tf.newTransformer()
                transformer.transform(domSource, result)
                Log.d(TAG, "listToXML: $writer")

                val exportFile = File(mActivity.cacheDir.absolutePath + "/" + it.name + FILE_FORMAT)
                Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
                val fos = FileOutputStream(exportFile)
                val dataWrite = writer.toString()
                fos.write(dataWrite.toByteArray())
                fos.close()

                return FileProvider.getUriForFile(
                    mActivity,
                    "it.cammino.risuscito.fileprovider",
                    exportFile
                )

            } catch (e: ParserConfigurationException) {
                Log.e(TAG, "listToXML: " + e.localizedMessage, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            } catch (e: TransformerConfigurationException) {
                Log.e(TAG, "listToXML: " + e.localizedMessage, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            } catch (e: TransformerException) {
                Log.e(TAG, "listToXML: " + e.localizedMessage, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "listToXML: " + e.localizedMessage, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            } catch (e: IOException) {
                Log.e(TAG, "listToXML: " + e.localizedMessage, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            }
        }
        Log.e(TAG, "input lista null")
        return null
    }

    fun convertIntPreferences() {
        convert(Utility.DEFAULT_INDEX)
        convert(Utility.SAVE_LOCATION)
        convert(Utility.DEFAULT_SEARCH)
    }

    private fun convert(prefName: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(mActivity)
        try {
            pref.getString(prefName, "0")
            Log.d(TAG, "onCreateView: $prefName STRING")
        } catch (e: ClassCastException) {
            Log.d(TAG, "onCreateView: $prefName INTEGER >> CONVERTO")
            PreferenceManager.getDefaultSharedPreferences(mActivity)
                .edit { putString(prefName, pref.getInt(prefName, 0).toString()) }
        }

    }

    // Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout
    // enters
    internal fun animateIn(view: View) {
        view.isVisible = true
        view.animate().translationY(0f)
            .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(225L)
            .setListener(null).start()
    }

    internal fun animateOut(view: View) {
        view.animate().translationY(view.height.toFloat())
            .setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR).setDuration(175L)
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        view.isVisible = false
                    }
                }
            ).start()
    }

    val hasStorageAccess: Boolean
        get() = hasQ() || ContextCompat.checkSelfPermission(
            mActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    companion object {

        private const val FILE_FORMAT = ".risuscito"
        internal val TAG = LUtils::class.java.canonicalName
        internal val FAST_OUT_LINEAR_IN_INTERPOLATOR: TimeInterpolator =
            FastOutLinearInInterpolator()
        internal val LINEAR_OUT_SLOW_IN_INTERPOLATOR: TimeInterpolator =
            LinearOutSlowInInterpolator()

        fun getInstance(activity: Activity): LUtils {
            return LUtils(activity)
        }

        fun hasO(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

        fun hasQ(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        fun hasM(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }

        fun hasN(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        }

        fun hasP(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        }

        fun hasR(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }

        fun hasS(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }

        @Suppress("DEPRECATION")
        private fun fromHtmlLegacy(input: String): Spanned {
            return Html.fromHtml(input)
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun fromHtml(input: String): Spanned {
            return Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY)
        }

        fun fromHtmlWrapper(input: String): Spanned {
            return if (hasN())
                fromHtml(input)
            else
                fromHtmlLegacy(input)
        }

        fun getResId(resName: String?, c: Class<*>): Int {
            resName?.let {
                return try {
                    val idField = c.getDeclaredField(it)
                    idField.getInt(idField)
                } catch (e: Exception) {
                    Log.e(TAG, "getResId: $resName" + e.localizedMessage, e)
                    -1
                }
            }
            Log.e(TAG, "resName NULL")
            return -1
        }

    }

}
