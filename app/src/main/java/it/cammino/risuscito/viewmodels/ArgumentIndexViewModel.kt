package it.cammino.risuscito.viewmodels

import android.app.Application

import com.mikepenz.fastadapter.IItem

import java.util.ArrayList

class ArgumentIndexViewModel(application: Application) : GenericIndexViewModel(application) {

    var titoliList: ArrayList<IItem<*>> = ArrayList()

}
