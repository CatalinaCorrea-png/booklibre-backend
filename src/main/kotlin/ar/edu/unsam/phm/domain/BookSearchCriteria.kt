package ar.edu.unsam.phm.domain

import java.time.LocalDate

// esto no se lo vemos que opinan
data class BookSearchCriteria (
    val gender: Gender? = null,
    val pagesRangeFrom: Int? = null,
    val pagesRangeTo: Int? = null,
    val title: String? = null,
    val ISBN: String? = null,
    val ownBy: User? = null,
    val from: LocalDate = LocalDate.now(),
    val to: LocalDate = LocalDate.now(),
)

