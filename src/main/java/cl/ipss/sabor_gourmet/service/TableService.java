package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.dto.TableRequest;
import cl.ipss.sabor_gourmet.repository.RestaurantJsonRepository;

import java.util.List;
import java.util.Objects;

/**
 * Servicio para las mesas
 */
@Service
public class TableService {

    private static final String CONFIRMED = "CONFIRMED";
    private static final int MAX_TABLES = 10;

    private final RestaurantJsonRepository repository;

    public TableService(RestaurantJsonRepository repository) {
        this.repository = repository;
    }

    // Obtiene todas las mesas
    public List<Table> getAllTables() {
        return repository.read().getTables();
    }

    // Obtiene una mesa por su ID
    public Table getTable(Long id) {
        return repository.read().getTables().stream()
                .filter(table -> Objects.equals(table.id(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada: " + id));
    }

    // Obtiene las mesas disponibles para una fecha, hora y cantidad de personas
    public List<Table> getAvailableTables(String date, String startTime, Integer people) {
        if (people != null && people < 1) {
            throw new IllegalArgumentException("La cantidad de personas debe ser mayor o igual a 1");
        }

        TimeRange requestedRange = TimeRange.resolve(date, startTime);
        var data = repository.read();
        return data.getTables().stream()
                .filter(table -> Boolean.TRUE.equals(table.available()))
                .filter(table -> people == null || table.capacity() >= people)
                .filter(table -> data.getReservations().stream()
                        .filter(reservation -> CONFIRMED.equals(reservation.status()))
                        .filter(reservation -> Objects.equals(reservation.tableId(), table.id()))
                        .noneMatch(reservation -> requestedRange.overlaps(
                                TimeRange.fromStart(reservation.startTime()))))
                .toList();
    }

    // Obtiene las mesas reservadas para una fecha y hora
    public List<Table> getReservedTables(String date, String startTime) {
        TimeRange requestedRange = TimeRange.resolve(date, startTime);
        var data = repository.read();
        return data.getTables().stream()
                .filter(table -> data.getReservations().stream()
                        .filter(reservation -> CONFIRMED.equals(reservation.status()))
                        .filter(reservation -> Objects.equals(reservation.tableId(), table.id()))
                        .anyMatch(reservation -> requestedRange.overlaps(
                                TimeRange.fromStart(reservation.startTime()))))
                .toList();
    }

    // Crea una nueva mesa
    public Table createTable(TableRequest request) {
        return repository.update(data -> {
            if (data.getTables().size() >= MAX_TABLES) {
                throw new ConflictException("No se pueden registrar más de 10 mesas");
            }

            Table table = new Table(nextId(data.getTables()), request.number(), request.capacity(), request.available());
            data.getTables().add(table);
            return table;
        });
    }

    // Actualiza una mesa existente
    public Table updateTable(Long id, TableRequest request) {
        return repository.update(data -> {
            int index = indexOf(data.getTables(), id);
            validateActiveReservationsCapacity(data.getReservations(), id, request.capacity());
            Table updated = new Table(id, request.number(), request.capacity(), request.available());
            data.getTables().set(index, updated);
            return updated;
        });
    }

    // Elimina una mesa existente
    public void deleteTable(Long id) {
        repository.update(data -> {
            int index = indexOf(data.getTables(), id);
            boolean referenced = data.getReservations().stream()
                    .anyMatch(reservation -> Objects.equals(reservation.tableId(), id));
            if (referenced) {
                throw new ConflictException("Mesa referenciada por una reserva: " + id);
            }
            data.getTables().remove(index);
            return null;
        });
    }


    // Valida la capacidad de la mesa
    private void validateActiveReservationsCapacity(List<Reservation> reservations, Long tableId, int capacity) {
        var now = TimeRange.currentDateTime();
        boolean invalid = reservations.stream()
                .filter(reservation -> CONFIRMED.equals(reservation.status()))
                .filter(reservation -> Objects.equals(reservation.tableId(), tableId))
                .filter(reservation -> TimeRange.fromStart(reservation.startTime()).end().isAfter(now))
                .anyMatch(reservation -> reservation.people() > capacity);

        if (invalid) {
            throw new ConflictException("La capacidad no puede ser menor que una reserva activa");
        }
    }

    // Obtiene el índice de una mesa por su ID
    private int indexOf(List<Table> tables, Long id) {
        for (int index = 0; index < tables.size(); index++) {
            if (Objects.equals(tables.get(index).id(), id)) {
                return index;
            }
        }
        throw new ResourceNotFoundException("Mesa no encontrada: " + id);
    }

    // Obtiene el siguiente ID disponible para una mesa
    private Long nextId(List<Table> tables) {
        return tables.stream()
                .map(Table::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
