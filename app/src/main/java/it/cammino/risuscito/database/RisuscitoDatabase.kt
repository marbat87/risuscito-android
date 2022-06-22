package it.cammino.risuscito.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import it.cammino.risuscito.database.dao.*
import it.cammino.risuscito.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [(Canto::class), (ListaPers::class), (CustomList::class), (IndiceLiturgico::class), (NomeLiturgico::class), (Cronologia::class), (Consegnato::class), (LocalLink::class), (IndiceBiblico::class)],
    version = 10
)
@TypeConverters(Converters::class)
abstract class RisuscitoDatabase : RoomDatabase() {

    abstract fun cantoDao(): CantoDao

    abstract fun favoritesDao(): FavoritesDao

    abstract fun listePersDao(): ListePersDao

    abstract fun customListDao(): CustomListDao

    abstract fun indiceLiturgicoDao(): IndiceLiturgicoDao

    abstract fun cronologiaDao(): CronologiaDao

    abstract fun consegnatiDao(): ConsegnatiDao

    abstract fun localLinksDao(): LocalLinksDao

    abstract fun indiceBiblicoDao(): IndiceBiblicoDao

    companion object {

        private const val TAG = "RisuscitoDatabase"

        private const val dbName = "RisuscitoDB"

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
                reinsertDefaultOnlyCanti(database)
            }
        }

        private val MIGRATION_4_5 = Migration4to5()

        class Migration4to5 : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 4 to 5")
                reinsertDefaultOnlyCanti(database)
            }
        }

        private val MIGRATION_5_6 = Migration5to6()

        class Migration5to6 : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 5 to 6")
                database.execSQL("ALTER TABLE Consegnato ADD COLUMN txtNota TEXT NOT NULL DEFAULT \"\"")
            }
        }

        private val MIGRATION_6_7 = Migration6to7()

        class Migration6to7 : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 6 to 7")
                database.execSQL("ALTER TABLE Consegnato ADD COLUMN numPassaggio INTEGER NOT NULL DEFAULT -1")
            }
        }

        private val MIGRATION_7_8 = Migration7to8()

        class Migration7to8 : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 7 to 8")
//                reinsertDefaultOnlySalmi(database)
            }
        }

        private val MIGRATION_8_9 = Migration8to9()

        class Migration8to9 : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 8 to 9")
                database.execSQL("DROP TABLE IF EXISTS NomeArgomento")
                database.execSQL("DROP TABLE IF EXISTS Argomento")
                database.execSQL("DROP TABLE IF EXISTS salmo")
                database.execSQL("CREATE TABLE IF NOT EXISTS `IndiceBiblico` (`ordinamento` INTEGER NOT NULL, `idCanto` INTEGER NOT NULL, `titoloIndice` TEXT, PRIMARY KEY(`ordinamento`))")
                reinsertDefault(database)
            }
        }

        private val MIGRATION_9_10 = Migration9to10()

        class Migration9to10 : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate 9 to 10")
                database.execSQL("DELETE FROM IndiceLiturgico")

                //8. Prepopulate new table NomeLiturgico
                IndiceLiturgico.defaultData()
                    .forEach {
                        val insertValue = ContentValues()
                        insertValue.put("idIndice", it.idIndice)
                        insertValue.put("idCanto", it.idCanto)
                        database.insert("IndiceLiturgico", OnConflictStrategy.REPLACE, insertValue)
                    }
            }
        }

        private fun reinsertDefault(database: SupportSQLiteDatabase) {
            Log.d(TAG, "reinsertDefault")

            // 1. backup table
            val backup = ArrayList<Backup>()
            val sql =
                "SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM Canto"
            val cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                backup.add(
                    Backup(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7)
                    )
                )
                cursor.moveToNext()
            }
            cursor.close()

            // 2. DROP table and RECREATE
            database.execSQL("DROP TABLE Canto")
            database.execSQL("CREATE TABLE Canto (id INTEGER NOT NULL DEFAULT 0, pagina TEXT, titolo TEXT, source TEXT, favorite INTEGER NOT NULL DEFAULT 0, color TEXT, link TEXT, zoom INTEGER NOT NULL DEFAULT 0, scrollX INTEGER NOT NULL DEFAULT 0, scrollY INTEGER NOT NULL DEFAULT 0, savedTab TEXT, savedBarre TEXT, savedSpeed TEXT, PRIMARY KEY(id))")

            //3. Prepopulate new table
            Canto.defaultCantoData().forEach {
                database.execSQL(
                    "INSERT INTO Canto (id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedSpeed) " +
                            "VALUES(" + it.id + ",'" + it.pagina + "','" + it.titolo + "','" + it.source + "'," + it.favorite + ",'" + it.color + "','" + it.link + "'," + it.zoom + "," + it.scrollX + "," + it.scrollY + ",'" + it.savedSpeed + "')"
                )
            }

            //4. Restore backup
            backup.forEach {
                database.execSQL(
                    "UPDATE Canto set zoom=" + it.zoom + ", scrollX=" + it.scrollX + ", scrollY=" + it.scrollY + ", favorite=" + it.favorite +
                            ", savedTab=" + (if (it.savedTab != null) "'" + it.savedTab + "'" else "null") +
                            ", savedBarre=" + (if (it.savedBarre != null) "'" + it.savedBarre + "'" else "null") +
                            ",savedSpeed='" + it.savedSpeed + "' WHERE id=" + it.id
                )
            }

            // 7. Empty table NomeLiturgico
            database.execSQL("DELETE FROM NomeLiturgico")

            //8. Prepopulate new table NomeLiturgico
            NomeLiturgico.defaultData()
                .forEach { database.execSQL("INSERT INTO NomeLiturgico VALUES(" + it.idIndice + ",'" + it.nome + "')") }

            //11. DROP table and RECREATE Cronologia
            database.execSQL("CREATE TABLE Cronologia_new (idCanto INTEGER NOT NULL DEFAULT 0, ultimaVisita INTEGER NOT NULL, PRIMARY KEY(idCanto))")
            database.execSQL("INSERT INTO Cronologia_new SELECT * from Cronologia")
            database.execSQL("DROP TABLE Cronologia")
            database.execSQL("ALTER TABLE Cronologia_new RENAME TO Cronologia")

            // 7. Empty table NomeLiturgico
            database.execSQL("DELETE FROM indicebiblico")

            //8. Prepopulate new table NomeLiturgico
            IndiceBiblico.defaultIndiceBiblicoData()
                .forEach { database.execSQL("INSERT INTO indicebiblico VALUES(${it.ordinamento},${it.idCanto}, '${it.titoloIndice}')") }

            cleanNonExistentSongs(database)

        }

        private fun reinsertDefaultOnlyCanti(database: SupportSQLiteDatabase) {
            val backup = ArrayList<Backup>()
            val sql =
                "SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM Canto ORDER by 1"
            val cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                backup.add(
                    Backup(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7)
                    )
                )
                cursor.moveToNext()
            }
            cursor.close()

            // 2. EMPTY table
            database.execSQL("DELETE FROM Canto")

            //3. Prepopulate new table
            Canto.defaultCantoData().forEach {
                database.execSQL(
                    "INSERT INTO Canto (id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedSpeed) " +
                            "VALUES(" + it.id + ",'" + it.pagina + "','" + it.titolo + "','" + it.source + "'," + it.favorite + ",'" + it.color + "','" + it.link + "'," + it.zoom + "," + it.scrollX + "," + it.scrollY + ",'" + it.savedSpeed + "')"
                )
            }

            //4. Restore backup
            backup.forEach {
                database.execSQL(
                    "UPDATE Canto set zoom=" + it.zoom + ", scrollX=" + it.scrollX + ", scrollY=" + it.scrollY + ", favorite=" + it.favorite +
                            ", savedTab=" + (if (it.savedTab != null) "'" + it.savedTab + "'" else "null") +
                            ", savedBarre=" + (if (it.savedBarre != null) "'" + it.savedBarre + "'" else "null") +
                            ",savedSpeed='" + it.savedSpeed + "' WHERE id=" + it.id
                )
            }
        }

//        private fun reinsertDefaultOnlySalmi(database: SupportSQLiteDatabase) {
//            database.execSQL("DELETE FROM Salmo")
//            Salmo.defaultSalmiData()
//                .forEach { database.execSQL("INSERT INTO Salmo VALUES(" + it.id + ",'" + it.numSalmo + "','" + it.titoloSalmo + "')") }
//        }

        private var sInstance: RisuscitoDatabase? = null

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
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        RisuscitoDatabase::class.java,
                        dbName
                    )
                        .addMigrations(
                            MIGRATION_1_3,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10
                        )
                        .addCallback(object : Callback() {
                            /**
                             * Called when the database is created for the first time. This is called after all the
                             * tables are created.
                             *
                             * @param db The database.
                             */
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                Log.d(TAG, "Callback onCreate")
                                GlobalScope.launch(Dispatchers.IO) { insertDefaultData(sInstance as RisuscitoDatabase) }
                            }
                        })
                        .build()
                }
            } else
                Log.d(TAG, "getInstance: EXISTS")
            return sInstance as RisuscitoDatabase
        }

        private fun insertDefaultData(mDb: RisuscitoDatabase) {
            Log.d(TAG, "insertDefaultData: ${mDb.cantoDao().count()}")
            if (mDb.cantoDao().count() == 0) {
                mDb.cantoDao().insertCanto(Canto.defaultCantoData())

                // INDICE_LIT
                mDb.indiceLiturgicoDao().insertIndice(IndiceLiturgico.defaultData())

                // INDICE_LIT_NAMES
                mDb.indiceLiturgicoDao().insertNomeIndice(NomeLiturgico.defaultData())

                mDb.indiceBiblicoDao().insertIndiceBiblico(IndiceBiblico.defaultIndiceBiblicoData())
            }
        }

        private fun cleanNonExistentSongs(database: SupportSQLiteDatabase) {
            val converter = Converters()

            //CUSTOMLIST
            val custList = ArrayList<CustomList>()
            var sql =
                "SELECT id, idCanto FROM customlist"
            var cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                custList.add(
                    CustomList().apply {
                        id = cursor.getInt(0)
                        idCanto = cursor.getInt(1)
                    }
                )
                cursor.moveToNext()
            }
            cursor.close()

            for (lista in custList) {
                if (!isCantoExistent(database, lista.idCanto))
                    database.execSQL("DELETE FROM customlist WHERE id = ${lista.id}")
            }

            //LOCALLINK
            val localinks = ArrayList<LocalLink>()
            sql =
                "SELECT idCanto FROM locallink"
            cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                localinks.add(
                    LocalLink().apply {
                        idCanto = cursor.getInt(0)
                    }
                )
                cursor.moveToNext()
            }
            cursor.close()

            for (link in localinks) {
                if (!isCantoExistent(database, link.idCanto))
                    database.execSQL("DELETE FROM locallink WHERE idCanto = ${link.idCanto}")
            }

            //CONSEGNATO
            val consegnati = ArrayList<Consegnato>()
            sql =
                "SELECT idConsegnato, idCanto from consegnato"
            cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                consegnati.add(
                    Consegnato().apply {
                        idConsegnato = cursor.getInt(0)
                        idCanto = cursor.getInt(1)
                    }
                )
                cursor.moveToNext()
            }
            cursor.close()

            for (consegnato in consegnati) {
                if (!isCantoExistent(database, consegnato.idCanto))
                    database.execSQL("DELETE FROM consegnato WHERE idConsegnato = ${consegnato.idConsegnato}")
            }

            //CRONOLOGIA
            val cronologia = ArrayList<Cronologia>()
            sql =
                "SELECT idCanto from cronologia"
            cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                cronologia.add(
                    Cronologia().apply {
                        idCanto = cursor.getInt(0)
                    }
                )
                cursor.moveToNext()
            }
            cursor.close()

            for (crono in cronologia) {
                if (!isCantoExistent(database, crono.idCanto))
                    database.execSQL("DELETE FROM cronologia WHERE idCanto = ${crono.idCanto}")
            }

            //CRONOLOGIA
            val listePers = ArrayList<ListaPers>()
            sql =
                "SELECT id, lista from listapers"
            cursor = database.query(sql)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                listePers.add(
                    ListaPers().apply {
                        id = cursor.getInt(0)
                        lista = converter.fromBlob(cursor.getBlob(1))
                    }
                )
                cursor.moveToNext()
            }
            cursor.close()

            for (listapers in listePers) {
                var update = false
                listapers.lista?.let {
                    for (i in 0 until it.numPosizioni) {
                        if (it.getCantoPosizione(i).isNotEmpty() && !isCantoExistent(
                                database,
                                it.getCantoPosizione(i).toInt()
                            )
                        ) {
                            it.removeCanto(i)
                            update = true
                        }
                    }
                    if (update) {
                        val updateValues = ContentValues()
                        updateValues.put("lista", converter.fromListaPersonalizzata(it))
                        database.update(
                            "listapers",
                            SQLiteDatabase.CONFLICT_REPLACE,
                            updateValues,
                            "id = ?",
                            arrayOf(listapers.id)
                        )
                    }
                }
            }
        }

        private fun isCantoExistent(database: SupportSQLiteDatabase, idCanto: Int): Boolean {
            val sql =
                "SELECT count(*) FROM canto WHERE id = $idCanto"
            val cursor = database.query(sql)
            cursor.moveToFirst()
            val ret = cursor.getInt(0) > 0
            cursor.close()
            return ret
        }
    }
}
