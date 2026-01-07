@file:Suppress("SameParameterValue")

package it.cammino.risuscito.utils.extension

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import it.cammino.risuscito.utils.OSUtils

fun <T : java.io.Serializable> Bundle.getSerializableWrapper(
    key: String,
    clazz: Class<T>
): java.io.Serializable? {
    return if (OSUtils.hasT()) getSerializableTiramisu(key, clazz) else getSerializableLegacy(key)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun <T : java.io.Serializable> Bundle.getSerializableTiramisu(
    key: String,
    clazz: Class<T>
): java.io.Serializable? {
    return getSerializable(key, clazz)
}

@Suppress("DEPRECATION")
private fun Bundle.getSerializableLegacy(key: String): java.io.Serializable? {
    return getSerializable(key)
}