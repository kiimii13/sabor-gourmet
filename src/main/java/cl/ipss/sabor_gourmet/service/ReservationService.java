package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.dto.ReservationRequest;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.repository.CustomerRepository;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;
import cl.ipss.sabor_gourmet.repository.TableRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Coordina las reservas: valida referencias, capacidad, fechas y solapamientos
 * utilizando repositorios Spring Data JPA para todas las operaciones de base de datos.
 */
@Service
@Transactional
public class ReservationService {

    private static final String CONFIRMED = "CONFIRMED";
    private static final String CANCELLED = "CANCELLED";

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final TableRepository tableRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              CustomerRepository customerRepository,
                              TableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.tableRepository = tableRepository;
    }

    // Con date se construye un día completo y se devuelven solo reservas confirmadas que lo cruzan.
    @Transactional(readOnly = true)
    public List<Reservation> getReservations(String date) {
        if (date == null) {
            return getCurrentReservations();
        }

        TimeRange requestedDay = TimeRange.fromDate(date);
        return reservationRepository.findByStatus(CONFIRMED).stream()
                .filter(reservation -> requestedDay.overlaps(
                        TimeRange.fromStart(reservation.startTime())))
                .toList();
    }

    // Se mantiene el criterio original para la consulta sin date: confirmada y no terminada.
    @Transactional(readOnly = true)
    public List<Reservation> getCurrentReservations() {
        LocalDateTime now = TimeRange.currentDateTime();
        return reservationRepository.findByStatus(CONFIRMED).stream()
                .filter(reservation -> isCurrent(reservation, now))
                .toList();
    }

    // La consulta individual mantiene la reserva completa, incluidos sus IDs y su estado.
    @Transactional(readOnly = true)
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    // La creación valida referencias, capacidad y solapamiento antes de guardar la entidad en la base de datos.
    public Reservation createReservation(ReservationRequest request) {
        TimeRange range = validateStartTime(request);
        validateReferences(request);
        validateTableCapacity(request);
        validateOverlap(request.tableId(), range, null);

        Reservation reservation = new Reservation(
                null,
                request.customerId(),
                request.tableId(),
                TimeRange.formatStart(range.start()),
                request.people(),
                CONFIRMED
        );
        return reservationRepository.save(reservation);
    }

    // La edición repite las validaciones con el nuevo turno y conserva el mismo ID de reserva.
    public Reservation updateReservation(Long id, ReservationRequest request) {
        TimeRange range = validateStartTime(request);
        Reservation existing = getReservation(id);

        if (!CONFIRMED.equals(existing.status())) {
            throw new ConflictException("Cancelled reservations cannot be modified: " + id);
        }

        validateReferences(request);
        validateTableCapacity(request);
        validateOverlap(request.tableId(), range, id);

        existing.setCustomerId(request.customerId());
        existing.setTableId(request.tableId());
        existing.setStartTime(TimeRange.formatStart(range.start()));
        existing.setPeople(request.people());
        existing.setStatus(CONFIRMED);

        return reservationRepository.save(existing);
    }

    // Se conserva el registro y solo cambia el estado a CANCELLED; libera la mesa manteniendo el historial.
    public Reservation cancelReservation(Long id) {
        Reservation reservation = getReservation(id);
        if (CANCELLED.equals(reservation.status())) {
            return reservation;
        }

        reservation.setStatus(CANCELLED);
        return reservationRepository.save(reservation);
    }

    // TimeRange valida el formato y calcula el término fijo; además se rechazan horas pasadas.
    private TimeRange validateStartTime(ReservationRequest request) {
        TimeRange range = TimeRange.fromStart(request.startTime());
        if (range.start().isBefore(TimeRange.currentDateTime())) {
            throw new IllegalArgumentException("startTime cannot be in the past");
        }
        return range;
    }

    // Las reservas comprueban que existan el cliente y la mesa en la base de datos.
    private void validateReferences(ReservationRequest request) {
        if (!customerRepository.existsById(request.customerId())) {
            throw new ResourceNotFoundException("Customer not found: " + request.customerId());
        }
        if (!tableRepository.existsById(request.tableId())) {
            throw new ResourceNotFoundException("Table not found: " + request.tableId());
        }
    }

    // Aquí se valida la configuración de la mesa y la cantidad de personas solicitada.
    private void validateTableCapacity(ReservationRequest request) {
        Table table = tableRepository.findById(request.tableId())
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
    private void validateOverlap(Long tableId, TimeRange requestedRange, Long ignoredId) {
        List<Reservation> tableReservations = reservationRepository.findByTableIdAndStatus(tableId, CONFIRMED);
        boolean overlaps = tableReservations.stream()
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
}
