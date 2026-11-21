package cl.ipss.sabor_gourmet.config;

import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.repository.CustomerRepository;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;
import cl.ipss.sabor_gourmet.repository.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inicializador de datos en la base de datos H2 mediante Spring Data JPA.
 * Carga el inventario inicial de 10 mesas, clientes y reservas de demostración
 * si la base de datos se encuentra vacía.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public DataInitializer(CustomerRepository customerRepository,
                           TableRepository tableRepository,
                           ReservationRepository reservationRepository) {
        this.customerRepository = customerRepository;
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) {
        if (tableRepository.count() == 0) {
            // Inicializar las 10 mesas del restaurante
            tableRepository.saveAll(List.of(
                    new Table(null, 1, 4, true),
                    new Table(null, 2, 2, true),
                    new Table(null, 3, 2, true),
                    new Table(null, 4, 4, true),
                    new Table(null, 5, 4, true),
                    new Table(null, 6, 6, true),
                    new Table(null, 7, 6, true),
                    new Table(null, 8, 8, true),
                    new Table(null, 9, 10, true),
                    new Table(null, 10, 10, true)
            ));
        }

        if (customerRepository.count() == 0) {
            // Inicializar clientes iniciales
            customerRepository.saveAll(List.of(
                    new Customer(null, "Ana Pérez", "ana.perez@example.com", "+56911111111"),
                    new Customer(null, "Valentina Soto Saavedra", "valentina.soto@example.com", "+56911110001"),
                    new Customer(null, "Matías Rojas", "matias.rojas@example.com", "+56911110002"),
                    new Customer(null, "Camila Torres", "camila.torres@example.com", "+56911110003"),
                    new Customer(null, "Sofía Herrera", "sofia.herrera@example.com", "+56911110005"),
                    new Customer(null, "Nicolás Vega", "nicolas.vega@example.com", "+56911110006"),
                    new Customer(null, "Constanza Valenzuela", "constanza.valenzuela@example.com", "+56911110007")
            ));
        }

        if (reservationRepository.count() == 0) {
            Long cust1Id = customerRepository.findAll().stream()
                    .filter(c -> "ana.perez@example.com".equalsIgnoreCase(c.getEmail()))
                    .findFirst().map(Customer::getId).orElse(1L);
            Long cust2Id = customerRepository.findAll().stream()
                    .filter(c -> "valentina.soto@example.com".equalsIgnoreCase(c.getEmail()))
                    .findFirst().map(Customer::getId).orElse(2L);
            Long table1Id = tableRepository.findByNumber(1).map(Table::getId).orElse(1L);
            Long table2Id = tableRepository.findByNumber(2).map(Table::getId).orElse(2L);

            // Inicializar reservas de prueba
            reservationRepository.saveAll(List.of(
                    new Reservation(null, cust1Id, table1Id, "20-11-2026T20:00", 2, "CONFIRMED"),
                    new Reservation(null, cust2Id, table2Id, "16-11-2026T20:00", 2, "CONFIRMED")
            ));
        }
    }
}
