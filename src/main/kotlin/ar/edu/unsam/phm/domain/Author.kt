package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Author(
    val name: String,
    val avatar: String
): RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    override fun validate() {
        TODO("Not yet implemented")
    }

    override fun meetsSearchCriteria(criteria: String) : Boolean {
        TODO("Not yet implemented")
    }
}