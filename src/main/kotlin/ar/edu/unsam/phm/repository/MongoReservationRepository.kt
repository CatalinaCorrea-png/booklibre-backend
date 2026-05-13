package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.ReservationDoc
import ar.edu.unsam.phm.dto.ActiveReservationBookId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDate

interface MongoReservationRepository : MongoRepository<ReservationDoc, String> {

    @Query("""{
            "ownerId" : ?0,
            "pickUpDate" : { "${"$"}lte" : ?1 },
            "dropOffDate" : { "${"$"}gte": ?1 }
        }""")
    fun findBooksIdsByActiveReservation(ownerId: String, today: LocalDate): List<ActiveReservationBookId>
}