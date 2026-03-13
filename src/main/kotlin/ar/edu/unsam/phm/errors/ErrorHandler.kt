package ar.edu.unsam.phm.errors

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ControllerAdvice
class GlobalExceptionHandler {

    // NotFoundException (HTTP 404 Not Found)
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<Map<String, Any>> {
        val errorResponse = mapOf(
            "status" to HttpStatus.NOT_FOUND.value(),
            "error" to "Recurso no encontrado",
            "detail" to (ex.message ?: "El elemento solicitado no existe."),
            "timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    // ConflictException (HTTP 409 Conflict)
    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException): ResponseEntity<Map<String, Any>> {
        val errorResponse = mapOf(
            "status" to HttpStatus.CONFLICT.value(),
            "error" to "Conflicto en la solicitud",
            "detail" to (ex.message ?: "La operación no se puede completar debido a un conflicto en los datos."),
            "timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    // BusinessException (HTTP 400 Bad Request)
    // Se usa 400 para errores de negocio (validación, reglas de negocio fallidas)
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<Map<String, Any>> {
        val errorResponse = mapOf(
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Regla de negocio no cumplida",
            "detail" to (ex.message ?: "La solicitud es incorrecta o no cumple una regla de negocio."),
            "timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // InternalException (HTTP 500 Internal Server Error)
    // Captura errores internos, RuntimeException es mas generico.
    @ExceptionHandler(InternalException::class)
    fun handleInternalException(ex: InternalException): ResponseEntity<Map<String, Any>> {
        val errorResponse = mapOf(
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Error interno del servidor",
            "detail" to (ex.message ?: "Ocurrió un error inesperado en el servidor."),
            "timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}