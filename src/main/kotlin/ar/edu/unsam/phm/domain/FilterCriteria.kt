package ar.edu.unsam.phm.domain


enum class FilterCriteria(val predicate: (Reservation) -> Boolean) {
    ALL({ reservation -> true }),
    AVAILABLE({ reservation -> reservation.state == State.RETURNED
                            || reservation.state == State.RESERVED
                            || reservation.state == State.AVAILABLE }),
    BORROWED({ reservation -> reservation.state == State.ACTIVE
                           || reservation.state == State.BORROWED
                           || reservation.state == State.SOON_TO_END })
}