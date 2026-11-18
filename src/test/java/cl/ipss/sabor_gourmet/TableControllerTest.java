package cl.ipss.sabor_gourmet;

import cl.ipss.sabor_gourmet.controller.TableController;
import cl.ipss.sabor_gourmet.model.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TableControllerTest {

    @Autowired
    private TableController tableController;

    @Test
    void getTablesReturnsTablesFromJsonFile() {
        ResponseEntity<List<Table>> response = tableController.getTables();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        assertEquals(4, response.getBody().get(0).capacity());
    }

    @Test
    void currentDayWithoutParametersFindsTodaysReservation() {
        ResponseEntity<List<Table>> reserved = tableController.getReservedTables(null, null);
        ResponseEntity<List<Table>> available = tableController.getAvailableTables(null, null, null);

        assertTrue(reserved.getBody().stream().anyMatch(table -> table.id() == 2L));
        assertFalse(available.getBody().stream().anyMatch(table -> table.id() == 2L));
    }

    @Test
    void dateFilterSeparatesTodayFromNovemberTwentieth() {
        ResponseEntity<List<Table>> todayReserved = tableController.getReservedTables("16-11-2026", null);
        ResponseEntity<List<Table>> todayAvailable = tableController.getAvailableTables("16-11-2026", null, null);
        ResponseEntity<List<Table>> futureReserved = tableController.getReservedTables("20-11-2026", null);
        ResponseEntity<List<Table>> futureAvailable = tableController.getAvailableTables("20-11-2026", null, null);

        assertTrue(todayReserved.getBody().stream().anyMatch(table -> table.id() == 2L));
        assertTrue(todayAvailable.getBody().stream().anyMatch(table -> table.id() == 1L));
        assertTrue(futureReserved.getBody().stream().anyMatch(table -> table.id() == 1L));
        assertTrue(futureAvailable.getBody().stream().anyMatch(table -> table.id() == 2L));
    }

    @Test
    void filtersAvailableTablesByCapacity() {
        ResponseEntity<List<Table>> response = tableController.getAvailableTables(
                "01-01-2099",
                null,
                4
        );

        assertTrue(response.getBody().stream().anyMatch(table -> table.id() == 4L));
        assertFalse(response.getBody().stream().anyMatch(table -> table.id() == 3L));
    }

    @Test
    void rejectsInvalidPeople() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tableController.getAvailableTables(null, null, 0)
        );
    }

    @Test
    void dateAndStartTimeCannotBeCombined() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tableController.getAvailableTables("16-11-2026", "16-11-2026T20:00", null)
        );
    }
}
