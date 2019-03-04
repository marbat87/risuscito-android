package it.cammino.risuscito.database.dao

class Backup constructor(
        val id: Int
        , val zoom: Int
        , val scrollX: Int
        , val scrollY: Int
        , val favorite: Int
        , val savedTab: String?
        , val savedBarre: String?
        , val savedSpeed: String?
) {
    override fun toString(): String {
        return "id: $id / zoom: $zoom / scrollX: $scrollX / scrollY: $scrollY / favorite: $favorite / savedTab: $savedTab / savedBarre: $savedBarre / savedSpeed: $savedSpeed"
    }
}