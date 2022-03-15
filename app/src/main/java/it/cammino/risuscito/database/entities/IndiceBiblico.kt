package it.cammino.risuscito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class IndiceBiblico {

    @PrimaryKey
    var ordinamento: Int = 0

    var idCanto: Int = 0

    var titoloIndice: String? = null

    companion object {
        fun defaultIndiceBiblicoData(): ArrayList<IndiceBiblico> {
            val indiceBiblico = ArrayList<IndiceBiblico>()

            var ordinamento = 1

            var nBiblico = IndiceBiblico()
            nBiblico.idCanto = 67
            nBiblico.ordinamento = ordinamento
            nBiblico.titoloIndice = "abramo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 192
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "akeda_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 172
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "giacobbe_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 68
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "cantico_di_mose_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 109
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "canto_di_balaam_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 188
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "shema_israel_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 39
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "canto_di_giosue_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 173
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "debora_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 114
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "benedici_anima_mia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 139
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "felice_uomo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 96
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "perche_genti_congiurano_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 151
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_non_punirmi_nel_tuo_sdegno_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 131
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "o_signore_nostro_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 123
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "se_signore_sono_rifugiato_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 41
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "fino_a_quando_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 161
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "stolto_pensa_che_non_ce_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 159
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "mi_indicherai_sentiero_vita_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 100
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "al_risveglio_mi_saziero_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 155
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ti_amo_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 72
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_mio_pastore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 183
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "eli_eli_lamma_sabactani_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 71
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "alzate_o_porte_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 148
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "a_te_signore_innalzo_la_mia_anima_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 53
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_e_mia_luce_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 127
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ti_ho_manifestato_mio_peccato_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 153
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "esultate_giusti_nel_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 136
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "benediro_il_signore_in_ogni_tempo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 93
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "gustate_e_vedete_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 193
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "non_ti_adirare_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 94
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ho_sperato_nel_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 97
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "come_una_cerva_anela_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 141
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "tu_sei_il_piu_bello_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 106
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "sale_dio_tra_acclamazioni_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 75
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "misericordia_dio_misericordia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 74
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "pieta_di_me_o_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 160
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "o_dio_per_il_tuo_nome_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 95
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "voglio_cantare_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 105
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "o_dio_tu_sei_il_mio_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 122
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "a_te_si_deve_lode_in_sion_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 124
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_annuncia_una_notizia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 143
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "sorga_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 115
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "quanto_sono_amabili_dimore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 191
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "sue_fondamenta_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 116
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "viene_il_signore_vestito_di_maesta_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 82
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "se_oggi_ascoltate_sua_voce_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 98
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "acclamate_al_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 40
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "benedici_anima_mia_jahve_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 138
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "dice_il_signore_al_mio_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 70
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "quando_israele_usci_egitto_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 120
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "amo_il_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 59
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "innalzero_la_coppa_di_salvezza_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 69
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "lodate_il_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 102
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "non_moriro_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 44
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "giunga_la_mia_preghiera_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 80
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "alzo_gli_occhi_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 93
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "per_amore_dei_miei_fratelli_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 150
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "a_te_levo_i_miei_occhi_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 60
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "quando_il_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 91
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "se_signore_non_costruisce_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 142
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "felicita_per_l_uomo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 154
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "molto_mi_hanno_perseguitato_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 38
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "dal_profondo_a_te_grido_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 186
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_mio_cuore_pretese_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 46
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "come_bello_come_da_gioia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 45
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "guardate_come_e_bello_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 47
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "guardate_come_e_bello_gustate_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 125
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "benedite_il_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 48
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "grazie_a_jahve_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 73
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "giunti_fiumi_babilonia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 110
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "davanti_agli_angeli_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 182
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_tu_scruti_conosci_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 149
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ti_sto_chiamando_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 146
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "a_te_signore_con_la_mia_voce_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 162
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "signore_ascolta_mia_preghiera_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 51
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "lodate_il_signore_dai_cieli_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 52
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "lodate_iddio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 113
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "tempo_ogni_cosa_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 199
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "che_mi_baci_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 200
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "mia_diletta_e_per_me_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 208
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "voce_del_mio_amato_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 201
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "vieni_dal_libano_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 266
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "mi_rubasti_cuore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 202
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "quando_dormivo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 223
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "tu_sei_bella_amica_mia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 207
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "chi_e_colei_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 203
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "tu_che_abiti_nei_giardini_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 49
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "canto_giovani_fornace_I_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 50
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "canto_giovani_fornace_II_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 164
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "vi_prendero_dalle_genti_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 107
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "popolo_camminava_tenebre_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 147
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "un_germoglio_spunta_tronco_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 99
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "gridate_con_gioia_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 81
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "canto_liberati_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 42
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "jahve_tu_sei_mio_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 104
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "dite_agli_smarriti_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 118
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "consolate_il_mio_popolo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 231
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ecco_il_mio_servo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 63
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "o_cieli_piovete_dall_alto_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 171
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ti_vedranno_i_re_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 175
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "il_signore_mi_ha_dato_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 205
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "non_ce_in_lui_bellezza_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 178
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "spirito_del_signore_sopra_di_me_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 176
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "pigiatore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 152
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "gloria_gloria_gloria_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 221
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "mi_hai_sedotto_signore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 169
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "siedi_solitario_silenzioso_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 204
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "agnella_di_dio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 165
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "voi_siete_la_luce_del_mondo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 198
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "non_resistete_al_male_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 33
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "padre_nostro_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 185
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "nessuno_puo_servire_due_padroni_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 197
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "gesu_percorreva_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 121
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "venite_a_me_voi_tutti_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 163
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "non_e_qui_e_risorto_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 158
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "andate_ed_annunziate_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 177
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "seminatore_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 77
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ave_maria_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 78
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ave_maria_1984_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 132
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "benedetta_sei_tu_maria_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 58
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "magnificat_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 61
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "cantico_di_zaccaria_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 178
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "spirito_del_signore_sopra_di_me_II_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 222
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "amate_i_vostri_nemici_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 222
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "amate_i_vostri_nemici_II_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 167
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "in_mezzo_grande_folla_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 168
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "zaccheo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 126
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "figlie_di_gerusalemme_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 137
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "mietitura_delle_nazioni_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 117
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "giorno_di_riposo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 230
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "resurrexit_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 225
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "questo_e_io_mio_comandamento_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 128
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "maria_madre_della_chiesa_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 219
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "noli_me_tangere_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 56
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "abba_padre_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 57
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "chi_ci_separera_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 196
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "come_condannati_a_morte_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 194
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "inno_alla_carita_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 62
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "o_morte_dove_la_tua_vittoria_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 35
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "risuscito_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 195
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "stesso_iddio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 218
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "caritas_christi_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 224
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "fratelli_non_diamo_a_nessuno_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 181
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "benedetto_sia_iddio_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 190
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "rivestitevi_dell_armatura_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 76
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "inno_della_kenosis_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 228
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "se_siete_risorti_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 116
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "viene_il_signore_vestito_di_maesta_II_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 170
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "cosi_parla_amen_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 206
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "canto_dell_agnello_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 90
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "amen_amen_amen_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 229
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "una_gran_senal_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 214
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "una_donna_vestita_di_sole_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 55
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "gia_viene_il_regno_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 174
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "vedo_cieli_aperti_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 66
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "vieni_figlio_dell_uomo_biblico"
            indiceBiblico.add(nBiblico)

            nBiblico = IndiceBiblico()
            nBiblico.idCanto = 65
            nBiblico.ordinamento = ++ordinamento
            nBiblico.titoloIndice = "ecco_qui_vengo_presto_biblico"
            indiceBiblico.add(nBiblico)

            return indiceBiblico
        }
    }

}
