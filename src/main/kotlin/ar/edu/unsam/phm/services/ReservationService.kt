package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import org.springframework.stereotype.Service

@Service
class ReservationService(
    val reservationRepository: ReservationRepository,
    val bookRepository: BookRepository
){
    fun createReservation(reservation: Reservation) {
        if (!canReserve(reservation)) throw BusinessException("Reserva no disponible en esa fecha")
        reservationRepository.create(reservation)

        // Acá sumo la reserva al libro?????
        val book = bookRepository.getObject(reservation.book.id)
        book.addReservation(reservation.id)
    }

    fun canReserve(reservation: Reservation) : Boolean = reservationRepository.repositoryObjects().any { it.dateOverlaps(reservation) }

    // GET DE RESERVAS (con sus libros) FILTRADOS! Y PAGINADO!
    // Ahora uso metodos, luego creamos una query dinamica para pedirle a la bbdd TODO filtrado.
    fun getAvailableReservationsBy(criteria: BookSearchCriteria) : PageResponse<ReservationDTO> {
        val allReservations = reservationRepository.repositoryObjects()
        val allBooks = bookRepository.repositoryObjects()

        val filtered = allReservations.filter { reservation ->
            matchesTitle(reservation, criteria) &&
            matchesGender(reservation, criteria) &&
            matchesPages(reservation, criteria) &&
            matchesISBN(reservation, criteria) &&
            matchesOwner(reservation, criteria) &&
            matchesAvailability(reservation, criteria)
        }

        // PAGINACION
        // Cuantas paginas son
        val totalElements = filtered.size
        val totalPages = if (totalElements == 0) 0 else Math.ceil(totalElements.toDouble() / criteria.pageSize).toInt()
        // Qué pagina devuelvo
        val fromIndex = (criteria.page * criteria.pageSize).coerceAtMost(totalElements) // primer libro de la pagina
        val toIndex = (fromIndex + criteria.pageSize).coerceAtMost(totalElements) // ultimo libro de la pagina
        // Creo la lista de libros por pagina
        val paged = filtered.subList(fromIndex, toIndex)

        return PageResponse(
            content = paged.map { it.toDTO() },
            page = criteria.page,
            pageSize = criteria.pageSize,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    // FILTROS DE BUSQUEDA >.<
    private fun matchesTitle(reservation: Reservation, criteria: BookSearchCriteria): Boolean {
        val title = criteria.title?.trim()
        return title.isNullOrBlank() || reservation.book.title.contains(title, ignoreCase = true)
    }

    private fun matchesGender(reservation: Reservation, criteria: BookSearchCriteria): Boolean {
        return criteria.genders.isEmpty() || criteria.genders.contains(reservation.book.gender)
    }

    private fun matchesPages(reservation: Reservation, criteria: BookSearchCriteria): Boolean {
        val min = criteria.pagesRangeMin ?: 0
        val max = criteria.pagesRangeMax ?: 1500 // Regla de negocio (Por ahora)
        return reservation.book.numPages in min..max
    }

    private fun matchesISBN(reservation: Reservation, criteria: BookSearchCriteria): Boolean {
        val isbn = criteria.ISBN?.trim()
        return isbn.isNullOrBlank() || reservation.book.ISBN.contains(isbn, ignoreCase = true)
    }

    private fun matchesOwner(reservation: Reservation, criteria: BookSearchCriteria): Boolean {
        val ownerName = criteria.ownersName?.trim()
        return ownerName.isNullOrBlank() || reservation.book.owner.name.contains(ownerName, ignoreCase = true)
    }

    private fun matchesAvailability(reservation: Reservation, criteria: BookSearchCriteria): Boolean {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)
        return !reservation.dateOverlaps(reservationTemp)
    }


}