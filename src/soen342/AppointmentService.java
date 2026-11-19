package soen342;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AppointmentService {
    private static final List<String> SERVICE_HEADER =
            List.of("Service ID", "Name", "Description", "Requires Resources");

    private final Clock clock;
    private final List<Appointment> appointments = new ArrayList<>();
    private Map<Integer, MedicalService> services = new LinkedHashMap<>();

    public AppointmentService(Clock clock) {
        this.clock = clock;
    }

    public int loadMedicalServices(Path file) throws IOException {
        var rows = Csv.read(file, SERVICE_HEADER);
        var updated = new LinkedHashMap<>(services);
        for (var row : rows) {
            int id = Csv.id(row.get(0));
            String requiresResources = row.get(3);
            if (!requiresResources.equalsIgnoreCase("true")
                    && !requiresResources.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("Requires Resources must be true or false");
            }
            Csv.putRecord(updated, id, new MedicalService(id, Csv.required(row.get(1), "Service name"),
                    row.get(2), Boolean.parseBoolean(requiresResources)));
        }
        services = updated;
        return rows.size();
    }

    public List<MedicalService> medicalServices() {
        return List.copyOf(services.values());
    }

    public List<Appointment> appointmentsForPatient(int patientId) {
        return appointments.stream()
                .filter(appointment -> appointment.patient().patientId() == patientId)
                .sorted(Comparator.comparing(Appointment::startTime))
                .toList();
    }

    public boolean isAvailable(Schedule schedule) {
        return appointments.stream().noneMatch(appointment ->
                appointment.professional().professionalId() == schedule.professionalId()
                        && schedule.overlaps(appointment.startTime(), appointment.endTime()));
    }

    public Appointment createAppointment(Patient patient, Schedule schedule,
                                         HealthcareProfessional professional, int serviceId) {
        if (schedule.startTime().isBefore(LocalDateTime.now(clock)) || !isAvailable(schedule)) {
            throw new IllegalArgumentException("date not available");
        }
        MedicalService service = services.get(serviceId);
        if (service == null) {
            throw new IllegalArgumentException("medical service not found");
        }
        if (service.requiresResources()) {
            throw new IllegalArgumentException("required resource unavailable");
        }

        int nextId = appointments.size() + 1;
        var request = new AppointmentRequest(nextId, LocalDateTime.now(clock), "Approved", patient);
        var appointment = new Appointment(nextId, schedule.startTime(), schedule.endTime(), "Confirmed",
                patient, professional, schedule.location(), service, request);
        appointments.add(appointment);
        return appointment;
    }
}
