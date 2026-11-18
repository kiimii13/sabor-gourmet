package cl.ipss.sabor_gourmet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Representa el intervalo que se usa para comparar reservas. La aplicación no
 * guarda endTime: siempre lo calcula sumando 120 minutos al inicio.
 */
public record TimeRange(LocalDateTime start, LocalDateTime end) {

    private static final long RESERVATION_DURATION_MINUTES = 120;
    private static final ZoneId SANTIAGO_ZONE = ZoneId.of("America/Santiago");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter START_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu'T'HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    // El turno de una reserva comienza en startTime y termina exactamente 120 minutos después.
    public static TimeRange fromStart(String startTime) {
        try {
            if (startTime == null || startTime.isBlank()) {
                throw new IllegalArgumentException("startTime is required");
            }

            LocalDateTime start = LocalDateTime.parse(startTime, START_FORMAT);
            return new TimeRange(start, start.plusMinutes(RESERVATION_DURATION_MINUTES));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("startTime must use format dd-MM-yyyy'T'HH:mm", exception);
        }
    }

    // date usa día-mes-año. El día va desde la medianoche incluida hasta la medianoche siguiente excluida.
    public static TimeRange fromDate(String date) {
        try {
            return fromLocalDate(LocalDate.parse(date, DATE_FORMAT));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("date must use format dd-MM-yyyy", exception);
        }
    }

    // Guarda y devuelve startTime en el mismo formato día-mes-año que recibe la API.
    public static String formatStart(LocalDateTime start) {
        return START_FORMAT.format(start);
    }

    private static TimeRange fromLocalDate(LocalDate day) {
        return new TimeRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
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

        return fromLocalDate(currentDate());
    }
}
