package cl.ipss.sabor_gourmet.repository;

import cl.ipss.sabor_gourmet.model.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la gestión de entidades Table.
 */
@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    List<Table> findByAvailableTrue();
    Optional<Table> findByNumber(Integer number);
}
