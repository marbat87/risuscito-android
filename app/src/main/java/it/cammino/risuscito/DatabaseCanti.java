package it.cammino.risuscito;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseCanti extends SQLiteOpenHelper {

    private static final String DB_NAME = "DBCanti";
    //la versione 20 è la prima con salvataggio tonalità e barrè
    //la versione 21 è la prima con il salvataggio velocità di scorrimento
    private static final int DB_VERSION = 39;

    private final String GIALLO = "#EBD0A5";
    private final String BIANCO = "#FCFCFC";
    private final String AZZURRO = "#6F949A";
    private final String VERDE = "#8FC490";
    private final String GRIGIO = "#CAC8BC";
    private Context appContext;

    public DatabaseCanti(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creazione della tabella
        String sql = "CREATE TABLE IF NOT EXISTS ELENCO (";
        sql += "_id INTEGER PRIMARY KEY,";
        sql += "pagina INTEGER NOT NULL,";
        sql += "titolo TEXT NOT NULL,";
        sql += "source TEXT NOT NULL,";
        sql += "favourite INTEGER NOT NULL,";
        sql += "color TEXT NOT NULL,";
        sql += "link TEXT,";
        sql += "zoom INTEGER NOT NULL,";
        sql += "scroll_x INTEGER NOT NULL,";
        sql += "scroll_y INTEGER NOT NULL,";
        sql += "saved_tab TEXT,";
        sql += "saved_barre TEXT,";
        sql += "saved_speed TEXT";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS ARGOMENTI (";
        sql += "_id INTEGER NOT NULL,";
        sql += "id_canto TEXT NOT NULL,";
        sql += "PRIMARY KEY (_id, id_canto)";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS ARG_NAMES (";
        sql += "_id INTEGER PRIMARY KEY,";
        sql += "nome TEXT NOT NULL";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS CUST_LISTS (";
        sql += "_id INTEGER NOT NULL,";
        sql += "position INTEGER NOT NULL,";
        sql += "id_canto TEXT NOT NULL,";
        sql += "timestamp DATETIME NOT NULL,";
        sql += "PRIMARY KEY (_id, position, id_canto)";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS LISTE_PERS (";
        sql += "_id INTEGER PRIMARY KEY AUTOINCREMENT,";
        sql += "titolo_lista TEXT NOT NULL,";
        sql += "lista BLOB NOT NULL";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS SALMI_MUSICA (";
        sql += "_id INTEGER PRIMARY KEY,";
        sql += "num_salmo TEXT NOT NULL,";
        sql += "titolo_salmo TEXT NOT NULL";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS LOCAL_LINKS (";
        sql += "_id INTEGER PRIMARY KEY,";
        sql += "local_path TEXT NOT NULL";
        sql += ");";
        db.execSQL(sql);

        //nuove tabelle indice liturgico
        sql = "CREATE TABLE IF NOT EXISTS INDICE_LIT (";
        sql += "_id INTEGER NOT NULL,";
        sql += "id_canto TEXT NOT NULL,";
        sql += "PRIMARY KEY (_id, id_canto)";
        sql += ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS INDICE_LIT_NAMES (";
        sql += "_id INTEGER PRIMARY KEY,";
        sql += "nome TEXT NOT NULL";
        sql += ");";
        db.execSQL(sql);
        //fine tabella indice liturgico

        // CANTI
        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (1, " + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_page) + ", '" + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_title) + "', '"
                + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (2, " + appContext.getResources().getString(R.string.preghiera_litanica_penitenziale_page) + ", '" + appContext.getResources().getString(R.string.preghiera_litanica_penitenziale_title) + "', '"
                + appContext.getResources().getString(R.string.preghiera_litanica_penitenziale_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.preghiera_litanica_penitenziale_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (3, " + appContext.getResources().getString(R.string.celebrazione_penitenziale_page) + ", '" + appContext.getResources().getString(R.string.celebrazione_penitenziale_title) + "', '"
                + appContext.getResources().getString(R.string.celebrazione_penitenziale_source)+ "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.celebrazione_penitenziale_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (4, " + appContext.getResources().getString(R.string.gloria_a_dio_cieli_page) + ", '" + appContext.getResources().getString(R.string.gloria_a_dio_cieli_title) + "', '"
                + appContext.getResources().getString(R.string.gloria_a_dio_cieli_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.gloria_a_dio_cieli_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (5, " + appContext.getResources().getString(R.string.santo_e_santo_quaresima_page) + ", '" + appContext.getResources().getString(R.string.santo_e_santo_quaresima_title) + "', '"
                + appContext.getResources().getString(R.string.santo_e_santo_quaresima_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.santo_e_santo_quaresima_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (6, " + appContext.getResources().getString(R.string.santo_ordinario_page) + ", '" + appContext.getResources().getString(R.string.santo_ordinario_title) + "', '"
                + appContext.getResources().getString(R.string.santo_ordinario_source) + "', 0, '" + GIALLO
                + "', '" + appContext.getResources().getString(R.string.santo_ordinario_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (7, " + appContext.getResources().getString(R.string.santo_baracche_page) + ", '" + appContext.getResources().getString(R.string.santo_baracche_title) + "', '"
                + appContext.getResources().getString(R.string.santo_baracche_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.santo_baracche_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (8, " + appContext.getResources().getString(R.string.santo_palme_page) + ",  '" + appContext.getResources().getString(R.string.santo_palme_title) + "', '"
                + appContext.getResources().getString(R.string.santo_palme_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.santo_palme_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (9, " + appContext.getResources().getString(R.string.santo_1988_page) + ", '" + appContext.getResources().getString(R.string.santo_1988_title) + "', '"
                + appContext.getResources().getString(R.string.santo_1988_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.santo_1988_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (10, " + appContext.getResources().getString(R.string.santo_1983_page) + ", '" + appContext.getResources().getString(R.string.santo_1983_title) + "', '"
                + appContext.getResources().getString(R.string.santo_1983_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.santo_1983_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (11, " + appContext.getResources().getString(R.string.preghiera_eucarestica_II_page) + ", '" + appContext.getResources().getString(R.string.preghiera_eucarestica_II_title) + "', '"
                + appContext.getResources().getString(R.string.preghiera_eucarestica_II_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.preghiera_eucarestica_II_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (12, " + appContext.getResources().getString(R.string.preghiera_eucaristica_II_parte2_page) + ", '" + appContext.getResources().getString(R.string.preghiera_eucaristica_II_parte2_title) + "', '"
                + appContext.getResources().getString(R.string.preghiera_eucaristica_II_parte2_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.preghiera_eucaristica_II_parte2_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (13, " + appContext.getResources().getString(R.string.benedizione_acqua_fonte_page) + ", '" + appContext.getResources().getString(R.string.benedizione_acqua_fonte_title) + "', '"
                + appContext.getResources().getString(R.string.benedizione_acqua_fonte_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.benedizione_acqua_fonte_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (14, 11, 'Benedizione dell''acqua del fonte battesimale (2)', 'benedizione_acqua_fonte_2', 0, '"
//				+ GIALLO + "', 'http://www.resuscicanti.com/05Benedizionedell''acquadelfontebattesimale%208,30.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (15, " + appContext.getResources().getString(R.string.preconio_pasquale_page) + ", '" + appContext.getResources().getString(R.string.preconio_pasquale_title) + "', '"
                + appContext.getResources().getString(R.string.preconio_pasquale_source) + "', 0, '" + GIALLO
                + "', '" + appContext.getResources().getString(R.string.preconio_pasquale_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (17, " + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_page) + ", '" + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_title) + "', '"
                + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (18, " + appContext.getResources().getString(R.string.inno_lodi_avvento_fino16_page) + ", '" + appContext.getResources().getString(R.string.inno_lodi_avvento_fino16_title) + "', '"
                + appContext.getResources().getString(R.string.inno_lodi_avvento_fino16_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_lodi_avvento_fino16_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (19, " + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_page) + ", '" + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_title) + "', '"
                + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (20, " + appContext.getResources().getString(R.string.inno_lodi_pasqua_fino_ascensione_page) + ", '" + appContext.getResources().getString(R.string.inno_lodi_pasqua_fino_ascensione_title) + "', '"
                + appContext.getResources().getString(R.string.inno_lodi_pasqua_fino_ascensione_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_lodi_pasqua_fino_ascensione_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (21, 16, 'Inno dei vespri da Pasqua all''Ascensione', 'inno_vespri_pasqua_fino_ascensione', 0, '"
//				+ GIALLO + "', 'http://www.resuscicanti.com/010Innodipentecoste-vieni%20spiritocreatore-3,06.mp3', "
//				+ "0, 0, 0, NULL, NULL)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (21, " +  appContext.getResources().getString(R.string.inno_vespri_pasqua_fino_ascensione_page) + ", '" + appContext.getResources().getString(R.string.inno_vespri_pasqua_fino_ascensione_title) + "', '"
                + appContext.getResources().getString(R.string.inno_vespri_pasqua_fino_ascensione_source) + "', 0, '"
                + GIALLO + "', ' + " + appContext.getResources().getString(R.string.inno_vespri_pasqua_fino_ascensione_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (22, " + appContext.getResources().getString(R.string.inno_lodi_pasqua_ascensione_pentecoste_page) + ", '" + appContext.getResources().getString(R.string.inno_lodi_pasqua_ascensione_pentecoste_title) + "'," +
                " '" + appContext.getResources().getString(R.string.inno_lodi_pasqua_ascensione_pentecoste_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_lodi_pasqua_ascensione_pentecoste_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (23, " + appContext.getResources().getString(R.string.inno_vespri_pasqua_ascensione_pentecoste_page) + ", '" + appContext.getResources().getString(R.string.inno_vespri_pasqua_ascensione_pentecoste_title) + "', '"
                + appContext.getResources().getString(R.string.inno_vespri_pasqua_ascensione_pentecoste_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_vespri_pasqua_ascensione_pentecoste_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (24, " + appContext.getResources().getString(R.string.inno_lodi_pentecoste_page) + ", '" + appContext.getResources().getString(R.string.inno_lodi_pentecoste_title) + "', '"
                + appContext.getResources().getString(R.string.inno_lodi_pentecoste_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_lodi_pentecoste_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (25, " + appContext.getResources().getString(R.string.sequenza_di_pentecoste_page) + ", '" + appContext.getResources().getString(R.string.sequenza_di_pentecoste_title) + "', '"
                + appContext.getResources().getString(R.string.sequenza_di_pentecoste_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.sequenza_di_pentecoste_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (26, " + appContext.getResources().getString(R.string.preghiera_eucaristia_II_2_prefazio_page) + ", '" + appContext.getResources().getString(R.string.preghiera_eucaristia_II_2_prefazio_title) + "', '"
                + appContext.getResources().getString(R.string.preghiera_eucaristia_II_2_prefazio_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.preghiera_eucaristia_II_2_prefazio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (27, " + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_consacrazione_page) + ", '" + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_consacrazione_title) + "', '"
                + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_consacrazione_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_consacrazione_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (28, " + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_offerta_page) + ", '" + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_offerta_title) + "', '"
                + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_offerta_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.preghiera_eucaristica_II_2_offerta_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (29, " + appContext.getResources().getString(R.string.alleluja_pasquale_page)  + ", '" + appContext.getResources().getString(R.string.alleluja_pasquale_title) + "', '"
                + appContext.getResources().getString(R.string.alleluja_pasquale_source) + "', 0, '" + GIALLO
                + "', '" + appContext.getResources().getString(R.string.alleluja_pasquale_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (30, " + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_page) + ", '" + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_title) + "', '"
                + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (31, " + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_quaresima_page) + ", '" + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_quaresima_title) + "', '"
                + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_quaresima_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.acclamazioni_al_vangelo_quaresima_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (32, " + appContext.getResources().getString(R.string.te_deum_page) + ", '" + appContext.getResources().getString(R.string.te_deum_title) + "', '"
                + appContext.getResources().getString(R.string.te_deum_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.te_deum_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (140, " + appContext.getResources().getString(R.string.agnello_di_dio_page) + ", '" + appContext.getResources().getString(R.string.agnello_di_dio_title) + "', '"
                + appContext.getResources().getString(R.string.agnello_di_dio_source) + "', 0, '" + GIALLO
                + "', '" + appContext.getResources().getString(R.string.agnello_di_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (33, " + appContext.getResources().getString(R.string.padre_nostro_page) + ", '" + appContext.getResources().getString(R.string.padre_nostro_title) + "', '"
                + appContext.getResources().getString(R.string.padre_nostro_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.padre_nostro_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (34, " + appContext.getResources().getString(R.string.credo_apostolico_page) + ", '" + appContext.getResources().getString(R.string.credo_apostolico_title) + "', '"
                + appContext.getResources().getString(R.string.credo_apostolico_source) + "', 0, '" + GIALLO
                + "', '" + appContext.getResources().getString(R.string.credo_apostolico_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (35, " + appContext.getResources().getString(R.string.risuscito_page) + ", '" + appContext.getResources().getString(R.string.risuscito_title) + "', '"
                + appContext.getResources().getString(R.string.risuscito_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.risuscito_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (36, " + appContext.getResources().getString(R.string.verso_te_o_citta_santa_page) + ", '" + appContext.getResources().getString(R.string.verso_te_o_citta_santa_title) + "', '"
                + appContext.getResources().getString(R.string.verso_te_o_citta_santa_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.verso_te_o_citta_santa_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (37, " + appContext.getResources().getString(R.string.marcia_e_dura_page) + ", '" + appContext.getResources().getString(R.string.marcia_e_dura_title) + "', '"
                + appContext.getResources().getString(R.string.marcia_e_dura_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.marcia_e_dura_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (38, " + appContext.getResources().getString(R.string.dal_profondo_a_te_grido_page) + ", '" + appContext.getResources().getString(R.string.dal_profondo_a_te_grido_title) + "', '"
                + appContext.getResources().getString(R.string.dal_profondo_a_te_grido_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.dal_profondo_a_te_grido_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (39, " + appContext.getResources().getString(R.string.canto_di_giosue_page) + ", '" + appContext.getResources().getString(R.string.canto_di_giosue_title) + "', '"
                + appContext.getResources().getString(R.string.canto_di_giosue_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.canto_di_giosue_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (40, " + appContext.getResources().getString(R.string.benedici_anima_mia_jahve_page) + ", '" + appContext.getResources().getString(R.string.benedici_anima_mia_jahve_title) + "', '"
                + appContext.getResources().getString(R.string.benedici_anima_mia_jahve_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.benedici_anima_mia_jahve_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (41, " + appContext.getResources().getString(R.string.fino_a_quando_page) + ", '" + appContext.getResources().getString(R.string.fino_a_quando_title) + "', '"
                + appContext.getResources().getString(R.string.fino_a_quando_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.fino_a_quando_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (42, " + appContext.getResources().getString(R.string.jahve_tu_sei_mio_dio_page) + ", '" + appContext.getResources().getString(R.string.jahve_tu_sei_mio_dio_title) + "', '"
                + appContext.getResources().getString(R.string.jahve_tu_sei_mio_dio_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.jahve_tu_sei_mio_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (43, " + appContext.getResources().getString(R.string.cantiamo_cantiamo_page) + ", '" + appContext.getResources().getString(R.string.cantiamo_cantiamo_title) + "', '"
                + appContext.getResources().getString(R.string.cantiamo_cantiamo_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.cantiamo_cantiamo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (44, " + appContext.getResources().getString(R.string.giunga_la_mia_preghiera_page) + ", '" + appContext.getResources().getString(R.string.giunga_la_mia_preghiera_title) + "', '"
                + appContext.getResources().getString(R.string.giunga_la_mia_preghiera_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.giunga_la_mia_preghiera_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (45, " + appContext.getResources().getString(R.string.guardate_come_e_bello_page) + ", '" + appContext.getResources().getString(R.string.guardate_come_e_bello_title) + "', '"
                + appContext.getResources().getString(R.string.guardate_come_e_bello_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.guardate_come_e_bello_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (46, " + appContext.getResources().getString(R.string.come_bello_come_da_gioia_page) + ", '" + appContext.getResources().getString(R.string.come_bello_come_da_gioia_title) +  "', '"
                + appContext.getResources().getString(R.string.come_bello_come_da_gioia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.come_bello_come_da_gioia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (47, " + appContext.getResources().getString(R.string.guardate_come_e_bello_gustate_page) + ", '" + appContext.getResources().getString(R.string.guardate_come_e_bello_gustate_title) + "', '"
                + appContext.getResources().getString(R.string.guardate_come_e_bello_gustate_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.guardate_come_e_bello_gustate_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (48, " + appContext.getResources().getString(R.string.grazie_a_jahve_page) + ", '" + appContext.getResources().getString(R.string.grazie_a_jahve_title) + "', '"
                + appContext.getResources().getString(R.string.grazie_a_jahve_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.grazie_a_jahve_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (49, " + appContext.getResources().getString(R.string.canto_giovani_fornace_I_page) + ", '" + appContext.getResources().getString(R.string.canto_giovani_fornace_I_title) + "', '"
                + appContext.getResources().getString(R.string.canto_giovani_fornace_I_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.canto_giovani_fornace_I_link) +  "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (50, " + appContext.getResources().getString(R.string.canto_giovani_fornace_II_page) + ", '" + appContext.getResources().getString(R.string.canto_giovani_fornace_II_title) + "', '"
                + appContext.getResources().getString(R.string.canto_giovani_fornace_II_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.canto_giovani_fornace_II_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (51, " + appContext.getResources().getString(R.string.lodate_il_signore_dai_cieli_page) + ", '" + appContext.getResources().getString(R.string.lodate_il_signore_dai_cieli_title) + "', '"
                + appContext.getResources().getString(R.string.lodate_il_signore_dai_cieli_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.lodate_il_signore_dai_cieli_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (52, " + appContext.getResources().getString(R.string.lodate_iddio_page) + ", '" + appContext.getResources().getString(R.string.lodate_iddio_title) + "', '"
                + appContext.getResources().getString(R.string.lodate_iddio_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.lodate_iddio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (53, " + appContext.getResources().getString(R.string.signore_e_mia_luce_page) + ", '" + appContext.getResources().getString(R.string.signore_e_mia_luce_title) + "', '"
                + appContext.getResources().getString(R.string.signore_e_mia_luce_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.signore_e_mia_luce_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (54, " + appContext.getResources().getString(R.string.evenu_shalom_page) + ", '" + appContext.getResources().getString(R.string.evenu_shalom_title) + "', '"
                + appContext.getResources().getString(R.string.evenu_shalom_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.evenu_shalom_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (55, " + appContext.getResources().getString(R.string.gia_viene_il_regno_page) + ", '" + appContext.getResources().getString(R.string.gia_viene_il_regno_title) + "', '"
                + appContext.getResources().getString(R.string.gia_viene_il_regno_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.gia_viene_il_regno_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (56, " + appContext.getResources().getString(R.string.abba_padre_page) + ", '" + appContext.getResources().getString(R.string.abba_padre_title) + "', '"
                + appContext.getResources().getString(R.string.abba_padre_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.abba_padre_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (57, " + appContext.getResources().getString(R.string.chi_ci_separera_page) + ", '" + appContext.getResources().getString(R.string.chi_ci_separera_title) + "', '"
                + appContext.getResources().getString(R.string.chi_ci_separera_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.chi_ci_separera_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (58, " + appContext.getResources().getString(R.string.magnificat_page) + ", '" + appContext.getResources().getString(R.string.magnificat_title) + "', '"
                + appContext.getResources().getString(R.string.magnificat_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.magnificat_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (59, " + appContext.getResources().getString(R.string.innalzero_la_coppa_di_salvezza_page) + ", '" + appContext.getResources().getString(R.string.innalzero_la_coppa_di_salvezza_title) + "', '"
                + appContext.getResources().getString(R.string.innalzero_la_coppa_di_salvezza_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.innalzero_la_coppa_di_salvezza_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (60, " + appContext.getResources().getString(R.string.quando_il_signore_page) + ", '" + appContext.getResources().getString(R.string.quando_il_signore_title) + "', '"
                + appContext.getResources().getString(R.string.quando_il_signore_source)  + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.quando_il_signore_link)  + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (61, " + appContext.getResources().getString(R.string.cantico_di_zaccaria_page) + ", '" + appContext.getResources().getString(R.string.cantico_di_zaccaria_title) + "', '"
                + appContext.getResources().getString(R.string.cantico_di_zaccaria_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.cantico_di_zaccaria_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (62, " + appContext.getResources().getString(R.string.o_morte_dove_la_tua_vittoria_page) + ", '" + appContext.getResources().getString(R.string.o_morte_dove_la_tua_vittoria_title) + "', '"
                + appContext.getResources().getString(R.string.o_morte_dove_la_tua_vittoria_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.o_morte_dove_la_tua_vittoria_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (63, " + appContext.getResources().getString(R.string.o_cieli_piovete_dall_alto_page) + ", '" + appContext.getResources().getString(R.string.o_cieli_piovete_dall_alto_title) + "', '"
                + appContext.getResources().getString(R.string.o_cieli_piovete_dall_alto_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.o_cieli_piovete_dall_alto_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (64, " + appContext.getResources().getString(R.string.pentecoste_page) + ", '" + appContext.getResources().getString(R.string.pentecoste_title) + "', '"
                + appContext.getResources().getString(R.string.pentecoste_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.pentecoste_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (65, " + appContext.getResources().getString(R.string.ecco_qui_vengo_presto_page) + ", '" + appContext.getResources().getString(R.string.ecco_qui_vengo_presto_title) + "', '"
                + appContext.getResources().getString(R.string.ecco_qui_vengo_presto_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.ecco_qui_vengo_presto_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (66, " + appContext.getResources().getString(R.string.vieni_figlio_dell_uomo_page) + ", '" + appContext.getResources().getString(R.string.vieni_figlio_dell_uomo_title) + "', '"
                + appContext.getResources().getString(R.string.vieni_figlio_dell_uomo_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.vieni_figlio_dell_uomo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (67, " + appContext.getResources().getString(R.string.abramo_page) + ", '" + appContext.getResources().getString(R.string.abramo_title) + "', '"
                + appContext.getResources().getString(R.string.abramo_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.abramo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (68, " + appContext.getResources().getString(R.string.cantico_di_mose_page) + ", '" + appContext.getResources().getString(R.string.cantico_di_mose_title) + "', '"
                + appContext.getResources().getString(R.string.cantico_di_mose_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.cantico_di_mose_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (69, " + appContext.getResources().getString(R.string.lodate_il_signore_page) + ", '" + appContext.getResources().getString(R.string.lodate_il_signore_title) + "', '"
                + appContext.getResources().getString(R.string.lodate_il_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.lodate_il_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (70, " + appContext.getResources().getString(R.string.quando_israele_usci_egitto_page) + ", '" + appContext.getResources().getString(R.string.quando_israele_usci_egitto_title) + "', '"
                + appContext.getResources().getString(R.string.quando_israele_usci_egitto_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.quando_israele_usci_egitto_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (71, " + appContext.getResources().getString(R.string.alzate_o_porte_page) + ", '" + appContext.getResources().getString(R.string.alzate_o_porte_title) + "', '"
                + appContext.getResources().getString(R.string.alzate_o_porte_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.alzate_o_porte_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (72, " + appContext.getResources().getString(R.string.signore_mio_pastore_page)  + ", '" + appContext.getResources().getString(R.string.signore_mio_pastore_title) + "', '"
                + appContext.getResources().getString(R.string.signore_mio_pastore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.signore_mio_pastore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (73, " + appContext.getResources().getString(R.string.giunti_fiumi_babilonia_page) + ", '" + appContext.getResources().getString(R.string.giunti_fiumi_babilonia_title) + "', '"
                + appContext.getResources().getString(R.string.giunti_fiumi_babilonia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.giunti_fiumi_babilonia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (74, " + appContext.getResources().getString(R.string.pieta_di_me_o_dio_page) + ", '" + appContext.getResources().getString(R.string.pieta_di_me_o_dio_title) + "', '"
                + appContext.getResources().getString(R.string.pieta_di_me_o_dio_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.pieta_di_me_o_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (75, " + appContext.getResources().getString(R.string.misericordia_dio_misericordia_page) + ", '" + appContext.getResources().getString(R.string.misericordia_dio_misericordia_title) + "', '"
                + appContext.getResources().getString(R.string.misericordia_dio_misericordia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.misericordia_dio_misericordia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (76, " + appContext.getResources().getString(R.string.inno_della_kenosis_page) + ", '" + appContext.getResources().getString(R.string.inno_della_kenosis_title) + "', '"
                + appContext.getResources().getString(R.string.inno_della_kenosis_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.inno_della_kenosis_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (77, " + appContext.getResources().getString(R.string.ave_maria_page) + ", '" + appContext.getResources().getString(R.string.ave_maria_title) + "', '"
                + appContext.getResources().getString(R.string.ave_maria_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.ave_maria_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (78, " + appContext.getResources().getString(R.string.ave_maria_1984_page) + ", '" + appContext.getResources().getString(R.string.ave_maria_1984_title) +"', '"
                + appContext.getResources().getString(R.string.ave_maria_1984_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.ave_maria_1984_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (79, " + appContext.getResources().getString(R.string.maria_piccola_maria_page) + ", '" + appContext.getResources().getString(R.string.maria_piccola_maria_title) + "', '"
                + appContext.getResources().getString(R.string.maria_piccola_maria_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.maria_piccola_maria_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (80, " + appContext.getResources().getString(R.string.alzo_gli_occhi_page) + ", '" + appContext.getResources().getString(R.string.alzo_gli_occhi_title) + "', '"
                + appContext.getResources().getString(R.string.alzo_gli_occhi_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.alzo_gli_occhi_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (81, " + appContext.getResources().getString(R.string.canto_liberati_page) + ", '" + appContext.getResources().getString(R.string.canto_liberati_title) + "', '"
                + appContext.getResources().getString(R.string.canto_liberati_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.canto_liberati_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (82, " + appContext.getResources().getString(R.string.se_oggi_ascoltate_sua_voce_page) + ", '" + appContext.getResources().getString(R.string.se_oggi_ascoltate_sua_voce_title) + "', '"
                + appContext.getResources().getString(R.string.se_oggi_ascoltate_sua_voce_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.se_oggi_ascoltate_sua_voce_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (83, " + appContext.getResources().getString(R.string.venite_applaudiamo_al_signore_page) + ", '" + appContext.getResources().getString(R.string.venite_applaudiamo_al_signore_title) + "', '"
                + appContext.getResources().getString(R.string.venite_applaudiamo_al_signore_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.venite_applaudiamo_al_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (84, " + appContext.getResources().getString(R.string.dajenu_page) + ", '" + appContext.getResources().getString(R.string.dajenu_title) + "', '"
                + appContext.getResources().getString(R.string.dajenu_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.dajenu_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (85, " + appContext.getResources().getString(R.string.alla_vittima_pasquale_page) + ", '" + appContext.getResources().getString(R.string.alla_vittima_pasquale_title) + "', '"
                + appContext.getResources().getString(R.string.alla_vittima_pasquale_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.alla_vittima_pasquale_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (86, " + appContext.getResources().getString(R.string.inno_di_pasqua_page) + ", '" + appContext.getResources().getString(R.string.inno_di_pasqua_title) + "', '"
                + appContext.getResources().getString(R.string.inno_di_pasqua_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.inno_di_pasqua_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (87, " + appContext.getResources().getString(R.string.inno_avvento_page) + ", '" + appContext.getResources().getString(R.string.inno_avvento_title) + "', '"
                + appContext.getResources().getString(R.string.inno_avvento_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.inno_avvento_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (88, " + appContext.getResources().getString(R.string.uri_uri_ura_page) + ", '" + appContext.getResources().getString(R.string.uri_uri_ura_title) + "', '"
                + appContext.getResources().getString(R.string.uri_uri_ura_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.uri_uri_ura_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (89, " + appContext.getResources().getString(R.string.gia_viene_il_mio_dio_page) + ", '" + appContext.getResources().getString(R.string.gia_viene_il_mio_dio_title) + "', '"
                + appContext.getResources().getString(R.string.gia_viene_il_mio_dio_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.gia_viene_il_mio_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (90, " + appContext.getResources().getString(R.string.amen_amen_amen_page) + ", '" + appContext.getResources().getString(R.string.amen_amen_amen_title) + "', '"
                + appContext.getResources().getString(R.string.amen_amen_amen_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.amen_amen_amen_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (91, " + appContext.getResources().getString(R.string.se_signore_non_costruisce_page) + ", '" + appContext.getResources().getString(R.string.se_signore_non_costruisce_title) + "', '"
                + appContext.getResources().getString(R.string.se_signore_non_costruisce_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.se_signore_non_costruisce_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (92, " + appContext.getResources().getString(R.string.gustate_e_vedete_page) + ", '" + appContext.getResources().getString(R.string.gustate_e_vedete_title) + "', '"
                + appContext.getResources().getString(R.string.gustate_e_vedete_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.gustate_e_vedete_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (93, " + appContext.getResources().getString(R.string.per_amore_dei_miei_fratelli_page) + ", '" + appContext.getResources().getString(R.string.per_amore_dei_miei_fratelli_title) + "', '"
                + appContext.getResources().getString(R.string.per_amore_dei_miei_fratelli_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.per_amore_dei_miei_fratelli_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (94, " + appContext.getResources().getString(R.string.ho_sperato_nel_signore_page) + ", '" + appContext.getResources().getString(R.string.ho_sperato_nel_signore_title) + "', '"
                + appContext.getResources().getString(R.string.ho_sperato_nel_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.ho_sperato_nel_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (95, " + appContext.getResources().getString(R.string.voglio_cantare_page) + ", '" + appContext.getResources().getString(R.string.voglio_cantare_title) + "', '"
                + appContext.getResources().getString(R.string.voglio_cantare_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.voglio_cantare_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (96, " + appContext.getResources().getString(R.string.perche_genti_congiurano_page) + ", '" + appContext.getResources().getString(R.string.perche_genti_congiurano_title) + "', '"
                + appContext.getResources().getString(R.string.perche_genti_congiurano_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.perche_genti_congiurano_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (97, " + appContext.getResources().getString(R.string.come_una_cerva_anela_page) + ", '" + appContext.getResources().getString(R.string.come_una_cerva_anela_title) + "', '"
                + appContext.getResources().getString(R.string.come_una_cerva_anela_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.come_una_cerva_anela_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (98, " + appContext.getResources().getString(R.string.acclamate_al_signore_page) + ", '" + appContext.getResources().getString(R.string.acclamate_al_signore_title) + "', '"
                + appContext.getResources().getString(R.string.acclamate_al_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.acclamate_al_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (99, " + appContext.getResources().getString(R.string.gridate_con_gioia_page) + ", '" + appContext.getResources().getString(R.string.gridate_con_gioia_title) + "', '"
                + appContext.getResources().getString(R.string.gridate_con_gioia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.gridate_con_gioia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (100, " + appContext.getResources().getString(R.string.al_risveglio_mi_saziero_page) + ", '" + appContext.getResources().getString(R.string.al_risveglio_mi_saziero_title) + "', '"
                + appContext.getResources().getString(R.string.al_risveglio_mi_saziero_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.al_risveglio_mi_saziero_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (101, " + appContext.getResources().getString(R.string.canto_bambini_veglia_page) +  ", '" + appContext.getResources().getString(R.string.canto_bambini_veglia_title) + "', '"
                + appContext.getResources().getString(R.string.canto_bambini_veglia_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.canto_bambini_veglia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (102, " + appContext.getResources().getString(R.string.non_moriro_page) + ", '" + appContext.getResources().getString(R.string.non_moriro_title) + "', '"
                + appContext.getResources().getString(R.string.non_moriro_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.non_moriro_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (103, 85, 'Non morirò - Salmo 117(118) - II parte', 'non_moriro_II', 0, '"
//				+ BIANCO + "', 'http://www.resuscicanti.com/nonmoriro.mp3', "
//				+ "0, 0, 0, NULL, NULL)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (104, " + appContext.getResources().getString(R.string.dite_agli_smarriti_page) + ", '" + appContext.getResources().getString(R.string.dite_agli_smarriti_title) + "', '"
                + appContext.getResources().getString(R.string.dite_agli_smarriti_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.dite_agli_smarriti_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (105, " + appContext.getResources().getString(R.string.o_dio_tu_sei_il_mio_dio_page) + ", '" + appContext.getResources().getString(R.string.o_dio_tu_sei_il_mio_dio_title) + "', '"
                + appContext.getResources().getString(R.string.o_dio_tu_sei_il_mio_dio_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.o_dio_tu_sei_il_mio_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (106, " + appContext.getResources().getString(R.string.sale_dio_tra_acclamazioni_page) + ", '" + appContext.getResources().getString(R.string.sale_dio_tra_acclamazioni_title) + "', '"
                + appContext.getResources().getString(R.string.sale_dio_tra_acclamazioni_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.sale_dio_tra_acclamazioni_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (107, " + appContext.getResources().getString(R.string.popolo_camminava_tenebre_page)  + ", '" + appContext.getResources().getString(R.string.popolo_camminava_tenebre_title) + "', '"
                + appContext.getResources().getString(R.string.popolo_camminava_tenebre_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.popolo_camminava_tenebre_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (108, " + appContext.getResources().getString(R.string.da_lode_al_signore_page) + ", '" + appContext.getResources().getString(R.string.da_lode_al_signore_title) + "', '"
                + appContext.getResources().getString(R.string.da_lode_al_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.da_lode_al_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (109, " + appContext.getResources().getString(R.string.canto_di_balaam_page) + ", '" + appContext.getResources().getString(R.string.canto_di_balaam_title) + "', '"
                + appContext.getResources().getString(R.string.canto_di_balaam_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.canto_di_balaam_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (110, " + appContext.getResources().getString(R.string.davanti_agli_angeli_page) + ", '" + appContext.getResources().getString(R.string.davanti_agli_angeli_title) + "', '"
                + appContext.getResources().getString(R.string.davanti_agli_angeli_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.davanti_agli_angeli_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (111, " + appContext.getResources().getString(R.string.quando_israele_era_un_bimbo_page) + ", '" + appContext.getResources().getString(R.string.quando_israele_era_un_bimbo_title) + "', '"
                + appContext.getResources().getString(R.string.quando_israele_era_un_bimbo_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.quando_israele_era_un_bimbo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (112, " + appContext.getResources().getString(R.string.e_la_pasqua_del_signore_page) + ", '" + appContext.getResources().getString(R.string.e_la_pasqua_del_signore_title) + "', '"
                + appContext.getResources().getString(R.string.e_la_pasqua_del_signore_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.e_la_pasqua_del_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (113, " + appContext.getResources().getString(R.string.tempo_ogni_cosa_page) + ", '" + appContext.getResources().getString(R.string.tempo_ogni_cosa_title) + "', '"
                + appContext.getResources().getString(R.string.tempo_ogni_cosa_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.tempo_ogni_cosa_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (114, 93, 'Benedici anima mia il Signore (Cantico di Tobia) (Tb 13)', 'benedici_anima_mia', 0, '"
//				+ BIANCO
//				+ "', 'http://www.resuscicanti.com/BENEDICI%20ANIMA%20MIA%20IL%20SIGNOREGerusalemme%20ricostruita2.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (114, " + appContext.getResources().getString(R.string.benedici_anima_mia_page) + ", '" + appContext.getResources().getString(R.string.benedici_anima_mia_title)
                + "', '" + appContext.getResources().getString(R.string.benedici_anima_mia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.benedici_anima_mia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (115, " + appContext.getResources().getString(R.string.quanto_sono_amabili_dimore_page) + ", '" + appContext.getResources().getString(R.string.quanto_sono_amabili_dimore_title) + "', '"
                + appContext.getResources().getString(R.string.quanto_sono_amabili_dimore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.quanto_sono_amabili_dimore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (116, " + appContext.getResources().getString(R.string.viene_il_signore_vestito_di_maesta_page)  + ", '" + appContext.getResources().getString(R.string.viene_il_signore_vestito_di_maesta_title) + "', '"
                + appContext.getResources().getString(R.string.viene_il_signore_vestito_di_maesta_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.viene_il_signore_vestito_di_maesta_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (117, " + appContext.getResources().getString(R.string.giorno_di_riposo_page) + ", '" + appContext.getResources().getString(R.string.giorno_di_riposo_title) + "', '"
                + appContext.getResources().getString(R.string.giorno_di_riposo_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.giorno_di_riposo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (118, " + appContext.getResources().getString(R.string.consolate_il_mio_popolo_page) + ", '" + appContext.getResources().getString(R.string.consolate_il_mio_popolo_title) + "', '"
                + appContext.getResources().getString(R.string.consolate_il_mio_popolo_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.consolate_il_mio_popolo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (119, " + appContext.getResources().getString(R.string.cerano_due_angeli_page) + ", '" + appContext.getResources().getString(R.string.cerano_due_angeli_title) + "', '"
                + appContext.getResources().getString(R.string.cerano_due_angeli_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.cerano_due_angeli_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (120, " + appContext.getResources().getString(R.string.amo_il_signore_page) + ", '" + appContext.getResources().getString(R.string.amo_il_signore_title) + "', '"
                + appContext.getResources().getString(R.string.amo_il_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.amo_il_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (121, " + appContext.getResources().getString(R.string.venite_a_me_voi_tutti_page) + ", '" + appContext.getResources().getString(R.string.venite_a_me_voi_tutti_title) + "', '"
                + appContext.getResources().getString(R.string.venite_a_me_voi_tutti_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.venite_a_me_voi_tutti_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (122, " + appContext.getResources().getString(R.string.a_te_si_deve_lode_in_sion_page) + ", '" + appContext.getResources().getString(R.string.a_te_si_deve_lode_in_sion_title) + "', '"
                + appContext.getResources().getString(R.string.a_te_si_deve_lode_in_sion_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.a_te_si_deve_lode_in_sion_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (123, " + appContext.getResources().getString(R.string.se_signore_sono_rifugiato_page) + ", '" + appContext.getResources().getString(R.string.se_signore_sono_rifugiato_title) + "', '"
                + appContext.getResources().getString(R.string.se_signore_sono_rifugiato_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.se_signore_sono_rifugiato_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (124, " + appContext.getResources().getString(R.string.signore_annuncia_una_notizia_page) + ", '" + appContext.getResources().getString(R.string.signore_annuncia_una_notizia_title) + "', '"
                + appContext.getResources().getString(R.string.signore_annuncia_una_notizia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.signore_annuncia_una_notizia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (125, " + appContext.getResources().getString(R.string.benedite_il_signore_page) + ", '" + appContext.getResources().getString(R.string.benedite_il_signore_title) + "', '"
                + appContext.getResources().getString(R.string.benedite_il_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.benedite_il_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (126, " + appContext.getResources().getString(R.string.figlie_di_gerusalemme_page) + ", '" + appContext.getResources().getString(R.string.figlie_di_gerusalemme_title) + "', '"
                + appContext.getResources().getString(R.string.figlie_di_gerusalemme_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.figlie_di_gerusalemme_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (127, " + appContext.getResources().getString(R.string.ti_ho_manifestato_mio_peccato_page) + ", '" + appContext.getResources().getString(R.string.ti_ho_manifestato_mio_peccato_title) + "', '"
                + appContext.getResources().getString(R.string.ti_ho_manifestato_mio_peccato_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.ti_ho_manifestato_mio_peccato_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (128, " + appContext.getResources().getString(R.string.maria_madre_della_chiesa_page) + ", '" + appContext.getResources().getString(R.string.maria_madre_della_chiesa_title) + "', '"
                + appContext.getResources().getString(R.string.maria_madre_della_chiesa_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.maria_madre_della_chiesa_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (129, " + appContext.getResources().getString(R.string.stabat_mater_page) + ", '" + appContext.getResources().getString(R.string.stabat_mater_title) + "', '"
                + appContext.getResources().getString(R.string.stabat_mater_source) + "', 0, '" + BIANCO
                + "', 'h" + appContext.getResources().getString(R.string.stabat_mater_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (130, " + appContext.getResources().getString(R.string.lamenti_del_signore_page) + ", '" + appContext.getResources().getString(R.string.lamenti_del_signore_title) + "', '"
                + appContext.getResources().getString(R.string.lamenti_del_signore_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.lamenti_del_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (131, " + appContext.getResources().getString(R.string.o_signore_nostro_dio_page) + ", '" + appContext.getResources().getString(R.string.o_signore_nostro_dio_title) + "', '"
                + appContext.getResources().getString(R.string.o_signore_nostro_dio_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.o_signore_nostro_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (132, " + appContext.getResources().getString(R.string.benedetta_sei_tu_maria_page) + ", '" + appContext.getResources().getString(R.string.benedetta_sei_tu_maria_title) + "', '"
                + appContext.getResources().getString(R.string.benedetta_sei_tu_maria_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.benedetta_sei_tu_maria_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (133, " + appContext.getResources().getString(R.string.salve_regina_dei_cieli_page) + ", '" + appContext.getResources().getString(R.string.salve_regina_dei_cieli_title) + "', '"
                + appContext.getResources().getString(R.string.salve_regina_dei_cieli_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.salve_regina_dei_cieli_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (134, " + appContext.getResources().getString(R.string.vergine_della_meraviglia_page) + ", '" + appContext.getResources().getString(R.string.vergine_della_meraviglia_title) + "', '"
                + appContext.getResources().getString(R.string.vergine_della_meraviglia_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.vergine_della_meraviglia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (135, " + appContext.getResources().getString(R.string.maria_casa_di_benedizione_page) + ", '" + appContext.getResources().getString(R.string.maria_casa_di_benedizione_title) + "', '"
                + appContext.getResources().getString(R.string.maria_casa_di_benedizione_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.maria_casa_di_benedizione_source) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (136, " + appContext.getResources().getString(R.string.benediro_il_signore_in_ogni_tempo_page) + ", '" + appContext.getResources().getString(R.string.benediro_il_signore_in_ogni_tempo_title) + "', '"
                + appContext.getResources().getString(R.string.benediro_il_signore_in_ogni_tempo_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.benediro_il_signore_in_ogni_tempo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (137, " + appContext.getResources().getString(R.string.mietitura_delle_nazioni_page) + ", '" + appContext.getResources().getString(R.string.mietitura_delle_nazioni_title) + "', '"
                + appContext.getResources().getString(R.string.mietitura_delle_nazioni_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.mietitura_delle_nazioni_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (138, " + appContext.getResources().getString(R.string.dice_il_signore_al_mio_signore_page) + ", '" + appContext.getResources().getString(R.string.dice_il_signore_al_mio_signore_title) + "', '"
                + appContext.getResources().getString(R.string.dice_il_signore_al_mio_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.dice_il_signore_al_mio_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (139, " + appContext.getResources().getString(R.string.felice_uomo_page) + ", '" + appContext.getResources().getString(R.string.felice_uomo_title) + "', '"
                + appContext.getResources().getString(R.string.felice_uomo_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.felice_uomo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (141, " + appContext.getResources().getString(R.string.tu_sei_il_piu_bello_page) + ", '" + appContext.getResources().getString(R.string.tu_sei_il_piu_bello_title) + "', '"
                + appContext.getResources().getString(R.string.tu_sei_il_piu_bello_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.tu_sei_il_piu_bello_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (142, " + appContext.getResources().getString(R.string.felicita_per_l_uomo_page) + ", '" + appContext.getResources().getString(R.string.felicita_per_l_uomo_title) + "', '"
                + appContext.getResources().getString(R.string.felicita_per_l_uomo_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.felicita_per_l_uomo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (143, " + appContext.getResources().getString(R.string.sorga_dio_page) + ", '" + appContext.getResources().getString(R.string.sorga_dio_title) + "', '"
                + appContext.getResources().getString(R.string.sorga_dio_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.sorga_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (144, " + appContext.getResources().getString(R.string.andiamo_gia_pastori_page) + ", '" + appContext.getResources().getString(R.string.andiamo_gia_pastori_title) + "', '"
                + appContext.getResources().getString(R.string.andiamo_gia_pastori_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.andiamo_gia_pastori_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (145, " + appContext.getResources().getString(R.string.maria_di_jasna_gora_page) + ", '" + appContext.getResources().getString(R.string.maria_di_jasna_gora_title) + "', '"
                + appContext.getResources().getString(R.string.maria_di_jasna_gora_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.maria_di_jasna_gora_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (146, " + appContext.getResources().getString(R.string.a_te_signore_con_la_mia_voce_page) + ", '" + appContext.getResources().getString(R.string.a_te_signore_con_la_mia_voce_title) + "', '"
                + appContext.getResources().getString(R.string.a_te_signore_con_la_mia_voce_source) +  "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.a_te_signore_con_la_mia_voce_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (147, " + appContext.getResources().getString(R.string.un_germoglio_spunta_tronco_page) + ", '" + appContext.getResources().getString(R.string.un_germoglio_spunta_tronco_title) + "', '"
                + appContext.getResources().getString(R.string.un_germoglio_spunta_tronco_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.un_germoglio_spunta_tronco_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (148, " + appContext.getResources().getString(R.string.a_te_signore_innalzo_la_mia_anima_page) + ", '" + appContext.getResources().getString(R.string.a_te_signore_innalzo_la_mia_anima_title) + "', '"
                + appContext.getResources().getString(R.string.a_te_signore_innalzo_la_mia_anima_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.a_te_signore_innalzo_la_mia_anima_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (149, " + appContext.getResources().getString(R.string.ti_sto_chiamando_page) + ", '" + appContext.getResources().getString(R.string.ti_sto_chiamando_title) + "', '"
                + appContext.getResources().getString(R.string.ti_sto_chiamando_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.ti_sto_chiamando_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (150, " + appContext.getResources().getString(R.string.a_te_levo_i_miei_occhi_page) + ", '" + appContext.getResources().getString(R.string.a_te_levo_i_miei_occhi_title) + "', '"
                + appContext.getResources().getString(R.string.a_te_levo_i_miei_occhi_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.a_te_levo_i_miei_occhi_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (151, " + appContext.getResources().getString(R.string.signore_non_punirmi_nel_tuo_sdegno_page) + ", '" + appContext.getResources().getString(R.string.signore_non_punirmi_nel_tuo_sdegno_title) + "', '"
                + appContext.getResources().getString(R.string.signore_non_punirmi_nel_tuo_sdegno_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.signore_non_punirmi_nel_tuo_sdegno_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (152, " + appContext.getResources().getString(R.string.gloria_gloria_gloria_page) + ", '" + appContext.getResources().getString(R.string.gloria_gloria_gloria_title) + "', '"
                + appContext.getResources().getString(R.string.gloria_gloria_gloria_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.gloria_gloria_gloria_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (153, " + appContext.getResources().getString(R.string.esultate_giusti_nel_signore_page) + ", '" + appContext.getResources().getString(R.string.esultate_giusti_nel_signore_title) + "', '"
                + appContext.getResources().getString(R.string.esultate_giusti_nel_signore_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.esultate_giusti_nel_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (154, " + appContext.getResources().getString(R.string.molto_mi_hanno_perseguitato_page) + ", '" + appContext.getResources().getString(R.string.molto_mi_hanno_perseguitato_title) + "', '"
                + appContext.getResources().getString(R.string.molto_mi_hanno_perseguitato_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.molto_mi_hanno_perseguitato_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (155, " + appContext.getResources().getString(R.string.ti_amo_signore_page)  + ", '" + appContext.getResources().getString(R.string.ti_amo_signore_title) + "', '"
                + appContext.getResources().getString(R.string.ti_amo_signore_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.ti_amo_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (156, " + appContext.getResources().getString(R.string.maria_madre_cammino_ardente_page) + ", '" + appContext.getResources().getString(R.string.maria_madre_cammino_ardente_title) + "', '"
                + appContext.getResources().getString(R.string.maria_madre_cammino_ardente_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.maria_madre_cammino_ardente_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (157, " + appContext.getResources().getString(R.string.shlom_lech_mariam_page) + ", '" + appContext.getResources().getString(R.string.shlom_lech_mariam_title) + "', '"
                + appContext.getResources().getString(R.string.shlom_lech_mariam_source) + "', 0, '" + BIANCO
                + "', '" + appContext.getResources().getString(R.string.shlom_lech_mariam_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (158, " + appContext.getResources().getString(R.string.andate_ed_annunziate_page) + ", '" + appContext.getResources().getString(R.string.andate_ed_annunziate_title) + "', '"
                + appContext.getResources().getString(R.string.andate_ed_annunziate_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.andate_ed_annunziate_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (159, " + appContext.getResources().getString(R.string.mi_indicherai_sentiero_vita_page) + ", '" + appContext.getResources().getString(R.string.mi_indicherai_sentiero_vita_title) + "', '"
                + appContext.getResources().getString(R.string.mi_indicherai_sentiero_vita_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.mi_indicherai_sentiero_vita_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (160, " + appContext.getResources().getString(R.string.o_dio_per_il_tuo_nome_page) + ", '" + appContext.getResources().getString(R.string.o_dio_per_il_tuo_nome_title) + "', '"
                + appContext.getResources().getString(R.string.o_dio_per_il_tuo_nome_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.o_dio_per_il_tuo_nome_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (161, " + appContext.getResources().getString(R.string.stolto_pensa_che_non_ce_dio_page) + ", '" + appContext.getResources().getString(R.string.stolto_pensa_che_non_ce_dio_title) + "', '"
                + appContext.getResources().getString(R.string.stolto_pensa_che_non_ce_dio_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.stolto_pensa_che_non_ce_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (162, " + appContext.getResources().getString(R.string.signore_ascolta_mia_preghiera_page) + ", '" + appContext.getResources().getString(R.string.signore_ascolta_mia_preghiera_title) + "', '"
                + appContext.getResources().getString(R.string.signore_ascolta_mia_preghiera_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.signore_ascolta_mia_preghiera_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (163, " + appContext.getResources().getString(R.string.non_e_qui_e_risorto_page) + ", '" + appContext.getResources().getString(R.string.non_e_qui_e_risorto_title) + "', '"
                + appContext.getResources().getString(R.string.non_e_qui_e_risorto_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.non_e_qui_e_risorto_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (164, " + appContext.getResources().getString(R.string.vi_prendero_dalle_genti_page) + ", '" + appContext.getResources().getString(R.string.vi_prendero_dalle_genti_title) + "', '"
                + appContext.getResources().getString(R.string.vi_prendero_dalle_genti_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.vi_prendero_dalle_genti_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (165, " + appContext.getResources().getString(R.string.voi_siete_la_luce_del_mondo_page) + ", '" + appContext.getResources().getString(R.string.voi_siete_la_luce_del_mondo_title) + "', '"
                + appContext.getResources().getString(R.string.voi_siete_la_luce_del_mondo_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.voi_siete_la_luce_del_mondo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (166, " + appContext.getResources().getString(R.string.sola_a_solo_page) + ", '" + appContext.getResources().getString(R.string.sola_a_solo_title) + "', '"
                + appContext.getResources().getString(R.string.sola_a_solo_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.sola_a_solo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (167, " + appContext.getResources().getString(R.string.in_mezzo_grande_folla_page) + ", '" + appContext.getResources().getString(R.string.in_mezzo_grande_folla_title) + "', '"
                + appContext.getResources().getString(R.string.in_mezzo_grande_folla_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.in_mezzo_grande_folla_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (168, " + appContext.getResources().getString(R.string.zaccheo_page)  + ", '" + appContext.getResources().getString(R.string.zaccheo_title) + "', '"
                + appContext.getResources().getString(R.string.zaccheo_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.zaccheo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (169, " + appContext.getResources().getString(R.string.siedi_solitario_silenzioso_page) + ", '" + appContext.getResources().getString(R.string.siedi_solitario_silenzioso_title) + "', '"
                + appContext.getResources().getString(R.string.siedi_solitario_silenzioso_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.siedi_solitario_silenzioso_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (170, " + appContext.getResources().getString(R.string.cosi_parla_amen_page) + ", '" + appContext.getResources().getString(R.string.cosi_parla_amen_title) + "', '"
                + appContext.getResources().getString(R.string.cosi_parla_amen_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.cosi_parla_amen_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (171, " + appContext.getResources().getString(R.string.ti_vedranno_i_re_page) + ", '" + appContext.getResources().getString(R.string.ti_vedranno_i_re_title) + "', '"
                + appContext.getResources().getString(R.string.ti_vedranno_i_re_source) + "', 0, '"
                + VERDE + "', '" + appContext.getResources().getString(R.string.ti_vedranno_i_re_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (172, " + appContext.getResources().getString(R.string.giacobbe_page) + ", '" + appContext.getResources().getString(R.string.giacobbe_title) + "', '"
                + appContext.getResources().getString(R.string.giacobbe_source) + "', 0, '"
                + VERDE + "', '" + appContext.getResources().getString(R.string.giacobbe_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (173, " + appContext.getResources().getString(R.string.debora_page) + ", '" + appContext.getResources().getString(R.string.debora_title) + "', '"
                + appContext.getResources().getString(R.string.debora_source) + "', 0, '" + VERDE
                + "', '" + appContext.getResources().getString(R.string.debora_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (174, " + appContext.getResources().getString(R.string.vedo_cieli_aperti_page) + ", '" + appContext.getResources().getString(R.string.vedo_cieli_aperti_title) + "', '"
                + appContext.getResources().getString(R.string.vedo_cieli_aperti_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.vedo_cieli_aperti_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (175, " + appContext.getResources().getString(R.string.il_signore_mi_ha_dato_page) + ", '" + appContext.getResources().getString(R.string.il_signore_mi_ha_dato_title) + "', '"
                + appContext.getResources().getString(R.string.il_signore_mi_ha_dato_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.il_signore_mi_ha_dato_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (176, " + appContext.getResources().getString(R.string.pigiatore_page) + ", '" + appContext.getResources().getString(R.string.pigiatore_title) + "', '"
                + appContext.getResources().getString(R.string.pigiatore_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.pigiatore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (177, " + appContext.getResources().getString(R.string.seminatore_page) + ", '" + appContext.getResources().getString(R.string.seminatore_title) + "', '"
                + appContext.getResources().getString(R.string.seminatore_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.seminatore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (178, " + appContext.getResources().getString(R.string.spirito_del_signore_sopra_di_me_page) + ", '" + appContext.getResources().getString(R.string.spirito_del_signore_sopra_di_me_title) + "', '"
                + appContext.getResources().getString(R.string.spirito_del_signore_sopra_di_me_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.spirito_del_signore_sopra_di_me_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (179, " + appContext.getResources().getString(R.string.ecco_lo_specchio_nostro_page) + ", '" + appContext.getResources().getString(R.string.ecco_lo_specchio_nostro_title) + "', '"
                + appContext.getResources().getString(R.string.ecco_lo_specchio_nostro_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.ecco_lo_specchio_nostro_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (180, " + appContext.getResources().getString(R.string.come_slancio_ira_page) + ", '" + appContext.getResources().getString(R.string.come_slancio_ira_title) + "', '"
                + appContext.getResources().getString(R.string.come_slancio_ira_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.come_slancio_ira_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (181, " + appContext.getResources().getString(R.string.benedetto_sia_iddio_page) + ", '" + appContext.getResources().getString(R.string.benedetto_sia_iddio_title) + "', '"
                + appContext.getResources().getString(R.string.benedetto_sia_iddio_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.benedetto_sia_iddio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (182, " + appContext.getResources().getString(R.string.signore_tu_scruti_conosci_page) + ", '" + appContext.getResources().getString(R.string.signore_tu_scruti_conosci_title) + "', '"
                + appContext.getResources().getString(R.string.signore_tu_scruti_conosci_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.signore_tu_scruti_conosci_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (183, " + appContext.getResources().getString(R.string.eli_eli_lamma_sabactani_page) + ", '" + appContext.getResources().getString(R.string.eli_eli_lamma_sabactani_title) + "', '"
                + appContext.getResources().getString(R.string.eli_eli_lamma_sabactani_source) + "', 0, '"
                + VERDE + "', '" + appContext.getResources().getString(R.string.eli_eli_lamma_sabactani_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (184, 215, 'Eli, Eli, lammà sabactani? - parte II - Salmo 21(22)', 'eli_eli_lamma_sabactani_II', 0, '"
//				+ VERDE + "', 'http://www.resuscicanti.com/elielilammasabactani.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (185, " + appContext.getResources().getString(R.string.nessuno_puo_servire_due_padroni_page) + ", '" + appContext.getResources().getString(R.string.nessuno_puo_servire_due_padroni_title) + "', '"
                + appContext.getResources().getString(R.string.nessuno_puo_servire_due_padroni_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.nessuno_puo_servire_due_padroni_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (186, " + appContext.getResources().getString(R.string.signore_mio_cuore_pretese_page) + ", '" + appContext.getResources().getString(R.string.signore_mio_cuore_pretese_title) + "', '"
                + appContext.getResources().getString(R.string.signore_mio_cuore_pretese_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.signore_mio_cuore_pretese_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (187, " + appContext.getResources().getString(R.string.voglio_andare_a_gerusalemme_page) + ", '" + appContext.getResources().getString(R.string.voglio_andare_a_gerusalemme_title) + "', '"
                + appContext.getResources().getString(R.string.voglio_andare_a_gerusalemme_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.voglio_andare_a_gerusalemme_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (188, " + appContext.getResources().getString(R.string.shema_israel_page) + ", '" + appContext.getResources().getString(R.string.shema_israel_title) + "', '"
                + appContext.getResources().getString(R.string.shema_israel_source) + "', 0, '"
                + VERDE + "', '" + appContext.getResources().getString(R.string.shema_israel_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (189, " + appContext.getResources().getString(R.string.inno_croce_gloriosa_page) + ", '" + appContext.getResources().getString(R.string.inno_croce_gloriosa_title) + "', '"
                + appContext.getResources().getString(R.string.inno_croce_gloriosa_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.inno_croce_gloriosa_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (190, " + appContext.getResources().getString(R.string.rivestitevi_dell_armatura_page) + ", '" + appContext.getResources().getString(R.string.rivestitevi_dell_armatura_title) + "', '"
                + appContext.getResources().getString(R.string.rivestitevi_dell_armatura_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.rivestitevi_dell_armatura_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (191, " + appContext.getResources().getString(R.string.sue_fondamenta_page) + ", '" + appContext.getResources().getString(R.string.sue_fondamenta_title) + "', '"
                + appContext.getResources().getString(R.string.sue_fondamenta_source) + "', 0, '"
                + VERDE
                + "', '" + appContext.getResources().getString(R.string.sue_fondamenta_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (192, " + appContext.getResources().getString(R.string.akeda_page) + ", '" + appContext.getResources().getString(R.string.akeda_title) + "', '"
                + appContext.getResources().getString(R.string.akeda_source) + "', 0, '" + VERDE
                + "', '" + appContext.getResources().getString(R.string.akeda_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (193, " + appContext.getResources().getString(R.string.non_ti_adirare_page) + ", '" + appContext.getResources().getString(R.string.non_ti_adirare_title) + "', '"
                + appContext.getResources().getString(R.string.non_ti_adirare_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.non_ti_adirare_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (194, " + appContext.getResources().getString(R.string.inno_alla_carita_page) + ", '" + appContext.getResources().getString(R.string.inno_alla_carita_title) + "', '"
                + appContext.getResources().getString(R.string.inno_alla_carita_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.inno_alla_carita_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (195, " + appContext.getResources().getString(R.string.stesso_iddio_page) + ", '" + appContext.getResources().getString(R.string.stesso_iddio_title) + "', '"
                + appContext.getResources().getString(R.string.stesso_iddio_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.stesso_iddio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (196, " + appContext.getResources().getString(R.string.come_condannati_a_morte_page) + ", '" + appContext.getResources().getString(R.string.come_condannati_a_morte_title) + "', '"
                + appContext.getResources().getString(R.string.come_condannati_a_morte_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.come_condannati_a_morte_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (197, " + appContext.getResources().getString(R.string.gesu_percorreva_page) + ", '" + appContext.getResources().getString(R.string.gesu_percorreva_title) + "', '"
                + appContext.getResources().getString(R.string.gesu_percorreva_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.gesu_percorreva_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (198, " + appContext.getResources().getString(R.string.non_resistete_al_male_page) + ", '" + appContext.getResources().getString(R.string.non_resistete_al_male_title) + "', '"
                + appContext.getResources().getString(R.string.non_resistete_al_male_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.non_resistete_al_male_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (199, " + appContext.getResources().getString(R.string.che_mi_baci_page) + ", '" + appContext.getResources().getString(R.string.che_mi_baci_title) + "', '"
                + appContext.getResources().getString(R.string.che_mi_baci_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.che_mi_baci_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (200, " + appContext.getResources().getString(R.string.mia_diletta_e_per_me_page) + ", '" + appContext.getResources().getString(R.string.mia_diletta_e_per_me_title) + "', '"
                + appContext.getResources().getString(R.string.mia_diletta_e_per_me_source) + "', 0, '"
                + AZZURRO
                + "', 'h" + appContext.getResources().getString(R.string.mia_diletta_e_per_me_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (201, " + appContext.getResources().getString(R.string.vieni_dal_libano_page) + ", '" + appContext.getResources().getString(R.string.vieni_dal_libano_title) + "', '"
                + appContext.getResources().getString(R.string.vieni_dal_libano_source)+ "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.vieni_dal_libano_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (202, " + appContext.getResources().getString(R.string.quando_dormivo_page) + ", '" + appContext.getResources().getString(R.string.quando_dormivo_title) + "', '"
                + appContext.getResources().getString(R.string.quando_dormivo_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.quando_dormivo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (203, " + appContext.getResources().getString(R.string.tu_che_abiti_nei_giardini_page) + ", '" + appContext.getResources().getString(R.string.tu_che_abiti_nei_giardini_title) + "', '"
                + appContext.getResources().getString(R.string.tu_che_abiti_nei_giardini_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.tu_che_abiti_nei_giardini_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (204, " + appContext.getResources().getString(R.string.agnella_di_dio_page) + ", '" + appContext.getResources().getString(R.string.agnella_di_dio_title) + "', '"
                + appContext.getResources().getString(R.string.agnella_di_dio_source) + "', 0, '" + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.agnella_di_dio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (205, " + appContext.getResources().getString(R.string.non_ce_in_lui_bellezza_page) + ", '" + appContext.getResources().getString(R.string.non_ce_in_lui_bellezza_title) + "', '"
                + appContext.getResources().getString(R.string.non_ce_in_lui_bellezza_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.non_ce_in_lui_bellezza_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (206, " + appContext.getResources().getString(R.string.canto_dell_agnello_page) + ", '" + appContext.getResources().getString(R.string.canto_dell_agnello_title) + "', '"
                + appContext.getResources().getString(R.string.canto_dell_agnello_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.canto_dell_agnello_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (207, " + appContext.getResources().getString(R.string.chi_e_colei_page) + ", '" + appContext.getResources().getString(R.string.chi_e_colei_title) + "', '"
                + appContext.getResources().getString(R.string.chi_e_colei_source) + "', 0, '"
                + AZZURRO + "', '" + appContext.getResources().getString(R.string.chi_e_colei_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (208, " + appContext.getResources().getString(R.string.voce_del_mio_amato_page) + ", '" + appContext.getResources().getString(R.string.voce_del_mio_amato_title) + "', '"
                + appContext.getResources().getString(R.string.voce_del_mio_amato_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.voce_del_mio_amato_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (209, " + appContext.getResources().getString(R.string.colomba_volo_page) + ", '" + appContext.getResources().getString(R.string.colomba_volo_title) + "', '"
                + appContext.getResources().getString(R.string.colomba_volo_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.colomba_volo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (210, " + appContext.getResources().getString(R.string.come_stilla_il_miele_page) + ", '" + appContext.getResources().getString(R.string.come_stilla_il_miele_title) + "', '"
                + appContext.getResources().getString(R.string.come_stilla_il_miele_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.come_stilla_il_miele_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (211, " + appContext.getResources().getString(R.string.o_gesu_amore_mio_page) + ", '" + appContext.getResources().getString(R.string.o_gesu_amore_mio_title) + "', '"
                + appContext.getResources().getString(R.string.o_gesu_amore_mio_source) + "', 0, '" + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.o_gesu_amore_mio_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (212, " + appContext.getResources().getString(R.string.portami_in_cielo_page) + ", '" + appContext.getResources().getString(R.string.portami_in_cielo_title) + "', '"
                + appContext.getResources().getString(R.string.portami_in_cielo_source) + "', 0, '" + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.portami_in_cielo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (213, " + appContext.getResources().getString(R.string.tu_sei_mia_speranza_signore_page) + ", '" + appContext.getResources().getString(R.string.tu_sei_mia_speranza_signore_title) + "', '"
                + appContext.getResources().getString(R.string.tu_sei_mia_speranza_signore_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.tu_sei_mia_speranza_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (214, " + appContext.getResources().getString(R.string.una_donna_vestita_di_sole_page) + ", '" + appContext.getResources().getString(R.string.una_donna_vestita_di_sole_title) + "', '"
                + appContext.getResources().getString(R.string.una_donna_vestita_di_sole_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.una_donna_vestita_di_sole_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (215, " + appContext.getResources().getString(R.string.ho_steso_le_mani_page) + ", '" + appContext.getResources().getString(R.string.ho_steso_le_mani_title) + "', '"
                + appContext.getResources().getString(R.string.ho_steso_le_mani_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.ho_steso_le_mani_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (216, " + appContext.getResources().getString(R.string.omelia_pasquale_melitone_sardi_page) + ", '" + appContext.getResources().getString(R.string.omelia_pasquale_melitone_sardi_title) + "', '"
                + appContext.getResources().getString(R.string.omelia_pasquale_melitone_sardi_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.omelia_pasquale_melitone_sardi_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (217, " + appContext.getResources().getString(R.string.carmen_63_page) + ", '" + appContext.getResources().getString(R.string.carmen_63_title) + "', '"
                + appContext.getResources().getString(R.string.carmen_63_source) + "', 0, '"
                + AZZURRO + "', '" + appContext.getResources().getString(R.string.carmen_63_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (218, " + appContext.getResources().getString(R.string.caritas_christi_page) + ", '" + appContext.getResources().getString(R.string.caritas_christi_title) + "', '"
                + appContext.getResources().getString(R.string.caritas_christi_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.caritas_christi_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (219, " + appContext.getResources().getString(R.string.noli_me_tangere_page) + ", '" + appContext.getResources().getString(R.string.noli_me_tangere_title) + "', '"
                + appContext.getResources().getString(R.string.noli_me_tangere_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.noli_me_tangere_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (220, " + appContext.getResources().getString(R.string.signore_aiutami_signore_page) + ", '" + appContext.getResources().getString(R.string.signore_aiutami_signore_title) + "', '"
                + appContext.getResources().getString(R.string.signore_aiutami_signore_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.signore_aiutami_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (221, " + appContext.getResources().getString(R.string.mi_hai_sedotto_signore_page) + ", '" + appContext.getResources().getString(R.string.mi_hai_sedotto_signore_title) + "', '"
                + appContext.getResources().getString(R.string.mi_hai_sedotto_signore_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.mi_hai_sedotto_signore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (222, " + appContext.getResources().getString(R.string.amate_i_vostri_nemici_page) + ", '" + appContext.getResources().getString(R.string.amate_i_vostri_nemici_title) + "', '"
                + appContext.getResources().getString(R.string.amate_i_vostri_nemici_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.amate_i_vostri_nemici_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (223, " + appContext.getResources().getString(R.string.tu_sei_bella_amica_mia_page) + ", '" + appContext.getResources().getString(R.string.tu_sei_bella_amica_mia_title) + "', '"
                + appContext.getResources().getString(R.string.tu_sei_bella_amica_mia_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.tu_sei_bella_amica_mia_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (224, " + appContext.getResources().getString(R.string.fratelli_non_diamo_a_nessuno_page) + ", '" + appContext.getResources().getString(R.string.fratelli_non_diamo_a_nessuno_title) + "', '"
                + appContext.getResources().getString(R.string.fratelli_non_diamo_a_nessuno_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.fratelli_non_diamo_a_nessuno_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (225, " + appContext.getResources().getString(R.string.questo_e_io_mio_comandamento_page) + ", '" + appContext.getResources().getString(R.string.questo_e_io_mio_comandamento_title) + "', '"
                + appContext.getResources().getString(R.string.questo_e_io_mio_comandamento_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.questo_e_io_mio_comandamento_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (226, " + appContext.getResources().getString(R.string.mi_rubasti_cuore_page) + ", '" + appContext.getResources().getString(R.string.mi_rubasti_cuore_title) + "', '"
                + appContext.getResources().getString(R.string.mi_rubasti_cuore_source)+  "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.mi_rubasti_cuore_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (227, " + appContext.getResources().getString(R.string.in_una_notte_oscura_page) + ", '" + appContext.getResources().getString(R.string.in_una_notte_oscura_title) + "', '"
                + appContext.getResources().getString(R.string.in_una_notte_oscura_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.in_una_notte_oscura_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (228, " + appContext.getResources().getString(R.string.se_siete_risorti_page) + ", '" + appContext.getResources().getString(R.string.se_siete_risorti_title) + "', '"
                + appContext.getResources().getString(R.string.se_siete_risorti_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.se_siete_risorti_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (229, " + appContext.getResources().getString(R.string.una_gran_senal_page) + ", '" + appContext.getResources().getString(R.string.una_gran_senal_title) + "', '"
                + appContext.getResources().getString(R.string.una_gran_senal_source) + "', 0, '"
                + AZZURRO
                + "', '" + appContext.getResources().getString(R.string.una_gran_senal_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (230, " + appContext.getResources().getString(R.string.resurrexit_page) + ", '" + appContext.getResources().getString(R.string.resurrexit_title) + "', '"
                + appContext.getResources().getString(R.string.resurrexit_source) + "', 0, '"
                + BIANCO
                + "', '" + appContext.getResources().getString(R.string.resurrexit_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (231, " + appContext.getResources().getString(R.string.ecco_il_mio_servo_page) + ", '" + appContext.getResources().getString(R.string.ecco_il_mio_servo_title) + "', '"
                + appContext.getResources().getString(R.string.ecco_il_mio_servo_source) + "', 0, '"
                + GRIGIO
                + "', '" + appContext.getResources().getString(R.string.ecco_il_mio_servo_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (232, " + appContext.getResources().getString(R.string.ave_maria_colomba_page) + ", '" + appContext.getResources().getString(R.string.ave_maria_colomba_title) + "', '"
                + appContext.getResources().getString(R.string.ave_maria_colomba_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.ave_maria_colomba_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (233, " + appContext.getResources().getString(R.string.messia_leone_vincere_page) + ", '" + appContext.getResources().getString(R.string.messia_leone_vincere_title) + "', '"
                + appContext.getResources().getString(R.string.messia_leone_vincere_source) + "', 0, '"
                + BIANCO + "', '" + appContext.getResources().getString(R.string.messia_leone_vincere_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);
        // FINE CANTI

        // TITOLI DEGLI ARGOMENTI
        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (1, '" + appContext.getResources().getString(R.string.Canti_della_Bibbia_Antico_Testamento) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (2, '" + appContext.getResources().getString(R.string.Canti_della_Bibbia_Nuovo_Testamento) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (3, '" + appContext.getResources().getString(R.string.Canti_dalle_Odi_di_Salomone) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (4, '" + appContext.getResources().getString(R.string.Ispirati_a_melodie_e_rituali_ebraici) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (5, '" + appContext.getResources().getString(R.string.Canti_per_bambini) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (6, '" + appContext.getResources().getString(R.string.Dall_ordinario_della_Messa) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (7, '" + appContext.getResources().getString(R.string.Canti_per_la_frazione_del_pane) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (8, '" + appContext.getResources().getString(R.string.Dalla_liturgia_della_veglia_pasquale) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (9, '" + appContext.getResources().getString(R.string.Canti_per_il_sacramento_della_riconciliazione) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (10, '" + appContext.getResources().getString(R.string.Inni_liturgici) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (11, '" + appContext.getResources().getString(R.string.Canti_a_Maria) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (12, '" + appContext.getResources().getString(R.string.Vari) + "')";
        db.execSQL(sql);
        // FINE TITOLI DEGLI ARGOMENTI

        // LEGAMI ARGOMENTI-CANTI
        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 67)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 172)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 43)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 68)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 109)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 188)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 39)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 173)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 40)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 139)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 96)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 151)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 131)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 123)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 41)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 161)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 159)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 100)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 15)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 183)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 72)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 71)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 148)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 53)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 127)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 153)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 92)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 136)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 193)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 94)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 97)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 141)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 106)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 75)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 74)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 160)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 95)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 122)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 124)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 143)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 115)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 191)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 83)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 82)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 98)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 114)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 138)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 70)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 120)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 59)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 69)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 102)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 44)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 80)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 93)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 150)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 60)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 91)";
        db.execSQL(sql);
        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 142)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 154)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 38)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 186)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 46)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 45)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 47)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 125)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 48)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 73)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 110)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 182)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 149)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 146)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 162)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 108)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 51)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 52)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 113)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 199)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 200)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 208)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 201)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 226)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 202)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 223)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 207)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 203)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 107)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 147)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 99)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 81)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 42)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 104)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 118)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 63)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 171)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 175)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 105)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 178)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 176)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 152)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 221)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 169)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 164)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 49)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 50)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (1, 111)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 165)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 198)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 33)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 185)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 197)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 121)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 163)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 158)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 177)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 77)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 78)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 132)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 58)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 61)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 178)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 222)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 167)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 168)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 126)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 137)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 225)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 128)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 219)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 56)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 57)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 196)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 194)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 35)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 62)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 195)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 218)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 224)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 181)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 190)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 76)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 170)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 206)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 90)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 214)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 229)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 55)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 174)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 66)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (2, 65)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (3, 180)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (3, 210)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (3, 179)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (3, 209)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (3, 213)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 101)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 119)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 54)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 84)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 117)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 45)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 5)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (4, 187)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (5, 144)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (5, 125)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (5, 101)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (5, 119)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (5, 89)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (5, 88)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 30)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 31)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 4)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 34)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 11)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 12)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 26)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 27)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 28)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 5)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 6)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 7)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 8)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 9)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 10)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 33)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (6, 140)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 140)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 206)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 175)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 205)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 215)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 176)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (7, 216)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 29)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 13)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 14)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 15)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 16)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 17)";
        db.execSQL(sql);
        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (8, 8)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (9, 3)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (9, 1)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (9, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 21)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 85)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 77)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 78)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 61)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 18)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 20)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 22)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 24)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 86)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 19)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 58)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 32)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 133)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 23)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 25)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (10, 87)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 204)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 77)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 78)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 157)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 132)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 135)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 145)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 156)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 128)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 133)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 129)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 134)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (11, 232)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 192)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 217)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 112)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 227)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 189)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 37)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 130)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 211)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 64)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 212)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 220)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 36)";
        db.execSQL(sql);

        sql = "INSERT INTO ARGOMENTI ";
        sql += "VALUES (12, 116)";
        db.execSQL(sql);
        // FINE LEGAMI ARGOMENTI-CANTI

        //SALMI CANTATI
        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (38, '129', '" + appContext.getResources().getString(R.string.salmo_129) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (40, '102', '" + appContext.getResources().getString(R.string.salmo_102) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (41, '012', '" + appContext.getResources().getString(R.string.salmo_012) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (44, '118', '" + appContext.getResources().getString(R.string.salmo_118) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (45, '132', '" + appContext.getResources().getString(R.string.salmo_132_guardate) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (46, '132', '" + appContext.getResources().getString(R.string.salmo_132_come_bello) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (47, '132', '" + appContext.getResources().getString(R.string.salmo_132_guardate_gustate) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (48, '135', '" + appContext.getResources().getString(R.string.salmo_135) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (51, '148', '" + appContext.getResources().getString(R.string.salmo_148) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (52, '150', '" + appContext.getResources().getString(R.string.salmo_150) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (53, '026', '" + appContext.getResources().getString(R.string.salmo_026) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (59, '114-115', '" + appContext.getResources().getString(R.string.salmo_114_115) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (60, '125', '" + appContext.getResources().getString(R.string.salmo_125) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (69, '116', '" + appContext.getResources().getString(R.string.salmo_116) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (70, '113A', '" + appContext.getResources().getString(R.string.salmo_113A) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (71, '023', '" + appContext.getResources().getString(R.string.salmo_023) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (72, '022', '" + appContext.getResources().getString(R.string.salmo_022) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (73, '136', '" + appContext.getResources().getString(R.string.salmo_136) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (74, '050', '" + appContext.getResources().getString(R.string.salmo_050_pieta) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (75, '050', '" + appContext.getResources().getString(R.string.salmo_050_misericordia) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (80, '120', '" + appContext.getResources().getString(R.string.salmo_120) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (82, '094', '" + appContext.getResources().getString(R.string.salmo_094_se_oggi) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (83, '094', '" + appContext.getResources().getString(R.string.salmo_094_venite) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (91, '126', '" + appContext.getResources().getString(R.string.salmo_126) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (92, '033', '" + appContext.getResources().getString(R.string.salmo_033_gustate) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (93, '121', '" + appContext.getResources().getString(R.string.salmo_121) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (94, '039', '" + appContext.getResources().getString(R.string.salmo_039) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (95, '056', '" + appContext.getResources().getString(R.string.salmo_056) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (96, '002', '" + appContext.getResources().getString(R.string.salmo_002) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (97, '041-042', '" + appContext.getResources().getString(R.string.salmo_041_042) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (98, '099', '" + appContext.getResources().getString(R.string.salmo_099) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (100, '016', '" + appContext.getResources().getString(R.string.salmo_016) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (102, '117', '" + appContext.getResources().getString(R.string.salmo_117) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (105, '062', '" + appContext.getResources().getString(R.string.salmo_062) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (106, '046', '" + appContext.getResources().getString(R.string.salmo_046) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (108, '145', '" + appContext.getResources().getString(R.string.salmo_145) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (110, '137', '" + appContext.getResources().getString(R.string.salmo_137) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (115, '083', '" + appContext.getResources().getString(R.string.salmo_083) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (120, '114', '" + appContext.getResources().getString(R.string.salmo_114) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (122, '064', '" + appContext.getResources().getString(R.string.salmo_064) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (123, '010', '" + appContext.getResources().getString(R.string.salmo_010) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (124, '067-v12', '" + appContext.getResources().getString(R.string.salmo_067_v12) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (125, '133', '" + appContext.getResources().getString(R.string.salmo_133) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (127, '031', '" + appContext.getResources().getString(R.string.salmo_031) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (131, '008', '" + appContext.getResources().getString(R.string.salmo_008) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (136, '033', '" + appContext.getResources().getString(R.string.salmo_033_benediro) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (138, '109', '" + appContext.getResources().getString(R.string.salmo_109) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (139, '001', '" + appContext.getResources().getString(R.string.salmo_001) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (141, '044', '" + appContext.getResources().getString(R.string.salmo_044) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (142, '127', '" + appContext.getResources().getString(R.string.salmo_127) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (143, '067-v02', '" + appContext.getResources().getString(R.string.salmo_067_v02) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (146, '141', '"  +appContext.getResources().getString(R.string.salmo_141) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (148, '024', '" + appContext.getResources().getString(R.string.salmo_024) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (149, '140', '" + appContext.getResources().getString(R.string.salmo_140) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (150, '122', '" + appContext.getResources().getString(R.string.salmo_122)+  "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (151, '006', '" + appContext.getResources().getString(R.string.salmo_006) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (153, '032', '" + appContext.getResources().getString(R.string.salmo_032) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (154, '128', '" + appContext.getResources().getString(R.string.salmo_128) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (155, '017', '" + appContext.getResources().getString(R.string.salmo_017) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (159, '015', '" + appContext.getResources().getString(R.string.salmo_015) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (160, '053', '" + appContext.getResources().getString(R.string.salmo_053) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (161, '013', '" + appContext.getResources().getString(R.string.salmo_013) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (162, '142', '" + appContext.getResources().getString(R.string.salmo_142) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (182, '138', '" + appContext.getResources().getString(R.string.salmo_138) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (183, '021', '" + appContext.getResources().getString(R.string.salmo_021) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (186, '130', '"  +appContext.getResources().getString(R.string.salmo_130) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (191, '086', '" + appContext.getResources().getString(R.string.salmo_086) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (193, '036', '" + appContext.getResources().getString(R.string.salmo_036) + "')";
        db.execSQL(sql);
        //FINE SALMI CANTATI

        // TITOLI DELL'INDICE LITURGICO
        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (1, '" + appContext.getResources().getString(R.string.tempo_avvento) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (2, '" + appContext.getResources().getString(R.string.tempo_natale) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (3, '" + appContext.getResources().getString(R.string.tempo_quaresima) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (4, '" + appContext.getResources().getString(R.string.tempo_pasqua) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (5, '" + appContext.getResources().getString(R.string.canti_pentecoste) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (6, '" + appContext.getResources().getString(R.string.canti_vergine) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (7, '" + appContext.getResources().getString(R.string.lodi_vespri) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (8, '" + appContext.getResources().getString(R.string.canti_ingresso) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (9, '" + appContext.getResources().getString(R.string.canti_pace) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (10, '" + appContext.getResources().getString(R.string.canti_pane) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (11, '" + appContext.getResources().getString(R.string.canti_vino) + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (12, '" + appContext.getResources().getString(R.string.canti_fine) + "')";
        db.execSQL(sql);
        // FINE TITOLI DELL'INDICE LITURGICO

        // LEGAMI INDICE LITURGICO-CANTI
        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 118)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 173)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 94)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 124)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 87)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 159)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 63)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 105)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 212)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 213)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 214)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 116)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (1, 66)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 204)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 66)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 144)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 89)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 107)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 147)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (2, 88)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 150)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 146)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 148)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 122)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 192)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 100)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 80)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 120)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 40)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 136)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 114)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 97)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 170)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 38)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 104)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 179)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 183)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 126)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 41)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 44)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 73)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 94)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 215)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 76)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 42)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 37)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 1)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 79)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 74)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 75)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 193)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 211)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 96)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 130)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 60)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 190)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 123)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 169)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 220)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 129)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 213)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 121)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (3, 95)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 192)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 21)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 85)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 125)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 68)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 101)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 199)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 84)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 179)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 86)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 152)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 52)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 219)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 102)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 216)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 35)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 202)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 70)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 62)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 174)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (4, 203)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 85)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 86)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 64)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 212)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 25)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 188)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 147)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 23)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 214)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (5, 116)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 204)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 77)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 78)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 173)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 132)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 58)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 135)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 145)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 156)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 128)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 79)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 133)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 157)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 166)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 129)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 214)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (6, 134)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 56)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 67)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 192)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 100)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 21)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 80)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 120)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 181)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 114)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 136)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 49)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 50)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 206)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 57)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 180)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 97)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 110)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 138)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 179)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 44)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 73)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 99)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 189)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 86)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 209)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 178)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 52)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 51)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 69)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 58)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 74)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 75)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 105)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 131)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 202)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 70)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 96)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 190)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 91)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 82)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (7, 121)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 67)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 71)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 80)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 20)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 85)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 90)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 132)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 196)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 180)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 118)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 104)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 44)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 99)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 189)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 177)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 124)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 152)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 69)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 135)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 159)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 131)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 60)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 115)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 91)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 171)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 141)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 174)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 36)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (8, 201)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 114)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 125)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 119)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 109)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 118)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 84)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 54)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 48)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 45)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 46)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 47)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (9, 93)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 192)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 206)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 215)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 176)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 175)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 76)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 205)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 211)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 216)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (10, 171)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 56)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 85)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 274)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 109)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 199)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 57)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 207)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 180)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 210)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 203)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 55)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 48)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 72)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 194)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 209)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 208)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 198)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 211)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 62)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 64)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 202)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 35)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 220)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (11, 147)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 98)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 158)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 78)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 132)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 125)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 109)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 84)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 203)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 117)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 48)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 99)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 152)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 176)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 137)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 191)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 178)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 69)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 212)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 60)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 133)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 157)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 66)";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT ";
        sql += "VALUES (12, 187)";
        db.execSQL(sql);
        // FINE LEGAMI INDICE LITURGICO-CANTI

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Backup[] backup = backupTables(oldVersion, newVersion, db);

        BackupLocalLink[] backupLink = backupLocalLink(oldVersion, newVersion, db);

        reCreateDatabse(db);

        repopulateDB(oldVersion, newVersion, db, backup, backupLink);

        //cancella dalle liste predefinite i canti inesistenti
        String sql = "SELECT _id, position, id_canto FROM CUST_LISTS";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            sql = "SELECT _id" +
                    "  FROM ELENCO" +
                    "  WHERE _id =  " + cursor.getInt(2);
            Cursor cCheckExists = db.rawQuery(sql, null);
//            Log.i("ESISTE?", cCheckExists.getCount() + "");
            if (cCheckExists.getCount() == 0)
                db.delete("CUST_LISTS", "_id = " + cursor.getInt(0) + " AND position = " + cursor.getInt(1), null);
            cCheckExists.close();
            cursor.moveToNext();
        }

        cursor.close();

    }

    public Backup[] backupTables(int oldVersion, int newVersion, SQLiteDatabase db) {

        Backup[] backup = new Backup[300];
        if(oldVersion >= 21) {
            String sql = "SELECT _id, zoom, scroll_x, scroll_y, favourite, saved_tab, saved_barre, saved_speed FROM ELENCO";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Backup cantoBackup = new Backup();
                cantoBackup.setId(cursor.getInt(0));
                cantoBackup.setZoom(cursor.getInt(1));
                cantoBackup.setScroll_x(cursor.getInt(2));
                cantoBackup.setScroll_y(cursor.getInt(3));
                cantoBackup.setFavourite(cursor.getInt(4));
                cantoBackup.setNota(cursor.getString(5));
                cantoBackup.setBarre(cursor.getString(6));
                cantoBackup.setSpeed(cursor.getInt(7));
                backup[i] = cantoBackup;
                cursor.moveToNext();
            }
            cursor.close();
        }
        else if(oldVersion >= 20) {
            String sql = "SELECT _id, zoom, scroll_x, scroll_y, favourite, saved_tab, saved_barre FROM ELENCO";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Backup cantoBackup = new Backup();
                cantoBackup.setId(cursor.getInt(0));
                cantoBackup.setZoom(cursor.getInt(1));
                cantoBackup.setScroll_x(cursor.getInt(2));
                cantoBackup.setScroll_y(cursor.getInt(3));
                cantoBackup.setFavourite(cursor.getInt(4));
                cantoBackup.setNota(cursor.getString(5));
                cantoBackup.setBarre(cursor.getString(6));
                backup[i] = cantoBackup;
                cursor.moveToNext();
            }
            cursor.close();
        }
        else if(oldVersion == 19) {
            String sql = "SELECT _id, zoom, scroll_x, scroll_y, favourite, saved_tab FROM ELENCO";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Backup cantoBackup = new Backup();
                cantoBackup.setId(cursor.getInt(0));
                cantoBackup.setZoom(cursor.getInt(1));
                cantoBackup.setScroll_x(cursor.getInt(2));
                cantoBackup.setScroll_y(cursor.getInt(3));
                cantoBackup.setFavourite(cursor.getInt(4));
                cantoBackup.setNota(cursor.getString(5));
                backup[i] = cantoBackup;
                cursor.moveToNext();
            }
            cursor.close();
        }

        return backup;
    }

    public BackupLocalLink[] backupLocalLink(int oldVersion, int newVersion, SQLiteDatabase db) {

        //dalla versionee 25 è stata introdotta la tabella di link locali. Va fatto il backup
        BackupLocalLink[] backupLink = new BackupLocalLink[300];
        if(oldVersion >= 25) {
            String sql = "SELECT _id, local_path FROM LOCAL_LINKS";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                BackupLocalLink localLinkBackup = new BackupLocalLink();
                localLinkBackup.setIdCanto(cursor.getInt(0));
                localLinkBackup.setLocalPath(cursor.getString(1));
                backupLink[i] = localLinkBackup;
                cursor.moveToNext();
            }
            cursor.close();
        }

        return backupLink;
    }

    public void repopulateDB(int oldVersion, int newVersion, SQLiteDatabase db, Backup[] backup, BackupLocalLink[] backupLink) {
        ContentValues values = null;

        if (newVersion == 39 && oldVersion >= 19 && oldVersion <= 38) {
            //ricodifica i titoli dei canti con i loro ID
            String sql = "SELECT _id, lista FROM LISTE_PERS";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                int idLista = cursor.getInt(0);
                ListaPersonalizzata lista = (ListaPersonalizzata) ListaPersonalizzata
                        .deserializeObject(cursor.getBlob(1));

                int totPosiz = lista.getNumPosizioni();

                for (int j = 0; j < totPosiz; j++) {
                    if (!lista.getCantoPosizione(j).equals("")) {
//						Log.i("NOME DUP", lista.getCantoPosizione(j).substring(10));
                        String nomeCanto = Utility.duplicaApostrofi(lista.getCantoPosizione(j)).substring(10);
//						Log.i("NOME NO-DUP", nomeCanto.substring(10));
                        sql = "SELECT _id" +
                                "  FROM ELENCO" +
                                "  WHERE titolo =  '" + nomeCanto + "'";
                        Cursor cCheckExists = db.rawQuery(sql, null);
//						Log.i("ESISTE?", cCheckExists.getCount() + "");
                        if (cCheckExists.getCount() == 0)
                            lista.removeCanto(j);
                        else {
                            cCheckExists.moveToFirst();
                            lista.addCanto(String.valueOf(cCheckExists.getInt(0)), j);
                        }

                        cCheckExists.close();

                    }
                }
                values = new  ContentValues( );
                values.put("lista" , ListaPersonalizzata.serializeObject(lista));
                db.update("LISTE_PERS", values, "_id = " + idLista, null );
                cursor.moveToNext();
            }

            cursor.close();
        }

        if (oldVersion >= 19) {
            for (int i = 0; i < backup.length; i++) {
                if (backup[i] == null)
                    break;
                values = new  ContentValues( );
                values.put("zoom" , backup[i].getZoom());
                //Nella versione 22 sono stati completati i ritornelli di tutti i canti,
                //quindi meglio resettare lo scroll salvato
                if (newVersion != 22) {
                    values.put("scroll_x", backup[i].getScroll_x());
                    values.put("scroll_y", backup[i].getScroll_y());
                }
                values.put("favourite", backup[i].getFavourite());
                values.put("saved_tab", backup[i].getNota());
                if (oldVersion >= 20)
                    values.put("saved_barre", backup[i].getBarre());
                if (oldVersion >= 21)
                    values.put("saved_speed", backup[i].getSpeed());
                db.update("ELENCO", values, "_id = " + backup[i].getId(), null );
            }

            //cancella dalle liste i canti inesistenti
            String sql = "SELECT _id, lista FROM LISTE_PERS";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                int idLista = cursor.getInt(0);
                ListaPersonalizzata lista = (ListaPersonalizzata) ListaPersonalizzata
                        .deserializeObject(cursor.getBlob(1));

                int totPosiz = lista.getNumPosizioni();

                for (int j = 0; j < totPosiz; j++) {
                    if (!lista.getCantoPosizione(j).equals("")) {
//						Log.i("NOME DUP", lista.getCantoPosizione(j).substring(10));
//                        String nomeCanto = Utility.duplicaApostrofi(lista.getCantoPosizione(j)).substring(10);
//						Log.i("NOME NO-DUP", nomeCanto.substring(10));
//                        sql = "SELECT _id" +
//                                "  FROM ELENCO" +
//                                "  WHERE titolo =  '" + nomeCanto + "'";
                          sql = "SELECT _id" +
                                "  FROM ELENCO" +
                                "  WHERE _id = " + lista.getCantoPosizione(j);
                        Cursor cCheckExists = db.rawQuery(sql, null);
//						Log.i("ESISTE?", cCheckExists.getCount() + "");
                        if (cCheckExists.getCount() == 0)
                            lista.removeCanto(j);

                        cCheckExists.close();

                    }
                }
                values = new  ContentValues( );
                values.put("lista" , ListaPersonalizzata.serializeObject(lista));
                db.update("LISTE_PERS", values, "_id = " + idLista, null );
                cursor.moveToNext();
            }

            cursor.close();
        }

        if (oldVersion >= 25) {
            for (int i = 0; i < backupLink.length; i++) {
                if (backupLink[i] == null)
                    break;
                values = new  ContentValues( );
                values.put("_id", backupLink[i].getIdCanto());
                values.put("local_path", backupLink[i].getLocalPath());
                db.insert("LOCAL_LINKS", null, values);
            }
        }

        //cancella dalle liste predefinite i canti inesistenti
        String sql = "SELECT _id, position, id_canto FROM CUST_LISTS";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            sql = "SELECT _id" +
                    "  FROM ELENCO" +
                    "  WHERE _id =  " + cursor.getInt(2);
            Cursor cCheckExists = db.rawQuery(sql, null);
//            Log.i("ESISTE?", cCheckExists.getCount() + "");
            if (cCheckExists.getCount() == 0)
                db.delete("CUST_LISTS", "_id = " + cursor.getInt(0) + " AND position = " + cursor.getInt(1), null);
            cCheckExists.close();
            cursor.moveToNext();
        }

        cursor.close();

    }

    public void reCreateDatabse(SQLiteDatabase db) {
        String sql = "DROP TABLE IF EXISTS ELENCO";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS ARGOMENTI";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS ARG_NAMES";
        db.execSQL(sql);
//		sql = "DROP TABLE IF EXISTS CUST_LISTS";
//		db.execSQL(sql);
//		sql = "DROP TABLE IF EXISTS LISTE_PERS";
//		db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS INDICE_LIT";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS INDICE_LIT_NAMES";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS SALMI_MUSICA";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS LOCAL_LINKS";
        db.execSQL(sql);

        onCreate(db);
    }

    public class Backup {
        private int id;
        private int zoom;
        private int scroll_x;
        private int scroll_y;
        private int favourite;
        private String nota;
        private String barre;
        private int speed;

        public Backup() {
            this.id = 0;
            this.zoom = 0;
            this.scroll_x = 0;
            this.scroll_y = 0;
            this.favourite = 0;
            this.nota = null;
            this.barre = null;
            this.speed = 2;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getZoom() {
            return zoom;
        }

        public void setZoom(int zoom) {
            this.zoom = zoom;
        }

        public int getScroll_x() {
            return scroll_x;
        }

        public void setScroll_x(int scroll_x) {
            this.scroll_x = scroll_x;
        }

        public int getScroll_y() {
            return scroll_y;
        }

        public void setScroll_y(int scroll_y) {
            this.scroll_y = scroll_y;
        }

        public int getFavourite() {
            return favourite;
        }

        public void setFavourite(int favourite) {
            this.favourite = favourite;
        }

        public String getNota() {
            return nota;
        }

        public void setNota(String nota) {
            this.nota = nota;
        }

        public String getBarre() {
            return barre;
        }

        public void setBarre(String barre) {
            this.barre = barre;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }
    }

    public class BackupLocalLink{
        private int idCanto;
        private String localPath;


        public BackupLocalLink() {
            this.idCanto = 0;
            this.localPath = "";
        }

        public int getIdCanto() {
            return idCanto;
        }

        public void setIdCanto(int idCanto) {
            this.idCanto = idCanto;
        }

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }
    }

}