package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.State
import java.time.LocalDate

data class ReservationDTO(
    val book: BookDTO,
    var id: String,
    var user: UserDTO,
    var review: Int,
    var pickUpDate: LocalDate,
    var dropOffDate: LocalDate,
    var state: State,
    var canRate: Boolean,
    var bibliokarmas: Long = 0,
    var loanedBy: String,
    var loanedTo: String,
)

// Esto lo hice asi para no cambiar el front
fun Reservation.toDTO(hasReview: Boolean): ReservationDTO =
    ReservationDTO(
        book = BookDTO(
            id = this.bookId,
            title = this.bookTitle,
            bookType = "",
            desc = "",
            gender = "", authorName = this.bookAuthorName,
            authorAvatarUrl = "",
            numPages = 0,
            isbn = "",
            language = "",
            editorial = "",
            publishDate = LocalDate.EPOCH,
            condition = "",
            owner = OwnerDTO(name = this.ownerName),
            imageSrc = this.bookImageSrc,
        ),
        id = this.id!!,
        user = this.user.toUserDTO(),
        review = 0,
        pickUpDate = this.pickUpDate,
        dropOffDate = this.dropOffDate, 
        state = this.state,
        canRate = !hasReview && this.state == State.RETURNED,
        bibliokarmas = this.bibliokarmas,
        loanedBy = this.ownerName,
        loanedTo = this.user.name,
    )

//fun Reservation.toDTO(
//    hasReview: Boolean
//): ReservationDTO {
//
//    return ReservationDTO(
//        book = this.book?.toDTO()!!,
//        id = this.id!!,
//        user = this.user.toUserDTO(),
//        review = 0, // se pisa en el service
//        pickUpDate = this.pickUpDate,
//        dropOffDate = this.dropOffDate,
//        state = this.state,
//        canRate = this.state == State.RETURNED && !hasReview, // esto se calcula en el service, no se por que esta aca
//        bibliokarmas = 0, // this.book.calculateBibliokarmas(days, user.bibliokarmas, ), //todo: arreglar esto...
//        // se pisa en el service con el valor correcto
//        loanedBy = this.book!!.owner.name,
//        loanedTo = this.user.name,
//    )
//}