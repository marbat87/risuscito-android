package it.cammino.risuscito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Salmo {

    @PrimaryKey
    var id: Int = 0

    var numSalmo: String? = null

    var titoloSalmo: String? = null

    companion object {
        fun defaultSalmiData(): ArrayList<Salmo> {
            val salmiList = ArrayList<Salmo>()

            var nSalmo = Salmo()
            nSalmo.id = 38
            nSalmo.numSalmo = "129"
            nSalmo.titoloSalmo = "salmo_129"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 40
            nSalmo.numSalmo = "102"
            nSalmo.titoloSalmo = "salmo_102"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 41
            nSalmo.numSalmo = "012"
            nSalmo.titoloSalmo = "salmo_012"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 44
            nSalmo.numSalmo = "118"
            nSalmo.titoloSalmo = "salmo_118"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 45
            nSalmo.numSalmo = "132"
            nSalmo.titoloSalmo = "salmo_132_guardate"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 46
            nSalmo.numSalmo = "132"
            nSalmo.titoloSalmo = "salmo_132_come_bello"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 47
            nSalmo.numSalmo = "132"
            nSalmo.titoloSalmo = "salmo_132_guardate_gustate"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 48
            nSalmo.numSalmo = "135"
            nSalmo.titoloSalmo = "salmo_135"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 51
            nSalmo.numSalmo = "148"
            nSalmo.titoloSalmo = "salmo_148"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 52
            nSalmo.numSalmo = "150"
            nSalmo.titoloSalmo = "salmo_150"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 53
            nSalmo.numSalmo = "026"
            nSalmo.titoloSalmo = "salmo_026"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 59
            nSalmo.numSalmo = "114-115"
            nSalmo.titoloSalmo = "salmo_114_115"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 60
            nSalmo.numSalmo = "125"
            nSalmo.titoloSalmo = "salmo_125"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 69
            nSalmo.numSalmo = "116"
            nSalmo.titoloSalmo = "salmo_116"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 70
            nSalmo.numSalmo = "113A"
            nSalmo.titoloSalmo = "salmo_113A"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 71
            nSalmo.numSalmo = "023"
            nSalmo.titoloSalmo = "salmo_023"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 72
            nSalmo.numSalmo = "022"
            nSalmo.titoloSalmo = "salmo_022"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 73
            nSalmo.numSalmo = "136"
            nSalmo.titoloSalmo = "salmo_136"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 74
            nSalmo.numSalmo = "050"
            nSalmo.titoloSalmo = "salmo_050_pieta"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 75
            nSalmo.numSalmo = "050"
            nSalmo.titoloSalmo = "salmo_050_misericordia"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 80
            nSalmo.numSalmo = "120"
            nSalmo.titoloSalmo = "salmo_120"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 82
            nSalmo.numSalmo = "094"
            nSalmo.titoloSalmo = "salmo_094_se_oggi"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 83
            nSalmo.numSalmo = "094"
            nSalmo.titoloSalmo = "salmo_094_venite"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 91
            nSalmo.numSalmo = "126"
            nSalmo.titoloSalmo = "salmo_126"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 92
            nSalmo.numSalmo = "033"
            nSalmo.titoloSalmo = "salmo_033_gustate"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 93
            nSalmo.numSalmo = "121"
            nSalmo.titoloSalmo = "salmo_121"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 94
            nSalmo.numSalmo = "039"
            nSalmo.titoloSalmo = "salmo_039"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 95
            nSalmo.numSalmo = "056"
            nSalmo.titoloSalmo = "salmo_056"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 96
            nSalmo.numSalmo = "002"
            nSalmo.titoloSalmo = "salmo_002"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 97
            nSalmo.numSalmo = "041-042"
            nSalmo.titoloSalmo = "salmo_041_042"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 98
            nSalmo.numSalmo = "099"
            nSalmo.titoloSalmo = "salmo_099"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 100
            nSalmo.numSalmo = "016"
            nSalmo.titoloSalmo = "salmo_016"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 102
            nSalmo.numSalmo = "117"
            nSalmo.titoloSalmo = "salmo_117"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 105
            nSalmo.numSalmo = "062"
            nSalmo.titoloSalmo = "salmo_062"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 106
            nSalmo.numSalmo = "046"
            nSalmo.titoloSalmo = "salmo_046"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 108
            nSalmo.numSalmo = "145"
            nSalmo.titoloSalmo = "salmo_145"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 110
            nSalmo.numSalmo = "137"
            nSalmo.titoloSalmo = "salmo_137"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 115
            nSalmo.numSalmo = "083"
            nSalmo.titoloSalmo = "salmo_083"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 120
            nSalmo.numSalmo = "114"
            nSalmo.titoloSalmo = "salmo_114"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 122
            nSalmo.numSalmo = "064"
            nSalmo.titoloSalmo = "salmo_064"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 123
            nSalmo.numSalmo = "010"
            nSalmo.titoloSalmo = "salmo_010"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 124
            nSalmo.numSalmo = "067-v12"
            nSalmo.titoloSalmo = "salmo_067_v12"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 125
            nSalmo.numSalmo = "133"
            nSalmo.titoloSalmo = "salmo_133"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 127
            nSalmo.numSalmo = "031"
            nSalmo.titoloSalmo = "salmo_031"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 131
            nSalmo.numSalmo = "008"
            nSalmo.titoloSalmo = "salmo_008"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 136
            nSalmo.numSalmo = "033"
            nSalmo.titoloSalmo = "salmo_033_benediro"

            nSalmo = Salmo()
            nSalmo.id = 138
            nSalmo.numSalmo = "109"
            nSalmo.titoloSalmo = "salmo_109"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 139
            nSalmo.numSalmo = "001"
            nSalmo.titoloSalmo = "salmo_001"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 141
            nSalmo.numSalmo = "044"
            nSalmo.titoloSalmo = "salmo_044"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 142
            nSalmo.numSalmo = "127"
            nSalmo.titoloSalmo = "salmo_127"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 143
            nSalmo.numSalmo = "067-v02"
            nSalmo.titoloSalmo = "salmo_067_v02"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 146
            nSalmo.numSalmo = "141"
            nSalmo.titoloSalmo = "salmo_141"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 148
            nSalmo.numSalmo = "024"
            nSalmo.titoloSalmo = "salmo_024"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 149
            nSalmo.numSalmo = "140"
            nSalmo.titoloSalmo = "salmo_140"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 150
            nSalmo.numSalmo = "122"
            nSalmo.titoloSalmo = "salmo_122"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 151
            nSalmo.numSalmo = "006"
            nSalmo.titoloSalmo = "salmo_006"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 153
            nSalmo.numSalmo = "032"
            nSalmo.titoloSalmo = "salmo_032"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 154
            nSalmo.numSalmo = "128"
            nSalmo.titoloSalmo = "salmo_128"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 155
            nSalmo.numSalmo = "017"
            nSalmo.titoloSalmo = "salmo_017"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 159
            nSalmo.numSalmo = "015"
            nSalmo.titoloSalmo = "salmo_015"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 160
            nSalmo.numSalmo = "053"
            nSalmo.titoloSalmo = "salmo_053"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 161
            nSalmo.numSalmo = "013"
            nSalmo.titoloSalmo = "salmo_013"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 162
            nSalmo.numSalmo = "142"
            nSalmo.titoloSalmo = "salmo_142"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 182
            nSalmo.numSalmo = "138"
            nSalmo.titoloSalmo = "salmo_138"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 183
            nSalmo.numSalmo = "021"
            nSalmo.titoloSalmo = "salmo_021"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 186
            nSalmo.numSalmo = "130"
            nSalmo.titoloSalmo = "salmo_130"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 191
            nSalmo.numSalmo = "086"
            nSalmo.titoloSalmo = "salmo_086"
            salmiList.add(nSalmo)

            nSalmo = Salmo()
            nSalmo.id = 193
            nSalmo.numSalmo = "036"
            nSalmo.titoloSalmo = "salmo_036"
            salmiList.add(nSalmo)

            return salmiList
        }
    }

}
