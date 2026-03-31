//package ar.edu.unsam.phm.bootstrap
//
//import ar.edu.unsam.phm.repository.BookRepository
//import ar.edu.unsam.phm.repository.ReservationRepository
//import ar.edu.unsam.phm.repository.UserRepository
//import jakarta.annotation.PostConstruct
//import org.springframework.stereotype.Component
//
//@Component
//class DataInitializer(
//    val userRepository: UserRepository,
//    val bookRepository: BookRepository,
//    val reservationRepository: ReservationRepository,
//) {
//    @PostConstruct
//    fun init() {
//        ApplicationBootstrap.init(
//            userRepository,
//            bookRepository,
//            reservationRepository,
//        )
//    }
//}