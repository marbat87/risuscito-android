package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.LiveData

import com.mikepenz.fastadapter.IItem

import java.util.ArrayList

import it.cammino.risuscito.database.CantoArgomento

class LiturgicIndexViewModel(application: Application) : GenericIndexViewModel(application) {

    var titoliList: ArrayList<IItem<*, *>> = ArrayList()
}
