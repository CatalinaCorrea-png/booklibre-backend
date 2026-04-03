package ar.edu.unsam.phm.domain

enum class BookSortField(val property: String) {
    TITLE("title") {
        override fun selector(book: Book) = book.title
    },
    AUTHOR("author.name") {
        override fun selector(book: Book) = book.author.name
    },
    OWNER("owner.name") {
        override fun selector(book: Book) = book.owner.name
    };

    abstract fun selector(book: Book): String?

    // FACTORY METHOD
    companion object { // (static)
        fun from(property: String) = entries.find { it.property == property } ?: TITLE
    }
}