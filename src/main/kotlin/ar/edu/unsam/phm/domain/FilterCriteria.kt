package ar.edu.unsam.phm.domain

import org.springframework.data.mongodb.core.query.Criteria

enum class FilterCriteria {
    ALL {
        override fun bookFilter(borrowedBookIds: Set<String>): Criteria = Criteria()
    },
    AVAILABLE {
        override fun bookFilter(borrowedBookIds: Set<String>): Criteria =
            Criteria.where("bookId").nin(borrowedBookIds)
    },
    BORROWED {
        override fun bookFilter(borrowedBookIds: Set<String>): Criteria =
            Criteria.where("bookId").`in`(borrowedBookIds)
    };

    abstract fun bookFilter(borrowedBookIds: Set<String>): Criteria
}
