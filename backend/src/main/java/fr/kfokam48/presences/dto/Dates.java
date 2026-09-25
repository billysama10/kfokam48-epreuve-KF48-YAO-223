package fr.kfokam48.presences.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Les dates sont stockées en UTC et exposées en ISO-8601 avec décalage (ENF5). */
final class Dates {

    private Dates() {
    }

    static OffsetDateTime utc(LocalDateTime date) {
        return date == null ? null : date.atOffset(ZoneOffset.UTC);
    }
}
