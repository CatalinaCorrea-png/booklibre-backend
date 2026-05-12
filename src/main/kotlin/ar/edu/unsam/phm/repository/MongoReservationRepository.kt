package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.ReservationDoc
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoReservationRepository : MongoRepository<ReservationDoc, String> {

}