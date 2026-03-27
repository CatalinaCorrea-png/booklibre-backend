package ar.edu.unsam.phm.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class Language(val value: String) {
    SPANISH("ESPAÑOL"),
    ENGLISH("INGLES"),
    FRENCH("FRANCES"),
    PORTUGUESE("PORTUGUES");

    @JsonValue
    fun toJson() = value
}