package cl.ipss.sabor_gourmet.model;

/**
 * Cliente identificado por un ID que las reservas guardan en customerId.
 */
public record Customer(
        Long id,
        String name,
        String email,
        String phone
) {
}
