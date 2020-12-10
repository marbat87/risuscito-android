package it.cammino.risuscito.dialogs

import androidx.fragment.app.DialogFragment

sealed class DialogState<T: DialogFragment> {
    data class Positive<T : DialogFragment>(val dialog: T) : DialogState<T>()
    data class Negative<T : DialogFragment>(val dialog: T) : DialogState<T>()
}