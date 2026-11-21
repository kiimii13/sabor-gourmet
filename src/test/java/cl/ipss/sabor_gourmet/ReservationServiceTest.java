package cl.ipss.sabor_gourmet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.dto.ReservationRequest;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.repository.CustomerRepository;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;
import cl.ipss.sabor_gourmet.repository.TableRepository;
import cl.ipss.sabor_gourmet.service.ReservationService;
import cl.ipss.sabor_gourmet.service.TableService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TableService tableService;

    private Long testCustomerId;
    private Long testTableId;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = customerRepository.save(new Customer(null, "Ana Pérez", "ana@example.com", "+56911111111"));
        Table table = tableRepository.save(new Table(null, 1, 4, true));

        testCustomerId = customer.getId();
        testTableId = table.getId();
    }

    @Test
    void rejectsOverlapAndPersistsCancellationWithoutLosingCollections() {
        ReservationRequest firstRequest = new ReservationRequest(
                testCustomerId,
                testTableId,
                "01-01-2099T19:00",
                2
        );
        Reservation created = reservationService.createReservation(firstRequest);

        ReservationRequest overlappingRequest = new ReservationRequest(
                testCustomerId,
                testTableId,
                "01-01-2099T20:30",
                2
        );
        assertThrows(ConflictException.class, () -> reservationService.createReservation(overlappingRequest));
        assertEquals(0, tableService.getAvailableTables(null, "01-01-2099T19:30", null).size());

        Reservation cancelled = reservationService.cancelReservation(created.id());

        assertEquals("CANCELLED", cancelled.status());
        assertEquals(1, tableService.getAvailableTables(null, "01-01-2099T19:30", null).size());
        assertEquals(1, customerRepository.count());
        assertEquals(1, tableRepository.count());
        assertEquals("CANCELLED", reservationRepository.findById(created.id()).get().status());
    }

    @Test
    void rejectsMissingReferencesAndInsufficientCapacity() {
        ReservationRequest missingCustomer = new ReservationRequest(
                99999L,
                testTableId,
                "01-01-2099T19:00",
                2
        );
        assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(missingCustomer));

        ReservationRequest tooManyPeople = new ReservationRequest(
                testCustomerId,
                testTableId,
                "01-01-2099T19:00",
                5
        );
        assertThrows(ConflictException.class, () -> reservationService.createReservation(tooManyPeople));
    }

    @Test
    void dateFilterReturnsConfirmedReservationsThatCrossTheDay() {
        reservationService.createReservation(new ReservationRequest(
                testCustomerId,
                testTableId,
                "01-01-2099T19:00",
                2
        ));

        assertEquals(1, reservationService.getReservations("01-01-2099").size());
    }

    @Test
    void crossMidnightReservationAppearsInBothDays() {
        reservationService.createReservation(new ReservationRequest(
                testCustomerId,
                testTableId,
                "01-01-2099T23:30",
                2
        ));

        assertEquals(1, reservationService.getReservations("01-01-2099").size());
        assertEquals(1, reservationService.getReservations("02-01-2099").size());
        assertEquals(1, tableService.getReservedTables("01-01-2099", null).size());
        assertEquals(1, tableService.getReservedTables("02-01-2099", null).size());
    }

    @Test
    void allowsEditingReservationWithoutSelfConflict() {
        ReservationRequest request = new ReservationRequest(
                testCustomerId,
                testTableId,
                "01-01-2099T19:00",
                2
        );
        Reservation created = reservationService.createReservation(request);

        Reservation updated = reservationService.updateReservation(created.id(), request);

        assertEquals(created.id(), updated.id());
        assertEquals(request.startTime(), updated.startTime());
    }
}
