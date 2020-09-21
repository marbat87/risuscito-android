package it.cammino.risuscito.utils

sealed class DownloadState {
    data class Progress(val progress: Int) : DownloadState()
    data class Error(val message: String) : DownloadState()
    data class Completed(val message: String = "OK") : DownloadState()
}