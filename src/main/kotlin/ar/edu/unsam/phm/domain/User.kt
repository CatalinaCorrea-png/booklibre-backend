package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.UpdateUserProfileDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.*

@Entity
@Table(name = "app_user")
class User(
    val name: String = "",
    val description: String = "",
    @Column(length = 50)
    val email: String = "",
    @Column(length = 10)
    val cel: String = "",
    @Column(length = 50)
    val location: String = "",
    var userType: UserTypes = UserTypes.COMBINED,
    val timestamp: String = "",
    @Column(nullable = false)
    var bibliokarmas: Long = 0,
    var password: String = "",
    var img: String = ""

) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: String? = null

    fun addBibliokarmas(bibliokarmas: Long) {
        this.bibliokarmas += bibliokarmas
    }

    override fun meetsSearchCriteria(criteria: String) =
        matchesPartiallyWith(criteria, name) || matchesPartiallyWith(criteria, email)

    override fun validate() {
        if (!isNotEmpty(name)) throw NotFoundException("El usuario tiene que tener un nombre")
        if (!isNotEmpty(email)) throw NotFoundException("El usuario tiene que tener email")
        if (!isNotEmpty(password)) throw NotFoundException("El usuario tiene que tener password")
        if (password.length < 8) throw NotFoundException("El password debe tener al menos 8 caracteres")
    }

    fun validateProfileUpdate() {
        if (!isNotEmpty(name)) throw BusinessException("Debe ingresar un nombre valido")
        if (name.length <= 1) throw BusinessException("El nombre debe contener 2 o mas caracteres")
        if (name.length >= 25) throw BusinessException("El nombre no puede tener mas de 25 caracteres")
        if (!isNotEmpty(location)) throw BusinessException("Debe añadir una ubicacion valida")
        if (!isNotEmpty(email)) throw BusinessException("Debe ingrese un email valido")
    }

    companion object {
        fun fromDTO(userDTO: UpdateUserProfileDTO): User =
            User(
                name = userDTO.name,
                description = userDTO.description,
                email = userDTO.email,
                cel = userDTO.cel,
                location = userDTO.location,
                timestamp = userDTO.timestamp,
                bibliokarmas = userDTO.bibliokarmas,
                userType = UserTypes.fromValue(userDTO.userType),
            ).apply {
                this.id = userDTO.id
            }
    }
}