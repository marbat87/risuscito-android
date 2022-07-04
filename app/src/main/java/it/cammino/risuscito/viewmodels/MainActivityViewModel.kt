package it.cammino.risuscito.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.R

class MainActivityViewModel : ViewModel() {

    var showSnackbar = true
    var signedIn = MutableLiveData<Boolean>()

    var isTabletWithFixedDrawer: Boolean = false
    var isTabletWithNoFixedDrawer: Boolean = false
    var selectedMenuItemId: Int = R.id.navigation_home

}
