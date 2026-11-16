package cl.ipss.sabor_gourmet;

import cl.ipss.sabor_gourmet.service.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeTest {

    @Test
    void detectsOverlapAndAllowsExactBoundary() {
        TimeRange existing = new TimeRange(
                LocalDateTime.parse("2099-01-01T20:00"),
                LocalDateTime.parse("2099-01-01T21:30")
        );

        assertTrue(existing.overlaps(new TimeRange(
                LocalDateTime.parse("2099-01-01T21:00"),
                LocalDateTime.parse("2099-01-01T22:00")
        )));
        assertFalse(existing.overlaps(new TimeRange(
                LocalDateTime.parse("2099-01-01T21:30"),
                LocalDateTime.parse("2099-01-01T22:30")
        )));
    }

    @Test
    void resolvesCurrentDateAndTimeInSantiago() {
        LocalDate santiagoDate = LocalDate.now(ZoneId.of("America/Santiago"));

        assertEquals(santiagoDate, TimeRange.currentDateTime().toLocalDate());
        assertEquals(santiagoDate, TimeRange.resolve(null, null).start().toLocalDate());
    }
}
