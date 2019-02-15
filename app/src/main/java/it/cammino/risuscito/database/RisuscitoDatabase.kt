package it.cammino.risuscito.database

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import it.cammino.risuscito.DatabaseCanti
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.database.dao.*
import it.cammino.risuscito.database.entities.*
import java.sql.Date
import java.sql.Timestamp

@Database(entities = [(Canto::class), (ListaPers::class), (CustomList::class), (Argomento::class), (NomeArgomento::class), (Salmo::class), (IndiceLiturgico::class), (NomeLiturgico::class), (Cronologia::class), (Consegnato::class), (LocalLink::class)], version = 4, exportSchema = false)
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

    @Transaction
    fun importFromOldDB(mContext: Context) {
        clearAllTables()
        Log.d(TAG, "importFromOldDB: " + cantoDao().count())
        if (cantoDao().count() == 0) {
            //1. POPOLO I DATI DI DEFAULT
            cantoDao().insertCanto(Canto.defaultCantoData())

            // ARGOMENTI
            argomentiDao().insertArgomento(Argomento.defaultArgData())

            // ARG_NAMES
            argomentiDao().insertNomeArgomento(NomeArgomento.defaultNomeArgData())

            // SALMI_MUSICA
            salmiDao().insertSalmo(Salmo.defaultSalmiData())

            // INDICE_LIT
            indiceLiturgicoDao().insertIndice(IndiceLiturgico.defaultData())

            // INDICE_LIT_NAMES
            indiceLiturgicoDao().insertNomeIndice(NomeLiturgico.defaultData())

            //2. RIPRISTINO BACKUP CANTI
            val listaCanti = DatabaseCanti(mContext)
            val db = listaCanti.readableDatabase

            val columns = arrayOf("_id", "zoom", "scroll_x", "scroll_y", "favourite", "saved_tab", "saved_barre", "saved_speed")
            var result = db.query("ELENCO", columns, null, null, null, null, null)
            result.moveToFirst()
            while (!result.isAfterLast) {
                cantoDao()
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
                Log.d(TAG, "importFromOldDB - CUST_LISTS id:  " + result.getInt(0))
                val customList = CustomList()
                customList.id = result.getInt(0)
                customList.position = result.getInt(1)
                customList.idCanto = result.getInt(2)
                //          customList.timestamp = new Date(Long.parseLong(result.getString(3)));
                customList.timestamp = Date(Timestamp.valueOf(result.getString(3)).time)
                customListDao().insertPosition(customList)
                result.moveToNext()
            }
            result.close()

            //4. Ripristino LISTE_PERS
            val columns8 = arrayOf("titolo_lista", "lista")

            result = db.query("LISTE_PERS", columns8, null, null, null, null, null)
            result.moveToFirst()

            while (!result.isAfterLast) {
                Log.d(TAG, "importFromOldDB - LISTE_PERS id:  " + result.getString(0))
                val listaPers = ListaPers()
                listaPers.titolo = result.getString(0)
                listaPers.lista = ListaPersonalizzata.deserializeObject(result.getBlob(1)) as ListaPersonalizzata?
                listePersDao().insertLista(listaPers)
                result.moveToNext()
            }
            result.close()

            //5. Ripristino LOCAL_LINKS
            val columns9 = arrayOf("_id", "local_path")

            result = db.query("LOCAL_LINKS", columns9, null, null, null, null, null)
            result.moveToFirst()

            val localLinkList = ArrayList<LocalLink>()

            while (!result.isAfterLast) {
                Log.d(TAG, "importFromOldDB - LOCAL_LINKS id:  " + result.getInt(0))
                val localLink = LocalLink()
                localLink.idCanto = result.getInt(0)
                localLink.localPath = result.getString(1)
                localLinkList.add(localLink)
                result.moveToNext()
            }
            result.close()
            localLinksDao().insertLocalLink(localLinkList)

            //6. Ripristino CANTI_CONSEGNATI
            val columns10 = arrayOf("_id", "id_canto")

            result = db.query("CANTI_CONSEGNATI", columns10, null, null, null, null, null)
            result.moveToFirst()

            val consegnatoList = ArrayList<Consegnato>()

            while (!result.isAfterLast) {
                Log.d(TAG, "importFromOldDB - CANTI_CONSEGNATI id:  " + result.getInt(0))
                val consegnato = Consegnato()
                consegnato.idConsegnato = result.getInt(0)
                consegnato.idCanto = result.getInt(1)
                consegnatoList.add(consegnato)
                result.moveToNext()
            }
            result.close()
            consegnatiDao().insertConsegnati(consegnatoList)

            //7. Ripristino CRONOLOGIA
            val columns11 = arrayOf("id_canto", "ultima_visita")

            result = db.query("CRONOLOGIA", columns11, null, null, null, null, null)
            result.moveToFirst()

            while (!result.isAfterLast) {
                Log.d(TAG, "importFromOldDB - CRONOLOGIA id:  " + result.getInt(0))
                val cronologia = Cronologia()
                cronologia.idCanto = result.getInt(0)
                cronologia.ultimaVisita = Date(Timestamp.valueOf(result.getString(1)).time)
                cronologiaDao().insertCronologia(cronologia)
                result.moveToNext()
            }
            result.close()

            db.close()
            listaCanti.close()
        }
    }

    @Transaction
    fun recreateDB() {
        Log.d(TAG, "recreateDB")
        val backupCanti = cantoDao().backup
        cantoDao().truncateTable()
        argomentiDao().truncateArgomento()
        argomentiDao().truncateNomeArgomento()
        indiceLiturgicoDao().truncateIndiceLiturgico()
        indiceLiturgicoDao().truncateNomeIndiceLiturgico()
        salmiDao().truncateTable()
        Log.d(TAG, "recreateDB - canti presenti:  " + cantoDao().count())
        Log.d(TAG, "liste pers presenti: " + listePersDao().all.size)
        if (cantoDao().count() == 0) {
            cantoDao().insertCanto(Canto.defaultCantoData())

            // ARGOMENTI
            argomentiDao().insertArgomento(Argomento.defaultArgData())

            // ARG_NAMES
            argomentiDao().insertNomeArgomento(NomeArgomento.defaultNomeArgData())

            // SALMI_MUSICA
            salmiDao().insertSalmo(Salmo.defaultSalmiData())

            // INDICE_LIT
            indiceLiturgicoDao().insertIndice(IndiceLiturgico.defaultData())

            // INDICE_LIT_NAMES
            indiceLiturgicoDao().insertNomeIndice(NomeLiturgico.defaultData())
        }

        // reinserisce il backup
        for (backupCanto in backupCanti) {
            Log.d(TAG, "backupCanto.id + ${backupCanto.id} / backupCanto.savedTab ${backupCanto.savedTab} / backupCanto.savedBarre ${backupCanto.savedBarre}")
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
        }

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
        @Transaction
        override fun doInBackground(vararg params: Any): Void? {
            val mDb = params[0] as RisuscitoDatabase
            val mContext = params[1] as Context
            Log.d(TAG, "PopulateDbAsync: " + mDb.cantoDao().count())
            if (mDb.cantoDao().count() == 0) {
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

                //2. RIPRISTINO BACKUP CANTI
                val listaCanti = DatabaseCanti(mContext)
                val db = listaCanti.readableDatabase

                val columns = arrayOf("_id", "zoom", "scroll_x", "scroll_y", "favourite", "saved_tab", "saved_barre", "saved_speed")
                var result = db.query("ELENCO", columns, null, null, null, null, null)
                result.moveToFirst()
                while (!result.isAfterLast) {
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
                    Log.d(TAG, "PopulateDbAsync - CUST_LISTS id:  " + result.getInt(0))
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
                    Log.d(TAG, "PopulateDbAsync - LISTE_PERS id:  " + result.getString(0))
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
                    Log.d(TAG, "PopulateDbAsync - LOCAL_LINKS id:  " + result.getInt(0))
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
                    Log.d(TAG, "PopulateDbAsync - CANTI_CONSEGNATI id:  " + result.getInt(0))
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
                    Log.d(TAG, "PopulateDbAsync - CRONOLOGIA id:  " + result.getInt(0))
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
            }
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
                Log.d(TAG, "migrate 2 to 3")
                reinsertDefault(database)
            }
        }

        private val MIGRATION_1_3 = Migration1to3()

        class Migration1to3 : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 1 to 3")
                reinsertDefault(database)
            }
        }

        private val MIGRATION_3_4 = Migration3to4()

        class Migration3to4 : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 3 to 4")

                val backup = ArrayList<CantoDao.Backup>()
                val sql = "SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM Canto ORDER by 1"
                val cursor = database.query(sql)
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    backup.add(CantoDao.Backup(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)))
                    cursor.moveToNext()
                }
                cursor.close()

                // 2. EMPTY table
                database.execSQL("DELETE FROM Canto")

                //3. Prepopulate new table
                Canto.defaultCantoData().forEach {
                    database.execSQL("INSERT INTO Canto (id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedSpeed) " +
                            "VALUES(" + it.id + ",'" + it.pagina + "','" + it.titolo + "','" + it.source + "'," + it.favorite + ",'" + it.color + "','" + it.link + "'," + it.zoom + "," + it.scrollX + "," + it.scrollY + ",'" + it.savedSpeed + "')")
                }

                //4. Restore backup
                backup.forEach {
                    database.execSQL("UPDATE Canto set zoom=" + it.zoom + ", scrollX=" + it.scrollX + ", scrollY=" + it.scrollY + ", favorite=" + it.favorite +
                            ", savedTab=" + (if (it.savedTab != null) "'" + it.savedTab + "'" else "null") +
                            ", savedBarre=" + (if (it.savedBarre != null) "'" + it.savedBarre + "'" else "null") +
                            ",savedSpeed='" + it.savedSpeed + "' WHERE id=" + it.id)
                }

            }
        }

        private fun reinsertDefault(database: SupportSQLiteDatabase) {
            Log.d(TAG, "reinsertDefault")

            // 1. backup table
            val backup = ArrayList<CantoDao.Backup>()
            val sql = "SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM Canto"
            val cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                backup.add(CantoDao.Backup(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)))
                cursor.moveToNext()
            }
            cursor.close()

            // 2. DROP table and RECREATE
            database.execSQL("DROP TABLE Canto")
            database.execSQL("CREATE TABLE Canto (id INTEGER NOT NULL DEFAULT 0, pagina TEXT, titolo TEXT, source TEXT, favorite INTEGER NOT NULL DEFAULT 0, color TEXT, link TEXT, zoom INTEGER NOT NULL DEFAULT 0, scrollX INTEGER NOT NULL DEFAULT 0, scrollY INTEGER NOT NULL DEFAULT 0, savedTab TEXT, savedBarre TEXT, savedSpeed TEXT, PRIMARY KEY(id))")

            //3. Prepopulate new table
            Canto.defaultCantoData().forEach {
                database.execSQL("INSERT INTO Canto (id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedSpeed) " +
                        "VALUES(" + it.id + ",'" + it.pagina + "','" + it.titolo + "','" + it.source + "'," + it.favorite + ",'" + it.color + "','" + it.link + "'," + it.zoom + "," + it.scrollX + "," + it.scrollY + ",'" + it.savedSpeed + "')")
            }

            //4. Restore backup
            backup.forEach {
                database.execSQL("UPDATE Canto set zoom=" + it.zoom + ", scrollX=" + it.scrollX + ", scrollY=" + it.scrollY + ", favorite=" + it.favorite +
                        ", savedTab=" + (if (it.savedTab != null) "'" + it.savedTab + "'" else "null") +
                        ", savedBarre=" + (if (it.savedBarre != null) "'" + it.savedBarre + "'" else "null") +
                        ",savedSpeed='" + it.savedSpeed + "' WHERE id=" + it.id)
            }

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

        fun resetInstance() {
            sInstance = null
        }

        /**
         * Gets the singleton instance of RisuscitoDatabase.
         *
         * @param context The context.
         * @return The singleton instance of RisuscitoDatabase.
         */
        @Synchronized
        fun getInstance(context: Context): RisuscitoDatabase {
            Log.d(TAG, "getInstance()")
            if (sInstance == null) {
                Log.d(TAG, "getInstance: NULL")
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, RisuscitoDatabase::class.java, dbName)
                            .addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4)
//                            .addMigrations(MIGRATION_2_3)
//                            .addMigrations(MIGRATION_3_4)
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
                }
            } else
                Log.d(TAG, "getInstance: EXISTS")
            return sInstance as RisuscitoDatabase
        }

        private fun populateAsync(db: RisuscitoDatabase, context: Context) {
            val task = PopulateDbAsync()
            task.execute(db, context)
        }

    }

}
