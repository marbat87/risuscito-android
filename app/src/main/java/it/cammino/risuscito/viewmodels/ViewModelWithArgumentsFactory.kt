package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.Constructor

class ViewModelWithArgumentsFactory(private val application: Application, private val args: Bundle) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            val constructor: Constructor<T> = modelClass.getDeclaredConstructor(Application::class.java, Bundle::class.java)
            return constructor.newInstance(application, args)
        } catch (e: Exception) {
            Log.e( modelClass.canonicalName, "Could not create new instance of class %s", e)
            throw e
        }
    }

}