package it.cammino.risuscito.viewmodels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
class SharedScrollViewModel : ViewModel() {

    private val _scrollBehavior = MutableStateFlow<TopAppBarScrollBehavior?>(null)
    val scrollBehavior: StateFlow<TopAppBarScrollBehavior?> = _scrollBehavior.asStateFlow()

    fun setScrollBehavior(behavior: TopAppBarScrollBehavior) {
        _scrollBehavior.value = behavior
    }

}