package cl.ipss.sabor_gourmet.model;

/**
 * Modelo de cliente
 */
public record Customer(
        Long id,
        String name,
        String email,
        String phone
) {
}
