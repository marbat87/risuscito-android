package it.cammino.risuscito.viewmodels

import android.arch.lifecycle.ViewModel

import java.util.ArrayList

import it.cammino.risuscito.items.SwipeableItem

class CreaListaViewModel : ViewModel() {

    var positionToRename = 0

    var tempTitle = ""

    var data: ArrayList<String>? = null
    var dataDrag: ArrayList<SwipeableItem>? = null

}
