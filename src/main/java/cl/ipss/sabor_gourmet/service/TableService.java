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
 * Aplica las reglas del inventario: capacidad, habilitación, límite de mesas
 * y bloqueo producido por reservas confirmadas.
 */
@Service
public class TableService {

    private static final String CONFIRMED = "CONFIRMED";
    private static final int MAX_TABLES = 10;

    private final RestaurantJsonRepository repository;

    public TableService(RestaurantJsonRepository repository) {
        this.repository = repository;
    }

    // El Repository normaliza el documento y devuelve la colección tables del archivo JSON.
    public List<Table> getAllTables() {
        return repository.read().getTables();
    }

    // La búsqueda se hace por el ID persistido, que es también el valor usado por las reservas.
    public Table getTable(Long id) {
        return repository.read().getTables().stream()
                .filter(table -> Objects.equals(table.id(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + id));
    }

    // Se consideran solo mesas habilitadas y se exige que ninguna reserva confirmada cruce el rango completo.
    // El rango puede ser un turno de 120 minutos o un día completo.
    public List<Table> getAvailableTables(String date, String startTime, Integer people) {
        if (people != null && people < 1) {
            throw new IllegalArgumentException("people must be greater than or equal to 1");
        }

        TimeRange requestedRange = TimeRange.resolve(date, startTime);
        var data = repository.read();
        // La capacidad se filtra después de resolver el período y antes de devolver las mesas libres.
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

    // Una reserva confirmada superpuesta bloquea la mesa; una cancelada no participa en el filtro.
    // Como se parte de la lista de mesas, cada mesa aparece como máximo una vez.
    public List<Table> getReservedTables(String date, String startTime) {
        TimeRange requestedRange = TimeRange.resolve(date, startTime);
        var data = repository.read();
        // Se recorre cada mesa una sola vez y se ignoran las reservas canceladas.
        return data.getTables().stream()
                .filter(table -> data.getReservations().stream()
                        .filter(reservation -> CONFIRMED.equals(reservation.status()))
                        .filter(reservation -> Objects.equals(reservation.tableId(), table.id()))
                        .anyMatch(reservation -> requestedRange.overlaps(
                                TimeRange.fromStart(reservation.startTime()))))
                .toList();
    }

    // El límite se valida dentro del Service y la operación completa se persiste mediante el Repository.
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

    public Table updateTable(Long id, TableRequest request) {
        return repository.update(data -> {
            int index = indexOf(data.getTables(), id);
            // No se permite reducir la capacidad si una reserva vigente dejaría de caber.
            validateActiveReservationsCapacity(data.getReservations(), id, request.capacity());
            Table updated = new Table(id, request.number(), request.capacity(), request.available());
            data.getTables().set(index, updated);
            return updated;
        });
    }

    // Una mesa referenciada no se elimina: así nunca quedan reservas apuntando a un ID inexistente.
    public void deleteTable(Long id) {
        repository.update(data -> {
            int index = indexOf(data.getTables(), id);
            boolean referenced = data.getReservations().stream()
                    .anyMatch(reservation -> Objects.equals(reservation.tableId(), id));
            if (referenced) {
                throw new ConflictException("Table is referenced by a reservation: " + id);
            }
            data.getTables().remove(index);
            return null;
        });
    }


    // Reducir capacidad solo es válido si ninguna reserva confirmada aún vigente quedaría sin espacio.
    private void validateActiveReservationsCapacity(List<Reservation> reservations, Long tableId, int capacity) {
        var now = TimeRange.currentDateTime();
        boolean invalid = reservations.stream()
                .filter(reservation -> CONFIRMED.equals(reservation.status()))
                .filter(reservation -> Objects.equals(reservation.tableId(), tableId))
                .filter(reservation -> TimeRange.fromStart(reservation.startTime()).end().isAfter(now))
                .anyMatch(reservation -> reservation.people() > capacity);

        if (invalid) {
            throw new ConflictException("Capacity cannot be lower than an active reservation");
        }
    }

    // Los métodos de actualización y eliminación usan el ID, no el número visible de la mesa.
    private int indexOf(List<Table> tables, Long id) {
        for (int index = 0; index < tables.size(); index++) {
            if (Objects.equals(tables.get(index).id(), id)) {
                return index;
            }
        }
        throw new ResourceNotFoundException("Table not found: " + id);
    }

    // El siguiente ID se calcula a partir del mayor ID existente para no reutilizar referencias.
    private Long nextId(List<Table> tables) {
        return tables.stream()
                .map(Table::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
