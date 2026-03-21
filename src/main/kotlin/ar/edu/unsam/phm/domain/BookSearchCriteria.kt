package ar.edu.unsam.phm.domain

import java.time.LocalDate

data class BookSearchCriteria (
    val userId: Int,
    val title: String? = null,
    val genders: List<Gender> = listOf(),
    val pagesRangeMin: Int? = null,
    val pagesRangeMax: Int? = null,
    val pickUpDate: LocalDate = LocalDate.now(),
    val dropOffDate: LocalDate = LocalDate.now(),
    val isbn: String? = null,
    val ownersName: String? = null,
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
