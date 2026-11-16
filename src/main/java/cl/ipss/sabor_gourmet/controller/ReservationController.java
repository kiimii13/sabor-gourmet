package cl.ipss.sabor_gourmet.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.ReservationRequest;
import cl.ipss.sabor_gourmet.service.ReservationService;

import java.net.URI;
import java.util.List;

/**
 * Expone las consultas y operaciones de reservas mediante JSON y ResponseEntity.
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Con date se consulta un día completo; sin date se conserva el listado vigente.
    @GetMapping
    public ResponseEntity<List<Reservation>> getCurrentReservations(
            @RequestParam(required = false) String date
    ) {
        return ResponseEntity.ok(reservationService.getReservations(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    // La respuesta 201 incluye la reserva persistida y su ubicación REST.
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.id())).body(reservation);
    }

    // El Service vuelve a validar referencias, capacidad y solapamientos al modificar.
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    // Cancelar cambia el estado a CANCELLED; no elimina el historial del JSON.
    @DeleteMapping("/{id}")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }
}
