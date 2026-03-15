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
    fun sort(books: List<Book>, ascending: Boolean) : MutableList<Book>

    // Función helper para no repetir la lógica en cada implementación
    // Es un extension method de List<Book> - pero privada para la interface
    fun <T : Comparable<T>> List<Book>.sortWith(selector: (Book) -> T?, ascending: Boolean): MutableList<Book> =
        if (ascending) sortedBy(selector).toMutableList()
        else sortedByDescending(selector).toMutableList()
}

object SortByTitle :  BookSortCriteria {
    //override fun sort(books: List<Book>, ascending: Boolean) : MutableList<Book> = books.sortedBy { it.title }.toMutableList()
    override fun sort(books: List<Book>, ascending: Boolean) : MutableList<Book> = books.sortWith({ it.title }, ascending)
}

object SortByAuthor :  BookSortCriteria {
    override fun sort(books: List<Book>, ascending: Boolean) : MutableList<Book> = books.sortWith({ it.author.name }, ascending)
}

object SortByOwner :  BookSortCriteria {
    override fun sort(books: List<Book>, ascending: Boolean) : MutableList<Book> = books.sortWith({ it.owner.name }, ascending)
}