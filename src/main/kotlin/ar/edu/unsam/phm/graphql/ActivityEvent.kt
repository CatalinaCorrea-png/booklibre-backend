package ar.edu.unsam.phm.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeResolver

enum class ActivityEventType { BOOK_REGISTERED, RESERVATION }

// Modela la interface ActivityEvent del esquema. sealed para que el type resolver
// pueda hacer un when exhaustivo sobre las implementaciones.
sealed interface ActivityEvent {
    val date: String
    val typeEvent: ActivityEventType
    val user: String
    val bookTitle: String
}

// Alta de libro: user = owner que publicó el libro.
data class NewBookEvent(
    override val date: String,
    override val user: String,
    override val bookTitle: String,
) : ActivityEvent {
    override val typeEvent = ActivityEventType.BOOK_REGISTERED
}

// Reserva confirmada: user = lector que reservó.
data class NewReservationEvent(
    override val date: String,
    override val user: String,
    override val bookTitle: String,
) : ActivityEvent {
    override val typeEvent = ActivityEventType.RESERVATION
}

@DgsComponent
class ActivityEventTypeResolver {
    // Sin esto DGS no sabe resolver el __typename concreto de la interface.
    @DgsTypeResolver(name = "ActivityEvent")
    fun resolveType(event: ActivityEvent): String = when (event) {
        is NewBookEvent -> "NewBookEvent"
        is NewReservationEvent -> "NewReservationEvent"
    }
}
