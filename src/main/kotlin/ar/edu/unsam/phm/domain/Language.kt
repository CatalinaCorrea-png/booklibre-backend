package ar.edu.unsam.phm.domain

enum class Language(val value: String) {
    SPANISH("ESPAÑOL"),
    ENGLISH("INGLES"),
    FRENCH("FRANCES"),
    PORTUGUESE("PORTUGUES");
    companion object {
        fun fromValue(value: String): Language =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Language desconocido: $value")
    }
}