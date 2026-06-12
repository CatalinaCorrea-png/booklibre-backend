package ar.edu.unsam.phm.graphql

data class CatalogHealth(
    val total: Int,
    val prestados: Int,
    val disponiblesNuncaReservados: Int,
    val disponiblesReservadosAFuturo: Int,
    val disponiblesDevueltos: Int,
)
