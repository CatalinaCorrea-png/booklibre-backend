package ar.edu.unsam.phm.dto

data class PagedResult<T>(
    val items: List<T>,
    val total: Int,
    val totalPages: Int,
)