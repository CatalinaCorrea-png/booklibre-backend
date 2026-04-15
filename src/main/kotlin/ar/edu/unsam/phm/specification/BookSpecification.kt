package ar.edu.unsam.phm.specification

import ar.edu.unsam.phm.domain.*
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate

object BookSpecifications {

    fun notDeleted(): Specification<Book> =
        Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<Boolean>("deleted"), false)
        }

    fun notOwner(userId: Long?): Specification<Book> =
        Specification { root, _, cb ->
            userId?.let {
                cb.notEqual(root.get<User>("owner").get<Long>("id"), it)
            }
        }

    fun titleLike(title: String?): Specification<Book> =
        Specification { root, _, cb ->
            title?.let {
                cb.like(cb.lower(root.get("title")), "%${it.lowercase()}%")
            }
        }

    fun ownersNameLike(ownersName: String?): Specification<Book> =
        Specification { root, _, cb ->
            ownersName?.let {
                cb.like(cb.lower(root.get<User>("owner").get("name")), "%${it.lowercase()}%")
            }
        }

    fun isbnLike(isbn: String?): Specification<Book> =
        Specification { root, _, cb ->
            isbn?.let {
                cb.like(cb.lower(root.get("isbn")), "%${it.lowercase()}%")
            }
        }

    fun pagesMin(min: Int?): Specification<Book> =
        Specification { root, _, cb ->
            min?.let {
                cb.greaterThanOrEqualTo(root.get("numPages"), it)
            }
        }

    fun pagesMax(max: Int?): Specification<Book> =
        Specification { root, _, cb ->
            max?.let {
                cb.lessThanOrEqualTo(root.get("numPages"), it)
            }
        }

    fun gendersIn(genders: List<Gender>): Specification<Book> =
        Specification { root, _, _ ->
            if (genders.isNotEmpty()) {
                root.get<Gender>("gender").`in`(genders)
            } else null
        }

    fun noOverlappingReservations(pickUpDate: LocalDate, dropOffDate: LocalDate): Specification<Book> =
        Specification { root, query, cb ->
            // Creo subquery para traerme las reservas del libro en esas fechas
            val subquery = query.subquery(Reservation::class.java)
            val reservation = subquery.from(Reservation::class.java)
            subquery.select(reservation)
            subquery.where(
                cb.and(
                    cb.equal(reservation.get<Book>("book"), root),
                    cb.lessThanOrEqualTo(reservation.get("pickUpDate"), dropOffDate),
                    cb.greaterThanOrEqualTo(reservation.get("dropOffDate"), pickUpDate)
                )
            )
            // Si no hay reservas, pasa
            cb.not(cb.exists(subquery))
        }

    // Función de conveniencia que compone todo
    fun byCriteria(
        criteria: BookSearchCriteria
    ): Specification<Book> =
        Specification.where(notOwner(criteria.userId))
            .and(notDeleted())
            .and(titleLike(criteria.title))
            .and(ownersNameLike(criteria.ownersName))
            .and(isbnLike(criteria.isbn))
            .and(pagesMin(criteria.pagesRangeMin))
            .and(pagesMax(criteria.pagesRangeMax))
            .and(gendersIn(criteria.genders))
            .and(noOverlappingReservations(criteria.pickUpDate, criteria.dropOffDate))
}