package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Author(
    val name: String,
    val avatar: String = "assets/author_default.jpg"
) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: String? = null

    override fun validate() {
        if (!isNotEmpty(name)) throw ConflictException("El autor tiene que tener nombre")
        if (name.length > 50) throw ConflictException("El nombre del autor no debe superar los 50 caracteres")
        if (!isNotEmpty(avatar)) throw ConflictException("El autor debe tener avatar")
        if (avatar.length > 50) throw ConflictException("El avatar del autor no debe superar los 50 caracteres")
    }

    override fun meetsSearchCriteria(criteria: String): Boolean {
        return true
    }
}