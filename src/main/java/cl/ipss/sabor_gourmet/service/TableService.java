package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.dto.TableRequest;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;
import cl.ipss.sabor_gourmet.repository.TableRepository;

import java.util.List;
import java.util.Objects;

/**
 * Servicio para las mesas respaldado por Spring Data JPA.
 */
@Service
@Transactional
public class TableService {

    private static final String CONFIRMED = "CONFIRMED";
    private static final int MAX_TABLES = 10;

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public TableService(TableRepository tableRepository, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    // Obtiene todas las mesas
    @Transactional(readOnly = true)
    public List<Table> getAllTables() {
        return tableRepository.findAll();
    }

    // Obtiene una mesa por su ID
    @Transactional(readOnly = true)
    public Table getTable(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada: " + id));
    }

    // Obtiene las mesas disponibles para una fecha, hora y cantidad de personas
    @Transactional(readOnly = true)
    public List<Table> getAvailableTables(String date, String startTime, Integer people) {
        if (people != null && people < 1) {
            throw new IllegalArgumentException("La cantidad de personas debe ser mayor o igual a 1");
        }

        TimeRange requestedRange = TimeRange.resolve(date, startTime);
        List<Table> allTables = tableRepository.findAll();
        List<Reservation> confirmedReservations = reservationRepository.findByStatus(CONFIRMED);

        return allTables.stream()
                .filter(table -> Boolean.TRUE.equals(table.available()))
                .filter(table -> people == null || table.capacity() >= people)
                .filter(table -> confirmedReservations.stream()
                        .filter(reservation -> Objects.equals(reservation.tableId(), table.id()))
                        .noneMatch(reservation -> requestedRange.overlaps(
                                TimeRange.fromStart(reservation.startTime()))))
                .toList();
    }

    // Obtiene las mesas reservadas para una fecha y hora
    @Transactional(readOnly = true)
    public List<Table> getReservedTables(String date, String startTime) {
        TimeRange requestedRange = TimeRange.resolve(date, startTime);
        List<Table> allTables = tableRepository.findAll();
        List<Reservation> confirmedReservations = reservationRepository.findByStatus(CONFIRMED);

        return allTables.stream()
                .filter(table -> confirmedReservations.stream()
                        .filter(reservation -> Objects.equals(reservation.tableId(), table.id()))
                        .anyMatch(reservation -> requestedRange.overlaps(
                                TimeRange.fromStart(reservation.startTime()))))
                .toList();
    }

    // Crea una nueva mesa (máximo 10)
    public Table createTable(TableRequest request) {
        if (tableRepository.count() >= MAX_TABLES) {
            throw new ConflictException("No se pueden registrar más de 10 mesas");
        }

        Table table = new Table(null, request.number(), request.capacity(), request.available());
        return tableRepository.save(table);
    }

    // Actualiza una mesa existente
    public Table updateTable(Long id, TableRequest request) {
        Table table = getTable(id);
        List<Reservation> tableReservations = reservationRepository.findByTableIdAndStatus(id, CONFIRMED);
        validateActiveReservationsCapacity(tableReservations, id, request.capacity());

        table.setNumber(request.number());
        table.setCapacity(request.capacity());
        table.setAvailable(request.available() != null ? request.available() : true);
        return tableRepository.save(table);
    }

    // Elimina una mesa existente
    public void deleteTable(Long id) {
        Table table = getTable(id);
        if (reservationRepository.existsByTableId(id)) {
            throw new ConflictException("Mesa referenciada por una reserva: " + id);
        }
        tableRepository.delete(table);
    }

    // Valida que la capacidad no sea menor que una reserva activa en la mesa
    private void validateActiveReservationsCapacity(List<Reservation> reservations, Long tableId, int capacity) {
        var now = TimeRange.currentDateTime();
        boolean invalid = reservations.stream()
                .filter(reservation -> TimeRange.fromStart(reservation.startTime()).end().isAfter(now))
                .anyMatch(reservation -> reservation.people() > capacity);

        if (invalid) {
            throw new ConflictException("La capacidad no puede ser menor que una reserva activa");
        }
    }
}
