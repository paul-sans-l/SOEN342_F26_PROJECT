package soen342;

import java.time.LocalDate;

public record Patient(
        int patientId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        String insuranceNumber) {}
