package repository

import model.Book
import model.Repository
import model.Reservation
import model.User

class BookRepository: Repository<Book>() {

    fun availableBooks(reservation: Reservation): MutableList<Book> = this.repositoryObjects().filter { it.isAvailable(reservation) }.toMutableList()

}