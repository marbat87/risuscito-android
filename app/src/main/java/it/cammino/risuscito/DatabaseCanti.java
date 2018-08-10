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
    private static final int DB_VERSION = 53;

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

        //nuova tabella canti consegnati
        sql = "CREATE TABLE IF NOT EXISTS CANTI_CONSEGNATI (";
        sql += "_id INTEGER NOT NULL,";
        sql += "id_canto TEXT NOT NULL,";
        sql += "PRIMARY KEY (_id, id_canto)";
        sql += ");";
        db.execSQL(sql);

        //nuova tabella canti consegnati
        sql = "CREATE TABLE IF NOT EXISTS CRONOLOGIA (";
        sql += "id_canto INTEGER NOT NULL,";
        sql += "ultima_visita TIMESTAMP DEFAULT CURRENT_TIMESTAMP,";
        sql += "PRIMARY KEY (id_canto)";
        sql += ");";
        db.execSQL(sql);

        // CANTI
        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (1, '" + "R.string.litanie_penitenziali_brevi_page'" + ", '" + "R.string.litanie_penitenziali_brevi_title" + "', '"
                + "R.string.litanie_penitenziali_brevi_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.litanie_penitenziali_brevi_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (2, '" + "R.string.preghiera_litanica_penitenziale_page'" + ", '" + "R.string.preghiera_litanica_penitenziale_title" + "', '"
                + "R.string.preghiera_litanica_penitenziale_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.preghiera_litanica_penitenziale_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (3, '" + "R.string.celebrazione_penitenziale_page'" + ", '" + "R.string.celebrazione_penitenziale_title" + "', '"
                + "R.string.celebrazione_penitenziale_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.celebrazione_penitenziale_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (4, '" + "R.string.gloria_a_dio_cieli_page'" + ", '" + "R.string.gloria_a_dio_cieli_title" + "', '"
                + "R.string.gloria_a_dio_cieli_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.gloria_a_dio_cieli_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (5, '" + "R.string.santo_e_santo_quaresima_page'" + ", '" + "R.string.santo_e_santo_quaresima_title" + "', '"
                + "R.string.santo_e_santo_quaresima_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.santo_e_santo_quaresima_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (6, '" + "R.string.santo_ordinario_page'" + ", '" + "R.string.santo_ordinario_title" + "', '"
                + "R.string.santo_ordinario_source" + "', 0, '" + GIALLO
                + "', '" + "R.string.santo_ordinario_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (7, '" + "R.string.santo_baracche_page'" + ", '" + "R.string.santo_baracche_title" + "', '"
                + "R.string.santo_baracche_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.santo_baracche_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (8, '" + "R.string.santo_palme_page'" + ",  '" + "R.string.santo_palme_title" + "', '"
                + "R.string.santo_palme_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.santo_palme_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (9, '" + "R.string.santo_1988_page'" + ", '" + "R.string.santo_1988_title" + "', '"
                + "R.string.santo_1988_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.santo_1988_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (10, '" + "R.string.santo_1983_page'" + ", '" + "R.string.santo_1983_title" + "', '"
                + "R.string.santo_1983_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.santo_1983_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (11, '" + "R.string.preghiera_eucarestica_II_page'" + ", '" + "R.string.preghiera_eucarestica_II_title" + "', '"
                + "R.string.preghiera_eucarestica_II_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.preghiera_eucarestica_II_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (12, '" + "R.string.preghiera_eucaristica_II_parte2_page'" + ", '" + "R.string.preghiera_eucaristica_II_parte2_title" + "', '"
                + "R.string.preghiera_eucaristica_II_parte2_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.preghiera_eucaristica_II_parte2_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (13, '" + "R.string.benedizione_acqua_fonte_page'" + ", '" + "R.string.benedizione_acqua_fonte_title" + "', '"
                + "R.string.benedizione_acqua_fonte_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.benedizione_acqua_fonte_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (14, 11, 'Benedizione dell''acqua del fonte battesimale (2)', 'benedizione_acqua_fonte_2', 0, '"
//				+ GIALLO + "', 'http://www.resuscicanti.com/05Benedizionedell''acquadelfontebattesimale%208,30.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (15, '" + "R.string.preconio_pasquale_page'" + ", '" + "R.string.preconio_pasquale_title" + "', '"
                + "R.string.preconio_pasquale_source" + "', 0, '" + GIALLO
                + "', '" + "R.string.preconio_pasquale_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (17, '" + "R.string.prefazio_eucarestia_veglia_pasquale_page'" + ", '" + "R.string.prefazio_eucarestia_veglia_pasquale_title" + "', '"
                + "R.string.prefazio_eucarestia_veglia_pasquale_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.prefazio_eucarestia_veglia_pasquale_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (18, '" + "R.string.inno_lodi_avvento_fino16_page'" + ", '" + "R.string.inno_lodi_avvento_fino16_title" + "', '"
                + "R.string.inno_lodi_avvento_fino16_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_lodi_avvento_fino16_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (19, '" + "R.string.inno_lodi_avvento_dopo16_page'" + ", '" + "R.string.inno_lodi_avvento_dopo16_title" + "', '"
                + "R.string.inno_lodi_avvento_dopo16_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_lodi_avvento_dopo16_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (20, '" + "R.string.inno_lodi_pasqua_fino_ascensione_page'" + ", '" + "R.string.inno_lodi_pasqua_fino_ascensione_title" + "', '"
                + "R.string.inno_lodi_pasqua_fino_ascensione_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_lodi_pasqua_fino_ascensione_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (21, 16, 'Inno dei vespri da Pasqua all''Ascensione', 'inno_vespri_pasqua_fino_ascensione', 0, '"
//				+ GIALLO + "', 'http://www.resuscicanti.com/010Innodipentecoste-vieni%20spiritocreatore-3,06.mp3', "
//				+ "0, 0, 0, NULL, NULL)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (21, '" + "R.string.inno_vespri_pasqua_fino_ascensione_page'" + ", '" + "R.string.inno_vespri_pasqua_fino_ascensione_title" + "', '"
                + "R.string.inno_vespri_pasqua_fino_ascensione_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_vespri_pasqua_fino_ascensione_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (22, '" + "R.string.inno_lodi_pasqua_ascensione_pentecoste_page'" + ", '" + "R.string.inno_lodi_pasqua_ascensione_pentecoste_title" + "'," +
                " '" + "R.string.inno_lodi_pasqua_ascensione_pentecoste_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_lodi_pasqua_ascensione_pentecoste_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (23, '" + "R.string.inno_vespri_pasqua_ascensione_pentecoste_page'" + ", '" + "R.string.inno_vespri_pasqua_ascensione_pentecoste_title" + "', '"
                + "R.string.inno_vespri_pasqua_ascensione_pentecoste_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_vespri_pasqua_ascensione_pentecoste_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (24, '" + "R.string.inno_lodi_pentecoste_page'" + ", '" + "R.string.inno_lodi_pentecoste_title" + "', '"
                + "R.string.inno_lodi_pentecoste_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.inno_lodi_pentecoste_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (25, '" + "R.string.sequenza_di_pentecoste_page'" + ", '" + "R.string.sequenza_di_pentecoste_title" + "', '"
                + "R.string.sequenza_di_pentecoste_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.sequenza_di_pentecoste_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (26, '" + "R.string.preghiera_eucaristia_II_2_prefazio_page'" + ", '" + "R.string.preghiera_eucaristia_II_2_prefazio_title" + "', '"
                + "R.string.preghiera_eucaristia_II_2_prefazio_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.preghiera_eucaristia_II_2_prefazio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (27, '" + "R.string.preghiera_eucaristica_II_2_consacrazione_page'" + ", '" + "R.string.preghiera_eucaristica_II_2_consacrazione_title" + "', '"
                + "R.string.preghiera_eucaristica_II_2_consacrazione_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.preghiera_eucaristica_II_2_consacrazione_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (28, '" + "R.string.preghiera_eucaristica_II_2_offerta_page'" + ", '" + "R.string.preghiera_eucaristica_II_2_offerta_title" + "', '"
                + "R.string.preghiera_eucaristica_II_2_offerta_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.preghiera_eucaristica_II_2_offerta_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (29, '" + "R.string.alleluja_pasquale_page'" + ", '" + "R.string.alleluja_pasquale_title" + "', '"
                + "R.string.alleluja_pasquale_source" + "', 0, '" + GIALLO
                + "', '" + "R.string.alleluja_pasquale_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (30, '" + "R.string.acclamazioni_al_vangelo_page'" + ", '" + "R.string.acclamazioni_al_vangelo_title" + "', '"
                + "R.string.acclamazioni_al_vangelo_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.acclamazioni_al_vangelo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (31, '" + "R.string.acclamazioni_al_vangelo_quaresima_page'" + ", '" + "R.string.acclamazioni_al_vangelo_quaresima_title" + "', '"
                + "R.string.acclamazioni_al_vangelo_quaresima_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.acclamazioni_al_vangelo_quaresima_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (32, '" + "R.string.te_deum_page'" + ", '" + "R.string.te_deum_title" + "', '"
                + "R.string.te_deum_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.te_deum_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (140, '" + "R.string.agnello_di_dio_page'" + ", '" + "R.string.agnello_di_dio_title" + "', '"
                + "R.string.agnello_di_dio_source" + "', 0, '" + GIALLO
                + "', '" + "R.string.agnello_di_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (33, '" + "R.string.padre_nostro_page'" + ", '" + "R.string.padre_nostro_title" + "', '"
                + "R.string.padre_nostro_source" + "', 0, '"
                + GIALLO + "', '" + "R.string.padre_nostro_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (34, '" + "R.string.credo_apostolico_page'" + ", '" + "R.string.credo_apostolico_title" + "', '"
                + "R.string.credo_apostolico_source" + "', 0, '" + GIALLO
                + "', '" + "R.string.credo_apostolico_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (35, '" + "R.string.risuscito_page'" + ", '" + "R.string.risuscito_title" + "', '"
                + "R.string.risuscito_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.risuscito_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (36, '" + "R.string.verso_te_o_citta_santa_page'" + ", '" + "R.string.verso_te_o_citta_santa_title" + "', '"
                + "R.string.verso_te_o_citta_santa_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.verso_te_o_citta_santa_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (37, '" + "R.string.marcia_e_dura_page'" + ", '" + "R.string.marcia_e_dura_title" + "', '"
                + "R.string.marcia_e_dura_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.marcia_e_dura_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (38, '" + "R.string.dal_profondo_a_te_grido_page'" + ", '" + "R.string.dal_profondo_a_te_grido_title" + "', '"
                + "R.string.dal_profondo_a_te_grido_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.dal_profondo_a_te_grido_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (39, '" + "R.string.canto_di_giosue_page'" + ", '" + "R.string.canto_di_giosue_title" + "', '"
                + "R.string.canto_di_giosue_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.canto_di_giosue_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (40, '" + "R.string.benedici_anima_mia_jahve_page'" + ", '" + "R.string.benedici_anima_mia_jahve_title" + "', '"
                + "R.string.benedici_anima_mia_jahve_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.benedici_anima_mia_jahve_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (41, '" + "R.string.fino_a_quando_page'" + ", '" + "R.string.fino_a_quando_title" + "', '"
                + "R.string.fino_a_quando_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.fino_a_quando_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (42, '" + "R.string.jahve_tu_sei_mio_dio_page'" + ", '" + "R.string.jahve_tu_sei_mio_dio_title" + "', '"
                + "R.string.jahve_tu_sei_mio_dio_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.jahve_tu_sei_mio_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (43, '" + "R.string.cantiamo_cantiamo_page'" + ", '" + "R.string.cantiamo_cantiamo_title" + "', '"
                + "R.string.cantiamo_cantiamo_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.cantiamo_cantiamo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (44, '" + "R.string.giunga_la_mia_preghiera_page'" + ", '" + "R.string.giunga_la_mia_preghiera_title" + "', '"
                + "R.string.giunga_la_mia_preghiera_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.giunga_la_mia_preghiera_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (45, '" + "R.string.guardate_come_e_bello_page'" + ", '" + "R.string.guardate_come_e_bello_title" + "', '"
                + "R.string.guardate_come_e_bello_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.guardate_come_e_bello_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (46, '" + "R.string.come_bello_come_da_gioia_page'" + ", '" + "R.string.come_bello_come_da_gioia_title" + "', '"
                + "R.string.come_bello_come_da_gioia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.come_bello_come_da_gioia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (47, '" + "R.string.guardate_come_e_bello_gustate_page'" + ", '" + "R.string.guardate_come_e_bello_gustate_title" + "', '"
                + "R.string.guardate_come_e_bello_gustate_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.guardate_come_e_bello_gustate_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (48, '" + "R.string.grazie_a_jahve_page'" + ", '" + "R.string.grazie_a_jahve_title" + "', '"
                + "R.string.grazie_a_jahve_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.grazie_a_jahve_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (49, '" + "R.string.canto_giovani_fornace_I_page'" + ", '" + "R.string.canto_giovani_fornace_I_title" + "', '"
                + "R.string.canto_giovani_fornace_I_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.canto_giovani_fornace_I_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (50, '" + "R.string.canto_giovani_fornace_II_page'" + ", '" + "R.string.canto_giovani_fornace_II_title" + "', '"
                + "R.string.canto_giovani_fornace_II_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.canto_giovani_fornace_II_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (51, '" + "R.string.lodate_il_signore_dai_cieli_page'" + ", '" + "R.string.lodate_il_signore_dai_cieli_title" + "', '"
                + "R.string.lodate_il_signore_dai_cieli_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.lodate_il_signore_dai_cieli_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (52, '" + "R.string.lodate_iddio_page'" + ", '" + "R.string.lodate_iddio_title" + "', '"
                + "R.string.lodate_iddio_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.lodate_iddio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (53, '" + "R.string.signore_e_mia_luce_page'" + ", '" + "R.string.signore_e_mia_luce_title" + "', '"
                + "R.string.signore_e_mia_luce_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.signore_e_mia_luce_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (54, '" + "R.string.evenu_shalom_page'" + ", '" + "R.string.evenu_shalom_title" + "', '"
                + "R.string.evenu_shalom_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.evenu_shalom_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (55, '" + "R.string.gia_viene_il_regno_page'" + ", '" + "R.string.gia_viene_il_regno_title" + "', '"
                + "R.string.gia_viene_il_regno_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.gia_viene_il_regno_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (56, '" + "R.string.abba_padre_page'" + ", '" + "R.string.abba_padre_title" + "', '"
                + "R.string.abba_padre_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.abba_padre_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (57, '" + "R.string.chi_ci_separera_page'" + ", '" + "R.string.chi_ci_separera_title" + "', '"
                + "R.string.chi_ci_separera_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.chi_ci_separera_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (58, '" + "R.string.magnificat_page'" + ", '" + "R.string.magnificat_title" + "', '"
                + "R.string.magnificat_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.magnificat_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (59, '" + "R.string.innalzero_la_coppa_di_salvezza_page'" + ", '" + "R.string.innalzero_la_coppa_di_salvezza_title" + "', '"
                + "R.string.innalzero_la_coppa_di_salvezza_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.innalzero_la_coppa_di_salvezza_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (60, '" + "R.string.quando_il_signore_page'" + ", '" + "R.string.quando_il_signore_title" + "', '"
                + "R.string.quando_il_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.quando_il_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (61, '" + "R.string.cantico_di_zaccaria_page'" + ", '" + "R.string.cantico_di_zaccaria_title" + "', '"
                + "R.string.cantico_di_zaccaria_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.cantico_di_zaccaria_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (62, '" + "R.string.o_morte_dove_la_tua_vittoria_page'" + ", '" + "R.string.o_morte_dove_la_tua_vittoria_title" + "', '"
                + "R.string.o_morte_dove_la_tua_vittoria_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.o_morte_dove_la_tua_vittoria_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (63, '" + "R.string.o_cieli_piovete_dall_alto_page'" + ", '" + "R.string.o_cieli_piovete_dall_alto_title" + "', '"
                + "R.string.o_cieli_piovete_dall_alto_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.o_cieli_piovete_dall_alto_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (64, '" + "R.string.pentecoste_page'" + ", '" + "R.string.pentecoste_title" + "', '"
                + "R.string.pentecoste_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.pentecoste_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (65, '" + "R.string.ecco_qui_vengo_presto_page'" + ", '" + "R.string.ecco_qui_vengo_presto_title" + "', '"
                + "R.string.ecco_qui_vengo_presto_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.ecco_qui_vengo_presto_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (66, '" + "R.string.vieni_figlio_dell_uomo_page'" + ", '" + "R.string.vieni_figlio_dell_uomo_title" + "', '"
                + "R.string.vieni_figlio_dell_uomo_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.vieni_figlio_dell_uomo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (67, '" + "R.string.abramo_page'" + ", '" + "R.string.abramo_title" + "', '"
                + "R.string.abramo_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.abramo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (68, '" + "R.string.cantico_di_mose_page'" + ", '" + "R.string.cantico_di_mose_title" + "', '"
                + "R.string.cantico_di_mose_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.cantico_di_mose_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (69, '" + "R.string.lodate_il_signore_page'" + ", '" + "R.string.lodate_il_signore_title" + "', '"
                + "R.string.lodate_il_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.lodate_il_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (70, '" + "R.string.quando_israele_usci_egitto_page'" + ", '" + "R.string.quando_israele_usci_egitto_title" + "', '"
                + "R.string.quando_israele_usci_egitto_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.quando_israele_usci_egitto_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (71, '" + "R.string.alzate_o_porte_page'" + ", '" + "R.string.alzate_o_porte_title" + "', '"
                + "R.string.alzate_o_porte_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.alzate_o_porte_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (72, '" + "R.string.signore_mio_pastore_page'" + ", '" + "R.string.signore_mio_pastore_title" + "', '"
                + "R.string.signore_mio_pastore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.signore_mio_pastore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (73, '" + "R.string.giunti_fiumi_babilonia_page'" + ", '" + "R.string.giunti_fiumi_babilonia_title" + "', '"
                + "R.string.giunti_fiumi_babilonia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.giunti_fiumi_babilonia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (74, '" + "R.string.pieta_di_me_o_dio_page'" + ", '" + "R.string.pieta_di_me_o_dio_title" + "', '"
                + "R.string.pieta_di_me_o_dio_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.pieta_di_me_o_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (75, '" + "R.string.misericordia_dio_misericordia_page'" + ", '" + "R.string.misericordia_dio_misericordia_title" + "', '"
                + "R.string.misericordia_dio_misericordia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.misericordia_dio_misericordia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (76, '" + "R.string.inno_della_kenosis_page'" + ", '" + "R.string.inno_della_kenosis_title" + "', '"
                + "R.string.inno_della_kenosis_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.inno_della_kenosis_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (77, '" + "R.string.ave_maria_page'" + ", '" + "R.string.ave_maria_title" + "', '"
                + "R.string.ave_maria_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.ave_maria_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (78, '" + "R.string.ave_maria_1984_page'" + ", '" + "R.string.ave_maria_1984_title" + "', '"
                + "R.string.ave_maria_1984_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.ave_maria_1984_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (79, '" + "R.string.maria_piccola_maria_page'" + ", '" + "R.string.maria_piccola_maria_title" + "', '"
                + "R.string.maria_piccola_maria_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.maria_piccola_maria_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (80, '" + "R.string.alzo_gli_occhi_page'" + ", '" + "R.string.alzo_gli_occhi_title" + "', '"
                + "R.string.alzo_gli_occhi_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.alzo_gli_occhi_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (81, '" + "R.string.canto_liberati_page'" + ", '" + "R.string.canto_liberati_title" + "', '"
                + "R.string.canto_liberati_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.canto_liberati_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (82, '" + "R.string.se_oggi_ascoltate_sua_voce_page'" + ", '" + "R.string.se_oggi_ascoltate_sua_voce_title" + "', '"
                + "R.string.se_oggi_ascoltate_sua_voce_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.se_oggi_ascoltate_sua_voce_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (83, '" + "R.string.venite_applaudiamo_al_signore_page'" + ", '" + "R.string.venite_applaudiamo_al_signore_title" + "', '"
                + "R.string.venite_applaudiamo_al_signore_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.venite_applaudiamo_al_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (84, '" + "R.string.dajenu_page'" + ", '" + "R.string.dajenu_title" + "', '"
                + "R.string.dajenu_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.dajenu_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (85, '" + "R.string.alla_vittima_pasquale_page'" + ", '" + "R.string.alla_vittima_pasquale_title" + "', '"
                + "R.string.alla_vittima_pasquale_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.alla_vittima_pasquale_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (86, '" + "R.string.inno_di_pasqua_page'" + ", '" + "R.string.inno_di_pasqua_title" + "', '"
                + "R.string.inno_di_pasqua_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.inno_di_pasqua_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (87, '" + "R.string.inno_avvento_page'" + ", '" + "R.string.inno_avvento_title" + "', '"
                + "R.string.inno_avvento_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.inno_avvento_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (88, '" + "R.string.uri_uri_ura_page'" + ", '" + "R.string.uri_uri_ura_title" + "', '"
                + "R.string.uri_uri_ura_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.uri_uri_ura_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (89, '" + "R.string.gia_viene_il_mio_dio_page'" + ", '" + "R.string.gia_viene_il_mio_dio_title" + "', '"
                + "R.string.gia_viene_il_mio_dio_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.gia_viene_il_mio_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "";
        sql += "INSERT INTO ELENCO ";
        sql += "VALUES (90, '" + "R.string.amen_amen_amen_page'" + ", '" + "R.string.amen_amen_amen_title" + "', '"
                + "R.string.amen_amen_amen_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.amen_amen_amen_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (91, '" + "R.string.se_signore_non_costruisce_page'" + ", '" + "R.string.se_signore_non_costruisce_title" + "', '"
                + "R.string.se_signore_non_costruisce_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.se_signore_non_costruisce_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (92, '" + "R.string.gustate_e_vedete_page'" + ", '" + "R.string.gustate_e_vedete_title" + "', '"
                + "R.string.gustate_e_vedete_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.gustate_e_vedete_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (93, '" + "R.string.per_amore_dei_miei_fratelli_page'" + ", '" + "R.string.per_amore_dei_miei_fratelli_title" + "', '"
                + "R.string.per_amore_dei_miei_fratelli_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.per_amore_dei_miei_fratelli_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (94, '" + "R.string.ho_sperato_nel_signore_page'" + ", '" + "R.string.ho_sperato_nel_signore_title" + "', '"
                + "R.string.ho_sperato_nel_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.ho_sperato_nel_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (95, '" + "R.string.voglio_cantare_page'" + ", '" + "R.string.voglio_cantare_title" + "', '"
                + "R.string.voglio_cantare_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.voglio_cantare_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (96, '" + "R.string.perche_genti_congiurano_page'" + ", '" + "R.string.perche_genti_congiurano_title" + "', '"
                + "R.string.perche_genti_congiurano_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.perche_genti_congiurano_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (97, '" + "R.string.come_una_cerva_anela_page'" + ", '" + "R.string.come_una_cerva_anela_title" + "', '"
                + "R.string.come_una_cerva_anela_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.come_una_cerva_anela_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (98, '" + "R.string.acclamate_al_signore_page'" + ", '" + "R.string.acclamate_al_signore_title" + "', '"
                + "R.string.acclamate_al_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.acclamate_al_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (99, '" + "R.string.gridate_con_gioia_page'" + ", '" + "R.string.gridate_con_gioia_title" + "', '"
                + "R.string.gridate_con_gioia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.gridate_con_gioia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (100, '" + "R.string.al_risveglio_mi_saziero_page'" + ", '" + "R.string.al_risveglio_mi_saziero_title" + "', '"
                + "R.string.al_risveglio_mi_saziero_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.al_risveglio_mi_saziero_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (101, '" + "R.string.canto_bambini_veglia_page'" + ", '" + "R.string.canto_bambini_veglia_title" + "', '"
                + "R.string.canto_bambini_veglia_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.canto_bambini_veglia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (102, '" + "R.string.non_moriro_page'" + ", '" + "R.string.non_moriro_title" + "', '"
                + "R.string.non_moriro_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.non_moriro_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (103, 85, 'Non morirò - Salmo 117(118) - II parte', 'non_moriro_II', 0, '"
//				+ BIANCO + "', 'http://www.resuscicanti.com/nonmoriro.mp3', "
//				+ "0, 0, 0, NULL, NULL)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (104, '" + "R.string.dite_agli_smarriti_page'" + ", '" + "R.string.dite_agli_smarriti_title" + "', '"
                + "R.string.dite_agli_smarriti_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.dite_agli_smarriti_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (105, '" + "R.string.o_dio_tu_sei_il_mio_dio_page'" + ", '" + "R.string.o_dio_tu_sei_il_mio_dio_title" + "', '"
                + "R.string.o_dio_tu_sei_il_mio_dio_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.o_dio_tu_sei_il_mio_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (106, '" + "R.string.sale_dio_tra_acclamazioni_page'" + ", '" + "R.string.sale_dio_tra_acclamazioni_title" + "', '"
                + "R.string.sale_dio_tra_acclamazioni_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.sale_dio_tra_acclamazioni_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (107, '" + "R.string.popolo_camminava_tenebre_page'" + ", '" + "R.string.popolo_camminava_tenebre_title" + "', '"
                + "R.string.popolo_camminava_tenebre_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.popolo_camminava_tenebre_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (108, '" + "R.string.da_lode_al_signore_page'" + ", '" + "R.string.da_lode_al_signore_title" + "', '"
                + "R.string.da_lode_al_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.da_lode_al_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (109, '" + "R.string.canto_di_balaam_page'" + ", '" + "R.string.canto_di_balaam_title" + "', '"
                + "R.string.canto_di_balaam_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.canto_di_balaam_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (110, '" + "R.string.davanti_agli_angeli_page'" + ", '" + "R.string.davanti_agli_angeli_title" + "', '"
                + "R.string.davanti_agli_angeli_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.davanti_agli_angeli_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (111, '" + "R.string.quando_israele_era_un_bimbo_page'" + ", '" + "R.string.quando_israele_era_un_bimbo_title" + "', '"
                + "R.string.quando_israele_era_un_bimbo_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.quando_israele_era_un_bimbo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (112, '" + "R.string.e_la_pasqua_del_signore_page'" + ", '" + "R.string.e_la_pasqua_del_signore_title" + "', '"
                + "R.string.e_la_pasqua_del_signore_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.e_la_pasqua_del_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (113, '" + "R.string.tempo_ogni_cosa_page'" + ", '" + "R.string.tempo_ogni_cosa_title" + "', '"
                + "R.string.tempo_ogni_cosa_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.tempo_ogni_cosa_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (114, 93, 'Benedici anima mia il Signore (Cantico di Tobia) (Tb 13)', 'benedici_anima_mia', 0, '"
//				+ BIANCO
//				+ "', 'http://www.resuscicanti.com/BENEDICI%20ANIMA%20MIA%20IL%20SIGNOREGerusalemme%20ricostruita2.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (114, '" + "R.string.benedici_anima_mia_page'" + ", '" + "R.string.benedici_anima_mia_title"
                + "', '" + "R.string.benedici_anima_mia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.benedici_anima_mia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (115, '" + "R.string.quanto_sono_amabili_dimore_page'" + ", '" + "R.string.quanto_sono_amabili_dimore_title" + "', '"
                + "R.string.quanto_sono_amabili_dimore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.quanto_sono_amabili_dimore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (116, '" + "R.string.viene_il_signore_vestito_di_maesta_page'" + ", '" + "R.string.viene_il_signore_vestito_di_maesta_title" + "', '"
                + "R.string.viene_il_signore_vestito_di_maesta_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.viene_il_signore_vestito_di_maesta_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (117, '" + "R.string.giorno_di_riposo_page'" + ", '" + "R.string.giorno_di_riposo_title" + "', '"
                + "R.string.giorno_di_riposo_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.giorno_di_riposo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (118, '" + "R.string.consolate_il_mio_popolo_page'" + ", '" + "R.string.consolate_il_mio_popolo_title" + "', '"
                + "R.string.consolate_il_mio_popolo_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.consolate_il_mio_popolo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (119, '" + "R.string.cerano_due_angeli_page'" + ", '" + "R.string.cerano_due_angeli_title" + "', '"
                + "R.string.cerano_due_angeli_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.cerano_due_angeli_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (120, '" + "R.string.amo_il_signore_page'" + ", '" + "R.string.amo_il_signore_title" + "', '"
                + "R.string.amo_il_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.amo_il_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (121, '" + "R.string.venite_a_me_voi_tutti_page'" + ", '" + "R.string.venite_a_me_voi_tutti_title" + "', '"
                + "R.string.venite_a_me_voi_tutti_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.venite_a_me_voi_tutti_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (122, '" + "R.string.a_te_si_deve_lode_in_sion_page'" + ", '" + "R.string.a_te_si_deve_lode_in_sion_title" + "', '"
                + "R.string.a_te_si_deve_lode_in_sion_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.a_te_si_deve_lode_in_sion_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (123, '" + "R.string.se_signore_sono_rifugiato_page'" + ", '" + "R.string.se_signore_sono_rifugiato_title" + "', '"
                + "R.string.se_signore_sono_rifugiato_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.se_signore_sono_rifugiato_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (124, '" + "R.string.signore_annuncia_una_notizia_page'" + ", '" + "R.string.signore_annuncia_una_notizia_title" + "', '"
                + "R.string.signore_annuncia_una_notizia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.signore_annuncia_una_notizia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (125, '" + "R.string.benedite_il_signore_page'" + ", '" + "R.string.benedite_il_signore_title" + "', '"
                + "R.string.benedite_il_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.benedite_il_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (126, '" + "R.string.figlie_di_gerusalemme_page'" + ", '" + "R.string.figlie_di_gerusalemme_title" + "', '"
                + "R.string.figlie_di_gerusalemme_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.figlie_di_gerusalemme_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (127, '" + "R.string.ti_ho_manifestato_mio_peccato_page'" + ", '" + "R.string.ti_ho_manifestato_mio_peccato_title" + "', '"
                + "R.string.ti_ho_manifestato_mio_peccato_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.ti_ho_manifestato_mio_peccato_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (128, '" + "R.string.maria_madre_della_chiesa_page'" + ", '" + "R.string.maria_madre_della_chiesa_title" + "', '"
                + "R.string.maria_madre_della_chiesa_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.maria_madre_della_chiesa_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (129, '" + "R.string.stabat_mater_page'" + ", '" + "R.string.stabat_mater_title" + "', '"
                + "R.string.stabat_mater_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.stabat_mater_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (130, '" + "R.string.lamenti_del_signore_page'" + ", '" + "R.string.lamenti_del_signore_title" + "', '"
                + "R.string.lamenti_del_signore_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.lamenti_del_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (131, '" + "R.string.o_signore_nostro_dio_page'" + ", '" + "R.string.o_signore_nostro_dio_title" + "', '"
                + "R.string.o_signore_nostro_dio_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.o_signore_nostro_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (132, '" + "R.string.benedetta_sei_tu_maria_page'" + ", '" + "R.string.benedetta_sei_tu_maria_title" + "', '"
                + "R.string.benedetta_sei_tu_maria_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.benedetta_sei_tu_maria_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (133, '" + "R.string.salve_regina_dei_cieli_page'" + ", '" + "R.string.salve_regina_dei_cieli_title" + "', '"
                + "R.string.salve_regina_dei_cieli_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.salve_regina_dei_cieli_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (134, '" + "R.string.vergine_della_meraviglia_page'" + ", '" + "R.string.vergine_della_meraviglia_title" + "', '"
                + "R.string.vergine_della_meraviglia_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.vergine_della_meraviglia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (135, '" + "R.string.maria_casa_di_benedizione_page'" + ", '" + "R.string.maria_casa_di_benedizione_title" + "', '"
                + "R.string.maria_casa_di_benedizione_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.maria_casa_di_benedizione_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (136, '" + "R.string.benediro_il_signore_in_ogni_tempo_page'" + ", '" + "R.string.benediro_il_signore_in_ogni_tempo_title" + "', '"
                + "R.string.benediro_il_signore_in_ogni_tempo_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.benediro_il_signore_in_ogni_tempo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (137, '" + "R.string.mietitura_delle_nazioni_page'" + ", '" + "R.string.mietitura_delle_nazioni_title" + "', '"
                + "R.string.mietitura_delle_nazioni_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.mietitura_delle_nazioni_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (138, '" + "R.string.dice_il_signore_al_mio_signore_page'" + ", '" + "R.string.dice_il_signore_al_mio_signore_title" + "', '"
                + "R.string.dice_il_signore_al_mio_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.dice_il_signore_al_mio_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (139, '" + "R.string.felice_uomo_page'" + ", '" + "R.string.felice_uomo_title" + "', '"
                + "R.string.felice_uomo_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.felice_uomo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (141, '" + "R.string.tu_sei_il_piu_bello_page'" + ", '" + "R.string.tu_sei_il_piu_bello_title" + "', '"
                + "R.string.tu_sei_il_piu_bello_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.tu_sei_il_piu_bello_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (142, '" + "R.string.felicita_per_l_uomo_page'" + ", '" + "R.string.felicita_per_l_uomo_title" + "', '"
                + "R.string.felicita_per_l_uomo_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.felicita_per_l_uomo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (143, '" + "R.string.sorga_dio_page'" + ", '" + "R.string.sorga_dio_title" + "', '"
                + "R.string.sorga_dio_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.sorga_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (144, '" + "R.string.andiamo_gia_pastori_page'" + ", '" + "R.string.andiamo_gia_pastori_title" + "', '"
                + "R.string.andiamo_gia_pastori_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.andiamo_gia_pastori_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (145, '" + "R.string.maria_di_jasna_gora_page'" + ", '" + "R.string.maria_di_jasna_gora_title" + "', '"
                + "R.string.maria_di_jasna_gora_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.maria_di_jasna_gora_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (146, '" + "R.string.a_te_signore_con_la_mia_voce_page'" + ", '" + "R.string.a_te_signore_con_la_mia_voce_title" + "', '"
                + "R.string.a_te_signore_con_la_mia_voce_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.a_te_signore_con_la_mia_voce_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (147, '" + "R.string.un_germoglio_spunta_tronco_page'" + ", '" + "R.string.un_germoglio_spunta_tronco_title" + "', '"
                + "R.string.un_germoglio_spunta_tronco_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.un_germoglio_spunta_tronco_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (148, '" + "R.string.a_te_signore_innalzo_la_mia_anima_page'" + ", '" + "R.string.a_te_signore_innalzo_la_mia_anima_title" + "', '"
                + "R.string.a_te_signore_innalzo_la_mia_anima_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.a_te_signore_innalzo_la_mia_anima_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (149, '" + "R.string.ti_sto_chiamando_page'" + ", '" + "R.string.ti_sto_chiamando_title" + "', '"
                + "R.string.ti_sto_chiamando_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.ti_sto_chiamando_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (150, '" + "R.string.a_te_levo_i_miei_occhi_page'" + ", '" + "R.string.a_te_levo_i_miei_occhi_title" + "', '"
                + "R.string.a_te_levo_i_miei_occhi_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.a_te_levo_i_miei_occhi_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (151, '" + "R.string.signore_non_punirmi_nel_tuo_sdegno_page'" + ", '" + "R.string.signore_non_punirmi_nel_tuo_sdegno_title" + "', '"
                + "R.string.signore_non_punirmi_nel_tuo_sdegno_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.signore_non_punirmi_nel_tuo_sdegno_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (152, '" + "R.string.gloria_gloria_gloria_page'" + ", '" + "R.string.gloria_gloria_gloria_title" + "', '"
                + "R.string.gloria_gloria_gloria_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.gloria_gloria_gloria_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (153, '" + "R.string.esultate_giusti_nel_signore_page'" + ", '" + "R.string.esultate_giusti_nel_signore_title" + "', '"
                + "R.string.esultate_giusti_nel_signore_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.esultate_giusti_nel_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (154, '" + "R.string.molto_mi_hanno_perseguitato_page'" + ", '" + "R.string.molto_mi_hanno_perseguitato_title" + "', '"
                + "R.string.molto_mi_hanno_perseguitato_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.molto_mi_hanno_perseguitato_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (155, '" + "R.string.ti_amo_signore_page'" + ", '" + "R.string.ti_amo_signore_title" + "', '"
                + "R.string.ti_amo_signore_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.ti_amo_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (156, '" + "R.string.maria_madre_cammino_ardente_page'" + ", '" + "R.string.maria_madre_cammino_ardente_title" + "', '"
                + "R.string.maria_madre_cammino_ardente_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.maria_madre_cammino_ardente_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (157, '" + "R.string.shlom_lech_mariam_page'" + ", '" + "R.string.shlom_lech_mariam_title" + "', '"
                + "R.string.shlom_lech_mariam_source" + "', 0, '" + BIANCO
                + "', '" + "R.string.shlom_lech_mariam_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (158, '" + "R.string.andate_ed_annunziate_page'" + ", '" + "R.string.andate_ed_annunziate_title" + "', '"
                + "R.string.andate_ed_annunziate_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.andate_ed_annunziate_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (159, '" + "R.string.mi_indicherai_sentiero_vita_page'" + ", '" + "R.string.mi_indicherai_sentiero_vita_title" + "', '"
                + "R.string.mi_indicherai_sentiero_vita_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.mi_indicherai_sentiero_vita_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (160, '" + "R.string.o_dio_per_il_tuo_nome_page'" + ", '" + "R.string.o_dio_per_il_tuo_nome_title" + "', '"
                + "R.string.o_dio_per_il_tuo_nome_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.o_dio_per_il_tuo_nome_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (161, '" + "R.string.stolto_pensa_che_non_ce_dio_page'" + ", '" + "R.string.stolto_pensa_che_non_ce_dio_title" + "', '"
                + "R.string.stolto_pensa_che_non_ce_dio_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.stolto_pensa_che_non_ce_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (162, '" + "R.string.signore_ascolta_mia_preghiera_page'" + ", '" + "R.string.signore_ascolta_mia_preghiera_title" + "', '"
                + "R.string.signore_ascolta_mia_preghiera_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.signore_ascolta_mia_preghiera_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (163, '" + "R.string.non_e_qui_e_risorto_page'" + ", '" + "R.string.non_e_qui_e_risorto_title" + "', '"
                + "R.string.non_e_qui_e_risorto_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.non_e_qui_e_risorto_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (164, '" + "R.string.vi_prendero_dalle_genti_page'" + ", '" + "R.string.vi_prendero_dalle_genti_title" + "', '"
                + "R.string.vi_prendero_dalle_genti_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.vi_prendero_dalle_genti_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (165, '" + "R.string.voi_siete_la_luce_del_mondo_page'" + ", '" + "R.string.voi_siete_la_luce_del_mondo_title" + "', '"
                + "R.string.voi_siete_la_luce_del_mondo_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.voi_siete_la_luce_del_mondo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (166, '" + "R.string.sola_a_solo_page'" + ", '" + "R.string.sola_a_solo_title" + "', '"
                + "R.string.sola_a_solo_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.sola_a_solo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (167, '" + "R.string.in_mezzo_grande_folla_page'" + ", '" + "R.string.in_mezzo_grande_folla_title" + "', '"
                + "R.string.in_mezzo_grande_folla_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.in_mezzo_grande_folla_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (168, '" + "R.string.zaccheo_page'" + ", '" + "R.string.zaccheo_title" + "', '"
                + "R.string.zaccheo_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.zaccheo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (169, '" + "R.string.siedi_solitario_silenzioso_page'" + ", '" + "R.string.siedi_solitario_silenzioso_title" + "', '"
                + "R.string.siedi_solitario_silenzioso_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.siedi_solitario_silenzioso_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (170, '" + "R.string.cosi_parla_amen_page'" + ", '" + "R.string.cosi_parla_amen_title" + "', '"
                + "R.string.cosi_parla_amen_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.cosi_parla_amen_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (171, '" + "R.string.ti_vedranno_i_re_page'" + ", '" + "R.string.ti_vedranno_i_re_title" + "', '"
                + "R.string.ti_vedranno_i_re_source" + "', 0, '"
                + VERDE + "', '" + "R.string.ti_vedranno_i_re_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (172, '" + "R.string.giacobbe_page'" + ", '" + "R.string.giacobbe_title" + "', '"
                + "R.string.giacobbe_source" + "', 0, '"
                + VERDE + "', '" + "R.string.giacobbe_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (173, '" + "R.string.debora_page'" + ", '" + "R.string.debora_title" + "', '"
                + "R.string.debora_source" + "', 0, '" + VERDE
                + "', '" + "R.string.debora_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (174, '" + "R.string.vedo_cieli_aperti_page'" + ", '" + "R.string.vedo_cieli_aperti_title" + "', '"
                + "R.string.vedo_cieli_aperti_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.vedo_cieli_aperti_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (175, '" + "R.string.il_signore_mi_ha_dato_page'" + ", '" + "R.string.il_signore_mi_ha_dato_title" + "', '"
                + "R.string.il_signore_mi_ha_dato_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.il_signore_mi_ha_dato_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (176, '" + "R.string.pigiatore_page'" + ", '" + "R.string.pigiatore_title" + "', '"
                + "R.string.pigiatore_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.pigiatore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (177, '" + "R.string.seminatore_page'" + ", '" + "R.string.seminatore_title" + "', '"
                + "R.string.seminatore_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.seminatore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (178, '" + "R.string.spirito_del_signore_sopra_di_me_page'" + ", '" + "R.string.spirito_del_signore_sopra_di_me_title" + "', '"
                + "R.string.spirito_del_signore_sopra_di_me_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.spirito_del_signore_sopra_di_me_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (179, '" + "R.string.ecco_lo_specchio_nostro_page'" + ", '" + "R.string.ecco_lo_specchio_nostro_title" + "', '"
                + "R.string.ecco_lo_specchio_nostro_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.ecco_lo_specchio_nostro_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (180, '" + "R.string.come_slancio_ira_page'" + ", '" + "R.string.come_slancio_ira_title" + "', '"
                + "R.string.come_slancio_ira_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.come_slancio_ira_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (181, '" + "R.string.benedetto_sia_iddio_page'" + ", '" + "R.string.benedetto_sia_iddio_title" + "', '"
                + "R.string.benedetto_sia_iddio_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.benedetto_sia_iddio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (182, '" + "R.string.signore_tu_scruti_conosci_page'" + ", '" + "R.string.signore_tu_scruti_conosci_title" + "', '"
                + "R.string.signore_tu_scruti_conosci_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.signore_tu_scruti_conosci_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (183, '" + "R.string.eli_eli_lamma_sabactani_page'" + ", '" + "R.string.eli_eli_lamma_sabactani_title" + "', '"
                + "R.string.eli_eli_lamma_sabactani_source" + "', 0, '"
                + VERDE + "', '" + "R.string.eli_eli_lamma_sabactani_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (184, 215, 'Eli, Eli, lammà sabactani? - parte II - Salmo 21(22)', 'eli_eli_lamma_sabactani_II', 0, '"
//				+ VERDE + "', 'http://www.resuscicanti.com/elielilammasabactani.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (185, '" + "R.string.nessuno_puo_servire_due_padroni_page'" + ", '" + "R.string.nessuno_puo_servire_due_padroni_title" + "', '"
                + "R.string.nessuno_puo_servire_due_padroni_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.nessuno_puo_servire_due_padroni_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (186, '" + "R.string.signore_mio_cuore_pretese_page'" + ", '" + "R.string.signore_mio_cuore_pretese_title" + "', '"
                + "R.string.signore_mio_cuore_pretese_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.signore_mio_cuore_pretese_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (187, '" + "R.string.voglio_andare_a_gerusalemme_page'" + ", '" + "R.string.voglio_andare_a_gerusalemme_title" + "', '"
                + "R.string.voglio_andare_a_gerusalemme_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.voglio_andare_a_gerusalemme_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (188, '" + "R.string.shema_israel_page'" + ", '" + "R.string.shema_israel_title" + "', '"
                + "R.string.shema_israel_source" + "', 0, '"
                + VERDE + "', '" + "R.string.shema_israel_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (189, '" + "R.string.inno_croce_gloriosa_page'" + ", '" + "R.string.inno_croce_gloriosa_title" + "', '"
                + "R.string.inno_croce_gloriosa_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.inno_croce_gloriosa_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (190, '" + "R.string.rivestitevi_dell_armatura_page'" + ", '" + "R.string.rivestitevi_dell_armatura_title" + "', '"
                + "R.string.rivestitevi_dell_armatura_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.rivestitevi_dell_armatura_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (191, '" + "R.string.sue_fondamenta_page'" + ", '" + "R.string.sue_fondamenta_title" + "', '"
                + "R.string.sue_fondamenta_source" + "', 0, '"
                + VERDE
                + "', '" + "R.string.sue_fondamenta_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (192, '" + "R.string.akeda_page'" + ", '" + "R.string.akeda_title" + "', '"
                + "R.string.akeda_source" + "', 0, '" + VERDE
                + "', '" + "R.string.akeda_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (193, '" + "R.string.non_ti_adirare_page'" + ", '" + "R.string.non_ti_adirare_title" + "', '"
                + "R.string.non_ti_adirare_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.non_ti_adirare_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (194, '" + "R.string.inno_alla_carita_page'" + ", '" + "R.string.inno_alla_carita_title" + "', '"
                + "R.string.inno_alla_carita_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.inno_alla_carita_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (195, '" + "R.string.stesso_iddio_page'" + ", '" + "R.string.stesso_iddio_title" + "', '"
                + "R.string.stesso_iddio_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.stesso_iddio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (196, '" + "R.string.come_condannati_a_morte_page'" + ", '" + "R.string.come_condannati_a_morte_title" + "', '"
                + "R.string.come_condannati_a_morte_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.come_condannati_a_morte_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (197, '" + "R.string.gesu_percorreva_page'" + ", '" + "R.string.gesu_percorreva_title" + "', '"
                + "R.string.gesu_percorreva_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.gesu_percorreva_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (198, '" + "R.string.non_resistete_al_male_page'" + ", '" + "R.string.non_resistete_al_male_title" + "', '"
                + "R.string.non_resistete_al_male_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.non_resistete_al_male_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (199, '" + "R.string.che_mi_baci_page'" + ", '" + "R.string.che_mi_baci_title" + "', '"
                + "R.string.che_mi_baci_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.che_mi_baci_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (200, '" + "R.string.mia_diletta_e_per_me_page'" + ", '" + "R.string.mia_diletta_e_per_me_title" + "', '"
                + "R.string.mia_diletta_e_per_me_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.mia_diletta_e_per_me_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (201, '" + "R.string.vieni_dal_libano_page'" + ", '" + "R.string.vieni_dal_libano_title" + "', '"
                + "R.string.vieni_dal_libano_source" + " ', 0, ' "
                + AZZURRO
                + "', '" + "R.string.vieni_dal_libano_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (202, '" + "R.string.quando_dormivo_page'" + ", '" + "R.string.quando_dormivo_title" + "', '"
                + "R.string.quando_dormivo_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.quando_dormivo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (203, '" + "R.string.tu_che_abiti_nei_giardini_page'" + ", '" + "R.string.tu_che_abiti_nei_giardini_title" + "', '"
                + "R.string.tu_che_abiti_nei_giardini_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.tu_che_abiti_nei_giardini_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (204, '" + "R.string.agnella_di_dio_page'" + ", '" + "R.string.agnella_di_dio_title" + "', '"
                + "R.string.agnella_di_dio_source" + "', 0, '" + AZZURRO
                + "', '" + "R.string.agnella_di_dio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (205, '" + "R.string.non_ce_in_lui_bellezza_page'" + ", '" + "R.string.non_ce_in_lui_bellezza_title" + "', '"
                + "R.string.non_ce_in_lui_bellezza_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.non_ce_in_lui_bellezza_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (206, '" + "R.string.canto_dell_agnello_page'" + ", '" + "R.string.canto_dell_agnello_title" + "', '"
                + "R.string.canto_dell_agnello_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.canto_dell_agnello_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (207, '" + "R.string.chi_e_colei_page'" + ", '" + "R.string.chi_e_colei_title" + "', '"
                + "R.string.chi_e_colei_source" + "', 0, '"
                + AZZURRO + "', '" + "R.string.chi_e_colei_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (208, '" + "R.string.voce_del_mio_amato_page'" + ", '" + "R.string.voce_del_mio_amato_title" + "', '"
                + "R.string.voce_del_mio_amato_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.voce_del_mio_amato_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (209, '" + "R.string.colomba_volo_page'" + ", '" + "R.string.colomba_volo_title" + "', '"
                + "R.string.colomba_volo_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.colomba_volo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (210, '" + "R.string.come_stilla_il_miele_page'" + ", '" + "R.string.come_stilla_il_miele_title" + "', '"
                + "R.string.come_stilla_il_miele_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.come_stilla_il_miele_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (211, '" + "R.string.o_gesu_amore_mio_page'" + ", '" + "R.string.o_gesu_amore_mio_title" + "', '"
                + "R.string.o_gesu_amore_mio_source" + "', 0, '" + AZZURRO
                + "', '" + "R.string.o_gesu_amore_mio_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (212, '" + "R.string.portami_in_cielo_page'" + ", '" + "R.string.portami_in_cielo_title" + "', '"
                + "R.string.portami_in_cielo_source" + "', 0, '" + AZZURRO
                + "', '" + "R.string.portami_in_cielo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (213, '" + "R.string.tu_sei_mia_speranza_signore_page'" + ", '" + "R.string.tu_sei_mia_speranza_signore_title" + "', '"
                + "R.string.tu_sei_mia_speranza_signore_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.tu_sei_mia_speranza_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (214, '" + "R.string.una_donna_vestita_di_sole_page'" + ", '" + "R.string.una_donna_vestita_di_sole_title" + "', '"
                + "R.string.una_donna_vestita_di_sole_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.una_donna_vestita_di_sole_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (215, '" + "R.string.ho_steso_le_mani_page'" + ", '" + "R.string.ho_steso_le_mani_title" + "', '"
                + "R.string.ho_steso_le_mani_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.ho_steso_le_mani_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (216, '" + "R.string.omelia_pasquale_melitone_sardi_page'" + ", '" + "R.string.omelia_pasquale_melitone_sardi_title" + "', '"
                + "R.string.omelia_pasquale_melitone_sardi_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.omelia_pasquale_melitone_sardi_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (217, '" + "R.string.carmen_63_page'" + ", '" + "R.string.carmen_63_title" + "', '"
                + "R.string.carmen_63_source" + "', 0, '"
                + AZZURRO + "', '" + "R.string.carmen_63_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (218, '" + "R.string.caritas_christi_page'" + ", '" + "R.string.caritas_christi_title" + "', '"
                + "R.string.caritas_christi_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.caritas_christi_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (219, '" + "R.string.noli_me_tangere_page'" + ", '" + "R.string.noli_me_tangere_title" + "', '"
                + "R.string.noli_me_tangere_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.noli_me_tangere_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (220, '" + "R.string.signore_aiutami_signore_page'" + ", '" + "R.string.signore_aiutami_signore_title" + "', '"
                + "R.string.signore_aiutami_signore_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.signore_aiutami_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (221, '" + "R.string.mi_hai_sedotto_signore_page'" + ", '" + "R.string.mi_hai_sedotto_signore_title" + "', '"
                + "R.string.mi_hai_sedotto_signore_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.mi_hai_sedotto_signore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (222, '" + "R.string.amate_i_vostri_nemici_page'" + ", '" + "R.string.amate_i_vostri_nemici_title" + "', '"
                + "R.string.amate_i_vostri_nemici_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.amate_i_vostri_nemici_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (223, '" + "R.string.tu_sei_bella_amica_mia_page'" + ", '" + "R.string.tu_sei_bella_amica_mia_title" + "', '"
                + "R.string.tu_sei_bella_amica_mia_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.tu_sei_bella_amica_mia_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (224, '" + "R.string.fratelli_non_diamo_a_nessuno_page'" + ", '" + "R.string.fratelli_non_diamo_a_nessuno_title" + "', '"
                + "R.string.fratelli_non_diamo_a_nessuno_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.fratelli_non_diamo_a_nessuno_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (225, '" + "R.string.questo_e_io_mio_comandamento_page'" + ", '" + "R.string.questo_e_io_mio_comandamento_title" + "', '"
                + "R.string.questo_e_io_mio_comandamento_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.questo_e_io_mio_comandamento_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (226, '" + "R.string.mi_rubasti_cuore_page'" + ", '" + "R.string.mi_rubasti_cuore_title" + "', '"
                + "R.string.mi_rubasti_cuore_source" + " ', 0, ' "
                + AZZURRO
                + "', '" + "R.string.mi_rubasti_cuore_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (227, '" + "R.string.in_una_notte_oscura_page'" + ", '" + "R.string.in_una_notte_oscura_title" + "', '"
                + "R.string.in_una_notte_oscura_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.in_una_notte_oscura_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (228, '" + "R.string.se_siete_risorti_page'" + ", '" + "R.string.se_siete_risorti_title" + "', '"
                + "R.string.se_siete_risorti_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.se_siete_risorti_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (229, '" + "R.string.una_gran_senal_page'" + ", '" + "R.string.una_gran_senal_title" + "', '"
                + "R.string.una_gran_senal_source" + "', 0, '"
                + AZZURRO
                + "', '" + "R.string.una_gran_senal_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (230, '" + "R.string.resurrexit_page'" + ", '" + "R.string.resurrexit_title" + "', '"
                + "R.string.resurrexit_source" + "', 0, '"
                + BIANCO
                + "', '" + "R.string.resurrexit_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (231, '" + "R.string.ecco_il_mio_servo_page'" + ", '" + "R.string.ecco_il_mio_servo_title" + "', '"
                + "R.string.ecco_il_mio_servo_source" + "', 0, '"
                + GRIGIO
                + "', '" + "R.string.ecco_il_mio_servo_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (232, '" + "R.string.ave_maria_colomba_page'" + ", '" + "R.string.ave_maria_colomba_title" + "', '"
                + "R.string.ave_maria_colomba_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.ave_maria_colomba_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (233, '" + "R.string.messia_leone_vincere_page'" + ", '" + "R.string.messia_leone_vincere_title" + "', '"
                + "R.string.messia_leone_vincere_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.messia_leone_vincere_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (234, '" + "R.string.come_pecora_che_vede_page'" + ", '" + "R.string.come_pecora_che_vede_title" + "', '"
                + "R.string.come_pecora_che_vede_source" + "', 0, '"
                + BIANCO + "', '" + "R.string.come_pecora_che_vede_link" + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);
        // FINE CANTI

        // TITOLI DEGLI ARGOMENTI
        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (1, '" + "R.string.Canti_della_Bibbia_Antico_Testamento" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (2, '" + "R.string.Canti_della_Bibbia_Nuovo_Testamento" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (3, '" + "R.string.Canti_dalle_Odi_di_Salomone" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (4, '" + "R.string.Ispirati_a_melodie_e_rituali_ebraici" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (5, '" + "R.string.Canti_per_bambini" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (6, '" + "R.string.Dall_ordinario_della_Messa" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (7, '" + "R.string.Canti_per_la_frazione_del_pane" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (8, '" + "R.string.Dalla_liturgia_della_veglia_pasquale" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (9, '" + "R.string.Canti_per_il_sacramento_della_riconciliazione" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (10, '" + "R.string.Inni_liturgici" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (11, '" + "R.string.Canti_a_Maria" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO ARG_NAMES ";
        sql += "VALUES (12, '" + "R.string.Vari" + "')";
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
        sql += "VALUES (38, '129', '" + "R.string.salmo_129" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (40, '102', '" + "R.string.salmo_102" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (41, '012', '" + "R.string.salmo_012" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (44, '118', '" + "R.string.salmo_118" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (45, '132', '" + "R.string.salmo_132_guardate" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (46, '132', '" + "R.string.salmo_132_come_bello" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (47, '132', '" + "R.string.salmo_132_guardate_gustate" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (48, '135', '" + "R.string.salmo_135" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (51, '148', '" + "R.string.salmo_148" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (52, '150', '" + "R.string.salmo_150" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (53, '026', '" + "R.string.salmo_026" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (59, '114-115', '" + "R.string.salmo_114_115" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (60, '125', '" + "R.string.salmo_125" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (69, '116', '" + "R.string.salmo_116" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (70, '113A', '" + "R.string.salmo_113A" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (71, '023', '" + "R.string.salmo_023" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (72, '022', '" + "R.string.salmo_022" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (73, '136', '" + "R.string.salmo_136" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (74, '050', '" + "R.string.salmo_050_pieta" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (75, '050', '" + "R.string.salmo_050_misericordia" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (80, '120', '" + "R.string.salmo_120" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (82, '094', '" + "R.string.salmo_094_se_oggi" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (83, '094', '" + "R.string.salmo_094_venite" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (91, '126', '" + "R.string.salmo_126" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (92, '033', '" + "R.string.salmo_033_gustate" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (93, '121', '" + "R.string.salmo_121" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (94, '039', '" + "R.string.salmo_039" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (95, '056', '" + "R.string.salmo_056" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (96, '002', '" + "R.string.salmo_002" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (97, '041-042', '" + "R.string.salmo_041_042" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (98, '099', '" + "R.string.salmo_099" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (100, '016', '" + "R.string.salmo_016" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (102, '117', '" + "R.string.salmo_117" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (105, '062', '" + "R.string.salmo_062" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (106, '046', '" + "R.string.salmo_046" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (108, '145', '" + "R.string.salmo_145" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (110, '137', '" + "R.string.salmo_137" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (115, '083', '" + "R.string.salmo_083" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (120, '114', '" + "R.string.salmo_114" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (122, '064', '" + "R.string.salmo_064" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (123, '010', '" + "R.string.salmo_010" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (124, '067-v12', '" + "R.string.salmo_067_v12" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (125, '133', '" + "R.string.salmo_133" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (127, '031', '" + "R.string.salmo_031" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (131, '008', '" + "R.string.salmo_008" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (136, '033', '" + "R.string.salmo_033_benediro" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (138, '109', '" + "R.string.salmo_109" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (139, '001', '" + "R.string.salmo_001" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (141, '044', '" + "R.string.salmo_044" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (142, '127', '" + "R.string.salmo_127" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (143, '067-v02', '" + "R.string.salmo_067_v02" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (146, '141', '" + "R.string.salmo_141" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (148, '024', '" + "R.string.salmo_024" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (149, '140', '" + "R.string.salmo_140" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (150, '122', '" + "R.string.salmo_122" + " ')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (151, '006', '" + "R.string.salmo_006" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (153, '032', '" + "R.string.salmo_032" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (154, '128', '" + "R.string.salmo_128" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (155, '017', '" + "R.string.salmo_017" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (159, '015', '" + "R.string.salmo_015" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (160, '053', '" + "R.string.salmo_053" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (161, '013', '" + "R.string.salmo_013" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (162, '142', '" + "R.string.salmo_142" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (182, '138', '" + "R.string.salmo_138" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (183, '021', '" + "R.string.salmo_021" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (186, '130', '" + "R.string.salmo_130" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (191, '086', '" + "R.string.salmo_086" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO SALMI_MUSICA ";
        sql += "VALUES (193, '036', '" + "R.string.salmo_036" + "')";
        db.execSQL(sql);
        //FINE SALMI CANTATI

        // TITOLI DELL'INDICE LITURGICO
        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (1, '" + "R.string.tempo_avvento" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (2, '" + "R.string.tempo_natale" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (3, '" + "R.string.tempo_quaresima" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (4, '" + "R.string.tempo_pasqua" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (5, '" + "R.string.canti_pentecoste" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (6, '" + "R.string.canti_vergine" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (7, '" + "R.string.lodi_vespri" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (8, '" + "R.string.canti_ingresso" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (9, '" + "R.string.canti_pace" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (10, '" + "R.string.canti_pane" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (11, '" + "R.string.canti_vino" + "')";
        db.execSQL(sql);

        sql = "INSERT INTO INDICE_LIT_NAMES ";
        sql += "VALUES (12, '" + "R.string.canti_fine" + "')";
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
        sql += "VALUES (11, 59)";
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

        Backup[] backup = backupTables(oldVersion, db);

        BackupLocalLink[] backupLink = backupLocalLink(oldVersion, db);

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

    private Backup[] backupTables(int oldVersion, SQLiteDatabase db) {

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

    private BackupLocalLink[] backupLocalLink(int oldVersion, SQLiteDatabase db) {

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

    private void repopulateDB(int oldVersion, int newVersion, SQLiteDatabase db, Backup[] backup, BackupLocalLink[] backupLink) {
        ContentValues values;

        if (newVersion >= 43 && oldVersion >= 19 && oldVersion <= 38) {
            //ricodifica i titoli dei canti con i loro ID
            String sql = "SELECT _id, lista FROM LISTE_PERS";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                int idLista = cursor.getInt(0);
                ListaPersonalizzata lista = (ListaPersonalizzata) ListaPersonalizzata.Companion
                        .deserializeObject(cursor.getBlob(1));

                if (lista != null) {
                    int totPosiz = lista.getNumPosizioni();

                    for (int j = 0; j < totPosiz; j++) {
                        if (!lista.getCantoPosizione(j).equals("")) {
//						Log.i("NOME DUP", lista.getCantoPosizione(j).substring(10));
                            String nomeCanto = Utility.INSTANCE.duplicaApostrofi(lista.getCantoPosizione(j)).substring(10);
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
                    values = new ContentValues();
                    values.put("lista", ListaPersonalizzata.Companion.serializeObject(lista));
                    db.update("LISTE_PERS", values, "_id = " + idLista, null);
                }
                cursor.moveToNext();
            }

            cursor.close();
        }

        if (oldVersion >= 19) {
//            for (int i = 0; i < backup.length; i++) {
//                if (backup[i] == null)
//                    break;
//                values = new  ContentValues( );
//                values.put("zoom" , backup[i].getZoom());
//                //Nella versione 22 sono stati completati i ritornelli di tutti i canti,
//                //quindi meglio resettare lo scroll salvato
//                if (newVersion != 22) {
//                    values.put("scroll_x", backup[i].getScroll_x());
//                    values.put("scroll_y", backup[i].getScroll_y());
//                }
//                values.put("favourite", backup[i].getFavourite());
//                values.put("saved_tab", backup[i].getNota());
//                if (oldVersion >= 20)
//                    values.put("saved_barre", backup[i].getBarre());
//                if (oldVersion >= 21)
//                    values.put("saved_speed", backup[i].getSpeed());
//                db.update("ELENCO", values, "_id = " + backup[i].getId(), null );
//            }
            for (Backup aBackup : backup) {
                if (aBackup == null)
                    break;
                values = new ContentValues();
                values.put("zoom", aBackup.getZoom());
                //Nella versione 22 sono stati completati i ritornelli di tutti i canti,
                //quindi meglio resettare lo scroll salvato
                if (newVersion != 22) {
                    values.put("scroll_x", aBackup.getScroll_x());
                    values.put("scroll_y", aBackup.getScroll_y());
                }
                values.put("favourite", aBackup.getFavourite());
                values.put("saved_tab", aBackup.getNota());
                if (oldVersion >= 20)
                    values.put("saved_barre", aBackup.getBarre());
                if (oldVersion >= 21)
                    values.put("saved_speed", aBackup.getSpeed());
                db.update("ELENCO", values, "_id = " + aBackup.getId(), null);
            }

            //cancella dalle liste i canti inesistenti
            String sql = "SELECT _id, lista FROM LISTE_PERS";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                int idLista = cursor.getInt(0);
                ListaPersonalizzata lista = (ListaPersonalizzata) ListaPersonalizzata.Companion
                        .deserializeObject(cursor.getBlob(1));

                if (lista != null) {
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
                    values = new ContentValues();
                    values.put("lista", ListaPersonalizzata.Companion.serializeObject(lista));
                    db.update("LISTE_PERS", values, "_id = " + idLista, null);
                }
                cursor.moveToNext();
            }

            cursor.close();
        }

        if (oldVersion >= 25) {
//            for (int i = 0; i < backupLink.length; i++) {
//                if (backupLink[i] == null)
//                    break;
//                values = new  ContentValues( );
//                values.put("_id", backupLink[i].getIdCanto());
//                values.put("local_path", backupLink[i].getLocalPath());
//                db.insert("LOCAL_LINKS", null, values);
//            }
            for (BackupLocalLink aBackupLink : backupLink) {
                if (aBackupLink == null)
                    break;
                values = new ContentValues();
                values.put("_id", aBackupLink.getIdCanto());
                values.put("local_path", aBackupLink.getLocalPath());
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

        //cancella dai consegnati, i canti inesistenti
        sql = "SELECT _id, id_canto FROM CANTI_CONSEGNATI";
        cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            sql = "SELECT _id" +
                    "  FROM ELENCO" +
                    "  WHERE _id =  " + cursor.getInt(1);
            Cursor cCheckExists = db.rawQuery(sql, null);
//            Log.i("ESISTE?", cCheckExists.getCount() + "");
            if (cCheckExists.getCount() == 0)
                db.delete("CANTI_CONSEGNATI", "_id = " + cursor.getInt(0), null);
            cCheckExists.close();
            cursor.moveToNext();
        }

        //cancella dalla cronologia, i canti inesistenti
        sql = "SELECT id_canto FROM CRONOLOGIA";
        cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            sql = "SELECT _id" +
                    "  FROM ELENCO" +
                    "  WHERE _id =  " + cursor.getInt(0);
            Cursor cCheckExists = db.rawQuery(sql, null);
//            Log.i("ESISTE?", cCheckExists.getCount() + "");
            if (cCheckExists.getCount() == 0)
                db.delete("CRONOLOGIA", "id_canto = " + cursor.getInt(0), null);
            cCheckExists.close();
            cursor.moveToNext();
        }

        cursor.close();

    }

    void reCreateDatabse(SQLiteDatabase db) {
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
//        sql = "DROP TABLE IF EXISTS CANTI_CONSEGNATI";
//        db.execSQL(sql);
//        sql = "DROP TABLE IF EXISTS CRONOLOGIA";
//        db.execSQL(sql);

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

        int getZoom() {
            return zoom;
        }

        void setZoom(int zoom) {
            this.zoom = zoom;
        }

        int getScroll_x() {
            return scroll_x;
        }

        void setScroll_x(int scroll_x) {
            this.scroll_x = scroll_x;
        }

        int getScroll_y() {
            return scroll_y;
        }

        void setScroll_y(int scroll_y) {
            this.scroll_y = scroll_y;
        }

        int getFavourite() {
            return favourite;
        }

        void setFavourite(int favourite) {
            this.favourite = favourite;
        }

        String getNota() {
            return nota;
        }

        void setNota(String nota) {
            this.nota = nota;
        }

        String getBarre() {
            return barre;
        }

        void setBarre(String barre) {
            this.barre = barre;
        }

        int getSpeed() {
            return speed;
        }

        void setSpeed(int speed) {
            this.speed = speed;
        }
    }

    class BackupLocalLink{
        private int idCanto;
        private String localPath;


        BackupLocalLink() {
            this.idCanto = 0;
            this.localPath = "";
        }

        public int getIdCanto() {
            return idCanto;
        }

        public void setIdCanto(int idCanto) {
            this.idCanto = idCanto;
        }

        String getLocalPath() {
            return localPath;
        }

        void setLocalPath(String localPath) {
            this.localPath = localPath;
        }
    }

    public static String getDbName() {
        return DB_NAME;
    }
}