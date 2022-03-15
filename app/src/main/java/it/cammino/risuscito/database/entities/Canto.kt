package it.cammino.risuscito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class Canto {

    @PrimaryKey
    var id: Int = 0

    var pagina: String? = null

    var titolo: String? = null

    var source: String? = null

    var favorite: Int = 0

    var color: String? = null

    var link: String? = null

    var zoom: Int = 0

    var scrollX: Int = 0

    var scrollY: Int = 0

    var savedTab: String? = null

    var savedBarre: String? = null

    var savedSpeed: String? = null

    companion object {

        private const val GIALLO = "#EBD0A5"
        const val BIANCO = "#FCFCFC"
        private const val AZZURRO = "#6F949A"
        private const val VERDE = "#8FC490"

        fun defaultCantoData(): ArrayList<Canto> {
            val cantiList = ArrayList<Canto>()
            var mCanto = Canto()
            mCanto.id = 1
            mCanto.pagina = "litanie_penitenziali_brevi_page"
            mCanto.titolo = "litanie_penitenziali_brevi_title"
            mCanto.source = "litanie_penitenziali_brevi_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "litanie_penitenziali_brevi_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 2
            mCanto.pagina = "preghiera_litanica_penitenziale_page"
            mCanto.titolo = "preghiera_litanica_penitenziale_title"
            mCanto.source = "preghiera_litanica_penitenziale_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_litanica_penitenziale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 3
            mCanto.pagina = "celebrazione_penitenziale_page"
            mCanto.titolo = "celebrazione_penitenziale_title"
            mCanto.source = "celebrazione_penitenziale_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "celebrazione_penitenziale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 4
            mCanto.pagina = "gloria_a_dio_cieli_page"
            mCanto.titolo = "gloria_a_dio_cieli_title"
            mCanto.source = "gloria_a_dio_cieli_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "gloria_a_dio_cieli_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 5
            mCanto.pagina = "santo_e_santo_quaresima_page"
            mCanto.titolo = "santo_e_santo_quaresima_title"
            mCanto.source = "santo_e_santo_quaresima_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "santo_e_santo_quaresima_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 6
            mCanto.pagina = "santo_ordinario_page"
            mCanto.titolo = "santo_ordinario_title"
            mCanto.source = "santo_ordinario_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "santo_ordinario_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 7
            mCanto.pagina = "santo_baracche_page"
            mCanto.titolo = "santo_baracche_title"
            mCanto.source = "santo_baracche_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "santo_baracche_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 8
            mCanto.pagina = "santo_palme_page"
            mCanto.titolo = "santo_palme_title"
            mCanto.source = "santo_palme_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "santo_palme_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 9
            mCanto.pagina = "santo_1988_page"
            mCanto.titolo = "santo_1988_title"
            mCanto.source = "santo_1988_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "santo_1988_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 10
            mCanto.pagina = "santo_1983_page"
            mCanto.titolo = "santo_1983_title"
            mCanto.source = "santo_1983_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "santo_1983_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 11
            mCanto.pagina = "preghiera_eucarestica_II_page"
            mCanto.titolo = "preghiera_eucarestica_II_title"
            mCanto.source = "preghiera_eucarestica_II_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_eucarestica_II_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 12
            mCanto.pagina = "preghiera_eucaristica_II_parte2_page"
            mCanto.titolo = "preghiera_eucaristica_II_parte2_title"
            mCanto.source = "preghiera_eucaristica_II_parte2_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_eucaristica_II_parte2_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 13
            mCanto.pagina = "benedizione_acqua_fonte_page"
            mCanto.titolo = "benedizione_acqua_fonte_title"
            mCanto.source = "benedizione_acqua_fonte_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "benedizione_acqua_fonte_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 15
            mCanto.pagina = "preconio_pasquale_page"
            mCanto.titolo = "preconio_pasquale_title"
            mCanto.source = "preconio_pasquale_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preconio_pasquale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 17
            mCanto.pagina = "prefazio_eucarestia_veglia_pasquale_page"
            mCanto.titolo = "prefazio_eucarestia_veglia_pasquale_title"
            mCanto.source = "prefazio_eucarestia_veglia_pasquale_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "prefazio_eucarestia_veglia_pasquale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 18
            mCanto.pagina = "inno_lodi_avvento_fino16_page"
            mCanto.titolo = "inno_lodi_avvento_fino16_title"
            mCanto.source = "inno_lodi_avvento_fino16_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_lodi_avvento_fino16_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 19
            mCanto.pagina = "inno_lodi_avvento_dopo16_page"
            mCanto.titolo = "inno_lodi_avvento_dopo16_title"
            mCanto.source = "inno_lodi_avvento_dopo16_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_lodi_avvento_dopo16_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 20
            mCanto.pagina = "inno_lodi_pasqua_fino_ascensione_page"
            mCanto.titolo = "inno_lodi_pasqua_fino_ascensione_title"
            mCanto.source = "inno_lodi_pasqua_fino_ascensione_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_lodi_pasqua_fino_ascensione_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 21
            mCanto.pagina = "inno_vespri_pasqua_fino_ascensione_page"
            mCanto.titolo = "inno_vespri_pasqua_fino_ascensione_title"
            mCanto.source = "inno_vespri_pasqua_fino_ascensione_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "inno_vespri_pasqua_fino_ascensione_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 22
            mCanto.pagina = "inno_lodi_pasqua_ascensione_pentecoste_page"
            mCanto.titolo = "inno_lodi_pasqua_ascensione_pentecoste_title"
            mCanto.source = "inno_lodi_pasqua_ascensione_pentecoste_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_lodi_pasqua_ascensione_pentecoste_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 23
            mCanto.pagina = "inno_vespri_pasqua_ascensione_pentecoste_page"
            mCanto.titolo = "inno_vespri_pasqua_ascensione_pentecoste_title"
            mCanto.source = "inno_vespri_pasqua_ascensione_pentecoste_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_vespri_pasqua_ascensione_pentecoste_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 24
            mCanto.pagina = "inno_lodi_pentecoste_page"
            mCanto.titolo = "inno_lodi_pentecoste_title"
            mCanto.source = "inno_lodi_pentecoste_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_lodi_pentecoste_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 25
            mCanto.pagina = "sequenza_di_pentecoste_page"
            mCanto.titolo = "sequenza_di_pentecoste_title"
            mCanto.source = "sequenza_di_pentecoste_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "sequenza_di_pentecoste_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 26
            mCanto.pagina = "preghiera_eucaristia_II_2_prefazio_page"
            mCanto.titolo = "preghiera_eucaristia_II_2_prefazio_title"
            mCanto.source = "preghiera_eucaristia_II_2_prefazio_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_eucaristia_II_2_prefazio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 27
            mCanto.pagina = "preghiera_eucaristica_II_2_consacrazione_page"
            mCanto.titolo = "preghiera_eucaristica_II_2_consacrazione_title"
            mCanto.source = "preghiera_eucaristica_II_2_consacrazione_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_eucaristica_II_2_consacrazione_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 28
            mCanto.pagina = "preghiera_eucaristica_II_2_offerta_page"
            mCanto.titolo = "preghiera_eucaristica_II_2_offerta_title"
            mCanto.source = "preghiera_eucaristica_II_2_offerta_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_eucaristica_II_2_offerta_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 29
            mCanto.pagina = "alleluja_pasquale_page"
            mCanto.titolo = "alleluja_pasquale_title"
            mCanto.source = "alleluja_pasquale_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "alleluja_pasquale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 30
            mCanto.pagina = "acclamazioni_al_vangelo_page"
            mCanto.titolo = "acclamazioni_al_vangelo_title"
            mCanto.source = "acclamazioni_al_vangelo_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "acclamazioni_al_vangelo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 31
            mCanto.pagina = "acclamazioni_al_vangelo_quaresima_page"
            mCanto.titolo = "acclamazioni_al_vangelo_quaresima_title"
            mCanto.source = "acclamazioni_al_vangelo_quaresima_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "acclamazioni_al_vangelo_quaresima_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 32
            mCanto.pagina = "te_deum_page"
            mCanto.titolo = "te_deum_title"
            mCanto.source = "te_deum_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "te_deum_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 140
            mCanto.pagina = "agnello_di_dio_page"
            mCanto.titolo = "agnello_di_dio_title"
            mCanto.source = "agnello_di_dio_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "agnello_di_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 33
            mCanto.pagina = "padre_nostro_page"
            mCanto.titolo = "padre_nostro_title"
            mCanto.source = "padre_nostro_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "padre_nostro_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 34
            mCanto.pagina = "credo_apostolico_page"
            mCanto.titolo = "credo_apostolico_title"
            mCanto.source = "credo_apostolico_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "credo_apostolico_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 35
            mCanto.pagina = "risuscito_page"
            mCanto.titolo = "risuscito_title"
            mCanto.source = "risuscito_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "risuscito_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 36
            mCanto.pagina = "verso_te_o_citta_santa_page"
            mCanto.titolo = "verso_te_o_citta_santa_title"
            mCanto.source = "verso_te_o_citta_santa_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "verso_te_o_citta_santa_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 37
            mCanto.pagina = "marcia_e_dura_page"
            mCanto.titolo = "marcia_e_dura_title"
            mCanto.source = "marcia_e_dura_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "marcia_e_dura_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 38
            mCanto.pagina = "dal_profondo_a_te_grido_page"
            mCanto.titolo = "dal_profondo_a_te_grido_title"
            mCanto.source = "dal_profondo_a_te_grido_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "dal_profondo_a_te_grido_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 39
            mCanto.pagina = "canto_di_giosue_page"
            mCanto.titolo = "canto_di_giosue_title"
            mCanto.source = "canto_di_giosue_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_di_giosue_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 40
            mCanto.pagina = "benedici_anima_mia_jahve_page"
            mCanto.titolo = "benedici_anima_mia_jahve_title"
            mCanto.source = "benedici_anima_mia_jahve_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "benedici_anima_mia_jahve_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 41
            mCanto.pagina = "fino_a_quando_page"
            mCanto.titolo = "fino_a_quando_title"
            mCanto.source = "fino_a_quando_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "fino_a_quando_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 42
            mCanto.pagina = "jahve_tu_sei_mio_dio_page"
            mCanto.titolo = "jahve_tu_sei_mio_dio_title"
            mCanto.source = "jahve_tu_sei_mio_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "jahve_tu_sei_mio_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

//            mCanto = Canto()
//            mCanto.id = 43
//            mCanto.pagina = "cantiamo_cantiamo_page"
//            mCanto.titolo = "cantiamo_cantiamo_title"
//            mCanto.source = "cantiamo_cantiamo_source"
//            mCanto.favorite = 0
//            mCanto.color = BIANCO
//            mCanto.link = "cantiamo_cantiamo_link"
//            mCanto.zoom = 0
//            mCanto.scrollX = 0
//            mCanto.scrollY = 0
//            mCanto.savedSpeed = "2"
//            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 44
            mCanto.pagina = "giunga_la_mia_preghiera_page"
            mCanto.titolo = "giunga_la_mia_preghiera_title"
            mCanto.source = "giunga_la_mia_preghiera_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "giunga_la_mia_preghiera_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 45
            mCanto.pagina = "guardate_come_e_bello_page"
            mCanto.titolo = "guardate_come_e_bello_title"
            mCanto.source = "guardate_come_e_bello_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "guardate_come_e_bello_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 46
            mCanto.pagina = "come_bello_come_da_gioia_page"
            mCanto.titolo = "come_bello_come_da_gioia_title"
            mCanto.source = "come_bello_come_da_gioia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "come_bello_come_da_gioia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 47
            mCanto.pagina = "guardate_come_e_bello_gustate_page"
            mCanto.titolo = "guardate_come_e_bello_gustate_title"
            mCanto.source = "guardate_come_e_bello_gustate_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "guardate_come_e_bello_gustate_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 48
            mCanto.pagina = "grazie_a_jahve_page"
            mCanto.titolo = "grazie_a_jahve_title"
            mCanto.source = "grazie_a_jahve_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "grazie_a_jahve_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 49
            mCanto.pagina = "canto_giovani_fornace_I_page"
            mCanto.titolo = "canto_giovani_fornace_I_title"
            mCanto.source = "canto_giovani_fornace_I_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_giovani_fornace_I_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 50
            mCanto.pagina = "canto_giovani_fornace_II_page"
            mCanto.titolo = "canto_giovani_fornace_II_title"
            mCanto.source = "canto_giovani_fornace_II_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_giovani_fornace_II_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 51
            mCanto.pagina = "lodate_il_signore_dai_cieli_page"
            mCanto.titolo = "lodate_il_signore_dai_cieli_title"
            mCanto.source = "lodate_il_signore_dai_cieli_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "lodate_il_signore_dai_cieli_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 52
            mCanto.pagina = "lodate_iddio_page"
            mCanto.titolo = "lodate_iddio_title"
            mCanto.source = "lodate_iddio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "lodate_iddio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 53
            mCanto.pagina = "signore_e_mia_luce_page"
            mCanto.titolo = "signore_e_mia_luce_title"
            mCanto.source = "signore_e_mia_luce_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "signore_e_mia_luce_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 54
            mCanto.pagina = "evenu_shalom_page"
            mCanto.titolo = "evenu_shalom_title"
            mCanto.source = "evenu_shalom_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "evenu_shalom_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 55
            mCanto.pagina = "gia_viene_il_regno_page"
            mCanto.titolo = "gia_viene_il_regno_title"
            mCanto.source = "gia_viene_il_regno_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "gia_viene_il_regno_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 56
            mCanto.pagina = "abba_padre_page"
            mCanto.titolo = "abba_padre_title"
            mCanto.source = "abba_padre_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "abba_padre_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 57
            mCanto.pagina = "chi_ci_separera_page"
            mCanto.titolo = "chi_ci_separera_title"
            mCanto.source = "chi_ci_separera_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "chi_ci_separera_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 58
            mCanto.pagina = "magnificat_page"
            mCanto.titolo = "magnificat_title"
            mCanto.source = "magnificat_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "magnificat_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 59
            mCanto.pagina = "innalzero_la_coppa_di_salvezza_page"
            mCanto.titolo = "innalzero_la_coppa_di_salvezza_title"
            mCanto.source = "innalzero_la_coppa_di_salvezza_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "innalzero_la_coppa_di_salvezza_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 60
            mCanto.pagina = "quando_il_signore_page"
            mCanto.titolo = "quando_il_signore_title"
            mCanto.source = "quando_il_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "quando_il_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 61
            mCanto.pagina = "cantico_di_zaccaria_page"
            mCanto.titolo = "cantico_di_zaccaria_title"
            mCanto.source = "cantico_di_zaccaria_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "cantico_di_zaccaria_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 62
            mCanto.pagina = "o_morte_dove_la_tua_vittoria_page"
            mCanto.titolo = "o_morte_dove_la_tua_vittoria_title"
            mCanto.source = "o_morte_dove_la_tua_vittoria_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "o_morte_dove_la_tua_vittoria_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 63
            mCanto.pagina = "o_cieli_piovete_dall_alto_page"
            mCanto.titolo = "o_cieli_piovete_dall_alto_title"
            mCanto.source = "o_cieli_piovete_dall_alto_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "o_cieli_piovete_dall_alto_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 64
            mCanto.pagina = "pentecoste_page"
            mCanto.titolo = "pentecoste_title"
            mCanto.source = "pentecoste_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "pentecoste_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 65
            mCanto.pagina = "ecco_qui_vengo_presto_page"
            mCanto.titolo = "ecco_qui_vengo_presto_title"
            mCanto.source = "ecco_qui_vengo_presto_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ecco_qui_vengo_presto_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 66
            mCanto.pagina = "vieni_figlio_dell_uomo_page"
            mCanto.titolo = "vieni_figlio_dell_uomo_title"
            mCanto.source = "vieni_figlio_dell_uomo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "vieni_figlio_dell_uomo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 67
            mCanto.pagina = "abramo_page"
            mCanto.titolo = "abramo_title"
            mCanto.source = "abramo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "abramo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 68
            mCanto.pagina = "cantico_di_mose_page"
            mCanto.titolo = "cantico_di_mose_title"
            mCanto.source = "cantico_di_mose_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "cantico_di_mose_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 69
            mCanto.pagina = "lodate_il_signore_page"
            mCanto.titolo = "lodate_il_signore_title"
            mCanto.source = "lodate_il_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "lodate_il_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 70
            mCanto.pagina = "quando_israele_usci_egitto_page"
            mCanto.titolo = "quando_israele_usci_egitto_title"
            mCanto.source = "quando_israele_usci_egitto_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "quando_israele_usci_egitto_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 71
            mCanto.pagina = "alzate_o_porte_page"
            mCanto.titolo = "alzate_o_porte_title"
            mCanto.source = "alzate_o_porte_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "alzate_o_porte_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 72
            mCanto.pagina = "signore_mio_pastore_page"
            mCanto.titolo = "signore_mio_pastore_title"
            mCanto.source = "signore_mio_pastore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "signore_mio_pastore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 73
            mCanto.pagina = "giunti_fiumi_babilonia_page"
            mCanto.titolo = "giunti_fiumi_babilonia_title"
            mCanto.source = "giunti_fiumi_babilonia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "giunti_fiumi_babilonia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 74
            mCanto.pagina = "pieta_di_me_o_dio_page"
            mCanto.titolo = "pieta_di_me_o_dio_title"
            mCanto.source = "pieta_di_me_o_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "pieta_di_me_o_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 75
            mCanto.pagina = "misericordia_dio_misericordia_page"
            mCanto.titolo = "misericordia_dio_misericordia_title"
            mCanto.source = "misericordia_dio_misericordia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "misericordia_dio_misericordia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 76
            mCanto.pagina = "inno_della_kenosis_page"
            mCanto.titolo = "inno_della_kenosis_title"
            mCanto.source = "inno_della_kenosis_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_della_kenosis_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 77
            mCanto.pagina = "ave_maria_page"
            mCanto.titolo = "ave_maria_title"
            mCanto.source = "ave_maria_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ave_maria_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 78
            mCanto.pagina = "ave_maria_1984_page"
            mCanto.titolo = "ave_maria_1984_title"
            mCanto.source = "ave_maria_1984_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ave_maria_1984_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 79
            mCanto.pagina = "maria_piccola_maria_page"
            mCanto.titolo = "maria_piccola_maria_title"
            mCanto.source = "maria_piccola_maria_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "maria_piccola_maria_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 80
            mCanto.pagina = "alzo_gli_occhi_page"
            mCanto.titolo = "alzo_gli_occhi_title"
            mCanto.source = "alzo_gli_occhi_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "alzo_gli_occhi_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 81
            mCanto.pagina = "canto_liberati_page"
            mCanto.titolo = "canto_liberati_title"
            mCanto.source = "canto_liberati_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_liberati_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 82
            mCanto.pagina = "se_oggi_ascoltate_sua_voce_page"
            mCanto.titolo = "se_oggi_ascoltate_sua_voce_title"
            mCanto.source = "se_oggi_ascoltate_sua_voce_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "se_oggi_ascoltate_sua_voce_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

//            mCanto = Canto()
//            mCanto.id = 83
//            mCanto.pagina = "venite_applaudiamo_al_signore_page"
//            mCanto.titolo = "venite_applaudiamo_al_signore_title"
//            mCanto.source = "venite_applaudiamo_al_signore_source"
//            mCanto.favorite = 0
//            mCanto.color = BIANCO
//            mCanto.link = "venite_applaudiamo_al_signore_link"
//            mCanto.zoom = 0
//            mCanto.scrollX = 0
//            mCanto.scrollY = 0
//            mCanto.savedSpeed = "2"
//            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 84
            mCanto.pagina = "dajenu_page"
            mCanto.titolo = "dajenu_title"
            mCanto.source = "dajenu_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "dajenu_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 85
            mCanto.pagina = "alla_vittima_pasquale_page"
            mCanto.titolo = "alla_vittima_pasquale_title"
            mCanto.source = "alla_vittima_pasquale_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "alla_vittima_pasquale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 86
            mCanto.pagina = "inno_di_pasqua_page"
            mCanto.titolo = "inno_di_pasqua_title"
            mCanto.source = "inno_di_pasqua_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_di_pasqua_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 87
            mCanto.pagina = "inno_avvento_page"
            mCanto.titolo = "inno_avvento_title"
            mCanto.source = "inno_avvento_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_avvento_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 88
            mCanto.pagina = "uri_uri_ura_page"
            mCanto.titolo = "uri_uri_ura_title"
            mCanto.source = "uri_uri_ura_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "uri_uri_ura_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 89
            mCanto.pagina = "gia_viene_il_mio_dio_page"
            mCanto.titolo = "gia_viene_il_mio_dio_title"
            mCanto.source = "gia_viene_il_mio_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "gia_viene_il_mio_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 90
            mCanto.pagina = "amen_amen_amen_page"
            mCanto.titolo = "amen_amen_amen_title"
            mCanto.source = "amen_amen_amen_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "amen_amen_amen_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 91
            mCanto.pagina = "se_signore_non_costruisce_page"
            mCanto.titolo = "se_signore_non_costruisce_title"
            mCanto.source = "se_signore_non_costruisce_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "se_signore_non_costruisce_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 92
            mCanto.pagina = "gustate_e_vedete_page"
            mCanto.titolo = "gustate_e_vedete_title"
            mCanto.source = "gustate_e_vedete_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "gustate_e_vedete_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 93
            mCanto.pagina = "per_amore_dei_miei_fratelli_page"
            mCanto.titolo = "per_amore_dei_miei_fratelli_title"
            mCanto.source = "per_amore_dei_miei_fratelli_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "per_amore_dei_miei_fratelli_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 94
            mCanto.pagina = "ho_sperato_nel_signore_page"
            mCanto.titolo = "ho_sperato_nel_signore_title"
            mCanto.source = "ho_sperato_nel_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ho_sperato_nel_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 95
            mCanto.pagina = "voglio_cantare_page"
            mCanto.titolo = "voglio_cantare_title"
            mCanto.source = "voglio_cantare_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "voglio_cantare_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 96
            mCanto.pagina = "perche_genti_congiurano_page"
            mCanto.titolo = "perche_genti_congiurano_title"
            mCanto.source = "perche_genti_congiurano_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "perche_genti_congiurano_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 97
            mCanto.pagina = "come_una_cerva_anela_page"
            mCanto.titolo = "come_una_cerva_anela_title"
            mCanto.source = "come_una_cerva_anela_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "come_una_cerva_anela_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 98
            mCanto.pagina = "acclamate_al_signore_page"
            mCanto.titolo = "acclamate_al_signore_title"
            mCanto.source = "acclamate_al_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "acclamate_al_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 99
            mCanto.pagina = "gridate_con_gioia_page"
            mCanto.titolo = "gridate_con_gioia_title"
            mCanto.source = "gridate_con_gioia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "gridate_con_gioia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 100
            mCanto.pagina = "al_risveglio_mi_saziero_page"
            mCanto.titolo = "al_risveglio_mi_saziero_title"
            mCanto.source = "al_risveglio_mi_saziero_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "al_risveglio_mi_saziero_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 101
            mCanto.pagina = "canto_bambini_veglia_page"
            mCanto.titolo = "canto_bambini_veglia_title"
            mCanto.source = "canto_bambini_veglia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_bambini_veglia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 102
            mCanto.pagina = "non_moriro_page"
            mCanto.titolo = "non_moriro_title"
            mCanto.source = "non_moriro_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "non_moriro_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 104
            mCanto.pagina = "dite_agli_smarriti_page"
            mCanto.titolo = "dite_agli_smarriti_title"
            mCanto.source = "dite_agli_smarriti_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "dite_agli_smarriti_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 105
            mCanto.pagina = "o_dio_tu_sei_il_mio_dio_page"
            mCanto.titolo = "o_dio_tu_sei_il_mio_dio_title"
            mCanto.source = "o_dio_tu_sei_il_mio_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "o_dio_tu_sei_il_mio_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 106
            mCanto.pagina = "sale_dio_tra_acclamazioni_page"
            mCanto.titolo = "sale_dio_tra_acclamazioni_title"
            mCanto.source = "sale_dio_tra_acclamazioni_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "sale_dio_tra_acclamazioni_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 107
            mCanto.pagina = "popolo_camminava_tenebre_page"
            mCanto.titolo = "popolo_camminava_tenebre_title"
            mCanto.source = "popolo_camminava_tenebre_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "popolo_camminava_tenebre_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

//            mCanto = Canto()
//            mCanto.id = 108
//            mCanto.pagina = "da_lode_al_signore_page"
//            mCanto.titolo = "da_lode_al_signore_title"
//            mCanto.source = "da_lode_al_signore_source"
//            mCanto.favorite = 0
//            mCanto.color = BIANCO
//            mCanto.link = "da_lode_al_signore_link"
//            mCanto.zoom = 0
//            mCanto.scrollX = 0
//            mCanto.scrollY = 0
//            mCanto.savedSpeed = "2"
//            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 109
            mCanto.pagina = "canto_di_balaam_page"
            mCanto.titolo = "canto_di_balaam_title"
            mCanto.source = "canto_di_balaam_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_di_balaam_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 110
            mCanto.pagina = "davanti_agli_angeli_page"
            mCanto.titolo = "davanti_agli_angeli_title"
            mCanto.source = "davanti_agli_angeli_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "davanti_agli_angeli_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

//            mCanto = Canto()
//            mCanto.id = 111
//            mCanto.pagina = "quando_israele_era_un_bimbo_page"
//            mCanto.titolo = "quando_israele_era_un_bimbo_title"
//            mCanto.source = "quando_israele_era_un_bimbo_source"
//            mCanto.favorite = 0
//            mCanto.color = BIANCO
//            mCanto.link = "quando_israele_era_un_bimbo_link"
//            mCanto.zoom = 0
//            mCanto.scrollX = 0
//            mCanto.scrollY = 0
//            mCanto.savedSpeed = "2"
//            cantiList.add(mCanto)

//            mCanto = Canto()
//            mCanto.id = 112
//            mCanto.pagina = "e_la_pasqua_del_signore_page"
//            mCanto.titolo = "e_la_pasqua_del_signore_title"
//            mCanto.source = "e_la_pasqua_del_signore_source"
//            mCanto.favorite = 0
//            mCanto.color = BIANCO
//            mCanto.link = "e_la_pasqua_del_signore_link"
//            mCanto.zoom = 0
//            mCanto.scrollX = 0
//            mCanto.scrollY = 0
//            mCanto.savedSpeed = "2"
//            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 113
            mCanto.pagina = "tempo_ogni_cosa_page"
            mCanto.titolo = "tempo_ogni_cosa_title"
            mCanto.source = "tempo_ogni_cosa_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "tempo_ogni_cosa_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 114
            mCanto.pagina = "benedici_anima_mia_page"
            mCanto.titolo = "benedici_anima_mia_title"
            mCanto.source = "benedici_anima_mia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "benedici_anima_mia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 115
            mCanto.pagina = "quanto_sono_amabili_dimore_page"
            mCanto.titolo = "quanto_sono_amabili_dimore_title"
            mCanto.source = "quanto_sono_amabili_dimore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "quanto_sono_amabili_dimore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 116
            mCanto.pagina = "viene_il_signore_vestito_di_maesta_page"
            mCanto.titolo = "viene_il_signore_vestito_di_maesta_title"
            mCanto.source = "viene_il_signore_vestito_di_maesta_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "viene_il_signore_vestito_di_maesta_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 117
            mCanto.pagina = "giorno_di_riposo_page"
            mCanto.titolo = "giorno_di_riposo_title"
            mCanto.source = "giorno_di_riposo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "giorno_di_riposo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 118
            mCanto.pagina = "consolate_il_mio_popolo_page"
            mCanto.titolo = "consolate_il_mio_popolo_title"
            mCanto.source = "consolate_il_mio_popolo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "consolate_il_mio_popolo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 119
            mCanto.pagina = "cerano_due_angeli_page"
            mCanto.titolo = "cerano_due_angeli_title"
            mCanto.source = "cerano_due_angeli_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "cerano_due_angeli_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 120
            mCanto.pagina = "amo_il_signore_page"
            mCanto.titolo = "amo_il_signore_title"
            mCanto.source = "amo_il_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "amo_il_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 121
            mCanto.pagina = "venite_a_me_voi_tutti_page"
            mCanto.titolo = "venite_a_me_voi_tutti_title"
            mCanto.source = "venite_a_me_voi_tutti_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "venite_a_me_voi_tutti_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 122
            mCanto.pagina = "a_te_si_deve_lode_in_sion_page"
            mCanto.titolo = "a_te_si_deve_lode_in_sion_title"
            mCanto.source = "a_te_si_deve_lode_in_sion_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "a_te_si_deve_lode_in_sion_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 123
            mCanto.pagina = "se_signore_sono_rifugiato_page"
            mCanto.titolo = "se_signore_sono_rifugiato_title"
            mCanto.source = "se_signore_sono_rifugiato_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "se_signore_sono_rifugiato_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 124
            mCanto.pagina = "signore_annuncia_una_notizia_page"
            mCanto.titolo = "signore_annuncia_una_notizia_title"
            mCanto.source = "signore_annuncia_una_notizia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "signore_annuncia_una_notizia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 125
            mCanto.pagina = "benedite_il_signore_page"
            mCanto.titolo = "benedite_il_signore_title"
            mCanto.source = "benedite_il_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "benedite_il_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 126
            mCanto.pagina = "figlie_di_gerusalemme_page"
            mCanto.titolo = "figlie_di_gerusalemme_title"
            mCanto.source = "figlie_di_gerusalemme_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "figlie_di_gerusalemme_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 127
            mCanto.pagina = "ti_ho_manifestato_mio_peccato_page"
            mCanto.titolo = "ti_ho_manifestato_mio_peccato_title"
            mCanto.source = "ti_ho_manifestato_mio_peccato_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ti_ho_manifestato_mio_peccato_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 128
            mCanto.pagina = "maria_madre_della_chiesa_page"
            mCanto.titolo = "maria_madre_della_chiesa_title"
            mCanto.source = "maria_madre_della_chiesa_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "maria_madre_della_chiesa_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 129
            mCanto.pagina = "stabat_mater_page"
            mCanto.titolo = "stabat_mater_title"
            mCanto.source = "stabat_mater_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "stabat_mater_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 130
            mCanto.pagina = "lamenti_del_signore_page"
            mCanto.titolo = "lamenti_del_signore_title"
            mCanto.source = "lamenti_del_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "lamenti_del_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 131
            mCanto.pagina = "o_signore_nostro_dio_page"
            mCanto.titolo = "o_signore_nostro_dio_title"
            mCanto.source = "o_signore_nostro_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "o_signore_nostro_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 132
            mCanto.pagina = "benedetta_sei_tu_maria_page"
            mCanto.titolo = "benedetta_sei_tu_maria_title"
            mCanto.source = "benedetta_sei_tu_maria_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "benedetta_sei_tu_maria_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 133
            mCanto.pagina = "salve_regina_dei_cieli_page"
            mCanto.titolo = "salve_regina_dei_cieli_title"
            mCanto.source = "salve_regina_dei_cieli_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "salve_regina_dei_cieli_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 134
            mCanto.pagina = "vergine_della_meraviglia_page"
            mCanto.titolo = "vergine_della_meraviglia_title"
            mCanto.source = "vergine_della_meraviglia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "vergine_della_meraviglia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 135
            mCanto.pagina = "maria_casa_di_benedizione_page"
            mCanto.titolo = "maria_casa_di_benedizione_title"
            mCanto.source = "maria_casa_di_benedizione_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "maria_casa_di_benedizione_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 136
            mCanto.pagina = "benediro_il_signore_in_ogni_tempo_page"
            mCanto.titolo = "benediro_il_signore_in_ogni_tempo_title"
            mCanto.source = "benediro_il_signore_in_ogni_tempo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "benediro_il_signore_in_ogni_tempo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 137
            mCanto.pagina = "mietitura_delle_nazioni_page"
            mCanto.titolo = "mietitura_delle_nazioni_title"
            mCanto.source = "mietitura_delle_nazioni_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "mietitura_delle_nazioni_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 138
            mCanto.pagina = "dice_il_signore_al_mio_signore_page"
            mCanto.titolo = "dice_il_signore_al_mio_signore_title"
            mCanto.source = "dice_il_signore_al_mio_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "dice_il_signore_al_mio_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 139
            mCanto.pagina = "felice_uomo_page"
            mCanto.titolo = "felice_uomo_title"
            mCanto.source = "felice_uomo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "felice_uomo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 141
            mCanto.pagina = "tu_sei_il_piu_bello_page"
            mCanto.titolo = "tu_sei_il_piu_bello_title"
            mCanto.source = "tu_sei_il_piu_bello_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "tu_sei_il_piu_bello_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 142
            mCanto.pagina = "felicita_per_l_uomo_page"
            mCanto.titolo = "felicita_per_l_uomo_title"
            mCanto.source = "felicita_per_l_uomo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "felicita_per_l_uomo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 143
            mCanto.pagina = "sorga_dio_page"
            mCanto.titolo = "sorga_dio_title"
            mCanto.source = "sorga_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "sorga_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 144
            mCanto.pagina = "andiamo_gia_pastori_page"
            mCanto.titolo = "andiamo_gia_pastori_title"
            mCanto.source = "andiamo_gia_pastori_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "andiamo_gia_pastori_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 145
            mCanto.pagina = "maria_di_jasna_gora_page"
            mCanto.titolo = "maria_di_jasna_gora_title"
            mCanto.source = "maria_di_jasna_gora_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "maria_di_jasna_gora_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 146
            mCanto.pagina = "a_te_signore_con_la_mia_voce_page"
            mCanto.titolo = "a_te_signore_con_la_mia_voce_title"
            mCanto.source = "a_te_signore_con_la_mia_voce_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "a_te_signore_con_la_mia_voce_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 147
            mCanto.pagina = "un_germoglio_spunta_tronco_page"
            mCanto.titolo = "un_germoglio_spunta_tronco_title"
            mCanto.source = "un_germoglio_spunta_tronco_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "un_germoglio_spunta_tronco_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 148
            mCanto.pagina = "a_te_signore_innalzo_la_mia_anima_page"
            mCanto.titolo = "a_te_signore_innalzo_la_mia_anima_title"
            mCanto.source = "a_te_signore_innalzo_la_mia_anima_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "a_te_signore_innalzo_la_mia_anima_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 149
            mCanto.pagina = "ti_sto_chiamando_page"
            mCanto.titolo = "ti_sto_chiamando_title"
            mCanto.source = "ti_sto_chiamando_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ti_sto_chiamando_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 150
            mCanto.pagina = "a_te_levo_i_miei_occhi_page"
            mCanto.titolo = "a_te_levo_i_miei_occhi_title"
            mCanto.source = "a_te_levo_i_miei_occhi_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "a_te_levo_i_miei_occhi_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 151
            mCanto.pagina = "signore_non_punirmi_nel_tuo_sdegno_page"
            mCanto.titolo = "signore_non_punirmi_nel_tuo_sdegno_title"
            mCanto.source = "signore_non_punirmi_nel_tuo_sdegno_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "signore_non_punirmi_nel_tuo_sdegno_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 152
            mCanto.pagina = "gloria_gloria_gloria_page"
            mCanto.titolo = "gloria_gloria_gloria_title"
            mCanto.source = "gloria_gloria_gloria_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "gloria_gloria_gloria_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 153
            mCanto.pagina = "esultate_giusti_nel_signore_page"
            mCanto.titolo = "esultate_giusti_nel_signore_title"
            mCanto.source = "esultate_giusti_nel_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "esultate_giusti_nel_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 154
            mCanto.pagina = "molto_mi_hanno_perseguitato_page"
            mCanto.titolo = "molto_mi_hanno_perseguitato_title"
            mCanto.source = "molto_mi_hanno_perseguitato_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "molto_mi_hanno_perseguitato_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 155
            mCanto.pagina = "ti_amo_signore_page"
            mCanto.titolo = "ti_amo_signore_title"
            mCanto.source = "ti_amo_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ti_amo_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 156
            mCanto.pagina = "maria_madre_cammino_ardente_page"
            mCanto.titolo = "maria_madre_cammino_ardente_title"
            mCanto.source = "maria_madre_cammino_ardente_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "maria_madre_cammino_ardente_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 157
            mCanto.pagina = "shlom_lech_mariam_page"
            mCanto.titolo = "shlom_lech_mariam_title"
            mCanto.source = "shlom_lech_mariam_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "shlom_lech_mariam_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 158
            mCanto.pagina = "andate_ed_annunziate_page"
            mCanto.titolo = "andate_ed_annunziate_title"
            mCanto.source = "andate_ed_annunziate_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "andate_ed_annunziate_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 159
            mCanto.pagina = "mi_indicherai_sentiero_vita_page"
            mCanto.titolo = "mi_indicherai_sentiero_vita_title"
            mCanto.source = "mi_indicherai_sentiero_vita_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "mi_indicherai_sentiero_vita_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 160
            mCanto.pagina = "o_dio_per_il_tuo_nome_page"
            mCanto.titolo = "o_dio_per_il_tuo_nome_title"
            mCanto.source = "o_dio_per_il_tuo_nome_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "o_dio_per_il_tuo_nome_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 161
            mCanto.pagina = "stolto_pensa_che_non_ce_dio_page"
            mCanto.titolo = "stolto_pensa_che_non_ce_dio_title"
            mCanto.source = "stolto_pensa_che_non_ce_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "stolto_pensa_che_non_ce_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 162
            mCanto.pagina = "signore_ascolta_mia_preghiera_page"
            mCanto.titolo = "signore_ascolta_mia_preghiera_title"
            mCanto.source = "signore_ascolta_mia_preghiera_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "signore_ascolta_mia_preghiera_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 163
            mCanto.pagina = "non_e_qui_e_risorto_page"
            mCanto.titolo = "non_e_qui_e_risorto_title"
            mCanto.source = "non_e_qui_e_risorto_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "non_e_qui_e_risorto_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 164
            mCanto.pagina = "vi_prendero_dalle_genti_page"
            mCanto.titolo = "vi_prendero_dalle_genti_title"
            mCanto.source = "vi_prendero_dalle_genti_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "vi_prendero_dalle_genti_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 165
            mCanto.pagina = "voi_siete_la_luce_del_mondo_page"
            mCanto.titolo = "voi_siete_la_luce_del_mondo_title"
            mCanto.source = "voi_siete_la_luce_del_mondo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "voi_siete_la_luce_del_mondo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 166
            mCanto.pagina = "sola_a_solo_page"
            mCanto.titolo = "sola_a_solo_title"
            mCanto.source = "sola_a_solo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "sola_a_solo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 167
            mCanto.pagina = "in_mezzo_grande_folla_page"
            mCanto.titolo = "in_mezzo_grande_folla_title"
            mCanto.source = "in_mezzo_grande_folla_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "in_mezzo_grande_folla_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 168
            mCanto.pagina = "zaccheo_page"
            mCanto.titolo = "zaccheo_title"
            mCanto.source = "zaccheo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "zaccheo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 169
            mCanto.pagina = "siedi_solitario_silenzioso_page"
            mCanto.titolo = "siedi_solitario_silenzioso_title"
            mCanto.source = "siedi_solitario_silenzioso_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "siedi_solitario_silenzioso_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 170
            mCanto.pagina = "cosi_parla_amen_page"
            mCanto.titolo = "cosi_parla_amen_title"
            mCanto.source = "cosi_parla_amen_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "cosi_parla_amen_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 171
            mCanto.pagina = "ti_vedranno_i_re_page"
            mCanto.titolo = "ti_vedranno_i_re_title"
            mCanto.source = "ti_vedranno_i_re_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ti_vedranno_i_re_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 172
            mCanto.pagina = "giacobbe_page"
            mCanto.titolo = "giacobbe_title"
            mCanto.source = "giacobbe_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "giacobbe_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 173
            mCanto.pagina = "debora_page"
            mCanto.titolo = "debora_title"
            mCanto.source = "debora_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "debora_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 174
            mCanto.pagina = "vedo_cieli_aperti_page"
            mCanto.titolo = "vedo_cieli_aperti_title"
            mCanto.source = "vedo_cieli_aperti_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "vedo_cieli_aperti_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 175
            mCanto.pagina = "il_signore_mi_ha_dato_page"
            mCanto.titolo = "il_signore_mi_ha_dato_title"
            mCanto.source = "il_signore_mi_ha_dato_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "il_signore_mi_ha_dato_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 176
            mCanto.pagina = "pigiatore_page"
            mCanto.titolo = "pigiatore_title"
            mCanto.source = "pigiatore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "pigiatore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 177
            mCanto.pagina = "seminatore_page"
            mCanto.titolo = "seminatore_title"
            mCanto.source = "seminatore_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "seminatore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 178
            mCanto.pagina = "spirito_del_signore_sopra_di_me_page"
            mCanto.titolo = "spirito_del_signore_sopra_di_me_title"
            mCanto.source = "spirito_del_signore_sopra_di_me_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "spirito_del_signore_sopra_di_me_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 179
            mCanto.pagina = "ecco_lo_specchio_nostro_page"
            mCanto.titolo = "ecco_lo_specchio_nostro_title"
            mCanto.source = "ecco_lo_specchio_nostro_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "ecco_lo_specchio_nostro_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 180
            mCanto.pagina = "come_slancio_ira_page"
            mCanto.titolo = "come_slancio_ira_title"
            mCanto.source = "come_slancio_ira_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "come_slancio_ira_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 181
            mCanto.pagina = "benedetto_sia_iddio_page"
            mCanto.titolo = "benedetto_sia_iddio_title"
            mCanto.source = "benedetto_sia_iddio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "benedetto_sia_iddio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 182
            mCanto.pagina = "signore_tu_scruti_conosci_page"
            mCanto.titolo = "signore_tu_scruti_conosci_title"
            mCanto.source = "signore_tu_scruti_conosci_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "signore_tu_scruti_conosci_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 183
            mCanto.pagina = "eli_eli_lamma_sabactani_page"
            mCanto.titolo = "eli_eli_lamma_sabactani_title"
            mCanto.source = "eli_eli_lamma_sabactani_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "eli_eli_lamma_sabactani_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 185
            mCanto.pagina = "nessuno_puo_servire_due_padroni_page"
            mCanto.titolo = "nessuno_puo_servire_due_padroni_title"
            mCanto.source = "nessuno_puo_servire_due_padroni_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "nessuno_puo_servire_due_padroni_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 186
            mCanto.pagina = "signore_mio_cuore_pretese_page"
            mCanto.titolo = "signore_mio_cuore_pretese_title"
            mCanto.source = "signore_mio_cuore_pretese_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "signore_mio_cuore_pretese_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 187
            mCanto.pagina = "voglio_andare_a_gerusalemme_page"
            mCanto.titolo = "voglio_andare_a_gerusalemme_title"
            mCanto.source = "voglio_andare_a_gerusalemme_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "voglio_andare_a_gerusalemme_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 188
            mCanto.pagina = "shema_israel_page"
            mCanto.titolo = "shema_israel_title"
            mCanto.source = "shema_israel_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "shema_israel_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 189
            mCanto.pagina = "inno_croce_gloriosa_page"
            mCanto.titolo = "inno_croce_gloriosa_title"
            mCanto.source = "inno_croce_gloriosa_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "inno_croce_gloriosa_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 190
            mCanto.pagina = "rivestitevi_dell_armatura_page"
            mCanto.titolo = "rivestitevi_dell_armatura_title"
            mCanto.source = "rivestitevi_dell_armatura_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "rivestitevi_dell_armatura_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 191
            mCanto.pagina = "sue_fondamenta_page"
            mCanto.titolo = "sue_fondamenta_title"
            mCanto.source = "sue_fondamenta_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "sue_fondamenta_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 192
            mCanto.pagina = "akeda_page"
            mCanto.titolo = "akeda_title"
            mCanto.source = "akeda_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "akeda_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 193
            mCanto.pagina = "non_ti_adirare_page"
            mCanto.titolo = "non_ti_adirare_title"
            mCanto.source = "non_ti_adirare_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "non_ti_adirare_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 194
            mCanto.pagina = "inno_alla_carita_page"
            mCanto.titolo = "inno_alla_carita_title"
            mCanto.source = "inno_alla_carita_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_alla_carita_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 195
            mCanto.pagina = "stesso_iddio_page"
            mCanto.titolo = "stesso_iddio_title"
            mCanto.source = "stesso_iddio_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "stesso_iddio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 196
            mCanto.pagina = "come_condannati_a_morte_page"
            mCanto.titolo = "come_condannati_a_morte_title"
            mCanto.source = "come_condannati_a_morte_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "come_condannati_a_morte_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 197
            mCanto.pagina = "gesu_percorreva_page"
            mCanto.titolo = "gesu_percorreva_title"
            mCanto.source = "gesu_percorreva_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "gesu_percorreva_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 198
            mCanto.pagina = "non_resistete_al_male_page"
            mCanto.titolo = "non_resistete_al_male_title"
            mCanto.source = "non_resistete_al_male_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "non_resistete_al_male_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 199
            mCanto.pagina = "che_mi_baci_page"
            mCanto.titolo = "che_mi_baci_title"
            mCanto.source = "che_mi_baci_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "che_mi_baci_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 200
            mCanto.pagina = "mia_diletta_e_per_me_page"
            mCanto.titolo = "mia_diletta_e_per_me_title"
            mCanto.source = "mia_diletta_e_per_me_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "mia_diletta_e_per_me_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 201
            mCanto.pagina = "vieni_dal_libano_page"
            mCanto.titolo = "vieni_dal_libano_title"
            mCanto.source = "vieni_dal_libano_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "vieni_dal_libano_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 202
            mCanto.pagina = "quando_dormivo_page"
            mCanto.titolo = "quando_dormivo_title"
            mCanto.source = "quando_dormivo_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "quando_dormivo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 203
            mCanto.pagina = "tu_che_abiti_nei_giardini_page"
            mCanto.titolo = "tu_che_abiti_nei_giardini_title"
            mCanto.source = "tu_che_abiti_nei_giardini_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "tu_che_abiti_nei_giardini_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 204
            mCanto.pagina = "agnella_di_dio_page"
            mCanto.titolo = "agnella_di_dio_title"
            mCanto.source = "agnella_di_dio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "agnella_di_dio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 205
            mCanto.pagina = "non_ce_in_lui_bellezza_page"
            mCanto.titolo = "non_ce_in_lui_bellezza_title"
            mCanto.source = "non_ce_in_lui_bellezza_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "non_ce_in_lui_bellezza_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 206
            mCanto.pagina = "canto_dell_agnello_page"
            mCanto.titolo = "canto_dell_agnello_title"
            mCanto.source = "canto_dell_agnello_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "canto_dell_agnello_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 207
            mCanto.pagina = "chi_e_colei_page"
            mCanto.titolo = "chi_e_colei_title"
            mCanto.source = "chi_e_colei_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "chi_e_colei_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 208
            mCanto.pagina = "voce_del_mio_amato_page"
            mCanto.titolo = "voce_del_mio_amato_title"
            mCanto.source = "voce_del_mio_amato_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "voce_del_mio_amato_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 209
            mCanto.pagina = "colomba_volo_page"
            mCanto.titolo = "colomba_volo_title"
            mCanto.source = "colomba_volo_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "colomba_volo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 210
            mCanto.pagina = "come_stilla_il_miele_page"
            mCanto.titolo = "come_stilla_il_miele_title"
            mCanto.source = "come_stilla_il_miele_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "come_stilla_il_miele_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 211
            mCanto.pagina = "o_gesu_amore_mio_page"
            mCanto.titolo = "o_gesu_amore_mio_title"
            mCanto.source = "o_gesu_amore_mio_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "o_gesu_amore_mio_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 212
            mCanto.pagina = "portami_in_cielo_page"
            mCanto.titolo = "portami_in_cielo_title"
            mCanto.source = "portami_in_cielo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "portami_in_cielo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 213
            mCanto.pagina = "tu_sei_mia_speranza_signore_page"
            mCanto.titolo = "tu_sei_mia_speranza_signore_title"
            mCanto.source = "tu_sei_mia_speranza_signore_source"
            mCanto.favorite = 0
            mCanto.color = AZZURRO
            mCanto.link = "tu_sei_mia_speranza_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 214
            mCanto.pagina = "una_donna_vestita_di_sole_page"
            mCanto.titolo = "una_donna_vestita_di_sole_title"
            mCanto.source = "una_donna_vestita_di_sole_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "una_donna_vestita_di_sole_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 215
            mCanto.pagina = "ho_steso_le_mani_page"
            mCanto.titolo = "ho_steso_le_mani_title"
            mCanto.source = "ho_steso_le_mani_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ho_steso_le_mani_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 216
            mCanto.pagina = "omelia_pasquale_melitone_sardi_page"
            mCanto.titolo = "omelia_pasquale_melitone_sardi_title"
            mCanto.source = "omelia_pasquale_melitone_sardi_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "omelia_pasquale_melitone_sardi_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 217
            mCanto.pagina = "carmen_63_page"
            mCanto.titolo = "carmen_63_title"
            mCanto.source = "carmen_63_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "carmen_63_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 218
            mCanto.pagina = "caritas_christi_page"
            mCanto.titolo = "caritas_christi_title"
            mCanto.source = "caritas_christi_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "caritas_christi_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 219
            mCanto.pagina = "noli_me_tangere_page"
            mCanto.titolo = "noli_me_tangere_title"
            mCanto.source = "noli_me_tangere_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "noli_me_tangere_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 220
            mCanto.pagina = "signore_aiutami_signore_page"
            mCanto.titolo = "signore_aiutami_signore_title"
            mCanto.source = "signore_aiutami_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "signore_aiutami_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 221
            mCanto.pagina = "mi_hai_sedotto_signore_page"
            mCanto.titolo = "mi_hai_sedotto_signore_title"
            mCanto.source = "mi_hai_sedotto_signore_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "mi_hai_sedotto_signore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 222
            mCanto.pagina = "amate_i_vostri_nemici_page"
            mCanto.titolo = "amate_i_vostri_nemici_title"
            mCanto.source = "amate_i_vostri_nemici_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "amate_i_vostri_nemici_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 223
            mCanto.pagina = "tu_sei_bella_amica_mia_page"
            mCanto.titolo = "tu_sei_bella_amica_mia_title"
            mCanto.source = "tu_sei_bella_amica_mia_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "tu_sei_bella_amica_mia_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 224
            mCanto.pagina = "fratelli_non_diamo_a_nessuno_page"
            mCanto.titolo = "fratelli_non_diamo_a_nessuno_title"
            mCanto.source = "fratelli_non_diamo_a_nessuno_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "fratelli_non_diamo_a_nessuno_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 225
            mCanto.pagina = "questo_e_io_mio_comandamento_page"
            mCanto.titolo = "questo_e_io_mio_comandamento_title"
            mCanto.source = "questo_e_io_mio_comandamento_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "questo_e_io_mio_comandamento_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 226
            mCanto.pagina = "mi_rubasti_cuore_page"
            mCanto.titolo = "mi_rubasti_cuore_title"
            mCanto.source = "mi_rubasti_cuore_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "mi_rubasti_cuore_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 227
            mCanto.pagina = "in_una_notte_oscura_page"
            mCanto.titolo = "in_una_notte_oscura_title"
            mCanto.source = "in_una_notte_oscura_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "in_una_notte_oscura_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 228
            mCanto.pagina = "se_siete_risorti_page"
            mCanto.titolo = "se_siete_risorti_title"
            mCanto.source = "se_siete_risorti_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "se_siete_risorti_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 229
            mCanto.pagina = "una_gran_senal_page"
            mCanto.titolo = "una_gran_senal_title"
            mCanto.source = "una_gran_senal_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "una_gran_senal_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 230
            mCanto.pagina = "resurrexit_page"
            mCanto.titolo = "resurrexit_title"
            mCanto.source = "resurrexit_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "resurrexit_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 231
            mCanto.pagina = "ecco_il_mio_servo_page"
            mCanto.titolo = "ecco_il_mio_servo_title"
            mCanto.source = "ecco_il_mio_servo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ecco_il_mio_servo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 232
            mCanto.pagina = "ave_maria_colomba_page"
            mCanto.titolo = "ave_maria_colomba_title"
            mCanto.source = "ave_maria_colomba_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "ave_maria_colomba_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 233
            mCanto.pagina = "messia_leone_vincere_page"
            mCanto.titolo = "messia_leone_vincere_title"
            mCanto.source = "messia_leone_vincere_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "messia_leone_vincere_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 234
            mCanto.pagina = "come_pecora_che_vede_page"
            mCanto.titolo = "come_pecora_che_vede_title"
            mCanto.source = "come_pecora_che_vede_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "come_pecora_che_vede_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 235
            mCanto.pagina = "e_paziente_page"
            mCanto.titolo = "e_paziente_title"
            mCanto.source = "e_paziente_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "e_paziente_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 236
            mCanto.pagina = "inno_cristo_luce_page"
            mCanto.titolo = "inno_cristo_luce_title"
            mCanto.source = "inno_cristo_luce_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "inno_cristo_luce_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 237
            mCanto.pagina = "preghiera_eucaristica_IV_page"
            mCanto.titolo = "preghiera_eucaristica_IV_title"
            mCanto.source = "preghiera_eucaristica_IV_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiera_eucaristica_IV_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 238
            mCanto.pagina = "preghiere_universali_cantate_page"
            mCanto.titolo = "preghiere_universali_cantate_title"
            mCanto.source = "preghiere_universali_cantate_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "preghiere_universali_cantate_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 239
            mCanto.pagina = "salga_sposo_sul_legno_page"
            mCanto.titolo = "salga_sposo_sul_legno_title"
            mCanto.source = "salga_sposo_sul_legno_source"
            mCanto.favorite = 0
            mCanto.color = VERDE
            mCanto.link = "salga_sposo_sul_legno_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 240
            mCanto.pagina = "salmodia_salmo_responsoriale_page"
            mCanto.titolo = "salmodia_salmo_responsoriale_title"
            mCanto.source = "salmodia_salmo_responsoriale_source"
            mCanto.favorite = 0
            mCanto.color = GIALLO
            mCanto.link = "salmodia_salmo_responsoriale_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 241
            mCanto.pagina = "salve_regina_page"
            mCanto.titolo = "salve_regina_title"
            mCanto.source = "salve_regina_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "salve_regina_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 242
            mCanto.pagina = "sequenza_corpus_domini_page"
            mCanto.titolo = "sequenza_corpus_domini_title"
            mCanto.source = "sequenza_corpus_domini_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "sequenza_corpus_domini_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            mCanto = Canto()
            mCanto.id = 243
            mCanto.pagina = "un_angelo_venne_dal_cielo_page"
            mCanto.titolo = "un_angelo_venne_dal_cielo_title"
            mCanto.source = "un_angelo_venne_dal_cielo_source"
            mCanto.favorite = 0
            mCanto.color = BIANCO
            mCanto.link = "un_angelo_venne_dal_cielo_link"
            mCanto.zoom = 0
            mCanto.scrollX = 0
            mCanto.scrollY = 0
            mCanto.savedSpeed = "2"
            cantiList.add(mCanto)

            return cantiList
        }

    }

}
