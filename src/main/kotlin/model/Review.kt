package model

import java.time.LocalDate
import java.util.Date

data class Review(
    var reviewerId: Int = 0,
    var rating: Int = 0,
    var comment: String = "",
    var timestamp: LocalDate = LocalDate.now(),

): RepositoryElement {
    override var id: Int = 0

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}
