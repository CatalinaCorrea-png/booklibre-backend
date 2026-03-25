package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.ReservationProfileDTO

enum class FilterCriteria(val predicate: (ReservationProfileDTO) -> Boolean) {
    ALL({ reservation -> true }),
    AVAILABLE({ reservation -> reservation.state == State.RETURNED }),
    BORROWED({ reservation -> reservation.state == State.ACTIVE
                           || reservation.state == State.BORROWED
                           || reservation.state == State.SOON_TO_END })
}