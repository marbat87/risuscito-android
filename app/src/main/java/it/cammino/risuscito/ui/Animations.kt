package it.cammino.risuscito.ui

import android.app.Activity
import it.cammino.risuscito.R

object Animations {

//    fun enterRight(activity: Activity?) {
//        activity?.overridePendingTransition(
//            R.anim.animate_slide_left_enter,
//            R.anim.animate_zoom_exit
//        )
//    }
//
//    fun exitRight(activity: Activity?) {
//        activity?.overridePendingTransition(
//            R.anim.animate_shrink_enter,
//            R.anim.animate_slide_out_right
//        )
//    }

//    fun enterDown(activity: Activity?) {
//        activity?.overridePendingTransition(
//            R.anim.animate_slide_up_enter,
//            R.anim.animate_zoom_exit
//        )
//    }
//
//    fun exitDown(activity: Activity?) {
//        activity?.overridePendingTransition(
//            R.anim.animate_shrink_enter,
//            R.anim.animate_slide_down_exit
//        )
//    }

    fun enterZoom(activity: Activity?) {
        activity?.overridePendingTransition(
            R.anim.animate_shrink_enter,
            R.anim.animate_zoom_exit
        )
    }

    fun exitZoom(activity: Activity?) {
        activity?.overridePendingTransition(
            R.anim.animate_shrink_enter,
            R.anim.animate_zoom_exit
        )
    }

    fun slideInRight(activity: Activity?) {
        activity?.overridePendingTransition(
            R.anim.animate_slide_in_right,
            R.anim.animate_slide_out_left
        )
    }
}