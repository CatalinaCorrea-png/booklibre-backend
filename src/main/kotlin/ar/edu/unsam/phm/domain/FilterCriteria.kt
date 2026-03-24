package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.ReservationProfileDTO

enum class FilterCriteria(val predicate: (ReservationProfileDTO) -> Boolean) {
    ALL({ reservation -> true }),
    AVAILABLE({ reservation -> reservation.state == "Disponible"}),
    BORROWED({ reservation -> reservation.state == "Prestado" })
}