package ar.edu.unsam.phm.graphql

data class BookGql (
    val bookId: String,
    val title: String,
    val isbn: String,
    val imageSrc: String,
)