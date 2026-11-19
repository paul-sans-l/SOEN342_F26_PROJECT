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
    private final BufferedReader input;
    private final PrintStream output;
    private final Path defaultAvailabilityCsv;
    private final PatientRepository patients = new PatientRepository();
    private final AppointmentService appointments;
    private final HealthcareProfessionalSchedule availability;
    private final BookingInterface booking;

    Main(BufferedReader input, PrintStream output, Clock clock, Path defaultAvailabilityCsv) {
        this.input = input;
        this.output = output;
        this.defaultAvailabilityCsv = defaultAvailabilityCsv;
        appointments = new AppointmentService(clock);
        availability = new HealthcareProfessionalSchedule(clock, appointments);
        booking = new BookingInterface(new AppointmentController(patients, availability, appointments));
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

                    1. Load clinic locations from CSV
                    2. Load patients from CSV
                    3. Load medical services from CSV
                    4. Import availability from CSV
                    5. Make availability
                    6. Search availability
                    7. Book an appointment
                    8. Export availability to CSV
                    0. Exit""");
            try {
                switch (prompt("Choice")) {
                    case "1" -> output.println("Clinic location rows loaded: "
                            + availability.loadClinicLocations(file("Clinic CSV", Path.of("data/clinic_locations.csv"))));
                    case "2" -> output.println("Patient rows loaded: "
                            + patients.loadPatients(file("Patient CSV", Path.of("data/patients.csv"))));
                    case "3" -> output.println("Medical service rows loaded: "
                            + appointments.loadMedicalServices(file("Medical service CSV", Path.of("data/medical_services.csv"))));
                    case "4" -> importAvailability();
                    case "5" -> makeAvailability();
                    case "6" -> showSchedules(searchAvailability());
                    case "7" -> bookAppointment();
                    case "8" -> output.println("Availability schedules exported: "
                            + availability.exportAvailability(file("Output CSV")));
                    case "0" -> {
                        return;
                    }
                    default -> output.println("Choose a number from 0 to 8.");
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
        List<ClinicLocation> clinics = availability.clinicLocations();
        showClinics(clinics);
        Integer suggestedClinicId = clinics.size() == 1 ? clinics.get(0).locationId() : null;
        String clinicPrompt = suggestedClinicId == null
                ? "Default clinic ID (blank if the CSV has a Location ID column)"
                : "Default clinic ID [" + suggestedClinicId + "]";
        String clinicId = prompt(clinicPrompt);
        int count = availability.importAvailability(path,
                clinicId.isEmpty() ? suggestedClinicId : Integer.valueOf(Csv.id(clinicId)));
        output.println("Availability schedules imported: " + count);
    }

    private void makeAvailability() throws IOException {
        showProfessionals();
        HealthcareProfessional professional = availability.selectProfessional(id("Professional ID"));
        output.println("Professional selected: " + professional.firstName() + " " + professional.lastName());
        showClinics(professional.locations());
        int locationId = id("Clinic ID");
        LocalDate startDate = date("Start date");
        LocalDate endDate = date("End date");
        RepetitionPattern pattern = switch (prompt("Recurrence (weekly/monthly)").toLowerCase(Locale.ROOT)) {
            case "weekly" -> readWeeklyPattern();
            case "monthly" -> RepetitionPattern.monthly(id("Day of the month (1-31)"));
            default -> throw new IllegalArgumentException("Select weekly or monthly");
        };
        LocalTime startTime = time("Start time");
        LocalTime endTime = time("End time");
        var created = availability.makeAvailability(professional.professionalId(), locationId,
                startDate, endDate, pattern, startTime, endTime);
        output.println("Availability created: " + created.size());
        showSchedules(created);
    }

    private RepetitionPattern readWeeklyPattern() throws IOException {
        var days = EnumSet.noneOf(DayOfWeek.class);
        for (String day : prompt("Days of week (e.g. MONDAY,WEDNESDAY)").split(",", -1)) {
            try {
                days.add(DayOfWeek.valueOf(day.strip().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Enter full day names separated by commas");
            }
        }
        return RepetitionPattern.weekly(days);
    }

    private List<Schedule> searchAvailability() throws IOException {
        boolean bySpecialty = switch (prompt("Search by specialty or professional").toLowerCase(Locale.ROOT)) {
            case "specialty" -> true;
            case "professional" -> false;
            default -> throw new IllegalArgumentException("Select specialty or professional");
        };
        if (bySpecialty) {
            availability.specialties().forEach(specialty ->
                    output.printf("%d | %s%n", specialty.specialtyId(), specialty.name()));
        } else {
            showProfessionals();
        }
        int id = id(bySpecialty ? "Specialty ID" : "Professional ID");
        LocalDate startDate = date("Start date");
        LocalDate endDate = date("End date");
        return bySpecialty ? availability.searchAvailability(id, startDate, endDate)
                : availability.searchAvailabilityForProfessional(id, startDate, endDate);
    }

    private void bookAppointment() throws IOException {
        int patientId = booking.identification(prompt("Patient first name"), prompt("Patient last name"),
                prompt("Insurance number (blank if unavailable)"));
        output.println("Successful identification: patient " + patientId);
        var schedules = searchAvailability();
        if (schedules.isEmpty()) {
            output.println("No date available");
            return;
        }
        showSchedules(schedules);
        int scheduleId = id("Schedule ID");
        if (schedules.stream().noneMatch(schedule -> schedule.scheduleId() == scheduleId)) {
            throw new IllegalArgumentException("Select a schedule from the search results");
        }
        appointments.medicalServices().forEach(service ->
                output.printf("%d | %s%n", service.serviceId(), service.name()));
        Appointment confirmed = booking.requestAppointment(scheduleId, id("Service ID"));
        output.printf("%s | appointment %d | %s to %s%n",
                confirmed.status(), confirmed.appointmentId(), confirmed.startTime(), confirmed.endTime());
    }

    private void showSchedules(List<Schedule> schedules) {
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
            output.printf("%d | %s%n", location.locationId(), location.name());
        }
    }

    private void showProfessionals() {
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
        return Csv.id(prompt(label));
    }

    private Path file(String label) throws IOException {
        return Path.of(Csv.required(prompt(label + " path"), label + " path"));
    }

    private Path file(String label, Path defaultPath) throws IOException {
        String selected = prompt(label + " path [" + defaultPath + "]");
        return selected.isEmpty() ? defaultPath : Path.of(selected);
    }

    private LocalDate date(String label) throws IOException {
        return LocalDate.parse(prompt(label + " (YYYY-MM-DD)"));
    }

    private LocalTime time(String label) throws IOException {
        return LocalTime.parse(prompt(label + " (HH:mm)"), HealthcareProfessionalSchedule.TIME);
    }
}
