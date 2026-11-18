package cl.ipss.sabor_gourmet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Dto para la creación o actualización de una mesa
 */
public record TableRequest(
        @NotNull(message = "Número de mesa es requerido") @Min(value = 1, message = "Número de mesa debe ser positivo") Integer number,
        @NotNull(message = "Capacidad es requerida") @Min(value = 1, message = "Capacidad debe ser positiva") Integer capacity,
        @NotNull(message = "Disponible es requerido") Boolean available
) {
}
