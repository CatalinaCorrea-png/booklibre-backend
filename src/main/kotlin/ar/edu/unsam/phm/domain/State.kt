package ar.edu.unsam.phm.domain

enum class State(val value: String){
    AVAILABLE("Disponible"),
    BORROWED("Prestado"),
    SOON_TO_END("Proximo a vencer"),
    RETURNED("Devuelto")
}