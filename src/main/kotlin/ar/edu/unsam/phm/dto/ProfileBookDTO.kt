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
    var state: BookAvailability
)

fun Book.toProfileBookDTO(borrowedBookIds: Set<String>) = ProfileBookDTO(
    id = id,
    title = title,
    author = author.name,
    gender = gender,
    timestamp = timestamp,
    imageSrc = imageSrc,
    state = BookAvailability.of(bookId in borrowedBookIds)
)