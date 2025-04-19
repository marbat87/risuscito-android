package it.cammino.risuscito.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["idIndice", "idCanto"])
class IndiceLiturgico {

    var idIndice: Int = 0

    var idCanto: Int = 0

    @ColumnInfo(defaultValue = "0")
    var idGruppo: Int = 0

    companion object {
        fun defaultData(): ArrayList<IndiceLiturgico> {
            val mList = ArrayList<IndiceLiturgico>()

            var mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 56
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 67
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 98
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 71
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 80
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 90
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 132
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 118
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 104
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 235
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 65
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 153
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 44
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 124
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 152
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 155
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 178
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 69
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 159
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 131
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 60
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 115
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 91
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 171
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 162
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 174
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 36
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 164
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 201
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 1
            mDato.idCanto = 168
            mDato.idGruppo = 1
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 125
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 119
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 109
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 46
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 118
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 84
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 54
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 114
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 48
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 45
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 47
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 93
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 2
            mDato.idCanto = 106
            mDato.idGruppo = 1
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 234
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 206
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 231
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 126
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 215
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 233
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 176
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 175
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 76
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 128
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 205
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 211
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 130
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 166
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 129
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 3
            mDato.idCanto = 216
            mDato.idGruppo = 1
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 136
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 217
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 199
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 57
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 207
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 235
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 153
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 203
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 48
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 99
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 72
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 167
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 194
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 208
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 178
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 195
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 135
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 62
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 64
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 202
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 35
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 171
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 141
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 147
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 4
            mDato.idCanto = 201
            mDato.idGruppo = 1
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 158
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 217
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 109
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 207
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 118
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 203
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 117
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 48
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 99
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 152
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 137
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 191
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 212
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 141
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 214
            mDato.idGruppo = 1
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 5
            mDato.idCanto = 187
            mDato.idGruppo = 1
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 204
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 243
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 144
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 181
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 118
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 104
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 65
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 89
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 44
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 107
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 87
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 156
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 63
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 212
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 247
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 190
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 171
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 213
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 147
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 214
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 88
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 134
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 116
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 66
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 6
            mDato.idCanto = 253
            mDato.idGruppo = 2
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 150
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 148
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 146
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 122
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 98
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 30
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 100
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 80
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 120
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 40
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 136
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 218
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 57
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 97
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 234
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 170
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 38
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 179
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 231
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 183
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 126
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 41
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 73
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 94
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 215
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 42
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 251
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 37
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 241
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 128
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 79
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 193
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 211
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 96
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 130
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 60
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 115
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 123
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 169
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 220
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 151
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 166
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 129
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 127
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 149
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 162
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 121
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 95
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 7
            mDato.idCanto = 168
            mDato.idGruppo = 2
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 56
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 192
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 85
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 29
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 158
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 125
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 13
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 68
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 101
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 199
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 207
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 232
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 234
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 244
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 20
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 84
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 206
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 138
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 22
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 235
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 153
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 203
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 114
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 55
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 48
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 99
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 233
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 72
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 236
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 194
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 76
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 86
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 208
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 246
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 178
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 135
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 159
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 219
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 163
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 62
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 64
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 212
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 15
            mDato.idGruppo = 2
            mList.add(mDato)


            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 17
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 70
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 225
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 230
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 35
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 106
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 228
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 242
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 25
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 216
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 223
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 147
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 214
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 174
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 164
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 201
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 23
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 8
            mDato.idCanto = 187
            mDato.idGruppo = 2
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 204
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 77
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 78
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 132
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 232
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 234
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 251
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 241
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 191
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 58
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 145
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 135
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 156
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 128
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 79
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 133
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 157
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 166
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 129
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 216
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 214
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 9
            mDato.idCanto = 134
            mDato.idGruppo = 2
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 243
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 144
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 125
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 119
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 101
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 89
            mDato.idGruppo = 2
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 10
            mDato.idCanto = 88
            mDato.idGruppo = 2
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 11
            mDato.idCanto = 67
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 11
            mDato.idCanto = 39
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 11
            mDato.idCanto = 173
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 11
            mDato.idCanto = 172
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 11
            mDato.idCanto = 168
            mDato.idGruppo = 3
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 61
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 49
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 50
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 52
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 51
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 102
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 105
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 12
            mDato.idCanto = 82
            mDato.idGruppo = 3
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 57
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 104
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 235
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 45
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 194
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 76
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 13
            mDato.idCanto = 212
            mDato.idGruppo = 3
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 234
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 38
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 183
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 126
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 215
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 176
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 189
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 128
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 205
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 211
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 14
            mDato.idCanto = 130
            mDato.idGruppo = 3
            mList.add(mDato)

            /* ////////////////////////// */

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 192
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 85
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 29
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 158
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 13
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 168
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 101
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 97
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 84
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 153
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 114
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 4
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 99
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 236
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 86
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 159
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 15
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 17
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 70
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 230
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 35
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 8
            mDato.idGruppo = 3
            mList.add(mDato)

            mDato = IndiceLiturgico()
            mDato.idIndice = 15
            mDato.idCanto = 216
            mDato.idGruppo = 3
            mList.add(mDato)

            return mList
        }
    }
}
