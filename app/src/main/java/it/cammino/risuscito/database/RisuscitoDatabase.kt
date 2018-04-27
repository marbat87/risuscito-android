package it.cammino.risuscito.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import it.cammino.risuscito.DatabaseCanti
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.database.dao.*
import it.cammino.risuscito.database.entities.*
import java.sql.Date
import java.sql.Timestamp

@Database(entities = [(Canto::class), (ListaPers::class), (CustomList::class), (Argomento::class), (NomeArgomento::class), (Salmo::class), (IndiceLiturgico::class), (NomeLiturgico::class), (Cronologia::class), (Consegnato::class), (LocalLink::class)], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RisuscitoDatabase : RoomDatabase() {

    abstract fun cantoDao(): CantoDao

    abstract fun favoritesDao(): FavoritesDao

    abstract fun listePersDao(): ListePersDao

    abstract fun customListDao(): CustomListDao

    abstract fun argomentiDao(): ArgomentiDao

    abstract fun salmiDao(): SalmiDao

    abstract fun indiceLiturgicoDao(): IndiceLiturgicoDao

    abstract fun cronologiaDao(): CronologiaDao

    abstract fun consegnatiDao(): ConsegnatiDao

    abstract fun localLinksDao(): LocalLinksDao

    private fun truncateCompleteDB() {
        cantoDao().truncateTable()
        argomentiDao().truncateArgomento()
        argomentiDao().truncateNomeArgomento()
        indiceLiturgicoDao().truncateIndiceLiturgico()
        indiceLiturgicoDao().truncateNomeIndiceLiturgico()
        salmiDao().truncateTable()
        customListDao().truncateTable()
        listePersDao().truncateTable()
        localLinksDao().truncateTable()
        consegnatiDao().truncateTable()
        cronologiaDao().truncateTable()
    }

    private fun truncatePartialDB() {
        cantoDao().truncateTable()
        argomentiDao().truncateArgomento()
        argomentiDao().truncateNomeArgomento()
        indiceLiturgicoDao().truncateIndiceLiturgico()
        indiceLiturgicoDao().truncateNomeIndiceLiturgico()
        salmiDao().truncateTable()
    }

    fun importFromOldDB(mContext: Context) {
        truncateCompleteDB()
        populateInitialDataFromOldDB(sInstance, mContext)
    }

    fun recreateDB() {
        val backupCanti = cantoDao().backup
        truncatePartialDB()
        populateInitialData(sInstance)
        // reinserisce il backup
        for (backupCanto in backupCanti)
            cantoDao()
                    .setBackup(
                            backupCanto.id,
                            backupCanto.zoom,
                            backupCanto.scrollX,
                            backupCanto.scrollY,
                            backupCanto.favorite,
                            backupCanto.savedTab,
                            backupCanto.savedBarre,
                            backupCanto.savedSpeed)
        // cancella dalle liste predefinite i canti inesistenti
        val customLists = customListDao().all
        for (position in customLists) {
            val canto = cantoDao().getCantoById(position.idCanto)
            @Suppress("UNNECESSARY_SAFE_CALL")
            if (canto?.id == 0) {
                customListDao().deletePosition(position)
            }
        }
    }

    private class PopulateDbAsync : AsyncTask<Any, Void, Void>() {
        override fun doInBackground(vararg params: Any): Void? {
            populateInitialDataFromOldDB(params[0] as RisuscitoDatabase, params[1] as Context)
            return null
        }
    }

    companion object {

        private const val TAG = "RisuscitoDatabase"

        const val dbName = "RisuscitoDB"

        // For Singleton instantiation
        private val LOCK = Any()

        private val MIGRATION_2_3 = Migration2to3()

        class Migration2to3 : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i(TAG, "migrate 2 to 3")
                reinsertDefault(database)
            }
        }

        private val MIGRATION_1_3 = Migration1to3()

        class Migration1to3 : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i(TAG, "migrate 1 to 3")
                reinsertDefault(database)
            }
        }


        private fun reinsertDefault(database: SupportSQLiteDatabase) {
            Log.i(TAG, "migrate")

            // 1. backup table
            val backup = ArrayList<Backup>()
            val sql = "SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM Canto"
            val cursor = database.query(sql)
            cursor.moveToFirst()
            for (i in 0 until cursor.count) {
                backup.add(Backup(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7)))
                cursor.moveToNext()
            }
            cursor.close()

            // 2. DROP table and RECREATE
            database.execSQL("DROP TABLE Canto")
            database.execSQL("CREATE TABLE Canto (id INTEGER NOT NULL DEFAULT 0, pagina TEXT, titolo TEXT, source TEXT, favorite INTEGER NOT NULL DEFAULT 0, color TEXT, link TEXT, zoom INTEGER NOT NULL DEFAULT 0, scrollX INTEGER NOT NULL DEFAULT 0, scrollY INTEGER NOT NULL DEFAULT 0, savedTab TEXT, savedBarre TEXT, savedSpeed TEXT, PRIMARY KEY(id))")

            //3. Prepopulate new table
            Canto.defaultCantoData().forEach { database.execSQL("INSERT INTO Canto VALUES(" + it.id + ",'" + it.pagina + "','" + it.titolo + "','" + it.source + "'," + it.favorite + ",'" + it.color + "','" + it.link + "'," + it.zoom + "," + it.scrollX + "," + it.scrollY + ",null,null,'" + it.savedSpeed + "')") }

            //4. Restore backup
            backup.forEach { database.execSQL("UPDATE Canto set zoom=" + it.zoom + ",scrollX=" + it.scrollX + ",scrollY=" + it.scrollY + ",favorite=" + it.favourite + ",savedTab=" + if (it.nota != null) "'" + it.nota + "'" else "null" + ",savedBarre=" + if (it.barre != null) "'" + it.barre + "'" else "null" + ",savedSpeed='" + it.speed + "' WHERE id=" + it.id) }

            // 5. Empty table SALMO
            database.execSQL("DELETE FROM Salmo")

            //6. Prepopulate new table SALMO
            Salmo.defaultSalmiData().forEach { database.execSQL("INSERT INTO Salmo VALUES(" + it.id + ",'" + it.numSalmo + "','" + it.titoloSalmo + "')") }

            // 7. Empty table NomeLiturgico
            database.execSQL("DELETE FROM NomeLiturgico")

            //8. Prepopulate new table NomeLiturgico
            NomeLiturgico.defaultData().forEach { database.execSQL("INSERT INTO NomeLiturgico VALUES(" + it.idIndice + ",'" + it.nome + "')") }

            // 9. Empty table NomeArgomento
            database.execSQL("DELETE FROM NomeArgomento")

            //10. Prepopulate new table NomeArgomento
            NomeArgomento.defaultNomeArgData().forEach { database.execSQL("INSERT INTO NomeArgomento VALUES(" + it.idArgomento + ",'" + it.nomeArgomento + "')") }

            //11. DROP table and RECREATE Cronologia
            database.execSQL("CREATE TABLE Cronologia_new (idCanto INTEGER NOT NULL DEFAULT 0, ultimaVisita INTEGER NOT NULL, PRIMARY KEY(idCanto))")
            database.execSQL("INSERT INTO Cronologia_new SELECT * from Cronologia")
            database.execSQL("DROP TABLE Cronologia")
            database.execSQL("ALTER TABLE Cronologia_new RENAME TO Cronologia")

        }

        /** The only instance  */
        private var sInstance: RisuscitoDatabase? = null

        /**
         * Gets the singleton instance of RisuscitoDatabase.
         *
         * @param context The context.
         * @return The singleton instance of RisuscitoDatabase.
         */
        @Synchronized
        fun getInstance(context: Context): RisuscitoDatabase {
            Log.d(TAG, "getInstance: ")
            if (sInstance == null) {
                Log.d(TAG, "getInstance: NULL")
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, RisuscitoDatabase::class.java, dbName)
                            .addMigrations(MIGRATION_1_3)
                            .addMigrations(MIGRATION_2_3)
                            .addCallback(object : Callback() {
                                /**
                                 * Called when the database is created for the first time. This is called after all the
                                 * tables are created.
                                 *
                                 * @param db The database.
                                 */
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Log.d(TAG, "onCreate")
                                    populateAsync(sInstance as RisuscitoDatabase, context)
                                }
                            })
                            .build()
//                    populateAsync(sInstance as RisuscitoDatabase, context)
                }
            }
            Log.d(TAG, "getInstance: EXISTS")
            return sInstance as RisuscitoDatabase
        }

        /** Inserts the dummy data into the database if it is currently empty.  */
        private fun populateInitialDataFromOldDB(mDb: RisuscitoDatabase?, context: Context) {
            Log.d(TAG, "populateInitialData: " + mDb!!.cantoDao().count())
            if (mDb.cantoDao().count() == 0) {
                mDb.beginTransaction()
                try {

                    //1. POPOLO I DATI DI DEFAULT
                    populateInitialData(mDb)

                    //2. RIPRISTINO BACKUP CANTI
                    val listaCanti = DatabaseCanti(context)
                    val db = listaCanti.readableDatabase

                    val columns = arrayOf("_id", "zoom", "scroll_x", "scroll_y", "favourite", "saved_tab", "saved_barre", "saved_speed")
                    var result = db.query("ELENCO", columns, null, null, null, null, null)
                    result.moveToFirst()
                    for (i in 0 until result.count) {
                        mDb.cantoDao()
                                .setBackup(
                                        result.getInt(0),
                                        result.getInt(1),
                                        result.getInt(2),
                                        result.getInt(3),
                                        result.getInt(4),
                                        result.getString(5),
                                        result.getString(6),
                                        result.getString(7))
                        result.moveToNext()
                    }
                    result.close()

                    //3. Riprisitino CUST_LISTS
                    val columns7 = arrayOf("_id", "position", "id_canto", "timestamp")

                    result = db.query("CUST_LISTS", columns7, null, null, null, null, null)
                    result.moveToFirst()

                    while (!result.isAfterLast) {
                        Log.d(TAG, "populateInitialData - CUST_LISTS id:  " + result.getInt(0))
                        val customList = CustomList()
                        customList.id = result.getInt(0)
                        customList.position = result.getInt(1)
                        customList.idCanto = result.getInt(2)
                        //          customList.timestamp = new Date(Long.parseLong(result.getString(3)));
                        customList.timestamp = Date(Timestamp.valueOf(result.getString(3)).time)
                        mDb.customListDao().insertPosition(customList)
                        result.moveToNext()
                    }
                    result.close()

                    //4. Ripristino LISTE_PERS
                    val columns8 = arrayOf("titolo_lista", "lista")

                    result = db.query("LISTE_PERS", columns8, null, null, null, null, null)
                    result.moveToFirst()

                    while (!result.isAfterLast) {
                        Log.d(TAG, "populateInitialData - LISTE_PERS id:  " + result.getString(0))
                        val listaPers = ListaPers()
                        listaPers.titolo = result.getString(0)
                        listaPers.lista = ListaPersonalizzata.deserializeObject(result.getBlob(1)) as ListaPersonalizzata?
                        mDb.listePersDao().insertLista(listaPers)
                        result.moveToNext()
                    }
                    result.close()

                    //5. Ripristino LOCAL_LINKS
                    val columns9 = arrayOf("_id", "local_path")

                    result = db.query("LOCAL_LINKS", columns9, null, null, null, null, null)
                    result.moveToFirst()

                    val localLinkList = ArrayList<LocalLink>()

                    while (!result.isAfterLast) {
                        Log.d(TAG, "populateInitialData - LOCAL_LINKS id:  " + result.getInt(0))
                        val localLink = LocalLink()
                        localLink.idCanto = result.getInt(0)
                        localLink.localPath = result.getString(1)
                        localLinkList.add(localLink)
                        result.moveToNext()
                    }
                    result.close()
                    mDb.localLinksDao().insertLocalLink(localLinkList)

                    //6. Ripristino CANTI_CONSEGNATI
                    val columns10 = arrayOf("_id", "id_canto")

                    result = db.query("CANTI_CONSEGNATI", columns10, null, null, null, null, null)
                    result.moveToFirst()

                    val consegnatoList = ArrayList<Consegnato>()

                    while (!result.isAfterLast) {
                        Log.d(TAG, "populateInitialData - CANTI_CONSEGNATI id:  " + result.getInt(0))
                        val consegnato = Consegnato()
                        consegnato.idConsegnato = result.getInt(0)
                        consegnato.idCanto = result.getInt(1)
                        consegnatoList.add(consegnato)
                        result.moveToNext()
                    }
                    result.close()
                    mDb.consegnatiDao().insertConsegnati(consegnatoList)

                    //7. Ripristino CRONOLOGIA
                    val columns11 = arrayOf("id_canto", "ultima_visita")

                    result = db.query("CRONOLOGIA", columns11, null, null, null, null, null)
                    result.moveToFirst()

                    while (!result.isAfterLast) {
                        Log.d(TAG, "populateInitialData - CRONOLOGIA id:  " + result.getInt(0))
                        val cronologia = Cronologia()
                        cronologia.idCanto = result.getInt(0)
                        //          cronologia.ultimaVisita = new Date(Long.parseLong(result.getString(1)));
                        cronologia.ultimaVisita = Date(Timestamp.valueOf(result.getString(1)).time)
                        mDb.cronologiaDao().insertCronologia(cronologia)
                        result.moveToNext()
                    }
                    result.close()

                    db.close()
                    listaCanti.close()
                    mDb.setTransactionSuccessful()
                } finally {
                    mDb.endTransaction()
                }
            }
        }

        /** Inserts the dummy data into the database if it is currently empty.  */
        private fun populateInitialData(mDb: RisuscitoDatabase?) {
            Log.d(TAG, "populateInitialData: " + mDb!!.cantoDao().count())
            if (mDb.cantoDao().count() == 0) {
                mDb.beginTransaction()
                try {

                    mDb.cantoDao().insertCanto(Canto.defaultCantoData())

                    // ARGOMENTI
                    mDb.argomentiDao().insertArgomento(Argomento.defaultArgData())

                    // ARG_NAMES
                    mDb.argomentiDao().insertNomeArgomento(NomeArgomento.defaultNomeArgData())

                    // SALMI_MUSICA
                    mDb.salmiDao().insertSalmo(Salmo.defaultSalmiData())

                    // INDICE_LIT
                    mDb.indiceLiturgicoDao().insertIndice(IndiceLiturgico.defaultData())

                    // INDICE_LIT_NAMES
                    mDb.indiceLiturgicoDao().insertNomeIndice(NomeLiturgico.defaultData())

                    mDb.setTransactionSuccessful()
                } finally {
                    mDb.endTransaction()
                }
            }
        }

        /** Inserts the dummy data into the database if it is currently empty.  */
//        private fun repopulateFixedData(mDb: RisuscitoDatabase?, context: Context) {
//            Log.d(TAG, "repopulateFixedData: " + mDb!!.cantoDao().count())
//            if (mDb.cantoDao().count() == 0) {
//                mDb.beginTransaction()
//                try {
//
//                    val listaCanti = DatabaseCanti(context)
//                    val db = listaCanti.readableDatabase
//
//                    val columns = arrayOf("_id", "pagina", "titolo", "source", "favourite", "color", "link", "zoom", "scroll_x", "scroll_y", "saved_tab", "saved_barre", "saved_speed")
//
//                    var result = db.query("ELENCO", columns, null, null, null, null, null)
//                    result.moveToFirst()
//
//                    val elenco = ArrayList<Canto>()
//
//                    while (!result.isAfterLast) {
//                        Log.d(TAG, "repopulateFixedData - ELENCO id:  " + result.getInt(0))
//                        val canto = Canto()
//                        canto.id = result.getInt(0)
//                        canto.pagina = result.getInt(1)
//                        canto.titolo = result.getString(2)
//                        canto.source = result.getString(3)
//                        canto.favorite = result.getInt(4)
//                        canto.color = result.getString(5)
//                        canto.link = result.getString(6)
//                        canto.zoom = result.getInt(7)
//                        canto.scrollX = result.getInt(8)
//                        canto.scrollY = result.getInt(9)
//                        canto.savedTab = result.getString(10)
//                        canto.savedBarre = result.getString(11)
//                        canto.savedSpeed = result.getString(12)
//                        elenco.add(canto)
//                        result.moveToNext()
//                    }
//                    result.close()
//                    mDb.cantoDao().insertCanto(elenco)
//
//                    // ARGOMENTI
//                    val columns2 = arrayOf("_id", "id_canto")
//
//                    result = db.query("ARGOMENTI", columns2, null, null, null, null, null)
//                    result.moveToFirst()
//
//                    val argomenti = ArrayList<Argomento>()
//
//                    while (!result.isAfterLast) {
//                        Log.d(TAG, "repopulateFixedData - ARGOMENTI id:  " + result.getInt(0))
//                        val argomento = Argomento()
//                        argomento.idArgomento = result.getInt(0)
//                        argomento.idCanto = result.getInt(1)
//                        argomenti.add(argomento)
//                        result.moveToNext()
//                    }
//                    result.close()
//                    mDb.argomentiDao().insertArgomento(argomenti)
//
//                    // ARG_NAMES
//                    val columns3 = arrayOf("_id", "nome")
//
//                    result = db.query("ARG_NAMES", columns3, null, null, null, null, null)
//                    result.moveToFirst()
//
//                    val nomiArgomenti = ArrayList<NomeArgomento>()
//
//                    while (!result.isAfterLast) {
//                        Log.d(TAG, "repopulateFixedData - ARG_NAMES id:  " + result.getInt(0))
//                        val argomento = NomeArgomento()
//                        argomento.idArgomento = result.getInt(0)
//                        argomento.nomeArgomento = result.getString(1)
//                        nomiArgomenti.add(argomento)
//                        result.moveToNext()
//                    }
//                    result.close()
//                    mDb.argomentiDao().insertNomeArgomento(nomiArgomenti)
//
//                    // SALMI_MUSICA
//                    val columns4 = arrayOf("_id", "num_salmo", "titolo_salmo")
//
//                    result = db.query("SALMI_MUSICA", columns4, null, null, null, null, null)
//                    result.moveToFirst()
//
//                    val salmi = ArrayList<Salmo>()
//
//                    while (!result.isAfterLast) {
//                        Log.d(TAG, "repopulateFixedData - SALMI_MUSICA id:  " + result.getInt(0))
//                        val salmo = Salmo()
//                        salmo.id = result.getInt(0)
//                        salmo.numSalmo = result.getString(1)
//                        salmo.titoloSalmo = result.getString(2)
//                        salmi.add(salmo)
//                        result.moveToNext()
//                    }
//                    result.close()
//                    mDb.salmiDao().insertSalmo(salmi)
//
//                    // INDICE_LIT
//                    val columns5 = arrayOf("_id", "id_canto")
//
//                    result = db.query("INDICE_LIT", columns5, null, null, null, null, null)
//                    result.moveToFirst()
//
//                    val indiceLit = ArrayList<IndiceLiturgico>()
//
//                    while (!result.isAfterLast) {
//                        Log.d(TAG, "repopulateFixedData - INDICE_LIT id:  " + result.getInt(0))
//                        val indice = IndiceLiturgico()
//                        indice.idIndice = result.getInt(0)
//                        indice.idCanto = result.getInt(1)
//                        indiceLit.add(indice)
//                        result.moveToNext()
//                    }
//                    result.close()
//                    mDb.indiceLiturgicoDao().insertIndice(indiceLit)
//
//                    // INDICE_LIT_NAMES
//                    val columns6 = arrayOf("_id", "nome")
//
//                    result = db.query("INDICE_LIT_NAMES", columns6, null, null, null, null, null)
//                    result.moveToFirst()
//
//                    val nomiLiturgici = ArrayList<NomeLiturgico>()
//
//                    while (!result.isAfterLast) {
//                        Log.d(TAG, "repopulateFixedData - INDICE_LIT_NAMES id:  " + result.getInt(0))
//                        val nomeIndice = NomeLiturgico()
//                        nomeIndice.idIndice = result.getInt(0)
//                        nomeIndice.nome = result.getString(1)
//                        nomiLiturgici.add(nomeIndice)
//                        result.moveToNext()
//                    }
//                    result.close()
//                    mDb.indiceLiturgicoDao().insertNomeIndice(nomiLiturgici)
//
//                    db.close()
//                    listaCanti.close()
//                    mDb.setTransactionSuccessful()
//                } finally {
//                    mDb.endTransaction()
//                }
//            }
//        }


        private fun populateAsync(db: RisuscitoDatabase, context: Context) {
            val task = PopulateDbAsync()
            task.execute(db, context)
        }

    }

    class Backup(val id: Int = 0, val zoom: Int = 0, val scrollX: Int = 0, val scrollY: Int = 0, val favourite: Int = 0, val nota: String?, val barre: String?, val speed: Int = 0)

}
