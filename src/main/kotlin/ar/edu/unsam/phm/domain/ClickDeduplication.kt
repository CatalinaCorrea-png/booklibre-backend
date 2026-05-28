package ar.edu.unsam.phm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("click_dedup")
@CompoundIndex(name = "user_book_unique", def = "{'userId': 1, 'bookId': 1}", unique = true)
data class ClickDeduplication(
    @Id val id: String? = null,
    val userId: String,
    val bookId: String,
    @Indexed(expireAfterSeconds = 60)  // TTL
    val createdAt: Instant = Instant.now()
)
