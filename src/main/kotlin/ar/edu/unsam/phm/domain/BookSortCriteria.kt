package ar.edu.unsam.phm.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type

// STRATEGY PARA CRITERIO DE ORDENAMIENTO
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    Type(value = SortByTitle::class, name = "title"),
    Type(value = SortByAuthor::class, name = "author"),
    Type(value = SortByOwner::class, name = "owner"),
)
interface BookSortCriteria {
    fun sort(books: List<Book>) : MutableList<Book>
}

object SortByTitle :  BookSortCriteria {
    override fun sort(books: List<Book>) : MutableList<Book> = books.sortedBy { it.title }.toMutableList()
}

object SortByAuthor :  BookSortCriteria {
    override fun sort(books: List<Book>) : MutableList<Book> = books.sortedBy { it.author.name }.toMutableList()
}

object SortByOwner :  BookSortCriteria {
    override fun sort(books: List<Book>) : MutableList<Book> = books.sortedBy { it.owner.name }.toMutableList()
}