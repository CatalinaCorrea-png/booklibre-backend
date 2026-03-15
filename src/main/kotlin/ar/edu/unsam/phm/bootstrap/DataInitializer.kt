package ar.edu.unsam.phm.bootstrap

import ar.edu.unsam.phm.domain.Author
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.repository.Repository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    val userRepository: Repository<User>,
    val bookRepository: Repository<Book>,
    val reservationRepository: Repository<Reservation>,
    //val authorRepository: Repository<Author>
) {
    @PostConstruct
    fun init() {
        ApplicationBootstrap.init(
            userRepository,
            bookRepository,
            reservationRepository,
            //authorRepository
        )
    }
}