package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
import org.springframework.stereotype.Service


@Service
class BookService (
    val bookRepository: BookRepository,
) {
    fun createBook(book: Book){
        if(!canCreate(book)) throw BusinessException("El libro que intenta crear ya existe")
        bookRepository.create(book)
    }

    fun canCreate(book: Book) : Boolean {
        //este != es que no esta en la lista
        return bookRepository.findIndexInCollection(book.id) != -1
    }

    /*
    fun updateBook(updatedBook : BookDTO) : Book {
        val newBook = updatedBook.fromDTO()


    }*/
}