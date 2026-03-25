package ar.edu.unsam.phm.domain

enum class State(val value: String){
    ACTIVE("Activo"),
    AVAILABLE("Disponible"),
    RESERVED("Reservado"),
    BORROWED("Prestado"),
    SOON_TO_END("Proximo a vencer"),
    RETURNED("Devuelto")
}