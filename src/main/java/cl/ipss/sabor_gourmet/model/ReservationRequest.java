package cl.ipss.sabor_gourmet.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos necesarios para reservar. startTime define un turno fijo de 120 minutos;
 * por eso el contrato no recibe ni guarda un endTime.
 */
public record ReservationRequest(
        @NotNull(message = "customerId is required") Long customerId,
        @NotNull(message = "tableId is required") Long tableId,
        @NotBlank(message = "startTime is required") String startTime,
        @NotNull(message = "people is required") @Min(value = 1, message = "people must be positive") Integer people
) {
}
