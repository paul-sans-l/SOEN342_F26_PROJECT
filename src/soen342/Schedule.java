package soen342;

import java.time.LocalDateTime;

public record Schedule(
        int scheduleId,
        int professionalId,
        ClinicLocation location,
        LocalDateTime startTime,
        LocalDateTime endTime) {

    boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return startTime.isBefore(end) && start.isBefore(endTime);
    }
}
