package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.repository.MongoBookRepository
import ar.edu.unsam.phm.services.OpenLibraryClient
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.slf4j.LoggerFactory

@DgsComponent
class BookDataFetcher(
    val bookRepository: MongoBookRepository,
    val openLibraryClient: OpenLibraryClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Query base: lee de Mongo y mapea Book (dominio) -> BookGql. NO llama a OL
    @DgsQuery
    fun book(@InputArgument isbn: String): BookGql? =
        bookRepository.findByIsbn(isbn).firstOrNull()?.let {
            BookGql(
                bookId = it.bookId,
                title = it.title,
                isbn = it.isbn,
                imageSrc = it.imageSrc,
            )
        }

    // Field resolver: SOLO corre si el cliente pide externalMetadata. Aca ocurre el stitching
    @DgsData(parentType = "BookGql", field = "externalMetadata")
    fun externalMetadata(dfe: DgsDataFetchingEnvironment): ExternalMetadata {
        val book = dfe.getSource<BookGql>()!!
        val external = openLibraryClient.fetchByIsbn(book.isbn)
        logger.info(">>> Llamando a OpenLibrary por ISBN {}", book.isbn)
        return ExternalMetadata(
            title = external?.title,
            cover = external?.cover ?: book.imageSrc, // <- con fallback
            pageCount = external?.pageCount ?: 0,
            publishDate = external?.publishDate,
        )
    }
}