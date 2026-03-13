package ar.edu.unsam.phm.dto
import ar.edu.unsam.phm.domain.Book

data class BookDTO(
    var title: String,

    ) {}
    fun Book.toDTO(): BookDTO{
        val bookDTO = BookDTO(
            title = this.title,


        )
        return bookDTO
    }
    /*
    fun Book.fromDTO(): Book{
        return Book(
            title = this.title
        ).apply { this.title = this@fromDTO.title }
    }

     */


