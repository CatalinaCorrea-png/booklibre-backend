package ar.edu.unsam.phm.domain

import jakarta.persistence.Entity

@Entity
class Common : Book(bookType = "COMUN") {
    override fun typeBibliokarmas(userBibliokarmas: Int) : Int = if (userBibliokarmas < 1000) this.numPages * 5 else this.numPages * 2
}

@Entity
class WithADedication : Book(bookType = "CON DEDICATORIA") {
    override fun typeBibliokarmas(userBibliokarmas: Int): Int = 200 + 10 * this.numOfReservations()
}

@Entity
class Collectable : Book(bookType = "COLECCIONABLE") {
    override fun typeBibliokarmas(userBibliokarmas: Int): Int {
        // redondeo hacia arriba
        val fifthPart = (userBibliokarmas + 4) / 5
        return fifthPart + this.numPages
    }
}