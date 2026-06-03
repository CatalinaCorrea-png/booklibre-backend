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
    val sortBy: String = "title",
    val ascending: Boolean = true,
)

// Volaron para utilizar el Sort y Pageable de Spring
//data class SortingCriteria (
//    val sortedBy: BookSortCriteria = SortByTitle,
//    val ascending: Boolean = true,
//)
//
//data class PageRequest (
//    val page: Int = 0,
//    val pageSize: Int = 6
//)
