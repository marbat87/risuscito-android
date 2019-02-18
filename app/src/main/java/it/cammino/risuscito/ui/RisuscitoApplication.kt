package it.cammino.risuscito.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.multidex.MultiDexApplication
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.utils.ioThread


@Suppress("unused")
class RisuscitoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        ioThread {
            RisuscitoDatabase.getInstance(this).cantoDao().getCantoById(1)
        }

        // initialize and create the image loader logic
        DrawerImageLoader.init(
                object : AbstractDrawerImageLoader() {
                    override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                        Picasso.get().load(uri).placeholder(placeholder).into(imageView)
                    }

                    override fun cancel(imageView: ImageView?) {
                        Picasso.get().cancelRequest(imageView!!)
                    }
                })
    }
}
