package cl.ipss.sabor_gourmet.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Datos que el personal puede enviar para crear o actualizar una mesa.
 * La validación básica ocurre en el Controller mediante Jakarta Validation.
 */
public record TableRequest(
        @NotNull(message = "number is required") @Min(value = 1, message = "number must be positive") Integer number,
        @NotNull(message = "capacity is required") @Min(value = 1, message = "capacity must be positive") Integer capacity,
        @NotNull(message = "available is required") Boolean available
) {
}
