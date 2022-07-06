package it.cammino.risuscito

import android.util.Log
import it.cammino.risuscito.utils.StringUtils
import java.io.*

@Suppress("unused")
class ListaPersonalizzata : Serializable {

    var name: String = StringUtils.EMPTY
    private var posizioni: Array<String?> = arrayOfNulls(MAX_POSIZIONI)
    var canti: Array<String?> = arrayOfNulls(MAX_POSIZIONI)
    var numPosizioni: Int = 0

    init {
        for (i in 0 until MAX_POSIZIONI) {
            posizioni[i] = StringUtils.EMPTY
            canti[i] = StringUtils.EMPTY
        }
    }

    //restituisce il titolo della posizione all'indice "index"
    fun getNomePosizione(index: Int): String {
        return if (index < 0 || index >= numPosizioni) StringUtils.EMPTY else posizioni[index].orEmpty()
            .trim()

    }

    //restituisce il titolo della canto in posizione "index"
    fun getCantoPosizione(index: Int): String {
        if (index < 0 || index >= numPosizioni)
            return StringUtils.EMPTY
        return canti[index].orEmpty().trim()
    }

    /*aggiunge una nuova posizione
     ritorna -1 se il nome inserito è nullo o vuoto
     ritorna -2 se la lista è piena
     */
    fun addPosizione(nomePosizione: String?): Int {
        if (nomePosizione.isNullOrBlank())
            return -1

        if (numPosizioni == MAX_POSIZIONI)
            return -2

        posizioni[numPosizioni++] = nomePosizione.trim()
        return 0
    }

    /*aggiunge una nuova canto alla posizione indicata
     ritorna -1 se il nome inserito è nullo o vuoto
     ritorna -2 se l'indice non è valido
     */
    fun addCanto(titoloCanto: String?, posizione: Int): Int {
        if (titoloCanto.isNullOrBlank())
            return -1

        if (posizione >= MAX_POSIZIONI || posizione >= numPosizioni)
            return -2

        canti[posizione] = titoloCanto.trim()
        return 0
    }

    //rimuove il canto alla posizione indicata
    fun removeCanto(posizione: Int): Int {
        if (posizione < 0 || posizione >= MAX_POSIZIONI)
            return -1

        canti[posizione] = StringUtils.EMPTY
        return 0
    }

    override fun toString(): String {
        val mOutput = StringBuilder("CELEBRAZIONE\nTITOLO: $name\nPOSIZIONI:")
        for (i in 0 until numPosizioni) {
            mOutput.append("\n $i) ${getNomePosizione(i)} / ${getCantoPosizione(i)} ")
        }
        return mOutput.toString()
    }

    // rimuove la posizione all'indice "index"
    fun removePosizione(index: Int): Int {
        if (index < 0 || index >= MAX_POSIZIONI)
            return -1

        val newPosizioni = arrayOfNulls<String>(20)
        System.arraycopy(posizioni, 0, newPosizioni, 0, index)
        System.arraycopy(posizioni, index + 1, newPosizioni, index, MAX_POSIZIONI - index - 1)
        posizioni = newPosizioni

        val newCanti = arrayOfNulls<String>(20)
        System.arraycopy(canti, 0, newCanti, 0, index)
        System.arraycopy(canti, index + 1, newCanti, index, MAX_POSIZIONI - index - 1)
        canti = newCanti

        return 0
    }

    companion object {

        private const val MAX_POSIZIONI = 30
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
                val inputStream = ObjectInputStream(ByteArrayInputStream(b))
                val inputObject = inputStream.readObject()
                inputStream.close()
                inputObject
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
