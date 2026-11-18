package cl.ipss.sabor_gourmet.model;

/**
 * Modelo de reserva
 */
public record Reservation(
        Long id,
        Long customerId,
        Long tableId,
        String startTime,
        Integer people,
        String status
) {
}
