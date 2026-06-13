package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookAvailability
import ar.edu.unsam.phm.domain.Gender
import java.time.LocalDate

data class ProfileBookDTO(
    var id: String?,
    var title: String,
    var author: String,
    var gender: Gender,
    var timestamp: LocalDate,
    var imageSrc: String,
    var state: BookAvailability,
    var clicks: Int,
)

fun Book.toProfileBookDTO(today: LocalDate) = ProfileBookDTO(
    id = id,
    title = title,
    author = author.name,
    gender = gender,
    // El DTO mantiene 'timestamp' como nombre de campo (contrato REST del perfil),
    // pero ahora lee del renombrado Book.createdAt.
    timestamp = createdAt,
    imageSrc = imageSrc,
    state = BookAvailability.of(
        reservations.any {it.pickUpDate <= today && it.dropOffDate >= today}
    ),
    clicks = bookClicks
)