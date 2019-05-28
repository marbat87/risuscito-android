package it.cammino.risuscito.ui

import com.mikepenz.crossfader.Crossfader
import com.mikepenz.materialdrawer.interfaces.ICrossfader

class CrossfadeWrapper(private val mCrossfader: Crossfader<*>?) : ICrossfader {
    override val isCrossfaded: Boolean
        get() = mCrossfader?.isCrossFaded() == true

    override fun crossfade() {
        mCrossfader?.crossFade()
    }
}
