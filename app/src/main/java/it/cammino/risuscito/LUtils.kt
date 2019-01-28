package it.cammino.risuscito

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
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

    val isFabScrollingActive: Boolean
        get() = mActivity.resources.getBoolean(R.bool.fab_behavior_active)

    fun startActivityWithTransition(
            intent: Intent) {
        mActivity.startActivity(intent)
//        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on)
        Animatoo.animateSlideLeft(mActivity)

        Thread(
                Runnable {
                    val mDao = RisuscitoDatabase.getInstance(mActivity).cronologiaDao()
                    val cronologia = Cronologia()
                    cronologia.idCanto = intent.extras!!.getInt("idCanto")
                    mDao.insertCronologia(cronologia)
                })
                .start()
    }

    fun startActivityWithFadeIn(intent: Intent) {
        mActivity.startActivity(intent)
//        mActivity.overridePendingTransition(R.anim.image_fade_in, R.anim.hold_on)
        Animatoo.animateZoom(mActivity)
    }

    fun closeActivityWithTransition() {
        mActivity.finish()
//        mActivity.overridePendingTransition(0, R.anim.slide_out_right)
        Animatoo.animateSlideRight(mActivity)
    }

    internal fun closeActivityWithFadeOut() {
        mActivity.finish()
//        mActivity.overridePendingTransition(0, R.anim.image_fade_out)
        Animatoo.animateZoom(mActivity)

    }

    internal fun goFullscreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mActivity
                    .window
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else
            mActivity
                    .window
                    .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    // controlla se l'app deve mantenere lo schermo acceso
    fun checkScreenAwake() {
        val pref = PreferenceManager.getDefaultSharedPreferences(mActivity)
        val screenOn = pref.getBoolean(Utility.SCREEN_ON, false)
        if (screenOn)
            mActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            mActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    internal fun listToXML(lista: ListaPersonalizzata): Uri? {

        try {
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()

            // root elements
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("list")
            rootElement.setAttribute("title", lista.name)
            doc.appendChild(rootElement)

            for (i in 0 until lista.numPosizioni) {
                val position = doc.createElement("position")
                position.setAttribute("name", lista.getNomePosizione(i))
                if (lista.getCantoPosizione(i) != "")
                    position.appendChild(doc.createTextNode(lista.getCantoPosizione(i)))
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
            //            writer.toString();

            val exportFile = File(mActivity.cacheDir.absolutePath + "/" + lista.name + FILE_FORMAT)
            Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
            val fos = FileOutputStream(exportFile)
            val dataWrite = writer.toString()
            fos.write(dataWrite.toByteArray())
            fos.close()

            return FileProvider.getUriForFile(mActivity, "it.cammino.risuscito.fileprovider", exportFile)

        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Crashlytics.logException(e)
            return null
        } catch (e: TransformerConfigurationException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Crashlytics.logException(e)
            return null
        } catch (e: TransformerException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Crashlytics.logException(e)
            return null
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Crashlytics.logException(e)
            return null
        } catch (e: IOException) {
            Log.e(TAG, "listToXML: " + e.localizedMessage, e)
            Crashlytics.logException(e)
            return null
        }

    }

    fun convertIntPreferences() {
        convert(Utility.DEFAULT_INDEX)
        convert(Utility.SAVE_LOCATION)
    }

    private fun convert(prefName: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(mActivity)
        try {
            pref.getString(prefName, "0")
            Log.d(TAG, "onCreateView: $prefName STRING")
        } catch (e: ClassCastException) {
            Log.d(TAG, "onCreateView: $prefName INTEGER >> CONVERTO")
            PreferenceManager.getDefaultSharedPreferences(mActivity).edit { putString(prefName, pref.getInt(prefName, 0).toString()) }
        }

    }

    // Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout
    // enters
    internal fun animateIn(view: View) {
        view.visibility = View.VISIBLE
        ViewCompat.animate(view)
                .setDuration(200)
                .translationY(0f)
                .setInterpolator(INTERPOLATOR)
                .withLayer()
                .setListener(null)
                .start()
    }

    companion object {

        private val INTERPOLATOR = FastOutSlowInInterpolator()
        private const val FILE_FORMAT = ".risuscito"
        internal val TAG = LUtils::class.java.canonicalName

        fun getInstance(activity: Activity): LUtils {
            return LUtils(activity)
        }

        fun hasL(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }

        fun hasO(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

        fun hasJB(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
        }

        fun hasN(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        }

        fun hasP(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
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
            return if (LUtils.hasN())
                fromHtml(input)
            else
                fromHtmlLegacy(input)
        }

        fun getResId(resName: String?, c: Class<*>): Int {
            return try {
                val idField = c.getDeclaredField(resName!!)
                idField.getInt(idField)
            } catch (e: Exception) {
                Log.e(TAG, "getResId: " + e.localizedMessage, e)
                -1
            }

        }
    }

}
