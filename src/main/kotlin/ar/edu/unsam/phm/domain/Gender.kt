package ar.edu.unsam.phm.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class Gender(val value: String) {
    DRAMA("Drama"),
    SCIENCE_FICTION("Ciencia Ficcion"),
    ROMANCE("Romance"),
    SELF_HELP("Auto Ayuda"),
    DESIGN("Diseño"),
    CLASSIC_LITERATURE("Literatura Clasica");

    @JsonValue
    fun toJson() = value
}

