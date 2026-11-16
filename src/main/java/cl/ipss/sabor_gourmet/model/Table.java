package cl.ipss.sabor_gourmet.model;

/**
 * Configuración persistida de una mesa y su capacidad para recibir personas.
 */
public record Table(
        Long id,
        Integer number,
        Integer capacity,
        Boolean available
) {
}
