package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.FilterCriteria
import ar.edu.unsam.phm.domain.SortCriteria
import org.springframework.data.domain.PageRequest

data class ProfileBookPageable(
    val filterCriteria: FilterCriteria,
    val sortCriteria: SortCriteria,
    val page: Int,
    val pageSize: Int
) {
    fun toPageRequest() = PageRequest.of(page, pageSize, sortCriteria.sortBy)
}