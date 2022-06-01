package it.cammino.risuscito.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R

class MainActivityViewModel : ViewModel() {

    var showSnackbar = true
    var signedIn = MutableLiveData<Boolean>()

    var isOnTablet = false
    var hasThreeColumns: Boolean = false
    var isGridLayout: Boolean = false
    var isLandscape: Boolean = false
    var isTabletWithFixedDrawer: Boolean = false
    var isTabletWithNoFixedDrawer: Boolean = false
    var selectedMenuItemId: Int = R.id.navigation_home
    lateinit var mLUtils: LUtils
}
