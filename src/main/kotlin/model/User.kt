package model

import errors.BusinessException

class User(
    val name: String = "",
    val description: String = "",
    val email: String = "",
    val cel: String = "",
    val location: String = "",
    var userType: UserType,
    val timestamp: String = "",
    var bibliokarmas: Int = 0,

): RepositoryElement {
    override var id = 0

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}