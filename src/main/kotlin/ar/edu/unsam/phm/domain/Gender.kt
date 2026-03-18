package ar.edu.unsam.phm.domain

enum class Gender(val value: String) {
    DRAMA("Drama"),
    SCIENCE_FICTION("Ciencia Ficcion"),
    ROMANCE("Romance"),
    SELF_HELP("Auto Ayuda"),
    DESIGN("Diseño"),
    CLASSIC_LITERATURE("Literatura Clasica");
    companion object {
        fun fromValue(value: String): Gender =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Gender desconocido: $value")
    }
}