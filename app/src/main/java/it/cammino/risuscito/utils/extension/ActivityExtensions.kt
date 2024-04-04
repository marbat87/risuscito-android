@file:Suppress("SameParameterValue")

package it.cammino.risuscito.utils.extension

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AnimRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.ui.activity.CantoHostActivity
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.ui.fragment.CantoFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.SHARED_AXIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringWriter
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Suppress("DEPRECATION")
private fun Resources.getSystemLocaleLegacy(): Locale {
    return configuration.locale
}

@TargetApi(Build.VERSION_CODES.N)
private fun Resources.getSystemLocaleN(): Locale {
    return configuration.locales.get(0)
}

val Resources.systemLocale: Locale
    get() {
        return if (OSUtils.hasN()) getSystemLocaleN()
        else getSystemLocaleLegacy()
    }

fun Activity.setupNavBarColor() {
    if (OSUtils.hasO()) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (!isDarkMode) setLightNavigationBar()
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Activity.setLightNavigationBar() {
    WindowInsetsControllerCompat(
        window, window.decorView
    ).isAppearanceLightNavigationBars = true
}

fun Activity.setLigthStatusBar(light: Boolean) {
    WindowCompat.getInsetsController(
        window, window.decorView
    ).isAppearanceLightStatusBars = light
    setLighStatusBarFlag(light)
}

private fun Activity.setLighStatusBarFlag(light: Boolean) {
    if (OSUtils.hasM()) setLighStatusBarFlagM(light)
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.M)
private fun Activity.setLighStatusBarFlagM(light: Boolean) {
    if (light) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

fun Activity.startActivityWithTransition(intent: Intent, axis: Int) {
    if (OSUtils.isObySamsung()) {
        startActivity(intent)
        slideInRight()
    } else {
        val exit = MaterialSharedAxis(axis, true).apply {
            addTarget(R.id.content_frame)
            duration = 700L
        }

        val enter = MaterialSharedAxis(axis, false).apply {
            addTarget(R.id.content_frame)
            duration = 700L
        }
        window.exitTransition = exit
        window.reenterTransition = enter
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
        startActivity(
            intent.putExtras(
                bundleOf(SHARED_AXIS to axis)
            ), options.toBundle()
        )
    }

}


fun Activity.setEnterTransition() {
    if (!OSUtils.isObySamsung()) {
        val axis = intent.getIntExtra(SHARED_AXIS, MaterialSharedAxis.X)
        val enter = MaterialSharedAxis(axis, true).apply {
            duration = 700L
        }
        val returnT = MaterialSharedAxis(axis, false).apply {
            duration = 700L
        }
        window.enterTransition = enter
        window.returnTransition = returnT

        // Allow Activity A’s exit transition to play at the same time as this Activity’s
        // enter transition instead of playing them sequentially.
        window.allowEnterTransitionOverlap = true
    }
}

fun Activity.startActivityWithFadeIn(intent: Intent) {
    startActivity(intent)
    enterZoom()
}

//ISSUE in API 21
fun Activity.finishAfterTransitionWrapper() {
    closeKeyboard()
    if (OSUtils.hasM()) finishAfterTransition()
    else finish()
}

private fun Activity.closeKeyboard() {
    // this will give us the view
    // which is currently focus
    // in this layout
    val view: View? = currentFocus

    // if nothing is currently
    // focus then this will protect
    // the app from crash
    if (view != null) {
        // now assign the system
        // service to InputMethodManager
        val manager: InputMethodManager = getSystemService(
            AppCompatActivity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        manager.hideSoftInputFromWindow(
            view.windowToken, 0
        )
    }
}

internal fun Activity.goFullscreen() {
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    // Hide both the status bar and the navigation bar
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

// controlla se l'app deve mantenere lo schermo acceso
fun Activity.checkScreenAwake() {
    val pref = PreferenceManager.getDefaultSharedPreferences(this)
    val screenOn = pref.getBoolean(Utility.SCREEN_ON, false)
    if (screenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

internal fun Activity.listToXML(lista: ListaPersonalizzata?): Uri? {
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
                if (it.getCantoPosizione(i)
                        .isNotEmpty()
                ) position.appendChild(doc.createTextNode(it.getCantoPosizione(i)))
                else position.appendChild(doc.createTextNode("0"))
                rootElement.appendChild(position)
            }

            val domSource = DOMSource(doc)
            val writer = StringWriter()
            val result = StreamResult(writer)
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.transform(domSource, result)
            Log.d(TAG, "listToXML: $writer")

            val exportFile = File("${cacheDir.absolutePath}/${it.name}.risuscito")
            Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
            val fos = FileOutputStream(exportFile)
            val dataWrite = writer.toString()
            fos.write(dataWrite.toByteArray())
            fos.close()

            return FileProvider.getUriForFile(
                this, "it.cammino.risuscito.fileprovider", exportFile
            )

        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Firebase.crashlytics.recordException(e)
            return null
        } catch (e: TransformerConfigurationException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Firebase.crashlytics.recordException(e)
            return null
        } catch (e: TransformerException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Firebase.crashlytics.recordException(e)
            return null
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Firebase.crashlytics.recordException(e)
            return null
        } catch (e: IOException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Firebase.crashlytics.recordException(e)
            return null
        }
    }
    Log.e(TAG, "input lista null")
    return null
}

internal const val TAG = "Extensions"

fun Activity.convertIntPreferences() {
    convert(Utility.DEFAULT_INDEX)
    convert(Utility.SAVE_LOCATION)
    convert(Utility.DEFAULT_SEARCH)
}

private fun Activity.convert(prefName: String) {
    val pref = PreferenceManager.getDefaultSharedPreferences(this)
    try {
        pref.getString(prefName, "0")
        Log.d(TAG, "onCreateView: $prefName STRING")
    } catch (e: ClassCastException) {
        Log.d(TAG, "onCreateView: $prefName INTEGER >> CONVERTO")
        pref.edit { putString(prefName, pref.getInt(prefName, 0).toString()) }
    }
}

val Activity.hasStorageAccess: Boolean
    get() = OSUtils.hasQ() || ContextCompat.checkSelfPermission(
        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

fun Activity.enterZoom() {
    overrideOpenTransition(R.anim.animate_shrink_enter, R.anim.animate_zoom_exit)
}

fun Activity.exitZoom() {
    overrideCloseTransition(R.anim.animate_shrink_enter, R.anim.animate_zoom_exit)
}

fun Activity.slideInRight() {
    overrideOpenTransition(R.anim.animate_slide_in_right, R.anim.animate_slide_out_left)
}

fun Activity.slideOutRight() {
    overrideCloseTransition(R.anim.animate_slide_in_left, R.anim.animate_slide_out_right)
}

fun ThemeableActivity.openCanto(
    function: String?,
    idCanto: Int,
    numPagina: String?,
    forceOpenActivity: Boolean = false
) {

    Firebase.crashlytics.log("open_canto - function: ${function.orEmpty()} - idCanto: $idCanto - numPagina: ${numPagina.orEmpty()} - onActivity: ${forceOpenActivity || isOnPhone}")

    val args = bundleOf(
        CantoFragment.ARG_NUM_PAGINA to numPagina,
        CantoFragment.ARG_ID_CANTO to idCanto,
        CantoFragment.ARG_ON_ACTIVITY to (forceOpenActivity || isOnPhone)
    )

    if (forceOpenActivity || isOnPhone) {
        val intent = Intent(this, CantoHostActivity::class.java)
        intent.putExtras(args)
        startActivityWithTransition(intent, MaterialSharedAxis.X)
    } else {
        stopMedia()
        val fragment: Fragment = CantoFragment()
        fragment.arguments = args
        supportFragmentManager.commit {
            replace(
                R.id.detail_fragment, fragment, R.id.canto_fragment.toString()
            )
        }
    }

    (this as? AppCompatActivity)?.updateHistory(idCanto)

}

@TargetApi(Build.VERSION_CODES.P)
fun Activity.getVersionCodeP(): Int {
    return packageManager.getPackageInfo(packageName).longVersionCode.toInt()
}

@Suppress("DEPRECATION")
fun Activity.getVersionCodeLegacy(): Int {
    return packageManager.getPackageInfo(packageName).versionCode
}

fun Activity.getVersionCode(): Int {
    return if (OSUtils.hasP()) getVersionCodeP()
    else getVersionCodeLegacy()
}

fun Activity.overrideOpenTransition(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    if (OSUtils.hasU())
        overrideOpenTransitionU(enterAnim, exitAnim)
    else
        overrideOpenTransitionLegacy(enterAnim, exitAnim)
}

@RequiresApi(34)
fun Activity.overrideOpenTransitionU(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, enterAnim, exitAnim)
}

@Suppress("DEPRECATION")
fun Activity.overrideOpenTransitionLegacy(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    overridePendingTransition(
        enterAnim, exitAnim
    )
}

fun Activity.overrideCloseTransition(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    if (OSUtils.hasU())
        overrideCloseTransitionU(enterAnim, exitAnim)
    else
        overrideCloseTransitionLegacy(enterAnim, exitAnim)
}

@RequiresApi(34)
fun Activity.overrideCloseTransitionU(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, enterAnim, exitAnim)
}

@Suppress("DEPRECATION")
fun Activity.overrideCloseTransitionLegacy(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    overridePendingTransition(
        enterAnim, exitAnim
    )
}

fun Activity.createTaskDescription(tag: String?): ActivityManager.TaskDescription {
    return when (true) {
        OSUtils.hasT() -> createTaskDescriptionTiramisu(tag)
        OSUtils.hasP() -> createTaskDescriptionP(tag)
        else -> createTaskDescriptionLegacy(tag)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Activity.createTaskDescriptionTiramisu(tag: String?): ActivityManager.TaskDescription {
    val builder = ActivityManager.TaskDescription.Builder()
    builder.setIcon(R.mipmap.ic_launcher)
    builder.setPrimaryColor(
        MaterialColors.getColor(
            this, R.attr.colorPrimary, tag
        )
    )
    return builder.build()
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.P)
private fun Activity.createTaskDescriptionP(tag: String?): ActivityManager.TaskDescription {
    return ActivityManager.TaskDescription(
        null, R.mipmap.ic_launcher, MaterialColors.getColor(this, R.attr.colorPrimary, tag)
    )
}

@Suppress("DEPRECATION")
private fun Activity.createTaskDescriptionLegacy(tag: String?): ActivityManager.TaskDescription {
    return ActivityManager.TaskDescription(
        null, null, MaterialColors.getColor(this, R.attr.colorPrimary, tag)
    )
}

private fun AppCompatActivity.updateHistory(idCanto: Int) {
    val mDao = RisuscitoDatabase.getInstance(this).cronologiaDao()
    val cronologia = Cronologia()
    cronologia.idCanto = idCanto
    this.lifecycleScope.launch(Dispatchers.IO) {
        mDao.insertCronologia(
            cronologia
        )
    }
}

fun AppCompatActivity.launchForResultWithAnimation(
    resultLauncher: ActivityResultLauncher<Intent>,
    intent: Intent,
    axis: Int
) {
    if (OSUtils.isObySamsung()) {
        resultLauncher.launch(intent)
        slideInRight()
    } else {
        val exit = MaterialSharedAxis(axis, true).apply {
            addTarget(R.id.content_frame)
            duration = 700L
        }

        val enter = MaterialSharedAxis(axis, false).apply {
            addTarget(R.id.content_frame)
            duration = 700L
        }
        window.exitTransition = exit
        window.reenterTransition = enter
        resultLauncher.launch(
            intent.putExtras(
                bundleOf(SHARED_AXIS to axis)
            ), ActivityOptionsCompat.makeSceneTransitionAnimation(
                this
            )
        )
    }
}