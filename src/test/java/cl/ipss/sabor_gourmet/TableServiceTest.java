package cl.ipss.sabor_gourmet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.dto.TableRequest;
import cl.ipss.sabor_gourmet.repository.CustomerRepository;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;
import cl.ipss.sabor_gourmet.repository.TableRepository;
import cl.ipss.sabor_gourmet.service.TableService;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TableServiceTest {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TableService tableService;

    private Long table1Id;
    private Long table3Id;
    private Long table4Id;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = customerRepository.save(new Customer(null, "Ana Pérez", "ana@example.com", "+56911111111"));

        List<Table> savedTables = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> tableRepository.save(new Table(null, i, i == 3 ? 2 : 4, true)))
                .toList();

        table1Id = savedTables.get(0).getId();
        table3Id = savedTables.get(2).getId();
        table4Id = savedTables.get(3).getId();

        reservationRepository.save(new Reservation(
                null,
                customer.getId(),
                table1Id,
                "01-01-2099T20:00",
                2,
                "CONFIRMED"
        ));
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
        List<Table> availableTables = tableService.getAvailableTables(null, "01-01-2099T20:30", null);

        assertEquals(9, availableTables.size());
        assertFalse(availableTables.stream().anyMatch(table -> table.id().equals(table1Id)));

        Table updated = tableService.updateTable(table1Id, new TableRequest(1, 6, true));

        assertEquals(6, updated.capacity());
    }

    @Test
    void filtersAvailableTablesByCapacity() {
        List<Table> availableTables = tableService.getAvailableTables("02-01-2099", null, 4);

        assertTrue(availableTables.stream().anyMatch(table -> table.id().equals(table4Id)));
        assertFalse(availableTables.stream().anyMatch(table -> table.id().equals(table3Id)));
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
        List<Table> reservedTables = tableService.getReservedTables(null, "01-01-2099T20:30");

        assertEquals(1, reservedTables.size());
        assertEquals(table1Id, reservedTables.get(0).id());
    }

    @Test
    void rejectsDateAndStartTimeTogether() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tableService.getReservedTables("01-01-2099", "01-01-2099T20:30")
        );
    }
}
