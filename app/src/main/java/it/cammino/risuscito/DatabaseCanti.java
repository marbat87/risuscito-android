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
	private static final int DB_VERSION = 31;

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

		// CANTI
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (1, 1, '" + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_title) + "', '"
                + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_source) + "', 0, '"
				+ GIALLO + "', '" + appContext.getResources().getString(R.string.litanie_penitenziali_brevi_link) + "', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (2, 2, 'Preghiera litanica penitenziale', 'preghiera_litanica_penitenziale', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/Preghiera%20Litanica%20penitenziale.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (3, 3, 'Benedizione per la celebrazione penitenziale', 'celebrazione_penitenziale', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/06Benedizioneperlacelebrazionepenitenziale%204,47.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (4, 4, 'Gloria a Dio nell''alto dei cieli', 'gloria_a_dio_cieli', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/Gloria%20a%20Dio%20nell''alto%20dei%20cieli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (5, 5, 'Santo è Santo (Tempo di Quaresima)', 'santo_e_santo_quaresima', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/24Santodiquaresima%202,41.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (6, 5, 'Santo (Tempo Ordinario)', 'santo_ordinario', 0, '" + GIALLO
				+ "', 'http://www.resuscicanti.com/23Santotempoordinario%203,11.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (7, 6, 'Santo è il Signor - Santo delle baracche (Tempo di avvento)', 'santo_baracche', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/22Santotempodiavvento%202,49.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (8, 6,  'Santo Santo Santo - Osanna delle palme (e Tempo Pasquale)', 'santo_palme', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/25Santotempopasquale%203,47.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (9, 7, 'Santo, Santo, Santo (1988)', 'santo_1988', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/21SantoSantoSanto%20(1988)%205,22.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (10, 7, 'Santo (1983)', 'santo_1983', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/Santo%20(1983).mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (11, 8, 'Preghiera Eucaristica II (1)', 'preghiera_eucarestica_II', 0, '"
				+ GIALLO + "', '', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (12, 9, 'Preghiera Eucaristica II (1) - parte II', 'preghiera_eucaristica_II_parte2', 0, '"
				+ GIALLO + "', '', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (13, 10, '" + appContext.getResources().getString(R.string.benedizione_acqua_fonte_title) + "', '"
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
		sql += "VALUES (15, 12, 'Preconio pasquale', 'preconio_pasquale', 0, '" + GIALLO
				+ "', 'http://www.resuscicanti.com/15Preconiopasquale%2011,41.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (17, 14, '" + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_title) + "', '"
                + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_source) + "', 0, '"
				+ GIALLO + "', '" + appContext.getResources().getString(R.string.prefazio_eucarestia_veglia_pasquale_link) + "', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (18, 15, 'Inno delle lodi di Avvento fino al 16 dicembre', 'inno_lodi_avvento_fino16', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/31%20inno%20di%20avvento%20alle%20lodi.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

        sql = "INSERT INTO ELENCO ";
        sql += "VALUES (19, 15, '" + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_title) + "', '"
                + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_source) + "', 0, '"
                + GIALLO + "', '" + appContext.getResources().getString(R.string.inno_lodi_avvento_dopo16_link) + "', "
                + "0, 0, 0, NULL, NULL, 2)";
        db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (20, 16, 'Inno delle lodi da Pasqua all''Ascensione', 'inno_lodi_pasqua_fino_ascensione', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/Inno%20delle%20lodi%20da%20pasqua%20all%C2%B4ascensione.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (21, 16, 'Inno dei vespri da Pasqua all''Ascensione', 'inno_vespri_pasqua_fino_ascensione', 0, '"
//				+ GIALLO + "', 'http://www.resuscicanti.com/010Innodipentecoste-vieni%20spiritocreatore-3,06.mp3', "
//				+ "0, 0, 0, NULL, NULL)";
//		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (21, 16, 'Inno dei vespri da Pasqua all''Ascensione', 'inno_vespri_pasqua_fino_ascensione', 0, '"
				+ GIALLO + "', '', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (22, 17, 'Inno dei vespri del giorno dell''Ascensione e inno delle lodi dall''Ascensione a Pentecoste'," +
				" 'inno_lodi_pasqua_ascensione_pentecoste', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/Inno%20dei%20vespri%20del%20giorno%20dell''Ascensione.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (23, 18, 'Inno dei vespri dall''Ascensione a Pentecoste', 'inno_vespri_pasqua_ascensione_pentecoste', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/inno%20dei%20vespri%20dall''ascensione%20a%20pentecoste.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (24, 19, 'Inno delle lodi di Pentecoste', 'inno_lodi_pentecoste', 0, '"
				+ GIALLO + "', '', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (25, 20, 'Sequenza di Pentecoste (Vieni, Sprito Santo)', 'sequenza_di_pentecoste', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/27%20sequenzadipentecoste.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (26, 21, 'Preghiera Eucaristica II (2): Prefazio', " +
				"'preghiera_eucaristia_II_2_prefazio', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/16PreghieraucaristicaIIprefazio%204,36.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (27, 22, 'Segue Preghiera Eucaristica II (2): Consacrazione e acclamazione', " +
				"'preghiera_eucaristica_II_2_consacrazione', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/18PreghieraeucaristicaII(Consagrazi0ne)4,58.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (28, 23, 'Segue Preghiera Eucaristica II (2): Offerta, intercessioni dossologia', " +
				"'preghiera_eucaristica_II_2_offerta', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/17PreghieraeucaristicaII(offerta)4,13.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (29, 24, 'Alleluja pasquale', 'alleluja_pasquale', 0, '" + GIALLO
				+ "', 'http://www.resuscicanti.com/03Alleluiapasquale1,17.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (30, 24, 'Alleluja per acclamazione al Vangelo', 'acclamazioni_al_vangelo', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/01Acclamazionealvangelo%201,40.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (31, 25, 'Acclamazione al Vangelo nel tempo di Quaresima', 'acclamazioni_al_vangelo_quaresima', 0, '"
				+ GIALLO + "', '', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (32, 26, 'Te Deum', 'te_deum', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/28Tedeuma6,25.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";				
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (140, 27, 'Agnello di Dio', 'agnello_di_dio', 0, '" + GIALLO
				+ "', 'http://www.resuscicanti.com/02AgnellodidioportoS.giorgiofamiglie%202005.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (33, 27, 'Padre Nostro (Mt. 6,9-13)', 'padre_nostro', 0, '"
				+ GIALLO + "', 'http://www.resuscicanti.com/14Padre%20nostro2,02.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (34, 28, 'Credo apostolico', 'credo_apostolico', 0, '" + GIALLO
				+ "', 'http://www.resuscicanti.com/08Credo%203,19.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (35, 31, 'Risuscitò', 'risuscito', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/risuscito.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (36, 32, 'Verso te, o città Santa', 'verso_te_o_citta_santa', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/Verso%20te%20o%20citta%20santa.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (37, 32, 'La marcia è dura', 'marcia_e_dura', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/La%20marcia%20e%20duraf.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (38, 33, 'Dal profondo a Te grido - Salmo 129(130)', 'dal_profondo_a_te_grido', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/dalprofondoategridosig.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (39, 34, 'Canto di Giosuè (Gs. 24,3-18)', 'canto_di_giosue', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/cantodigiosue.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (40, 34, 'Benedici anima mia, Jahvè - Salmo 102(103)', 'benedici_anima_mia_jahve', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/benedicianimamiajahve.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (41, 35, 'Fino a quando - Salmo 12(13)', 'fino_a_quando', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/finoaquando.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (42, 35, 'Jahvè Tu sei il mio Dio (Is. 25,1-8)', 'jahve_tu_sei_mio_dio', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Jahve%20tu%20sei%20il%20mio%20Dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (43, 36, 'Cantiamo cantiamo (Es. 15,1-2)', 'cantiamo_cantiamo', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/cantiamocantiamo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (44, 36, 'Giunga la mia preghiera fino a Te - Salmo 118(119)', 'giunga_la_mia_preghiera', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/giungalamiapreghiera.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (45, 37, 'Guardate come è bello - Salmo 132(133)', 'guardate_come_e_bello', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Guardate%20come%20%C3%A8%20bello%20stare%20con%20i%20fratellif.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (46, 37, 'Come è bello, come dà gioia - Salmo 132(133)', 'come_bello_come_da_gioia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/comeebellocomedagioia.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (47, 38, 'Guardate com''è bello, gustate quant''è - Salmo 132(133)', 'guardate_come_e_bello_gustate', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Guardatecomebell.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (48, 39, 'Grazie a Jahvè - Salmo 135(136)', 'grazie_a_jahve', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Grazie%20a%20Jahv%C3%A8.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";				
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (49, 40, 'Canto dei tre giovani nella fornace (Dn. 3,52-57) (I parte)', 'canto_giovani_fornace_I', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/benedettoseitusignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (50, 41, 'Canto dei tre giovani nella fornace (Dn. 3,58-88) (II parte)', 'canto_giovani_fornace_II', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Canto%20dei%20tre%20giovani%20nella%20fornace%20(II%20parte).mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (51, 42, 'Lodate il Signore dai cieli - Salmo 148', 'lodate_il_signore_dai_cieli', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/lodateilsignoredaicieli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (52, 43, 'Lodate Iddio - Salmo 150', 'lodate_iddio', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Alleluja%20lodate%20Iddiof.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (53, 44, 'Il Signore è mia luce e mia salvezza - Salmo 26(27)', 'signore_e_mia_luce', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/ilsignoremialuceemiasalvezza.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (54, 44, 'Evenu shalom alejem (canto ebraico)', 'evenu_shalom', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/evenushalomalejem.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (55, 45, 'Già viene il Regno (Ap. 19,6-9)', 'gia_viene_il_regno', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/giavieneilregno.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (56, 45, 'Abbà Padre (Rom. 8,15-17)', 'abba_padre', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/abbapadre.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (57, 46, 'Chi ci separerà (Rom. 8,33-39)', 'chi_ci_separera', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/chiciseparera.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (58, 47, 'Magnificat (Lc. 1,46-55)', 'magnificat', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/magnificat.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (59, 48, 'Innalzerò la coppa di salvezza - Salmo 114-115(116)', 'innalzero_la_coppa_di_salvezza', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/innalzerolacoppadisalvezza.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (60, 49, 'Quando il Signore - Salmo 125(126)', 'quando_il_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/percheallandarsiva.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (61, 50, 'Cantico di Zaccaria (Lc. 1,67-80)', 'cantico_di_zaccaria', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/benedictuscanticodizaccaria.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (62, 51, 'O morte, dov''è la tua vittoria? (1 Cor. 15)', 'o_morte_dove_la_tua_vittoria', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/O%20morte,%20dov%C2%B4e%20la%20tua%20vittoria.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (63, 52, 'O cieli, piovete dall''alto (Is. 45,8)', 'o_cieli_piovete_dall_alto', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/maranata.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (64, 53, 'Pentecoste', 'pentecoste', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/pentecoste.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (65, 54, 'Ecco qui, io vengo presto (Apoc. 22,12-16)', 'ecco_qui_vengo_presto', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/eccoquiiovengopresto.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (66, 54, 'Vieni, figlio dell''uomo (Apoc. 22,17 ss)', 'vieni_figlio_dell_uomo', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/vienifigliodell''uomo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (67, 55, 'Abramo (Gen. 18,1-5)', 'abramo', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/abramo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (68, 56, 'Cantico di Mosè (Es. 15,1-18)', 'cantico_di_mose', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/canticodimose.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (69, 57, 'Lodate il Signore tutti i popoli della terra - Salmo 116(117)', 'lodate_il_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/lodateilsignoretuttiipopolidellaterra.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (70, 58, 'Quando Israele uscì dall''Egitto - Salmo 113A(114)', 'quando_israele_usci_egitto', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/quandoisdraeleuscidall''Egitto.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (71, 59, 'Alzate o porte - Salmo 23(24)', 'alzate_o_porte', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Alzate%20o%20porte.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (72, 60, 'Il Signore è il mio pastore - Salmo 22(23)', 'signore_mio_pastore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Il%20Signore%20%C3%A8%20il%20mio%20pastore%20%20%20Kiko.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (73, 61, 'Giunti sui fiumi di Babilonia - Salmo 136(137)', 'giunti_fiumi_babilonia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/giuntisuifiumidibabilonia.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (74, 62, 'Pietà di me o Dio - Salmo 50(51)', 'pieta_di_me_o_dio', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/pietadime%20o%20dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (75, 63, 'Misericordia Dio, misericordia - Salmo 50(51)', 'misericordia_dio_misericordia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/misericordiadio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (76, 64, 'Cristo Gesù è il Signore! - Inno della Kenosis (Fil. 2,1-11)', 'inno_della_kenosis', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/cristogesueilsignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (77, 65, 'Ave Maria', 'ave_maria', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/Ave%20Mariaf.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (78, 65, 'Ave Maria (1984)', 'ave_maria_1984', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/avemaria%201984.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (79, 66, 'Maria, piccola Maria - Inno alla Vergine Maria', 'maria_piccola_maria', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/mariapiccolamaria.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (80, 67, 'Alzo gli occhi verso i monti - Salmo 120(121)', 'alzo_gli_occhi', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/alzogliocchiversoimonti.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (81, 67, 'Canto dei liberati (Is. 12,4-6)', 'canto_liberati', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Canto%20dei%20liberati.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (82, 68, 'Se oggi ascoltate la sua voce - Salmo 94(95)', 'se_oggi_ascoltate_sua_voce', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/seoggiascoltatelasuavoce.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (83, 69, 'Venite applaudiamo al Signore - Salmo 94(95)', 'venite_applaudiamo_al_signore', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/veniteapplaudiamoalsignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (84, 70, 'Dajenù', 'dajenu', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/Dajen%C3%B9.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (85, 71, 'Alla vittima pasquale', 'alla_vittima_pasquale', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/allavittimapasquale.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (86, 71, 'Inno di Pasqua', 'inno_di_pasqua', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/innodipasqua.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (87, 72, 'Inno di Avvento ''Chiara una voce dissipa l''oscurità''', 'inno_avvento', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/innodiavvento.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (88, 73, 'Urì, urì, urà', 'uri_uri_ura', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/uriuriura.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (89, 73, 'Già viene il mio Dio', 'gia_viene_il_mio_dio', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/giavieneilmiodio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "";
		sql += "INSERT INTO ELENCO ";
		sql += "VALUES (90, 74, 'Amen - Amen - Amen (Ap. 7,12-14)', 'amen_amen_amen', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/04AMENAMENAMEN%204,46.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (91, 75, 'Se il Signore non costruisce la casa - Salmo 126(127)', 'se_signore_non_costruisce', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/seilsignoncostruiscelacasa.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (92, 75, 'Gustate e vedete - Salmo 33(34)', 'gustate_e_vedete', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/Gustate%20e%20vedetef.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (93, 76, 'Per amore dei miei fratelli - Salmo 121(122)', 'per_amore_dei_miei_fratelli', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/peramoredeimieifratelli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (94, 77, 'Ho sperato, ho sperato nel Signore - Salmo 39(40)', 'ho_sperato_nel_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Ho%20sperato%20ho%20sperato%20nel%20Signore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (95, 78, 'Voglio cantare - Salmo 56(57)', 'voglio_cantare', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/vogliocantare.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (96, 79, 'Perchè le genti congiurano? - Salmo 2', 'perche_genti_congiurano', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/perchelegenticongiurano.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (97, 80, 'Come una cerva anela - Salmo 41-42(42-43)', 'come_una_cerva_anela', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Come%20una%20cerva%20anela.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (98, 81, 'Acclamate al Signore - Salmo 99(100)', 'acclamate_al_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/acclamate%20al%20signore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (99, 81, 'Gridate con gioia (Is. 12,1 ss)', 'gridate_con_gioia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/gridat%20congioia.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";		
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (100, 82, 'Al risveglio mi sazierò del tuo volto, Signor - Salmo 16(17)'," +
				" 'al_risveglio_mi_saziero', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/alrisvegliomisaziero%20deltuovoltosignor.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";		
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (101, 83, 'Canto dei bambini nella veglia di Pasqua (Melodia ebraica)', " +
				"'canto_bambini_veglia', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/Checosacedidiversoquestanotte.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";			
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (102, 84, 'Non morirò - Salmo 117(118)', 'non_moriro', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/nonmoriro.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";		
		db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (103, 85, 'Non morirò - Salmo 117(118) - II parte', 'non_moriro_II', 0, '"
//				+ BIANCO + "', 'http://www.resuscicanti.com/nonmoriro.mp3', "
//				+ "0, 0, 0, NULL, NULL)";
//		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (104, 86, 'Dite agli smarriti di cuore (Is. 35,4 ss)', 'dite_agli_smarriti', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/diteaglismarritidicuor.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (105, 87, 'O Dio tu sei il mio Dio - Salmo 62(63)', 'o_dio_tu_sei_il_mio_dio', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/O%20Dio%20tu%20sei%20il%20mio%20Dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (106, 87, 'Sale Dio tra acclamazioni - Salmo 46(47)', 'sale_dio_tra_acclamazioni', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/salediotraacclamazioni.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (107, 88, 'Il popolo che camminava nelle tenebre (Isaia 9,1-5)', 'popolo_camminava_tenebre', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/ilpopolochecamminava.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (108, 88, 'Da'' lode al Signore - Salmo 145(146)', 'da_lode_al_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/dalodealsignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (109, 89, 'Canto di Balaam (Nm. 23,7-24)', 'canto_di_balaam', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/cantodibalaam.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (110, 90, 'Davanti agli angeli - Salmo 137(138)', 'davanti_agli_angeli', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/davantiagliangeli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (111, 91, 'Quando Israele era un bimbo (Os. 11,1-11)', 'quando_israele_era_un_bimbo', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/cantodiosea.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (112, 91, 'E'' la Pasqua del Signore', 'e_la_pasqua_del_signore', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/elapasquadelsig.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (113, 92, 'C''è un tempo per ogni cosa (Qo. 3,1-5)', 'tempo_ogni_cosa', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Ceuntempoperognicosa2.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (114, 93, 'Benedici anima mia il Signore (Cantico di Tobia) (Tb 13)', 'benedici_anima_mia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/BENEDICI%20ANIMA%20MIA%20IL%20SIGNOREGerusalemme%20ricostruita2.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (114, 93, 'Benedici anima mia il Signore (Cantico di Tobia) (Tb 13)', 'benedici_anima_mia', 0, '"
//				+ BIANCO
//				+ "', 'http://www.camino-neocatecumenal.org/neo/CARISMAS/cantores/cantos%20mp3/mp3/ITALIANO/Benedici_anima_mia_il_Signore.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (115, 94, 'Quanto sono amabili le tue dimore - Salmo 83(84)', 'quanto_sono_amabili_dimore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Quanto%20sono%20amabili%20le%20tue%20dimore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (116, 95, 'Viene il Signore vestito di maestà', 'viene_il_signore_vestito_di_maesta', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/vieneilsignorevestitodimaesta.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (117, 95, 'Giorno di riposo', 'giorno_di_riposo', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/Giorno%20di%20riposo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (118, 96, 'Consolate il mio popolo (Is. 40,1-11)', 'consolate_il_mio_popolo', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/consolateilmiopopolo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (119, 97, 'C''erano due angeli', 'cerano_due_angeli', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/cerano2angeli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (120, 98, 'Amo il Signore - Salmo 114(115)', 'amo_il_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Amo%20il%20signore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (121, 99, 'Venite a me voi tutti (Matteo 11,28-30)', 'venite_a_me_voi_tutti', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/veniteamevoitutti.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (122, 100, 'A te Signore si deve lode in Sion - Salmo 64(65)', 'a_te_si_deve_lode_in_sion', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/A%20te%20signore%20si%20deve%20lode%20in%20Sion.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (123, 101, 'Se nel Signore mi sono rifugiato - Salmo 10(11)', 'se_signore_sono_rifugiato', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/senelsigmisonorifugiato.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (124, 102, 'Il Signore annuncia una notizia (vv. 12-16.33.34) - Salmo 67(68)', 'signore_annuncia_una_notizia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/ilsignoreannunciaunanotizia.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (125, 103, 'Benedite il Signore - Salmo 133(134)', 'benedite_il_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Benedite%20il%20Signore%20(Filippucci).mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (126, 104, 'Figlie di Gerusalemme (Lc. 23,28.31.34.43.46) (Marcia funebre)', 'figlie_di_gerusalemme', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/figliedigerusalemme.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (127, 105, 'Ti ho manifestato il mio peccato - Salmo 31(32)', 'ti_ho_manifestato_mio_peccato', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/tihomanifestatoilmi%20peccato.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (128, 106, 'Maria madre della Chiesa (Gv. 19,26-34) (1983)', 'maria_madre_della_chiesa', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Maria%20Madre%20della%20Chiesa.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (129, 107, 'Stabat Mater', 'stabat_mater', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/Stabat%20Mater.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (130, 108, 'Lamenti del Signore (Venerdì Santo - Adorazione della croce)', 'lamenti_del_signore', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/popolomio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (131, 109, 'O Signore nostro Dio - Salmo 8', 'o_signore_nostro_dio', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/O%20signore%20nostro%20Dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (132, 110, 'Benedetta sei tu, Maria (Lc. 1,42-44)', 'benedetta_sei_tu_maria', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/benedettaseitumaria.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (133, 110, 'Salve regina dei cieli', 'salve_regina_dei_cieli', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Salve%20Regina%20dei%20cieli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (134, 111, 'Vergine della meraviglia', 'vergine_della_meraviglia', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/mariafigliadeltuofiglio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (135, 112, 'Maria, casa di benedizione', 'maria_casa_di_benedizione', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Maria%20casa%20di%20benedizione.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (136, 113, 'Benedirò il Signore in ogni tempo (1986) - Salmo 33(34)', " +
				"'benediro_il_signore_in_ogni_tempo', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/benediroilsignoreinognitempo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (137, 114, 'La mietitura delle nazioni (Gv. 4,31-38)', 'mietitura_delle_nazioni', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/La%20mietitura%20delle%20nazioni.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (138, 115, 'Dice il Signore al mio Signore - Salmo 109(110)', 'dice_il_signore_al_mio_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/diceilsignorealmiosignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (139, 116, 'Felice l''uomo - Salmo 1', 'felice_uomo', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Felice%20l%C2%B4uomo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";	
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (141, 117, 'Tu sei il più bello - Salmo 44(45)', 'tu_sei_il_piu_bello', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/tuseiilpiubello.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (142, 118, 'Felicità per l''uomo (1990) - Salmo 127(128)', 'felicita_per_l_uomo', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/felicitaperluomo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (143, 119, 'Sorga Dio (vv. 2-4-5-6-7) - Salmo 67(68)', 'sorga_dio', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/sorgadio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (144, 120, 'Andiamo, già pastori', 'andiamo_gia_pastori', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/andiamogiapastori.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (145, 121, 'Maria di Jasna Gòra (Inno alla Madonna di Czestochowa)', 'maria_di_jasna_gora', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/mariadiyasnagora.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (146, 122, 'A te, Signore, con la mia voce - Salmo 141(142)', " +
				"'a_te_signore_con_la_mia_voce', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/a%20te%20signore%20conlamiavoce.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (147, 123, 'Un germoglio spunta dal tronco di Jesse (Is. 11)', " +
				"'un_germoglio_spunta_tronco', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/b%20ungermogliospuntadaltroncodijesse.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (148, 124, 'A te Signore innalzo la mia anima - Salmo 24(25)', " +
				"'a_te_signore_innalzo_la_mia_anima', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/atesignoreinnalzolamiaanima.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (149, 125, 'Ti sto chiamando (Contro le seduzioni del peccato) - Salmo 140(141)', " +
				"'ti_sto_chiamando', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/tistochiamando.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (150, 126, 'A te levo i miei occhi - Salmo 122(123)', " +
				"'a_te_levo_i_miei_occhi', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/atelevoimieiocchi.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (151, 127, 'Signore, non punirmi nel tuo sdegno - Salmo 6', " +
				"'signore_non_punirmi_nel_tuo_sdegno', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/signorenonpunirmineltuosdegno.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (152, 128, 'Gloria, gloria, gloria (Is. 66,18 ss)', 'gloria_gloria_gloria', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/gloria%20gloria%20gloria%20completo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (153, 129, 'Esultate, giusti, nel Signore - Salmo 32(33)', 'esultate_giusti_nel_signore', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/esultategiustinelsignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (154, 130, 'Molto mi hanno perseguitato - Salmo 128(129)', " +
				"'molto_mi_hanno_perseguitato', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/moltomihannoperseguitato.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (155, 131, 'Ti amo Signore - Salmo 17(18)', 'ti_amo_signore', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/tiamosignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (156, 132, 'Maria, madre del cammino ardente', 'maria_madre_cammino_ardente', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Maria,%20madre%20del%20camino%20ardiente.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (157, 133, 'Shlom lech Mariàm', 'shlom_lech_mariam', 0, '" + BIANCO
				+ "', 'http://www.resuscicanti.com/Shlom%20lech%20Mariam.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (158, 134, 'Andate ed annunziate ai miei fratelli (Mt. 28,16.20)', " +
				"'andate_ed_annunziate', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Andate%20ed%20annunziate%20ai%20miei%20fratelli.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (159, 135, 'Mi indicherai il sentiero della vita - Salmo 15(16)', 'mi_indicherai_sentiero_vita', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Mimindicherai%20il%20sentiero%20della%20vita.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (160, 136, 'O Dio, per il tuo nome - Salmo 53(54)', 'o_dio_per_il_tuo_nome', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/O%20Dio,%20per%20il%20tuo%20nome.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (161, 137, 'Lo stolto pensa che non c''è Dio - Salmo 13(14)', 'stolto_pensa_che_non_ce_dio', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Lo%20stolto%20pensa%20che%20non%20c%C2%B4e%20Dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (162, 138, 'Signore, ascolta la mia preghiera - Salmo 142(143)', 'signore_ascolta_mia_preghiera', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Signore,%20ascolta%20la%20mia%20preghiera.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (163, 139, 'Non è qui (Mt. 28,1-7)', 'non_e_qui_e_risorto', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/Non%20%C3%A8%20qu%C3%AC%20%C3%A8%20risorto%20A.P..mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (164, 140, 'Vi prenderò dalle genti (Ez. 36,24-28)', 'vi_prendero_dalle_genti', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/viprenderodallegenti.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (165, 141, 'Voi siete la luce del mondo (Mt. 5,14-16)', 'voi_siete_la_luce_del_mondo', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/voisietelalucedelmondo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (166, 142, 'Sola a solo (Amsterdam 30 aprile 2005)', 'sola_a_solo', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/solaasolo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (167, 143, 'In mezzo a una grande folla (Lc. 8,42-48)', 'in_mezzo_grande_folla', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/In%20mezzo%20a%20una%20grande%20folla.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (168, 144, 'Zaccheo (Lc. 19,1-10)', 'zaccheo', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/zaccheo%20sei%20tu.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (169, 201, 'Siedi solitario e silenzioso (Lam. 3,1-33)', 'siedi_solitario_silenzioso', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Siedi%20solitario%20e%20silenzioso.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (170, 202, 'Così parla l''amen (Apoc. 3,14-22)', 'cosi_parla_amen', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Cosi%20parla%20l%C2%B4amen.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (171, 203, 'Ti vedranno i re: II canto del servo (Is 49,1-16)', 'ti_vedranno_i_re', 0, '"
				+ VERDE + "', 'http://www.resuscicanti.com/tivedrannoire.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (172, 204, 'Giacobbe (Gen. 32,23-29)', 'giacobbe', 0, '"
				+ VERDE + "', 'http://www.resuscicanti.com/giacobbe.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (173, 205, 'Debora (Gdc. 5,1 ss)', 'debora', 0, '" + VERDE
				+ "', 'http://www.resuscicanti.com/debora.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (174, 206, 'Vedo i cieli aperti (Apoc. 19,11-20)', 'vedo_cieli_aperti', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/vedoicieliaperti.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (175, 207, 'Il Signore mi ha dato: III canto del servo (Is. 50,4-10)', 'il_signore_mi_ha_dato', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/ilsignoremihadato.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (176, 208, 'Il pigiatore (Is. 61,1-6)', 'pigiatore', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/chiecostuichevienedaedom.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (177, 209, 'Il seminatore (Mc. 4,3 ss)', 'seminatore', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Il%20seminatore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (178, 210, 'Lo Spirito del Signore è sopra di me (Lc. 4,18-19) (Is. 61,1-3)', 'spirito_del_signore_sopra_di_me', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/lospiritodelsignore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (179, 210, 'Ecco lo specchio nostro (Ode XIII di Salomone)', 'ecco_lo_specchio_nostro', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/eccolospecchionostro.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (180, 211, 'Come lo slancio dell''ira (Ode VII di Salomone)', 'come_slancio_ira', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Come%20lo%20slancio%20dell%C2%B4ira.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (181, 212, 'Benedetto sia Iddio (Ef. 1,3-13)', 'benedetto_sia_iddio', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/benedettosiaiddio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (182, 213, 'Signore tu mi scruti e mi conosci - Salmo 138(139)', 'signore_tu_scruti_conosci', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Signore%20tu%20mi%20scruti%20e%20mi%20conosci%20kiko.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (183, 214, 'Eli, Eli, lammà sabactani? - Salmo 21(22)', 'eli_eli_lamma_sabactani', 0, '"
				+ VERDE + "', 'http://www.resuscicanti.com/elielilammasabactani.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

//		sql = "INSERT INTO ELENCO ";
//		sql += "VALUES (184, 215, 'Eli, Eli, lammà sabactani? - parte II - Salmo 21(22)', 'eli_eli_lamma_sabactani_II', 0, '"
//				+ VERDE + "', 'http://www.resuscicanti.com/elielilammasabactani.mp3', "
//				+ "0, 0, 0, NULL, NULL, 2)";
//		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (185, 216, 'Nessuno può servire due padroni (Mt. 6,24-33)', 'nessuno_puo_servire_due_padroni', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/nessunopuoservireduepadroni.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (186, 217, 'Signore, il mio cuore non ha più pretese - Salmo 130(131)', 'signore_mio_cuore_pretese', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/signoreilmiocuorenonhapiupretese.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (187, 217, 'Voglio andare a Gerusalemme (Canto sefardita)', 'voglio_andare_a_gerusalemme', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/voglioandareagerusalemme.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (188, 218, 'Shemà Israel (Dt. 6,4-9)', 'shema_israel', 0, '"
				+ VERDE + "', 'http://www.resuscicanti.com/shemaisrael.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (189, 219, 'Inno alla croce gloriosa', 'inno_croce_gloriosa', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Inno%20alla%20croce%20gloriosa.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (190, 220, 'Rivestitevi dell''armatura di Dio (Ef. 6,11 ss)', 'rivestitevi_dell_armatura', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/Rivestitevi%20dell%C2%B4armatura%20di%20Dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (191, 221, 'Le sue fondamenta - Salmo 86(87)', 'sue_fondamenta', 0, '"
				+ VERDE
				+ "', 'http://www.resuscicanti.com/lesuefondamenta.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (192, 222, 'Akedà', 'akeda', 0, '" + VERDE
				+ "', 'http://www.resuscicanti.com/Akeda.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (193, 261, 'Non ti adirare - Salmo 36(37)', 'non_ti_adirare', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/nontiadirare.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (194, 262, 'Inno alla carità (1 Cor. 13,1-13)', 'inno_alla_carita', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/innoallacarita.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (195, 263, 'Lo stesso Iddio (2 Cor. 4,6-12)', 'stesso_iddio', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/lostessoiddio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (196, 264, 'Come condannati a morte (1 Cor. 4,9-13)', 'come_condannati_a_morte', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/come%20condannati%20a%20morte.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (197, 265, 'Gesù percorreva tutte le città (cfr. Mt. 9,35 ss; 10)', 'gesu_percorreva', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/gesupercorrevatuttelecitta.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (198, 266, 'Non resistete al male (Mt. 5,38 ss)', 'non_resistete_al_male', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/nonresistetealmale.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (199, 267, 'Che mi baci (Ct. 1,2 ss)', 'che_mi_baci', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Che%20mi%20baci.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (200, 268, 'La mia diletta è per me (Ct. 1,13 ss)', 'mia_diletta_e_per_me', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/lamiadilettaeperme.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (201, 269, 'Vieni dal Libano (Ct. 4,8 ss)', 'vieni_dal_libano', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/vienidallibano.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (202, 270, 'Quando dormivo (Ct. 5,2 ss)', 'quando_dormivo', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/quandodormivo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (203, 271, 'Tu che abiti nei giardini (Ct. 8, Appendici)', 'tu_che_abiti_nei_giardini', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Tu%20che%20abiti%20nei%20giardin.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (204, 272, 'Agnella di Dio', 'agnella_di_dio', 0, '" + AZZURRO
				+ "', 'http://www.resuscicanti.com/Agnella%20di%20Dio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (205, 273, 'Non c''è in lui bellezza: IV canto del servo di Jahvè (Is. 53,2 ss)', 'non_ce_in_lui_bellezza', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/davantialuisicopreilvolto.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (206, 274, 'Canto dell''Agnello (Ap. 5,9 ss)', 'canto_dell_agnello', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/cantodellagnello.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (207, 275, 'Chi è colei (Ct. 8,5-7)', 'chi_e_colei', 0, '"
				+ AZZURRO + "', 'http://www.resuscicanti.com/chiecolei.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (208, 276, 'La voce del mio amato (Ct. 2,8-17)', 'voce_del_mio_amato', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/lavocedelmioamato.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (209, 277, 'La colomba volò (Ode XXIV di Salomone)', 'colomba_volo', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/La%20colomba%20volo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (210, 278, 'Come stilla il miele (Ode XL di Salomone)', 'come_stilla_il_miele', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Come%20stilla%20il%20miele.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (211, 279, 'O Gesù, amore mio', 'o_gesu_amore_mio', 0, '" + AZZURRO
				+ "', 'http://www.resuscicanti.com/ogesuamoremio.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (212, 280, 'Portami in cielo', 'portami_in_cielo', 0, '" + AZZURRO
				+ "', 'http://www.resuscicanti.com/portamiincielo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (213, 281, 'Tu sei la mia speranza Signore (Ode XXIX di Salomone)', 'tu_sei_mia_speranza_signore', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Tu%20sei%20la%20mia%20speranza%20signore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (214, 282, 'Una donna vestita di sole (Ap. 12)', 'una_donna_vestita_di_sole', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/una%20gran%20senal%20italiano.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (215, 283, 'Ho steso le mie mani', 'ho_steso_le_mani', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/Ho%20steso%20le%20mie%20mani.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (216, 284, 'Omelia pasquale di Melitone di Sardi', 'omelia_pasquale_melitone_sardi', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/omeliapasqualedimelitonedisardi.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (217, 285, 'Carmen ''63 (di Tagore)', 'carmen_63', 0, '"
				+ AZZURRO + "', 'http://www.resuscicanti.com/carmen%2063.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (218, 286, 'Caritas Christi (2 Cor. 5,14-17.21; 1 Cor. 9,16b)', 'caritas_christi', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/caritascristia.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (219, 287, 'Noli me tangere (Gv. 20,3-17)', 'noli_me_tangere', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Nolimetangere.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";		
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (220, 288, 'Signore, aiutami, Signore', 'signore_aiutami_signore', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Signore,%20aiutami,%20signore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (221, 289, 'Mi hai sedotto, Signore (Ger. 20,7-18)', 'mi_hai_sedotto_signore', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/mihaisedottosignorek2.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (222, 291, 'Amate i vostri nemici (Le Beatitudini: Lc. 6,20 ss)', 'amate_i_vostri_nemici', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Amate%20i%20vostri%20nemici.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (223, 292, 'Tu sei bella, amica mia (Ct 6-7)', 'tu_sei_bella_amica_mia', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/tuseibellaamicamia.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (224, 293, 'Fratelli, non diamo a nessuno motivo d''inciampo (Cfr. 2 Cor. 6,3-16)', 'fratelli_non_diamo_a_nessuno', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/fratellinondiamo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (225, 294, 'Questo è il mio comandamento (Gv. 15,12.13.16.18; 17,21)', 'questo_e_io_mio_comandamento', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/questoeilmiocomandamento.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (226, 295, 'Mi rubasti il cuore (Ct. 4,9 - 5,1)', 'mi_rubasti_cuore', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/mi%20rubasti%20il%20cuore.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";		
		db.execSQL(sql);

		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (227, 290, 'In una notte oscura (S. Giovanni della Croce)', 'in_una_notte_oscura', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/inunanotteoscura.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (228, 297, 'Se siete risorti con Cristo (Col 3,1-4)', 'se_siete_risorti', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/Se%20siete%20risorti%20con%20Cristo%202012.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (229, 296, 'Una gran señal (Ap. 12)', 'una_gran_senal', 0, '"
				+ AZZURRO
				+ "', 'http://www.resuscicanti.com/una%20gran%20senal%20italiano.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (230, 146, 'Resurrexit (Dialogo di Nostro Signore Gesù Cristo con Marta) (Gv 11,25-27)', 'resurrexit', 0, '"
				+ BIANCO
				+ "', 'http://www.resuscicanti.com/resurrexit%202013.14%20.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (231, 298, 'Ecco il mio servo: I canto del servo di Jahvè (Is 42,1-4)', 'ecco_il_mio_servo', 0, '"
				+ GRIGIO
				+ "', 'http://www.resuscicanti.com/ecco%20il%20mio%20servo.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (232, 145, 'Ave, o Maria, colomba incorrotta', 'ave_maria_colomba', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/aveomariacolomba.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		
		sql = "INSERT INTO ELENCO ";
		sql += "VALUES (233, 147, 'Il Messia leone per vincere', 'messia_leone_vincere', 0, '"
				+ BIANCO + "', 'http://www.resuscicanti.com/Il%20Messia%20Italiano.mp3', "
				+ "0, 0, 0, NULL, NULL, 2)";
		db.execSQL(sql);
		// FINE CANTI

		// TITOLI DEGLI ARGOMENTI
		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (1, 'Canti della Bibbia - Antico Testamento')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (2, 'Canti della Bibbia - Nuovo Testamento')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (3, '" + appContext.getResources().getString(R.string.Canti_dalle_Odi_di_Salomone) + "')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (4, 'Ispirati a melodie e rituali ebraici')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (5, 'Canti per bambini')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (6, 'Dall''ordinario della Messa')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (7, 'Canti per la frazione del pane')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (8, 'Dalla liturgia della veglia pasquale')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (9, 'Canti per il sacramento della riconciliazione')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (10, 'Inni liturgici')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (11, 'Canti a Maria')";
		db.execSQL(sql);

		sql = "INSERT INTO ARG_NAMES ";
		sql += "VALUES (12, 'Vari')";
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
		sql += "VALUES (38, '129', 'Salmo 129(130) - Dal profondo a Te grido')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (40, '102', 'Salmo 102(103) - Benedici anima mia, Jahvè')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (41, '012', 'Salmo 12(13) - Fino a quando')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (44, '118', 'Salmo 118(119) - Giunga la mia preghiera fino a Te')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (45, '132', 'Salmo 132(133) - Guardate come è bello')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (46, '132', 'Salmo 132(133) - Come è bello, come dà gioia')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (47, '132', 'Salmo 132(133) - Guardate com''è bello, gustate quant''è')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (48, '135', 'Salmo 135(136) - Grazie a Jahvè')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (51, '148', 'Salmo 148 - Lodate il Signore dai cieli')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (52, '150', 'Salmo 150 - Lodate Iddio')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (53, '026', 'Salmo 26(27) - Il Signore è mia luce e mia salvezza')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (59, '114-115', 'Salmo 114-115(116) - Innalzerò la coppa di salvezza')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (60, '125', 'Salmo 125(126) - Quando il Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (69, '116', 'Salmo 116(117) - Lodate il Signore tutti i popoli della terra')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (70, '113A', 'Salmo 113A(114) - Quando Israele uscì dall''Egitto')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (71, '023', 'Salmo 23(24) - Alzate o porte')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (72, '022', 'Salmo 22(23) - Il Signore è il mio pastore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (73, '136', 'Salmo 136(137) - Giunti sui fiumi di Babilonia')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (74, '050', 'Salmo 50(51) - Pietà di me o Dio')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (75, '050', 'Salmo 50(51) - Misericordia Dio, misericordia')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (80, '120', 'Salmo 120(121) - Alzo gli occhi verso i monti')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (82, '094', 'Salmo 94(95) - Se oggi ascoltate la sua voce')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (83, '094', 'Salmo 94(95) - Venite applaudiamo al Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (91, '126', 'Salmo 126(127) - Se il Signore non costruisce la casa')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (92, '033', 'Salmo 33(34) - Gustate e vedete')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (93, '121', 'Salmo 121(122) - Per amore dei miei fratelli')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (94, '039', 'Salmo 39(40) - Ho sperato, ho sperato nel Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (95, '056', 'Salmo 56(57) - Voglio cantare')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (96, '002', 'Salmo 2 - Perchè le genti congiurano?')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (97, '041-042', 'Salmo 41-42(42-43) - Come una cerva anela')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (98, '099', 'Salmo 99(100) - Acclamate al Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (100, '016', 'Salmo 16(17) - Al risveglio mi sazierò del tuo volto, Signor')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (102, '117', 'Salmo 117(118) - Non morirò')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (105, '062', 'Salmo 62(63) - O Dio tu sei il mio Dio')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (106, '046', 'Salmo 46(47) - Sale Dio tra acclamazioni')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (108, '145', 'Salmo 145(146) - Da'' lode al Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (110, '137', 'Salmo 137(138) - Davanti agli angeli')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (115, '083', 'Salmo 83(84) - Quanto sono amabili le tue dimore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (120, '114', 'Salmo 114(115) - Amo il Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (122, '064', 'Salmo 64(65) - A te Signore se deve lode in Sion')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (123, '010', 'Salmo 10(11) - Se nel Signore mi sono rifugiato')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (124, '067-v12', 'Salmo 67(68) - Il Signore annuncia una notizia (vv. 12-16.33.34)')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (125, '133', 'Salmo 133(134) - Benedite il Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (127, '031', 'Salmo 31(32) - Ti ho manifestato il mio peccato')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (131, '008', 'Salmo 8 - O Signore nostro Dio')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (136, '033', 'Salmo 33(34) - Benedirò il Signore in ogni tempo (1986)')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (138, '109', 'Salmo 109(110) - Dice il Signore al mio Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (139, '001', 'Salmo 1 - Felice l''uomo')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (141, '044', 'Salmo 44(45) - Tu sei il più bello')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (142, '127', 'Salmo 127(128) - Felicità per l''uomo (1990)')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (143, '067-v02', 'Salmo 67(68) - Sorga Dio (vv. 2-4-5-6-7)')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (146, '141', 'Salmo 141(142) - A te, Signore, con la mia voce')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (148, '024', 'Salmo 24(25) - A te Signore innalzo la mia anima')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (149, '140', 'Salmo 140(141) - Ti sto chiamando (Contro le seduzioni del peccato)')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (150, '122', 'Salmo 122(123) - A te levo i miei occhi')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (151, '006', 'Salmo 6 - Signore, non punirmi nel tuo sdegno')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (153, '032', 'Salmo 32(33) - Esultate, giusti, nel Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (154, '128', 'Salmo 128(129) - Molto mi hanno perseguitato')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (155, '017', 'Salmo 17(18) - Ti amo Signore')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (159, '015', 'Salmo 15(16) - Mi indicherai il sentiero della vita')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (160, '053', 'Salmo 53(54) - O Dio, per il tuo nome')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (161, '013', 'Salmo 13(14) - Lo stolto pensa che non c''è Dio')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (162, '142', 'Salmo 142(143) - Signore, ascolta la mia preghiera')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (182, '138', 'Salmo 138(139) - Signore tu mi scruti e mi conosci')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (183, '021', 'Salmo 21(22) - Eli, Eli, lammà sabactani?')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (186, '130', 'Salmo 130(131) - Signore, il mio cuore non ha più pretese')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (191, '086', 'Salmo 86(87) - Le sue fondamenta')";
		db.execSQL(sql);
		
		sql = "INSERT INTO SALMI_MUSICA ";
		sql += "VALUES (193, '036', 'Salmo 36(37) - Non ti adirare')";
		db.execSQL(sql);
		//FINE SALMI CANTATI
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
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
		
		reCreateDatabse(db);
		
		ContentValues values = null;
		
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
						String nomeCanto = Utility.duplicaApostrofi(lista.getCantoPosizione(j)).substring(10);
//						Log.i("NOME NO-DUP", nomeCanto.substring(10));
						sql = "SELECT _id" +
					      		"  FROM ELENCO" +
					      		"  WHERE titolo =  '" + nomeCanto + "'";  
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
		
	}
	
	private void reCreateDatabse(SQLiteDatabase db) {
		String sql = "DROP TABLE IF EXISTS ELENCO";
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS ARGOMENTI";
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS ARG_NAMES";
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS CUST_LISTS";
		db.execSQL(sql);
//		sql = "DROP TABLE IF EXISTS LISTE_PERS";
//		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS SALMI_MUSICA";
		db.execSQL(sql);
		sql = "DROP TABLE IF EXISTS LOCAL_LINKS";
		db.execSQL(sql);
		
		onCreate(db);
	}
	
	private class Backup {
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
	
	private class BackupLocalLink{
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