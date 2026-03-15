package ar.edu.unsam.phm.domain

import java.time.LocalDate

data class BookSearchCriteria (
    val title: String? = null,
    val genders: Set<Gender> = mutableSetOf(),
    val pagesRangeMin: Int? = null,
    val pagesRangeMax: Int? = null,
    val pickUpDate: LocalDate = LocalDate.now(),
    val dropOffDate: LocalDate = LocalDate.now(),
    val ISBN: String? = null,
    val ownersName: String? = null,
    val page: Int = 0,
    val pageSize: Int = 6
)
