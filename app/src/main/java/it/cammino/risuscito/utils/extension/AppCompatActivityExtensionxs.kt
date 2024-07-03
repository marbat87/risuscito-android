package it.cammino.risuscito.utils.extension

import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

fun AppCompatActivity.updateListaPersonalizzata(listaUpd: ListaPers) {
    val mDao = RisuscitoDatabase.getInstance(this).listePersDao()
    lifecycleScope.launch {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.updateLista(
                listaUpd
            )
        }
        Snackbar.make(
            findViewById(R.id.main_content),
            R.string.list_added,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}

fun AppCompatActivity.addToListaDup(
    idLista: Int,
    listPosition: Int,
    idDaAgg: Int
) {
    val mDao = RisuscitoDatabase.getInstance(this).customListDao()
    lifecycleScope.launch {
        try {
            val position = CustomList()
            position.id = idLista
            position.position = listPosition
            position.idCanto = idDaAgg
            position.timestamp = Date(System.currentTimeMillis())
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.insertPosition(
                    position
                )
            }
        } catch (e: SQLException) {
            Snackbar.make(
                findViewById(R.id.main_content),
                R.string.present_yet,
                Snackbar.LENGTH_SHORT
            ).show()
        }
        Snackbar.make(
            findViewById(R.id.main_content),
            R.string.list_added,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}

fun AppCompatActivity.addToListaNoDup(
    idLista: Int,
    listPosition: Int,
    idDaAgg: Int,
    replaceTag: String
) {
    val db = RisuscitoDatabase.getInstance(this)
    lifecycleScope.launch {
        val listeDao = db.customListDao()
        val cantoDao = db.cantoDao()
        val idPresente =
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                listeDao.getIdByPosition(
                    idLista,
                    listPosition
                )
            }
        idPresente?.let {
            if (idDaAgg == it) {
                Snackbar.make(
                    findViewById(R.id.main_content),
                    R.string.present_yet,
                    Snackbar.LENGTH_SHORT
                ).show()
                return@launch
            } else {
                val titoloPresente =
                    withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                        resources.getString(
                            Utility.getResId(
                                cantoDao.getCantoById(it)?.titolo.orEmpty(),
                                R.string::class.java
                            )
                        )
                    }

                val sb = StringBuilder()
                sb.append(getString(R.string.dialog_present_yet))
                sb.append(" ")
                sb.append(titoloPresente)
                sb.append(".")
                sb.append(System.lineSeparator())
                sb.append(getString(R.string.dialog_wonna_replace))

                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        replaceTag
                    )
                        .title(R.string.dialog_replace_title)
                        .icon(R.drawable.find_replace_24px)
                        .content(sb.toString())
                        .positiveButton(R.string.replace_confirm)
                        .negativeButton(R.string.cancel),
                    supportFragmentManager
                )
                return@launch
            }
        }

        val position = CustomList()
        position.id = idLista
        position.position = listPosition
        position.idCanto = idDaAgg
        position.timestamp = Date(System.currentTimeMillis())
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            listeDao.insertPosition(
                position
            )
        }
        Snackbar.make(
            findViewById(R.id.main_content),
            R.string.list_added,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}

fun AppCompatActivity.addToFavorites(idDaAgg: Int, showSnackbar: Boolean) {
    val mDao = RisuscitoDatabase.getInstance(this).favoritesDao()
    lifecycleScope.launch {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.setFavorite(
                idDaAgg
            )
        }
        if (showSnackbar)
            Snackbar.make(
                findViewById(R.id.main_content),
                R.string.favorite_added,
                Snackbar.LENGTH_SHORT
            ).show()
    }
}

fun AppCompatActivity.manageReplaceDialog(idCanto: Int, replaceTag: String) {
    val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
    lifecycleScope.launch {
        val existingTitle =
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.getCantoById(idCanto)?.titolo.orEmpty()
            }

        val sb = StringBuilder()
        sb.append(getString(R.string.dialog_present_yet))
        sb.append(" ")
        sb.append(
            resources.getString(
                Utility.getResId(
                    existingTitle,
                    R.string::class.java
                )
            )
        )
        sb.append(".")
        sb.append(System.lineSeparator())
        sb.append(getString(R.string.dialog_wonna_replace))

        SimpleDialogFragment.show(
            SimpleDialogFragment.Builder(
                replaceTag
            )
                .title(R.string.dialog_replace_title)
                .icon(R.drawable.find_replace_24px)
                .content(sb.toString())
                .positiveButton(R.string.replace_confirm)
                .negativeButton(R.string.cancel),
            supportFragmentManager
        )
    }
}

fun AppCompatActivity.updatePosizione(idDaAgg: Int, idListaDaAgg: Int, posizioneDaAgg: Int) {
    val mCustomListDao =
        RisuscitoDatabase.getInstance(this).customListDao()
    lifecycleScope.launch {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mCustomListDao.updatePositionNoTimestamp(
                idDaAgg,
                idListaDaAgg,
                posizioneDaAgg
            )
        }
        Snackbar.make(
            findViewById(R.id.main_content),
            R.string.list_added,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}
