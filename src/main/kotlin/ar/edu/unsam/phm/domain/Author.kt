package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.repository.RepositoryElement

data class Author(
    val name: String,
    val avatar: String
): RepositoryElement {

    override var id: Int = 0

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }
}