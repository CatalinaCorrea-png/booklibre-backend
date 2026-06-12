package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.repository.MongoBookRepository
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import java.time.LocalDate

@DgsComponent
class CatalogHealthDataFetcher(
    val bookRepository: MongoBookRepository,
) {

    @DgsQuery
    fun catalogHealth(): CatalogHealth {
        val today = LocalDate.now()

        val books = bookRepository.findByDeletedFalse()

        var prestados = 0
        var nuncaReservados = 0
        var reservadosAFuturo = 0
        var devueltos = 0

        books.forEach { book ->
            val reservas = book.reservations
            when {
                reservas.isEmpty() -> nuncaReservados++                                                  // sin ninguna reserva                    |  2
                reservas.any { it.pickUpDate <= today && it.dropOffDate >= today } -> prestados++        // reserva vigente hoy                    |  1
                reservas.any { it.dropOffDate < today } -> devueltos++                                   // sin vigente, al menos una finalizada   |  4
                else -> reservadosAFuturo++                                                              // sin vigente, solo futuras              |  3
            }
        }

        return CatalogHealth(
            total = books.size,
            prestados = prestados,
            disponiblesNuncaReservados = nuncaReservados,
            disponiblesReservadosAFuturo = reservadosAFuturo,
            disponiblesDevueltos = devueltos,
        )
    }

//    @DgsQuery
//    fun catalogHealth(): CatalogHealth =
//        bookRepository.catalogHealth(LocalDate.now())
//            ?: CatalogHealth(0, 0, 0, 0, 0)
//
}
