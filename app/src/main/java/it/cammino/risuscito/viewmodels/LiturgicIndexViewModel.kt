package it.cammino.risuscito.viewmodels

import android.app.Application
import com.mikepenz.fastadapter.IItem
import java.util.*

class LiturgicIndexViewModel(application: Application) : GenericIndexViewModel(application) {

    var titoliList: ArrayList<IItem<*>> = ArrayList()
}
