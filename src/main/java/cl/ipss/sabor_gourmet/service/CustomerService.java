package cl.ipss.sabor_gourmet.service;

import org.springframework.stereotype.Service;
import cl.ipss.sabor_gourmet.exception.ConflictException;
import cl.ipss.sabor_gourmet.exception.ResourceNotFoundException;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.CustomerRequest;
import cl.ipss.sabor_gourmet.repository.RestaurantJsonRepository;

import java.util.List;
import java.util.Objects;

/**
 * Gestiona clientes y protege las referencias que las reservas guardan por ID.
 */
@Service
public class CustomerService {

    private final RestaurantJsonRepository repository;

    public CustomerService(RestaurantJsonRepository repository) {
        this.repository = repository;
    }

    // La colección se lee desde el documento completo y se devuelve sin duplicar datos.
    public List<Customer> getAllCustomers() {
        return repository.read().getCustomers();
    }

    public Customer getCustomer(Long id) {
        return repository.read().getCustomers().stream()
                .filter(customer -> Objects.equals(customer.id(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    // El cliente se agrega mediante Repository para persistirlo sin perder mesas ni reservas.
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

    // La actualización conserva el ID porque las reservas dependen de esa referencia.
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

    // No se permite eliminar un cliente si alguna reserva todavía conserva su customerId.
    public void deleteCustomer(Long id) {
        repository.update(data -> {
            int index = indexOf(data.getCustomers(), id);
            boolean referenced = data.getReservations().stream()
                    .anyMatch(reservation -> Objects.equals(reservation.customerId(), id));
            if (referenced) {
                throw new ConflictException("Customer is referenced by a reservation: " + id);
            }
            data.getCustomers().remove(index);
            return null;
        });
    }

    private int indexOf(List<Customer> customers, Long id) {
        for (int index = 0; index < customers.size(); index++) {
            if (Objects.equals(customers.get(index).id(), id)) {
                return index;
            }
        }
        throw new ResourceNotFoundException("Customer not found: " + id);
    }

    private Long nextId(List<Customer> customers) {
        return customers.stream()
                .map(Customer::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
