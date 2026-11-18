package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.dto.CustomerRequest;
import cl.ipss.sabor_gourmet.repository.RestaurantJsonRepository;

import java.util.List;
import java.util.Objects;

/**
 * Servicio para los clientes
 */
@Service
public class CustomerService {

    private final RestaurantJsonRepository repository;

    public CustomerService(RestaurantJsonRepository repository) {
        this.repository = repository;
    }

    // Obtiene todos los clientes
    public List<Customer> getAllCustomers() {
        return repository.read().getCustomers();
    }

    // Obtiene un cliente por su ID
    public Customer getCustomer(Long id) {
        return repository.read().getCustomers().stream()
                .filter(customer -> Objects.equals(customer.id(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    // Crea un nuevo cliente
    public Customer createCustomer(CustomerRequest request) {
        return repository.update(data -> {
            Customer customer = new Customer(
                    nextId(data.getCustomers()),
                    request.name().trim(),
                    request.email().trim(),
                    request.phone().trim()
            );
            data.getCustomers().add(customer);
            return customer;
        });
    }

    // Actualiza un cliente existente
    public Customer updateCustomer(Long id, CustomerRequest request) {
        return repository.update(data -> {
            int index = indexOf(data.getCustomers(), id);
            Customer updated = new Customer(
                    id,
                    request.name().trim(),
                    request.email().trim(),
                    request.phone().trim()
            );
            data.getCustomers().set(index, updated);
            return updated;
        });
    }

    // Elimina un cliente existente
    public void deleteCustomer(Long id) {
        repository.update(data -> {
            int index = indexOf(data.getCustomers(), id);
            boolean referenced = data.getReservations().stream()
                    .anyMatch(reservation -> Objects.equals(reservation.customerId(), id));
            if (referenced) {
                throw new ConflictException("Cliente referenciado por una reserva: " + id);
            }
            data.getCustomers().remove(index);
            return null;
        });
    }

    // Obtiene el índice de un cliente por su ID
    private int indexOf(List<Customer> customers, Long id) {
        for (int index = 0; index < customers.size(); index++) {
            if (Objects.equals(customers.get(index).id(), id)) {
                return index;
            }
        }
        throw new ResourceNotFoundException("Cliente no encontrado: " + id);
    }

    // Obtiene el siguiente ID disponible para un cliente
    private Long nextId(List<Customer> customers) {
        return customers.stream()
                .map(Customer::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
