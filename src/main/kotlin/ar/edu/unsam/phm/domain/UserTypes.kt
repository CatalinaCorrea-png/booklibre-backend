package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.ConflictException

enum class UserTypes(val value: String) {
    PUBLISHER("Publicador"),
    READER("Lector"),
    COMBINED("Lector / Publicador"),
    ADMIN("Admin");

    companion object {
        fun fromValue(value: String): UserTypes {
            return entries.find { it.value == value }
                ?: throw ConflictException("Tipo de usuario desconocido: $value")
        }
    }
}