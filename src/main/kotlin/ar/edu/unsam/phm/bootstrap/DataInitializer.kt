package ar.edu.unsam.phm.bootstrap

import ar.edu.unsam.phm.domain.Author
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.Repository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    val userRepository: UserRepository,
    val bookRepository: BookRepository,
    val reservationRepository: ReservationRepository,
) {
    @PostConstruct
    fun init() {
        ApplicationBootstrap.init(
            userRepository,
            bookRepository,
            reservationRepository,
        )
        println("usuarios cargados: ${userRepository.repositoryObjects().size}")
    }
}