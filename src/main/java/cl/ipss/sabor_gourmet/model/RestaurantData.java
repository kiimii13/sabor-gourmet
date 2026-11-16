package cl.ipss.sabor_gourmet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documento completo que se serializa en el único archivo JSON del restaurante.
 * Mantener las tres listas juntas permite actualizar una sin perder las demás.
 */
public class RestaurantData {

    private List<Customer> customers = new ArrayList<>();
    private List<Table> tables = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    public RestaurantData() {
    }

    public RestaurantData(List<Customer> customers, List<Table> tables, List<Reservation> reservations) {
        this.customers = customers != null ? customers : new ArrayList<>();
        this.tables = tables != null ? tables : new ArrayList<>();
        this.reservations = reservations != null ? reservations : new ArrayList<>();
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers != null ? customers : new ArrayList<>();
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables != null ? tables : new ArrayList<>();
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations != null ? reservations : new ArrayList<>();
    }
}
