package cl.ipss.sabor_gourmet.repository;

import cl.ipss.sabor_gourmet.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la gestión de entidades Reservation.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatus(String status);
    List<Reservation> findByTableIdAndStatus(Long tableId, String status);
    boolean existsByCustomerId(Long customerId);
    boolean existsByTableId(Long tableId);
}
