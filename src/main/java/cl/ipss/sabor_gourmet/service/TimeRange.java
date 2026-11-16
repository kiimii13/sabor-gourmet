package cl.ipss.sabor_gourmet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Representa el intervalo que se usa para comparar reservas. La aplicación no
 * guarda endTime: siempre lo calcula sumando 120 minutos al inicio.
 */
public record TimeRange(LocalDateTime start, LocalDateTime end) {

    private static final long RESERVATION_DURATION_MINUTES = 120;
    private static final ZoneId SANTIAGO_ZONE = ZoneId.of("America/Santiago");

    // El turno de una reserva comienza en startTime y termina exactamente 120 minutos después.
    public static TimeRange fromStart(String startTime) {
        try {
            if (startTime == null || startTime.isBlank()) {
                throw new IllegalArgumentException("startTime is required");
            }

            LocalDateTime start = LocalDateTime.parse(startTime);
            return new TimeRange(start, start.plusMinutes(RESERVATION_DURATION_MINUTES));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("startTime must use ISO format yyyy-MM-dd'T'HH:mm", exception);
        }
    }

    // date representa el día completo: desde la medianoche incluida hasta la medianoche siguiente excluida.
    public static TimeRange fromDate(String date) {
        try {
            LocalDate day = LocalDate.parse(date);
            return new TimeRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("date must use ISO format yyyy-MM-dd", exception);
        }
    }

    // La misma comparación se usa para disponibilidad, creación y modificación de reservas.
    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public static LocalDate currentDate() {
        return LocalDate.now(SANTIAGO_ZONE);
    }

    public static LocalDateTime currentDateTime() {
        return LocalDateTime.now(SANTIAGO_ZONE);
    }
    // Prioriza startTime cuando viene solo y rechaza la combinación ambigua de ambos parámetros.
    public static TimeRange resolve(String date, String startTime) {
        if (date != null && startTime != null) {
            throw new IllegalArgumentException("date and startTime cannot be used together");
        }
        if (startTime != null) {
            return fromStart(startTime);
        }
        if (date != null) {
            return fromDate(date);
        }

        LocalDate today = currentDate();
        return fromDate(today.toString());
    }
}
