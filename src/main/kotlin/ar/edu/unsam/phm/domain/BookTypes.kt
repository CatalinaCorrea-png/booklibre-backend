package ar.edu.unsam.phm.domain

import jakarta.persistence.Entity

@Entity
class Common : Book(bookType = "COMUN") {
    override fun typeBibliokarmas(userBibliokarmas: Int): Long = if (userBibliokarmas < 1000) this.numPagesLong() * 5 else this.numPagesLong() * 2
}

@Entity
class WithADedication : Book(bookType = "CON DEDICATORIA") {
    override fun typeBibliokarmas(userBibliokarmas: Int): Long = 200 + 10 * this.reservationCount()
}

@Entity
class Collectable : Book(bookType = "COLECCIONABLE") {
    override fun typeBibliokarmas(userBibliokarmas: Int): Long {
        // redondeo hacia arriba
        val fifthPart = (userBibliokarmas + 4) / 5
        return fifthPart + this.numPagesLong()
    }
}