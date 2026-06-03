package ar.edu.unsam.phm.domain

import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDate

enum class FilterCriteria {
    ALL {
        override fun bookFilter(today: LocalDate): Criteria = Criteria()
    },
    AVAILABLE {
        override fun bookFilter(today: LocalDate): Criteria =
            Criteria.where("reservations").not().elemMatch(
                Criteria.where("pickUpDate").lte(today)
                    .and("dropOffDate").gte(today)
            )
    },
    BORROWED {
        override fun bookFilter(today: LocalDate): Criteria =
            Criteria.where("reservations").elemMatch(
                Criteria.where("pickUpDate").lte(today)
                    .and("dropOffDate").gte(today)
            )
    };

    abstract fun bookFilter(today: LocalDate): Criteria
}

