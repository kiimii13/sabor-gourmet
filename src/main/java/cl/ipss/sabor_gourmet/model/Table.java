package cl.ipss.sabor_gourmet.model;

/**
 * Modelo de mesa
 */
public record Table(
        Long id,
        Integer number,
        Integer capacity,
        Boolean available
) {
}
