package it.cammino.risuscito

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.animation.AnimationUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
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
            intent: Intent) {
        mActivity.startActivity(intent)
        Animatoo.animateSlideLeft(mActivity)

        val mDao = RisuscitoDatabase.getInstance(mActivity).cronologiaDao()
        val cronologia = Cronologia()
        cronologia.idCanto = intent.extras?.getInt(Utility.ID_CANTO) ?: 0
        (mActivity as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) { mDao.insertCronologia(cronologia) }
    }

    fun startActivityWithFadeIn(intent: Intent) {
        mActivity.startActivity(intent)
        Animatoo.animateZoom(mActivity)
    }

    fun closeActivityWithTransition() {
        mActivity.finish()
        Animatoo.animateSlideRight(mActivity)
    }

    internal fun goFullscreen() {
        when {
            hasR() -> goFullscreenR()
            hasK() -> goFullscreenK()
            else -> goFullscreenJB()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    internal fun goFullscreenR() {
        mActivity.window.insetsController?.hide(WindowInsets.Type.navigationBars())
        mActivity.window.setDecorFitsSystemWindows(false)
        mActivity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        mActivity.window.decorView.rootWindowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
        mActivity.window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    @Suppress("DEPRECATION")
    internal fun goFullscreenJB() {
        mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mActivity
                .window
                .setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @Suppress("DEPRECATION")
    internal fun goFullscreenK() {
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
                //            writer.toString();

                val exportFile = File(mActivity.cacheDir.absolutePath + "/" + it.name + FILE_FORMAT)
                Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
                val fos = FileOutputStream(exportFile)
                val dataWrite = writer.toString()
                fos.write(dataWrite.toByteArray())
                fos.close()

                return FileProvider.getUriForFile(mActivity, "it.cammino.risuscito.fileprovider", exportFile)

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
            PreferenceManager.getDefaultSharedPreferences(mActivity).edit { putString(prefName, pref.getInt(prefName, 0).toString()) }
        }

    }

    // Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout
    // enters
    internal fun animateIn(view: View) {
        view.isVisible = true
        view.animate().translationY(0f).setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(225L).setListener(null).start()
    }

    internal fun animateOut(view: View) {
        view.animate().translationY(view.height.toFloat()).setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR).setDuration(175L).setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        view.isVisible = false
                    }
                }
        ).start()
    }

    companion object {

        //        private val INTERPOLATOR = FastOutSlowInInterpolator()
        private const val FILE_FORMAT = ".risuscito"
        internal val TAG = LUtils::class.java.canonicalName

        fun getInstance(activity: Activity): LUtils {
            return LUtils(activity)
        }

        fun hasK(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        }

        fun hasL(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }

        fun hasO(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

        fun hasQ(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        fun hasJB(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
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

//        fun hasQ(): Boolean {
//            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//        }

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
                    Log.e(TAG, "getResId: " + e.localizedMessage, e)
                    -1
                }
            }
            Log.e(TAG, "resName NULL")
            return -1
        }
    }

}
