package ch.admin.bag.covidcertificate.service;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class COSETimeTest {
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Zurich");
    private final JFixture jFixture = new JFixture();
    private Instant instant;
    private COSETime coseTime;

    @BeforeEach
    public void init() {
        // given
        instant = jFixture.create(Instant.class);
        coseTime = new COSETime(Clock.fixed(instant, ZONE_ID));
    }

    @Test
    public void whenGetIssuedAt_thenOk() {
        // when
        Instant result = coseTime.getIssuedAt();
        // then
        assertEquals(instant, result);
    }

    @Test
    public void whenGetExpiration_thenOk() {
        // when
        Instant result = coseTime.getExpiration();
        // then
        long diff = ChronoUnit.MONTHS.between(getLocalDateTime(instant), getLocalDateTime(result));
        assertEquals(diff, 24);
    }

    private LocalDateTime getLocalDateTime(Instant instant) {
        return instant.atZone(ZONE_ID).toLocalDateTime();
    }
}