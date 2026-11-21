package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.dto.CustomerRequest;
import cl.ipss.sabor_gourmet.repository.CustomerRepository;
import cl.ipss.sabor_gourmet.repository.ReservationRepository;

import java.util.List;

/**
 * Servicio para los clientes respaldado por Spring Data JPA.
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;

    public CustomerService(CustomerRepository customerRepository, ReservationRepository reservationRepository) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
    }

    // Obtiene todos los clientes
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // Obtiene un cliente por su ID
    @Transactional(readOnly = true)
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    // Crea un nuevo cliente
    public Customer createCustomer(CustomerRequest request) {
        Customer customer = new Customer(
                null,
                request.name().trim(),
                request.email().trim(),
                request.phone().trim()
        );
        return customerRepository.save(customer);
    }

    // Actualiza un cliente existente
    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer customer = getCustomer(id);
        customer.setName(request.name().trim());
        customer.setEmail(request.email().trim());
        customer.setPhone(request.phone().trim());
        return customerRepository.save(customer);
    }

    // Elimina un cliente existente
    public void deleteCustomer(Long id) {
        Customer customer = getCustomer(id);
        if (reservationRepository.existsByCustomerId(id)) {
            throw new ConflictException("Cliente referenciado por una reserva: " + id);
        }
        customerRepository.delete(customer);
    }
}
