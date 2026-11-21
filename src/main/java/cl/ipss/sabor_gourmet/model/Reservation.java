package cl.ipss.sabor_gourmet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Entidad JPA que representa una reserva en el restaurante.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(nullable = false)
    private Integer people;

    @Column(nullable = false)
    private String status;

    public Reservation() {
    }

    public Reservation(Long id, Long customerId, Long tableId, String startTime, Integer people, String status) {
        this.id = id;
        this.customerId = customerId;
        this.tableId = tableId;
        this.startTime = startTime;
        this.people = people;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getPeople() {
        return people;
    }

    public void setPeople(Integer people) {
        this.people = people;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Métodos accesores compatibles con records
    public Long id() {
        return id;
    }

    public Long customerId() {
        return customerId;
    }

    public Long tableId() {
        return tableId;
    }

    public String startTime() {
        return startTime;
    }

    public Integer people() {
        return people;
    }

    public String status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", tableId=" + tableId +
                ", startTime='" + startTime + '\'' +
                ", people=" + people +
                ", status='" + status + '\'' +
                '}';
    }
}
