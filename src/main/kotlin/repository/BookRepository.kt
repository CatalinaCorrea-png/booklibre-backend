package repository

import model.*

class BookRepository: Repository<Book>() {

    fun availableBooks(criteria: BookSearchCriteria): MutableList<Book> {
        val reservation = Reservation(
            user = User(),
            book = Common(),
            review = Review(),
            pickUpDate = criteria.from,
            dropOffDate = criteria.to
        )

        return this.repositoryObjects()
            .filter { criteria.gender == null || it.gender == criteria.gender }
            .filter { criteria.ISBN == null || it.ISBN == criteria.ISBN }
            .filter { criteria.ownBy == null || it.owner == criteria.ownBy}
//            .filter { criteria.pagesRangeFrom == null && criteria.pagesRangeTo == null || (it.numPages > criteria.pagesRangeFrom!! &&  it.numPages == criteria.pagesRangeTo!!) }
            .filter { criteria.title == null || it.title.contains(criteria.title, ignoreCase = true) }
            .toMutableList()
    }

}