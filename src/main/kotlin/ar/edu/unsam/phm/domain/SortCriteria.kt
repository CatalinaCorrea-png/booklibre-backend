package ar.edu.unsam.phm.domain

import org.springframework.data.domain.Sort

enum class SortCriteria(val sortBy: Sort) {
    DATE_ASC(Sort.by(Sort.Direction.ASC, "createdAt")),
    DATE_DESC(Sort.by(Sort.Direction.DESC, "createdAt")),
    ALPHABETICAL_ASC(Sort.by(Sort.Direction.ASC, "title")),
    ALPHABETICAL_DESC(Sort.by(Sort.Direction.DESC, "title")),
    CLICKS_ASC(Sort.by(Sort.Direction.ASC, "bookClicks")),
    CLICKS_DESC(Sort.by(Sort.Direction.DESC, "bookClicks"))
}