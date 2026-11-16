package cl.ipss.sabor_gourmet.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada del cliente. Las anotaciones hacen que el Controller rechace
 * campos vacíos o un correo con formato inválido antes de llegar al Service.
 */
public record CustomerRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        @NotBlank(message = "phone is required") String phone
) {
}
