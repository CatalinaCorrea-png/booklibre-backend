package ar.edu.unsam.phm.domain

import org.springframework.data.mongodb.core.query.Criteria

enum class FilterCriteria {
    ALL {
        override fun bookFilter(borrowedBookIds: Set<String>): Criteria = Criteria()
    },
    AVAILABLE {
        override fun bookFilter(borrowedBookIds: Set<String>): Criteria =
            Criteria.where("_id").nin(borrowedBookIds)
    },
    BORROWED {
        override fun bookFilter(borrowedBookIds: Set<String>): Criteria =
            Criteria.where("_id").`in`(borrowedBookIds)
    };

    abstract fun bookFilter(borrowedBookIds: Set<String>): Criteria
}
