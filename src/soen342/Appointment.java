package soen342;

import java.time.LocalDateTime;

public record Appointment(
        int appointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Patient patient,
        HealthcareProfessional professional,
        MedicalService medicalService,
        AppointmentRequest request) {}
