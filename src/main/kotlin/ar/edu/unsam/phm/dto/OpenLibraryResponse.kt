package ar.edu.unsam.phm.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenLibraryResponse(
    val title: String? = null,
    @JsonProperty("number_of_pages") val numberOfPages: Int? = null,
    @JsonProperty("publish_date") val publishDate: String? = null,
    val covers: List<Long>? = null, // IDs numericos de portada
)
