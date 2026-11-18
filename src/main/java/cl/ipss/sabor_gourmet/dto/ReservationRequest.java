package cl.ipss.sabor_gourmet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dto para la creación de una nueva reserva
 */
public record ReservationRequest(
        @NotNull(message = "ID del cliente es requerido") Long customerId,
        @NotNull(message = "ID de la mesa es requerido") Long tableId,
        @NotBlank(message = "Hora de inicio es requerida") String startTime,
        @NotNull(message = "Cantidad de personas es requerida") @Min(value = 1, message = "Cantidad de personas debe ser positiva") Integer people
) {
}
