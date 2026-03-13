package model

import java.util.Date

data class Review(
    var reviewerId: Int,
    var rating: Int,
    var comment: String,
    var timestamp: Date,
): RepositoryElement {
    override var id: Int = 0

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}
