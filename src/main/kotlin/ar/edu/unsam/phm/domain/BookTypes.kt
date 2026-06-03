package ar.edu.unsam.phm.domain

import jakarta.persistence.Entity
import kotlin.math.ceil

//@Entity
class Common : Book(bookType = "COMUN") {
    override fun typeBibliokarmas(userBibliokarmas: Long): Long = if (userBibliokarmas < 1000) this.numPagesLong() * 5 else this.numPagesLong() * 2
}

//@Entity
class WithADedication : Book(bookType = "CON DEDICATORIA") {
    override fun typeBibliokarmas(userBibliokarmas: Long): Long = 200 + 10 * this.reservationCount()
}

//@Entity
class Collectable : Book(bookType = "COLECCIONABLE") {
    override fun typeBibliokarmas(userBibliokarmas: Long): Long {
        // redondeo hacia arriba
        val fifthPart = ceil(userBibliokarmas / 5.0).toLong()
        return fifthPart + this.numPagesLong()
    }
}