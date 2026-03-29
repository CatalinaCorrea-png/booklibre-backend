package ar.edu.unsam.phm.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class BookCondition(val value: String) {
    EXCELLENT("EXCELENTE"),
    VERY_GOOD("MUY BUENO"),
    GOOD("BUENO"),
    BAD("MALO"),
    REGULAR("REGULAR");

    @JsonValue
    fun toJson() = value
}