package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.ReservationDoc
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MongoReservationRepository : MongoRepository<ReservationDoc, String> {

    @Query(
        """{ 
    "userId": ?0, 
    "bookDeleted": false, 
    "${'$'}or": [ 
        { "bookTitle": { "${'$'}regex": ?1, "${'$'}options": "i" } }, 
        { "bookAuthorName": { "${'$'}regex": ?1, "${'$'}options": "i" } } 
    ] 
    }"""
    )
    fun findByLectorIdFiltered(userId: String, search: String, pageable: Pageable): Page<ReservationDoc>


    @Query(
        """{ 
    "ownerId": ?0, 
    "bookDeleted": false, 
    "${'$'}or": [ 
        { "bookTitle": { "${'$'}regex": ?1, "${'$'}options": "i" } }, 
        { "bookAuthorName": { "${'$'}regex": ?1, "${'$'}options": "i" } } 
    ] 
    }"""
    )
    fun findByOwnerIdFiltered(userId: String, search: String, pageable: Pageable): Page<ReservationDoc>





}