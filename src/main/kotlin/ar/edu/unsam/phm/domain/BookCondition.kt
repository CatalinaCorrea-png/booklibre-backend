package ar.edu.unsam.phm.domain

enum class BookCondition(val value: String) {
    EXCELLENT("EXCELENTE"),
    VERY_GOOD("MUY BUENO"),
    GOOD("BUENO"),
    BAD("MALO"),
    REGULAR("REGULAR");

    companion object {
        fun fromValue(value: String): BookCondition =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("BookCondition desconocido: $value")
    }
}