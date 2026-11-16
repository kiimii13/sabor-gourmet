package cl.ipss.sabor_gourmet.model;

/**
 * Reserva persistida. Sus IDs mantienen la relación con un cliente y una mesa;
 * el término del turno se calcula, no se almacena.
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
