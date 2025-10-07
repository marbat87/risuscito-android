package it.cammino.risuscito.ui.interfaces

interface FabFragment {
    fun onFabClick(item: FabItem)

    enum class FabItem {
        MAIN
    }
}