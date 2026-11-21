package cl.ipss.sabor_gourmet;

import cl.ipss.sabor_gourmet.controller.TableController;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.config.DataInitializer;
import cl.ipss.sabor_gourmet.repository.CustomerRepository;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;
import cl.ipss.sabor_gourmet.repository.TableRepository;
import cl.ipss.sabor_gourmet.service.TimeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TableControllerTest {

    @Autowired
    private TableController tableController;

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long table1Id;
    private Long table2Id;
    private Long table3Id;
    private Long table4Id;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        customerRepository.deleteAll();
        dataInitializer.run();

        table1Id = tableRepository.findByNumber(1).map(Table::getId).orElseThrow();
        table2Id = tableRepository.findByNumber(2).map(Table::getId).orElseThrow();
        table3Id = tableRepository.findByNumber(3).map(Table::getId).orElseThrow();
        table4Id = tableRepository.findByNumber(4).map(Table::getId).orElseThrow();
    }

    @Test
    void getTablesReturnsTablesFromDatabase() {
        ResponseEntity<List<Table>> response = tableController.getTables();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody().size());
        assertEquals(table1Id, response.getBody().get(0).id());
        assertEquals(4, response.getBody().get(0).capacity());
    }

    @Test
    void currentDayWithoutParametersFindsTodaysReservation() {
        // Asegurar una reserva para el día actual
        String todayTime = TimeRange.formatStart(TimeRange.currentDateTime().plusHours(1).withMinute(0));
        reservationRepository.save(new Reservation(null, 1L, table2Id, todayTime, 2, "CONFIRMED"));

        ResponseEntity<List<Table>> reserved = tableController.getReservedTables(null, null);
        ResponseEntity<List<Table>> available = tableController.getAvailableTables(null, null, null);

        assertTrue(reserved.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table2Id)));
        assertFalse(available.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table2Id)));
    }

    @Test
    void dateFilterSeparatesTodayFromNovemberTwentieth() {
        ResponseEntity<List<Table>> todayReserved = tableController.getReservedTables("16-11-2026", null);
        ResponseEntity<List<Table>> todayAvailable = tableController.getAvailableTables("16-11-2026", null, null);
        ResponseEntity<List<Table>> futureReserved = tableController.getReservedTables("20-11-2026", null);
        ResponseEntity<List<Table>> futureAvailable = tableController.getAvailableTables("20-11-2026", null, null);

        assertTrue(todayReserved.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table2Id)));
        assertTrue(todayAvailable.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table1Id)));
        assertTrue(futureReserved.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table1Id)));
        assertTrue(futureAvailable.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table2Id)));
    }

    @Test
    void filtersAvailableTablesByCapacity() {
        ResponseEntity<List<Table>> response = tableController.getAvailableTables(
                "01-01-2099",
                null,
                4
        );

        assertTrue(response.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table4Id)));
        assertFalse(response.getBody().stream().anyMatch(table -> Objects.equals(table.id(), table3Id)));
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
