package ar.edu.unsam.phm.graphql

// Matchea el type BookConversion del esquema GraphQL (DGS mapea por nombre de campo).
data class BookConversion(
    val bookId: String,
    val title: String,
    val clicks: Int,
    val reservations: Int,
    val conversionRate: Double,   // reservas / clicks
)
