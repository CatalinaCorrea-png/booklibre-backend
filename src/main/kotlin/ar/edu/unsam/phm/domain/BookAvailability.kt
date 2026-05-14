package ar.edu.unsam.phm.domain

enum class BookAvailability(val value: String) {
    AVAILABLE("Disponible"),
    BORROWED("Prestado");

    companion object {
        fun of(isBorrowed: Boolean) = if (isBorrowed) BORROWED else AVAILABLE
    }
}