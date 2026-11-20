package soen342;

import java.time.LocalDateTime;

public record AppointmentRequest(
        int requestId, LocalDateTime requestedAt, String status, Patient patient) {}
