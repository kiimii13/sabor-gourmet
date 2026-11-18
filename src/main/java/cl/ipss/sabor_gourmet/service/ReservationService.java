package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.dto.ReservationRequest;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.repository.RestaurantJsonRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Coordina las reservas: valida referencias, capacidad, fechas y solapamientos
 * antes de conservar los cambios en el documento JSON.
 */
@Service
public class ReservationService {

    private static final String CONFIRMED = "CONFIRMED";
    private static final String CANCELLED = "CANCELLED";

    private final RestaurantJsonRepository repository;

    public ReservationService(RestaurantJsonRepository repository) {
        this.repository = repository;
    }

    // Con date se construye un día completo y se devuelven solo reservas confirmadas que lo cruzan.
    public List<Reservation> getReservations(String date) {
        if (date == null) {
            return getCurrentReservations();
        }

        TimeRange requestedDay = TimeRange.fromDate(date);
        return repository.read().getReservations().stream()
                .filter(reservation -> CONFIRMED.equals(reservation.status()))
                .filter(reservation -> requestedDay.overlaps(
                        TimeRange.fromStart(reservation.startTime())))
                .toList();
    }

    // Se mantiene el criterio original para la consulta sin date: confirmada y no terminada.
    public List<Reservation> getCurrentReservations() {
        LocalDateTime now = TimeRange.currentDateTime();
        // Sin filtro, se mantiene el listado de reservas confirmadas cuyo turno todavía no ha terminado.
        return repository.read().getReservations().stream()
                .filter(reservation -> isCurrent(reservation, now))
                .toList();
    }

    // La consulta individual mantiene la reserva completa, incluidos sus IDs y su estado.
    public Reservation getReservation(Long id) {
        return repository.read().getReservations().stream()
                .filter(reservation -> Objects.equals(reservation.id(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    // La creación valida referencias, capacidad y solapamiento antes de agregar la reserva a la colección.
    public Reservation createReservation(ReservationRequest request) {
        TimeRange range = validateStartTime(request);
        // La reserva usa siempre el turno calculado de 120 minutos desde startTime.
        return repository.update(data -> {
            validateReferences(data.getCustomers(), data.getTables(), request);
            validateTableCapacity(data.getTables(), request);
            validateOverlap(data.getReservations(), request.tableId(), range, null);

            Reservation reservation = new Reservation(
                    nextId(data.getReservations()),
                    request.customerId(),
                    request.tableId(),
                    TimeRange.formatStart(range.start()),
                    request.people(),
                    CONFIRMED
            );
            data.getReservations().add(reservation);
            return reservation;
        });
    }

    // La edición repite las validaciones con el nuevo turno y conserva el mismo ID de reserva.
    public Reservation updateReservation(Long id, ReservationRequest request) {
        TimeRange range = validateStartTime(request);
        // Al modificar se recalcula el turno y se excluye la reserva actual del control de solapamiento.
        return repository.update(data -> {
            int index = indexOf(data.getReservations(), id);
            Reservation existing = data.getReservations().get(index);
            if (!CONFIRMED.equals(existing.status())) {
                throw new ConflictException("Cancelled reservations cannot be modified: " + id);
            }

            validateReferences(data.getCustomers(), data.getTables(), request);
            validateTableCapacity(data.getTables(), request);
            validateOverlap(data.getReservations(), request.tableId(), range, id);

            Reservation updated = new Reservation(
                    id,
                    request.customerId(),
                    request.tableId(),
                    TimeRange.formatStart(range.start()),
                    request.people(),
                    CONFIRMED
            );
            data.getReservations().set(index, updated);
            return updated;
        });
    }

    // Se conserva el registro y solo cambia el estado; por eso el historial y sus IDs siguen disponibles.
    public Reservation cancelReservation(Long id) {
        return repository.update(data -> {
            int index = indexOf(data.getReservations(), id);
            Reservation reservation = data.getReservations().get(index);
            if (CANCELLED.equals(reservation.status())) {
                return reservation;
            }

            // La cancelación conserva el historial y libera la mesa sin eliminar las referencias.
            Reservation cancelled = new Reservation(
                    reservation.id(),
                    reservation.customerId(),
                    reservation.tableId(),
                    reservation.startTime(),
                    reservation.people(),
                    CANCELLED
            );
            data.getReservations().set(index, cancelled);
            return cancelled;
        });
    }

    // TimeRange valida el formato y calcula el término fijo; además se rechazan horas pasadas.
    private TimeRange validateStartTime(ReservationRequest request) {
        TimeRange range = TimeRange.fromStart(request.startTime());
        if (range.start().isBefore(TimeRange.currentDateTime())) {
            throw new IllegalArgumentException("startTime cannot be in the past");
        }
        return range;
    }

    // Las reservas no copian objetos completos: enlazan las colecciones mediante customerId y tableId.
    private void validateReferences(List<Customer> customers, List<Table> tables, ReservationRequest request) {
        boolean customerExists = customers.stream()
                .anyMatch(customer -> Objects.equals(customer.id(), request.customerId()));
        if (!customerExists) {
            throw new ResourceNotFoundException("Customer not found: " + request.customerId());
        }

        boolean tableExists = tables.stream()
                .anyMatch(table -> Objects.equals(table.id(), request.tableId()));
        if (!tableExists) {
            throw new ResourceNotFoundException("Table not found: " + request.tableId());
        }
    }

    // Aquí se valida la configuración de la mesa y la cantidad de personas solicitada.
    private void validateTableCapacity(List<Table> tables, ReservationRequest request) {
        Table table = tables.stream()
                .filter(candidate -> Objects.equals(candidate.id(), request.tableId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + request.tableId()));

        if (!Boolean.TRUE.equals(table.available())) {
            throw new ConflictException("Table is not available: " + request.tableId());
        }
        if (table.capacity() < request.people()) {
            throw new ConflictException("Table capacity is insufficient: " + request.tableId());
        }
    }

    // Solo se comparan reservas de la misma mesa y con estado CONFIRMED.
    // ignoredId evita que una reserva se considere en conflicto consigo misma al editarla.
    private void validateOverlap(List<Reservation> reservations, Long tableId, TimeRange requestedRange, Long ignoredId) {
        // Solo las reservas CONFIRMED bloquean la mesa; CANCELLED queda fuera de este cálculo.
        boolean overlaps = reservations.stream()
                .filter(reservation -> CONFIRMED.equals(reservation.status()))
                .filter(reservation -> Objects.equals(reservation.tableId(), tableId))
                .filter(reservation -> !Objects.equals(reservation.id(), ignoredId))
                .anyMatch(reservation -> requestedRange.overlaps(
                        TimeRange.fromStart(reservation.startTime())));

        if (overlaps) {
            throw new ConflictException("Table is already reserved in that time range: " + tableId);
        }
    }


    // Sin date, una reserva es vigente si está confirmada y su término calculado aún no pasó.
    private boolean isCurrent(Reservation reservation, LocalDateTime now) {
        return CONFIRMED.equals(reservation.status())
                && TimeRange.fromStart(reservation.startTime()).end().isAfter(now);
    }

    // La búsqueda por ID permite modificar o cancelar exactamente una reserva persistida.
    private int indexOf(List<Reservation> reservations, Long id) {
        for (int index = 0; index < reservations.size(); index++) {
            if (Objects.equals(reservations.get(index).id(), id)) {
                return index;
            }
        }
        throw new ResourceNotFoundException("Reservation not found: " + id);
    }

    // Los nuevos IDs se generan después del mayor ID guardado y preservan las referencias existentes.
    private Long nextId(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
