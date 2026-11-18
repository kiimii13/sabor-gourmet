package cl.ipss.sabor_gourmet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dto para la creación de un nuevo cliente
 */
public record CustomerRequest(
        @NotBlank(message = "Nombre es requerido") String name,
        @NotBlank(message = "Correo electrónico es requerido") @Email(message = "Correo electrónico debe ser válido") String email,
        @NotBlank(message = "Teléfono es requerido") String phone
) {
}
