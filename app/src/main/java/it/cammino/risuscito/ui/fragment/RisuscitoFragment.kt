package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.ui.activity.MainActivity

open class RisuscitoFragment : Fragment() {

    protected var mMainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? MainActivity
        Log.d(TAG, "Fragment: ${this::class.java.canonicalName}")
        Firebase.crashlytics.log("Fragment: ${this::class.java}")
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainActivity?.destroyActionMode()
    }

    companion object {
        internal val TAG = RisuscitoFragment::class.java.canonicalName
    }
}
