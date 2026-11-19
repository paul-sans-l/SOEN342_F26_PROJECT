package soen342;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PatientRepository {
    private static final List<String> HEADER = List.of(
            "Patient ID", "First Name", "Last Name", "Date of Birth", "Telephone", "RAMQ No.");

    private Map<Integer, Patient> patients = new LinkedHashMap<>();

    public int loadPatients(Path file) throws IOException {
        var rows = Csv.read(file, HEADER);
        var updated = new LinkedHashMap<>(patients);
        for (var row : rows) {
            int id = Csv.id(row.get(0));
            var patient = new Patient(id, Csv.required(row.get(1), "First name"),
                    Csv.required(row.get(2), "Last name"),
                    row.get(3).isEmpty() ? null : LocalDate.parse(row.get(3)),
                    row.get(4), row.get(5));
            Csv.putRecord(updated, id, patient);
        }
        patients = updated;
        return rows.size();
    }

    public Patient identifyPatient(String firstName, String lastName, String insuranceNumber) {
        var matches = patients.values().stream()
                .filter(patient -> patient.firstName().equalsIgnoreCase(firstName)
                        && patient.lastName().equalsIgnoreCase(lastName))
                .filter(patient -> insuranceNumber.isEmpty()
                        || patient.insuranceNumber().equalsIgnoreCase(insuranceNumber))
                .toList();
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("patient not found");
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("more than one patient matches; provide the insurance number");
        }
        return matches.get(0);
    }

    public Patient identifyPatient(int patientId) {
        Patient patient = patients.get(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("patient not found");
        }
        return patient;
    }
}
