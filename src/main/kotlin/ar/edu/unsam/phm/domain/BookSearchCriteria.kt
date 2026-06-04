package ar.edu.unsam.phm.domain

import java.time.LocalDate

data class BookSearchCriteria(
    val userId: String?,
    val title: String? = null,
    val genders: List<Gender> = listOf(),
    val pagesRangeMin: Int? = null,
    val pagesRangeMax: Int? = null,
    val pickUpDate: LocalDate = LocalDate.now(),
    val dropOffDate: LocalDate = LocalDate.now(),
    val isbn: String? = null,
    val ownersName: String? = null,
    val page: Int = 0,
    val pageSize: Int = 6,
    val sortBy: String = "title", // "author.name", "owner.name", "bookClicks"
    val ascending: Boolean = true,
)

// NOTAS sobre el SORTING:
// `ascending` se interpreta RELATIVO al orden natural de cada campo (no es ASC literal):
// texto (title/author.name/owner.name) → natural ASC.
// bookClicks (relevancia) → natural DESC.
// La conversión a Sort.Direction se hace en BookController.getFilteredBooks().