package it.cammino.risuscito.utils.extension

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.elevation.SurfaceColors
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
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
        return if (OSUtils.hasN())
            getSystemLocaleN()
        else
            getSystemLocaleLegacy()
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
        window,
        window.decorView
    ).isAppearanceLightNavigationBars = true
}

fun Activity.setLigthStatusBar(light: Boolean) {
    WindowCompat.getInsetsController(
        window,
        window.decorView
    ).isAppearanceLightStatusBars = light
    setLighStatusBarFlag(light)
}

private fun Activity.setLighStatusBarFlag(light: Boolean) {
    if (OSUtils.hasM())
        setLighStatusBarFlagM(light)
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.M)
private fun Activity.setLighStatusBarFlagM(light: Boolean) {
    if (light)
        window
            .decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

fun Activity.startActivityWithTransition(
    intent: Intent,
    startView: View?
) {

    if (OSUtils.isObySamsung()) {
        startActivity(intent)
        slideInRight()
    } else {
        val options = ActivityOptions.makeSceneTransitionAnimation(
            this,
            startView,
            "shared_element_container" // The transition name to be matched in Activity B.
        )
        startActivity(intent, options.toBundle())
    }

    val mDao = RisuscitoDatabase.getInstance(this).cronologiaDao()
    val cronologia = Cronologia()
    cronologia.idCanto = intent.extras?.getInt(Utility.ID_CANTO) ?: 0
    (this as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
        mDao.insertCronologia(
            cronologia
        )
    }
}

fun Activity.startActivityWithFadeIn(intent: Intent) {
    startActivity(intent)
    enterZoom()
}

//ISSUE in API 21
fun Activity.finishAfterTransitionWrapper() {
    closeKeyboard()
    if (OSUtils.hasM())
        finishAfterTransition()
    else
        finish()
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
        manager
            .hideSoftInputFromWindow(
                view.windowToken, 0
            )
    }
}

internal fun Activity.goFullscreen() {
    val windowInsetsController =
        WindowCompat.getInsetsController(window, window.decorView)
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
    if (screenOn)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    else
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                if (it.getCantoPosizione(i).isNotEmpty())
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

            val exportFile = File("${cacheDir.absolutePath}/${it.name}.risuscito")
            Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
            val fos = FileOutputStream(exportFile)
            val dataWrite = writer.toString()
            fos.write(dataWrite.toByteArray())
            fos.close()

            return FileProvider.getUriForFile(
                this,
                "it.cammino.risuscito.fileprovider",
                exportFile
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
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

fun Activity.enterZoom() {
    overridePendingTransition(
        R.anim.animate_shrink_enter,
        R.anim.animate_zoom_exit
    )
}

fun Activity.exitZoom() {
    overridePendingTransition(
        R.anim.animate_shrink_enter,
        R.anim.animate_zoom_exit
    )
}

fun Activity.slideInRight() {
    overridePendingTransition(
        R.anim.animate_slide_in_right,
        R.anim.animate_slide_out_left
    )
}