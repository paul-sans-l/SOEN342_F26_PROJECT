package soen342;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class Main {
    private static final class Session {
        final PatientRepository patients = new PatientRepository();
        final AppointmentService appointments;
        final HealthcareProfessionalSchedule availability;
        final BookingInterface booking;

        Session(Clock clock) {
            appointments = new AppointmentService(clock);
            availability = new HealthcareProfessionalSchedule(clock, appointments);
            booking = new BookingInterface(new AppointmentController(patients, availability, appointments));
        }
    }

    private final BufferedReader input;
    private final PrintStream output;
    private final Clock clock;
    private final Path defaultAvailabilityCsv;
    private Session session;

    Main(BufferedReader input, PrintStream output, Clock clock, Path defaultAvailabilityCsv) {
        this.input = input;
        this.output = output;
        this.clock = clock;
        this.defaultAvailabilityCsv = defaultAvailabilityCsv;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: soen342.Main [availability-csv-path]");
        }
        Path defaultAvailabilityCsv = Path.of(args.length == 0 ? "data/professional_availability.csv" : args[0]);
        var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        var output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        new Main(reader, output, Clock.systemDefaultZone(), defaultAvailabilityCsv).run();
    }

    void run() throws IOException {
        output.println("Appointment Tracking System | Iteration II PoC");
        while (true) {
            output.println("""

                    1. Import availability from CSV
                    2. View clinic locations
                    3. View patients and their appointments
                    4. Make availability
                    5. Search availability
                    6. Book an appointment
                    7. Export availability to CSV
                    0. Exit""");
            try {
                switch (prompt("Choice")) {
                    case "1" -> importAvailability();
                    case "2" -> viewClinics();
                    case "3" -> viewPatientsAndAppointments();
                    case "4" -> makeAvailability(requireSession().availability);
                    case "5" -> {
                        var availability = requireSession().availability;
                        showSchedules(availability, searchAvailability(availability));
                    }
                    case "6" -> bookAppointment(requireSession());
                    case "7" -> output.println("Availability schedules exported: "
                            + requireSession().availability.exportAvailability(file("Output CSV")));
                    case "0" -> {
                        return;
                    }
                    default -> output.println("Choose a number from 0 to 7.");
                }
            } catch (EOFException exception) {
                return;
            } catch (IOException | IllegalArgumentException | DateTimeException exception) {
                output.println("Error: " + exception.getMessage());
            }
        }
    }

    private void importAvailability() throws IOException {
        Path path = file("Availability CSV", defaultAvailabilityCsv);
        boolean firstImport = session == null;
        Session target = firstImport ? loadSupportingData() : session;
        List<ClinicLocation> clinics = target.availability.clinicLocations();
        showClinics(clinics);
        Integer suggestedClinicId = clinics.size() == 1 ? clinics.get(0).locationId() : null;
        String clinicPrompt = suggestedClinicId == null
                ? "Default clinic ID (blank if the CSV has a Location ID column)"
                : "Default clinic ID [" + suggestedClinicId + "]";
        String clinicId = prompt(clinicPrompt);
        int count = target.availability.importAvailability(path,
                clinicId.isEmpty() ? suggestedClinicId : Integer.valueOf(Csv.id(clinicId)));
        session = target;
        if (firstImport) {
            output.printf("Clinic locations: %d | Patients: %d | Medical services: %d%n",
                    clinics.size(), session.patients.patients().size(), session.appointments.medicalServices().size());
        }
        output.println("Availability schedules imported: " + count);
    }

    private Session loadSupportingData() throws IOException {
        Path clinicCsv = Path.of("data/clinic_locations.csv");
        Path patientCsv = Path.of("data/patients.csv");
        Path serviceCsv = Path.of("data/medical_services.csv");
        String choice = prompt("Supporting CSVs (Enter for sample files, or type custom)");
        if (choice.equalsIgnoreCase("custom")) {
            clinicCsv = file("Clinic CSV", clinicCsv);
            patientCsv = file("Patient CSV", patientCsv);
            serviceCsv = file("Medical service CSV", serviceCsv);
        } else if (!choice.isEmpty()) {
            throw new IllegalArgumentException(
                    "For supporting CSVs, press Enter to use the sample files or type custom to choose files.");
        }

        var loaded = new Session(clock);
        loaded.availability.loadClinicLocations(clinicCsv);
        loaded.patients.loadPatients(patientCsv);
        loaded.appointments.loadMedicalServices(serviceCsv);
        return loaded;
    }

    private Session requireSession() {
        if (session == null) {
            throw new IllegalArgumentException("No data is loaded yet. Choose option 1 to import availability first.");
        }
        return session;
    }

    private void viewClinics() {
        if (session == null) {
            output.println("Choose option 1 to load clinic locations with your availability import.");
            return;
        }
        var clinics = session.availability.clinicLocations();
        if (clinics.isEmpty()) {
            output.println("No clinic locations found");
            return;
        }
        output.println("Clinic locations:");
        showClinics(clinics);
    }

    private void viewPatientsAndAppointments() {
        if (session == null) {
            output.println("Choose option 1 to load patients with your availability import.");
            return;
        }
        var patients = session.patients.patients();
        if (patients.isEmpty()) {
            output.println("No patients found");
            return;
        }
        output.println("Patients and their appointments:");
        for (Patient patient : patients) {
            output.printf("Patient %d | %s %s%n", patient.patientId(), patient.firstName(), patient.lastName());
            var appointments = session.appointments.appointmentsForPatient(patient.patientId());
            if (appointments.isEmpty()) {
                output.println("  No appointments reserved");
            }
            for (Appointment appointment : appointments) {
                output.printf("  Appointment %d | %s | %s %s to %s | %s | %s %s | %s%n",
                        appointment.appointmentId(), appointment.status(), appointment.startTime().toLocalDate(),
                        HealthcareProfessionalSchedule.TIME.format(appointment.startTime()),
                        HealthcareProfessionalSchedule.TIME.format(appointment.endTime()),
                        appointment.location().name(), appointment.professional().firstName(),
                        appointment.professional().lastName(), appointment.medicalService().name());
            }
        }
    }

    private void makeAvailability(HealthcareProfessionalSchedule availability) throws IOException {
        showProfessionals(availability);
        HealthcareProfessional professional = availability.selectProfessional(id("Professional ID"));
        output.println("Professional selected: " + professional.firstName() + " " + professional.lastName());
        showClinics(professional.locations());
        int locationId = id("Clinic ID");
        LocalDate startDate = date("Start date");
        LocalDate endDate = date("End date");
        RepetitionPattern pattern = switch (prompt("Recurrence (weekly/monthly)").toLowerCase(Locale.ROOT)) {
            case "weekly" -> readWeeklyPattern();
            case "monthly" -> RepetitionPattern.monthly(id("Day of the month (1-31)"));
            default -> throw new IllegalArgumentException(
                    "Enter weekly or monthly for the recurrence.");
        };
        LocalTime startTime = time("Start time");
        LocalTime endTime = time("End time");
        var created = availability.makeAvailability(professional.professionalId(), locationId,
                startDate, endDate, pattern, startTime, endTime);
        output.println("Availability created: " + created.size());
        showSchedules(availability, created);
    }

    private RepetitionPattern readWeeklyPattern() throws IOException {
        var days = EnumSet.noneOf(DayOfWeek.class);
        for (String day : prompt("Days of week (e.g. MONDAY,WEDNESDAY)").split(",", -1)) {
            try {
                days.add(DayOfWeek.valueOf(day.strip().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Enter full weekday names separated by commas, such as MONDAY,WEDNESDAY.");
            }
        }
        return RepetitionPattern.weekly(days);
    }

    private List<Schedule> searchAvailability(HealthcareProfessionalSchedule availability) throws IOException {
        boolean bySpecialty = switch (prompt("Search by specialty or professional").toLowerCase(Locale.ROOT)) {
            case "specialty" -> true;
            case "professional" -> false;
            default -> throw new IllegalArgumentException(
                    "Enter specialty to search by specialty, or professional to search by professional.");
        };
        if (bySpecialty) {
            availability.specialties().forEach(specialty ->
                    output.printf("%d | %s%n", specialty.specialtyId(), specialty.name()));
        } else {
            showProfessionals(availability);
        }
        int id = id(bySpecialty ? "Specialty ID" : "Professional ID");
        LocalDate startDate = date("Start date");
        LocalDate endDate = date("End date");
        return bySpecialty ? availability.searchAvailability(id, startDate, endDate)
                : availability.searchAvailabilityForProfessional(id, startDate, endDate);
    }

    private void bookAppointment(Session current) throws IOException {
        int patientId = current.booking.identification(prompt("Patient first name"), prompt("Patient last name"),
                prompt("Insurance number (blank if unavailable)"));
        output.println("Successful identification: patient " + patientId);
        var schedules = searchAvailability(current.availability);
        if (schedules.isEmpty()) {
            output.println("No date available");
            return;
        }
        showSchedules(current.availability, schedules);
        int scheduleId = id("Schedule ID");
        if (schedules.stream().noneMatch(schedule -> schedule.scheduleId() == scheduleId)) {
            throw new IllegalArgumentException(
                    "That schedule ID is not in the search results. Enter one of the schedule IDs listed above.");
        }
        current.appointments.medicalServices().forEach(service ->
                output.printf("%d | %s%n", service.serviceId(), service.name()));
        Appointment confirmed = current.booking.requestAppointment(scheduleId, id("Service ID"));
        output.printf("%s | appointment %d | %s to %s%n",
                confirmed.status(), confirmed.appointmentId(), confirmed.startTime(), confirmed.endTime());
    }

    private void showSchedules(HealthcareProfessionalSchedule availability, List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            output.println("No availability found");
            return;
        }
        for (Schedule schedule : schedules) {
            HealthcareProfessional professional = availability.selectProfessional(schedule.professionalId());
            String specialties = professional.specialties().stream()
                    .map(Specialty::name).sorted().collect(Collectors.joining(", "));
            output.printf("%d | %s to %s | %s %s | %s | %s%n",
                    schedule.scheduleId(), schedule.startTime(), schedule.endTime(),
                    professional.firstName(), professional.lastName(), specialties, schedule.location().name());
        }
    }

    private void showClinics(Iterable<ClinicLocation> locations) {
        for (ClinicLocation location : locations) {
            output.printf("%d | %s | %s%n", location.locationId(), location.name(), location.address());
        }
    }

    private void showProfessionals(HealthcareProfessionalSchedule availability) {
        availability.healthcareProfessionals().forEach(professional ->
                output.printf("%d | %s %s%n", professional.professionalId(),
                        professional.firstName(), professional.lastName()));
    }

    private String prompt(String label) throws IOException {
        output.print(label + ": ");
        output.flush();
        String line = input.readLine();
        if (line == null) {
            throw new EOFException();
        }
        return line.strip();
    }

    private int id(String label) throws IOException {
        String value = prompt(label);
        try {
            return Csv.id(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a positive whole number.");
        }
    }

    private Path file(String label) throws IOException {
        return Path.of(Csv.required(prompt(label + " path"), label + " path"));
    }

    private Path file(String label, Path defaultPath) throws IOException {
        String selected = prompt(label + " path [" + defaultPath + "]");
        return selected.isEmpty() ? defaultPath : Path.of(selected);
    }

    private LocalDate date(String label) throws IOException {
        String value = prompt(label + " (YYYY-MM-DD)");
        try {
            return LocalDate.parse(value);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(label + " must be a valid date in YYYY-MM-DD format.");
        }
    }

    private LocalTime time(String label) throws IOException {
        String value = prompt(label + " (HH:mm)");
        try {
            return LocalTime.parse(value, HealthcareProfessionalSchedule.TIME);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(label + " must be a valid time in 24-hour HH:mm format.");
        }
    }
}
