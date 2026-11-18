package cl.ipss.sabor_gourmet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.dto.ReservationRequest;
import cl.ipss.sabor_gourmet.model.RestaurantData;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.repository.RestaurantJsonRepository;
import cl.ipss.sabor_gourmet.service.ReservationService;
import cl.ipss.sabor_gourmet.service.TableService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationServiceTest {

    @TempDir
    Path temporaryDirectory;

    private RestaurantJsonRepository repository;
    private ReservationService reservationService;
    private TableService tableService;

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Path dataFile = temporaryDirectory.resolve("sabor-gourmet.json");
        RestaurantData initialData = new RestaurantData(
                List.of(new Customer(1L, "Ana Pérez", "ana@example.com", "+56911111111")),
                List.of(new Table(1L, 1, 4, true)),
                List.of()
        );
        Files.writeString(dataFile, objectMapper.writeValueAsString(initialData));

        repository = new RestaurantJsonRepository(objectMapper, dataFile);
        reservationService = new ReservationService(repository);
        tableService = new TableService(repository);
    }

    @Test
    void rejectsOverlapAndPersistsCancellationWithoutLosingCollections() {
        ReservationRequest firstRequest = new ReservationRequest(
                1L,
                1L,
                "01-01-2099T19:00",
                2
        );
        Reservation created = reservationService.createReservation(firstRequest);

        ReservationRequest overlappingRequest = new ReservationRequest(
                1L,
                1L,
                "01-01-2099T20:30",
                2
        );
        assertThrows(ConflictException.class, () -> reservationService.createReservation(overlappingRequest));
        assertEquals(0, tableService.getAvailableTables(null, "01-01-2099T19:30", null).size());

        Reservation cancelled = reservationService.cancelReservation(created.id());

        assertEquals("CANCELLED", cancelled.status());
        assertEquals(1, tableService.getAvailableTables(null, "01-01-2099T19:30", null).size());
        assertEquals(1, repository.read().getCustomers().size());
        assertEquals(1, repository.read().getTables().size());
        assertEquals("CANCELLED", repository.read().getReservations().get(0).status());
    }

    @Test
    void rejectsMissingReferencesAndInsufficientCapacity() {
        ReservationRequest missingCustomer = new ReservationRequest(
                99L,
                1L,
                "01-01-2099T19:00",
                2
        );
        assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(missingCustomer));

        ReservationRequest tooManyPeople = new ReservationRequest(
                1L,
                1L,
                "01-01-2099T19:00",
                5
        );
        assertThrows(ConflictException.class, () -> reservationService.createReservation(tooManyPeople));
    }

    @Test
    void dateFilterReturnsConfirmedReservationsThatCrossTheDay() {
        reservationService.createReservation(new ReservationRequest(
                1L,
                1L,
                "01-01-2099T19:00",
                2
        ));

        assertEquals(1, reservationService.getReservations("01-01-2099").size());
    }

    @Test
    void crossMidnightReservationAppearsInBothDays() {
        reservationService.createReservation(new ReservationRequest(
                1L,
                1L,
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
                1L,
                1L,
                "01-01-2099T19:00",
                2
        );
        Reservation created = reservationService.createReservation(request);

        Reservation updated = reservationService.updateReservation(created.id(), request);

        assertEquals(created.id(), updated.id());
        assertEquals(request.startTime(), updated.startTime());
    }
}
