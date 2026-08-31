package it.cammino.risuscito.ui.interfaces

interface SnackBarFragment {

    fun onActionPerformed()

    fun onDismissed()

    fun showSnackBar(message: String, label: String? = "")

}