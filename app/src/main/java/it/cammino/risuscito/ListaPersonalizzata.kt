@file:Suppress("unused")

package it.cammino.risuscito

import android.util.Log
import java.io.*

class ListaPersonalizzata : Serializable {

    var name: String? = null
    private var posizioni: Array<String?>? = null
    var canti: Array<String?>? = null
    var numPosizioni: Int = 0

    init {
        name = ""
        posizioni = arrayOfNulls(MAX_POSIZIONI)
        canti = arrayOfNulls(MAX_POSIZIONI)
        numPosizioni = 0

        for (i in 0 until MAX_POSIZIONI) {
            posizioni!![i] = ""
            canti!![i] = ""
        }

    }

    //restituisce il titolo della posizione all'indice "index"
    fun getNomePosizione(index: Int): String {
        return if (index < 0 || index >= numPosizioni) "" else posizioni!![index]!!.trim { it <= ' ' }

    }

    //restituisce il titolo della canto in posizione "index"
    fun getCantoPosizione(index: Int): String {
        if (index < 0 || index >= numPosizioni)
            return ""

        return if (!canti!![index].equals("", ignoreCase = true))
            canti!![index]!!.trim { it <= ' ' }
        else
            ""
    }

    /*aggiunge una nuova posizione
     ritorna -1 se il nome inserito è nullo o vuoto
     ritorna -2 se la lista è piena
     */
    fun addPosizione(nomePosizione: String?): Int {
        if (nomePosizione == null || nomePosizione.trim { it <= ' ' }.equals("", ignoreCase = true))
            return -1

        if (numPosizioni == MAX_POSIZIONI)
            return -2

        posizioni!![numPosizioni++] = nomePosizione.trim { it <= ' ' }
        return 0
    }

    /*aggiunge una nuova canto alla posizione indicata
     ritorna -1 se il nome inserito è nullo o vuoto
     ritorna -2 se l'indice non è valido
     */
    fun addCanto(titoloCanto: String?, posizione: Int): Int {
        if (titoloCanto == null || titoloCanto.trim { it <= ' ' }.equals("", ignoreCase = true))
            return -1

        if (posizione >= MAX_POSIZIONI || posizione >= numPosizioni)
            return -2

        canti!![posizione] = titoloCanto.trim { it <= ' ' }
        return 0
    }

    //rimuove il canto alla posizione indicata
    fun removeCanto(posizione: Int): Int {
        if (posizione < 0 || posizione >= MAX_POSIZIONI)
            return -1

        canti!![posizione] = ""
        return 0
    }

    // rimuove la posizione all'indice "index"
    fun removePosizione(index: Int): Int {
        if (index < 0 || index >= MAX_POSIZIONI)
            return -1

        val newPosizioni = arrayOfNulls<String>(20)
        System.arraycopy(posizioni!!, 0, newPosizioni, 0, index)
        System.arraycopy(posizioni!!, index + 1, newPosizioni, index, MAX_POSIZIONI - index - 1)
        posizioni = newPosizioni

        val newCanti = arrayOfNulls<String>(20)
        System.arraycopy(canti!!, 0, newCanti, 0, index)
        System.arraycopy(canti!!, index + 1, newCanti, index, MAX_POSIZIONI - index - 1)
        canti = newCanti

        return 0
    }

    companion object {

        private val MAX_POSIZIONI = 30
        internal const val serialVersionUID = 123456789L

        fun serializeObject(o: Any): ByteArray? {
            val bos = ByteArrayOutputStream()

            return try {
                val out = ObjectOutputStream(bos)
                out.writeObject(o)
                out.close()
                // Get the bytes of the serialized object
                bos.toByteArray()
            } catch (ioe: IOException) {
                Log.e("serializeObject", "error", ioe)
                null
            }

        }

        fun deserializeObject(b: ByteArray): Any? {
            return try {
                val `in` = ObjectInputStream(ByteArrayInputStream(b))
                val `object` = `in`.readObject()
                `in`.close()
                `object`
            } catch (cnfe: ClassNotFoundException) {
                Log.e("deserializeObject", "class not found error", cnfe)
                null
            } catch (ioe: IOException) {
                Log.e("deserializeObject", "io error", ioe)
                null
            }

        }
    }
}
