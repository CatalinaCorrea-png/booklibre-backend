package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.ReservationProfileDTO

enum class SortCriteria(val comparator: Comparator<Reservation>) {
    DATE_ASC(compareBy{ reservation -> reservation.book.timestamp }),
    DATE_DESC(compareByDescending { reservation -> reservation.book.timestamp }),
    ALPHABETICAL_ASC(compareBy{ reservation -> reservation.book.title }),
    ALPHABETICAL_DESC(compareByDescending{ reservation -> reservation.book.title })
}