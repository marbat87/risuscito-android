package it.cammino.risuscito.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.multidex.MultiDexApplication
import android.widget.ImageView
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso
import it.cammino.risuscito.database.RisuscitoDatabase


@Suppress("unused")
class RisuscitoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        RisuscitoDatabase.getInstance(this)

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
