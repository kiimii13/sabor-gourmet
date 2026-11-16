package cl.ipss.sabor_gourmet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce excepciones de validación y negocio a respuestas HTTP JSON coherentes.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // Jakarta Validation informa los errores básicos de los cuerpos recibidos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(new ApiError("Request validation failed"));
    }

    // Fechas, horas y cuerpos JSON inválidos se responden como 400 Bad Request.
    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    // Un ID inexistente se transforma en 404 Not Found.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(exception.getMessage()));
    }

    // Los conflictos de capacidad, límite o solapamiento se exponen como 409 Conflict.
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(exception.getMessage()));
    }

    public record ApiError(String message) {
    }
}
