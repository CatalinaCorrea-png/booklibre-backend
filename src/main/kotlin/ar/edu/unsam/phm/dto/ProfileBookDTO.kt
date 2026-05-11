package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Gender
import java.time.LocalDate

data class ProfileBookDTO(
    var id: String?,
    var title: String,
    var author: String,
    var gender: Gender,
    var timestamp: LocalDate,
    var imageSrc: String,
    var state: String?
)