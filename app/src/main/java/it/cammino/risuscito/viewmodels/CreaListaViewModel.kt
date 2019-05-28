package it.cammino.risuscito.viewmodels

import androidx.lifecycle.ViewModel

import it.cammino.risuscito.items.SwipeableItem

class CreaListaViewModel : ViewModel() {

    var positionToRename = 0

    var tempTitle = ""

    var data: ArrayList<String>? = null
    var dataDrag: ArrayList<SwipeableItem>? = null

}
