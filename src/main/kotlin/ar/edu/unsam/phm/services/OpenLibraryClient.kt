package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.dto.OpenLibraryResponse
import ar.edu.unsam.phm.graphql.ExternalMetadata
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.http.HttpClient

@Component
class OpenLibraryClient {

    private val rest = RestClient.builder()
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build()
            )
        )
        .build()

    fun fetchByIsbn(isbn: String): ExternalMetadata? =
        try {
            val clean = isbn.replace("-", "")
            val response = rest.get()
                .uri("https://openlibrary.org/isbn/{isbn}.json", clean)
                .header("User-Agent", "booklibre-tp/1.0") // OL a veces rechaza reqs sin User-Agent
                .retrieve()
                .body<OpenLibraryResponse>() // tira excepción en 4xx/5xx

            response?.let {
                ExternalMetadata(
                    title = it.title,
                    cover = it.covers?.firstOrNull()?.let { id ->
                        "https://covers.openlibrary.org/b/id/$id-L.jpg"
                    },
                    pageCount = it.numberOfPages,
                    publishDate = it.publishDate,
                )
            }
        } catch (e: Exception) {
            null // 404 / timeout / parse error -> null (lo manejamos en el resolver)
        }
}