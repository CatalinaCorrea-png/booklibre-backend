package ar.edu.unsam.phm.domain

import java.time.LocalDate

enum class State(val value: String) {
    RETURNED("Devuelto") {
        override fun matches(today: LocalDate, start: LocalDate, end: LocalDate) = today.isAfter(end)
    },
    RESERVED("Reservado") {
        override fun matches(today: LocalDate, start: LocalDate, end: LocalDate) = today.isBefore(start)
    },
    SOON_TO_END("Próximo a vencer") {
        override fun matches(today: LocalDate, start: LocalDate, end: LocalDate) =
            (today.isEqual(end.minusDays(1)) || today.isEqual(end.minusDays(2)) || today.isEqual(end))
    },
    ACTIVE("Activo") {
        override fun matches(today: LocalDate, start: LocalDate, end: LocalDate) = true // Caso por defecto
    },
    @Deprecated("Use BookAvailability.AVAILABLE instead", ReplaceWith("BookAvailability.AVAILABLE"))
//    Esto solo devuelve false y yo necesitaba una logica (domain/BookAvailability), por eso lo depreque, si no lo cambian funciona igual es mas de gede que otra cosa ~ Niki
    AVAILABLE("Disponible") {
        override fun matches(t: LocalDate, s: LocalDate, e: LocalDate) = false
    },
    @Deprecated("Use BookAvailability.BORROWED instead", ReplaceWith("BookAvailability.BORROWED"))
//    Idem que arriba
    BORROWED("Prestado") {
        override fun matches(t: LocalDate, s: LocalDate, e: LocalDate) = false
    };

    abstract fun matches(today: LocalDate, start: LocalDate, end: LocalDate): Boolean

    companion object {
        fun get(today: LocalDate, start: LocalDate, end: LocalDate): State {
            return entries.first { it.matches(today, start, end) }
        }
    }
}