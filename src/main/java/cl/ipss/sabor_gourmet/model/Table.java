package cl.ipss.sabor_gourmet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * Entidad JPA que representa una mesa en el salón del restaurante.
 * Se mapea a la tabla 'restaurant_tables' para evitar conflictos con la palabra reservada 'table'.
 */
@Entity
@jakarta.persistence.Table(name = "restaurant_tables")
public class Table {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean available = true;

    public Table() {
    }

    public Table(Long id, Integer number, Integer capacity, Boolean available) {
        this.id = id;
        this.number = number;
        this.capacity = capacity;
        this.available = available != null ? available : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available != null ? available : true;
    }

    // Métodos accesores compatibles con records
    public Long id() {
        return id;
    }

    public Integer number() {
        return number;
    }

    public Integer capacity() {
        return capacity;
    }

    public Boolean available() {
        return available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(id, table.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                ", capacity=" + capacity +
                ", available=" + available +
                '}';
    }
}
