package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.ClickDeduplication
import ar.edu.unsam.phm.domain.ReservationDoc
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoClickDeduplicationRepository : MongoRepository<ClickDeduplication, String> {
}