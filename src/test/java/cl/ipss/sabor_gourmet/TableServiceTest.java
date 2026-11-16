package cl.ipss.sabor_gourmet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.RestaurantData;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.model.TableRequest;
import cl.ipss.sabor_gourmet.repository.RestaurantJsonRepository;
import cl.ipss.sabor_gourmet.service.TableService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableServiceTest {

    @TempDir
    Path temporaryDirectory;

    private TableService tableService;

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Path dataFile = temporaryDirectory.resolve("sabor-gourmet.json");
        List<Table> tables = IntStream.rangeClosed(1, 10)
                .mapToObj(id -> new Table((long) id, id, id == 3 ? 2 : 4, true))
                .toList();
        RestaurantData initialData = new RestaurantData(
                List.of(new Customer(1L, "Ana Pérez", "ana@example.com", "+56911111111")),
                tables,
                List.of(new Reservation(
                        1L,
                        1L,
                        1L,
                        "2099-01-01T20:00",
                        2,
                        "CONFIRMED"
                ))
        );
        Files.writeString(dataFile, objectMapper.writeValueAsString(initialData));
        tableService = new TableService(new RestaurantJsonRepository(objectMapper, dataFile));
    }

    @Test
    void rejectsAnEleventhTable() {
        assertThrows(
                ConflictException.class,
                () -> tableService.createTable(new TableRequest(11, 4, true))
        );
    }

    @Test
    void excludesReservedTableAndKeepsExistingTableUpdatable() {
        List<Table> availableTables = tableService.getAvailableTables(null, "2099-01-01T20:30", null);

        assertEquals(9, availableTables.size());
        assertFalse(availableTables.stream().anyMatch(table -> table.id() == 1L));

        Table updated = tableService.updateTable(1L, new TableRequest(1, 6, true));

        assertEquals(6, updated.capacity());
    }

    @Test
    void filtersAvailableTablesByCapacity() {
        List<Table> availableTables = tableService.getAvailableTables("2099-01-02", null, 4);

        assertTrue(availableTables.stream().anyMatch(table -> table.id() == 4L));
        assertFalse(availableTables.stream().anyMatch(table -> table.id() == 3L));
    }

    @Test
    void rejectsInvalidPeople() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tableService.getAvailableTables(null, null, 0)
        );
    }

    @Test
    void returnsEachReservedTableOnce() {
        List<Table> reservedTables = tableService.getReservedTables(null, "2099-01-01T20:30");

        assertEquals(1, reservedTables.size());
        assertEquals(1L, reservedTables.get(0).id());
    }

    @Test
    void rejectsDateAndStartTimeTogether() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tableService.getReservedTables("2099-01-01", "2099-01-01T20:30")
        );
    }
}
