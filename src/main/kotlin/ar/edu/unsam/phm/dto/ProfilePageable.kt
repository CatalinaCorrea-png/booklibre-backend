package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.FilterCriteria
import ar.edu.unsam.phm.domain.SortCriteria

data class ProfilePageable(
    val filterCriteria: FilterCriteria,
    val sortCriteria: SortCriteria,
    val page: Int,
    val pageSize: Int
)