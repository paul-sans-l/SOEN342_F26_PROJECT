package soen342;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class HealthcareProfessionalSchedule {
    private static final List<String> LOCATION_HEADER = List.of("Location ID", "Name", "Address");
    private static final List<String> AVAILABILITY_HEADER =
            List.of("First Name", "Last Name", "Title", "Specialty", "Date", "Start", "End");
    private static final List<String> EXPORT_HEADER =
            List.of("First Name", "Last Name", "Title", "Specialty", "Date", "Start", "End", "Location ID");
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private record ProfessionalKey(String firstName, String lastName, String title) {
        static ProfessionalKey from(HealthcareProfessional professional) {
            return new ProfessionalKey(professional.firstName(), professional.lastName(),
                    professional.professionalInformation());
        }
    }

    private final Clock clock;
    private final AppointmentService appointments;
    private Map<Integer, ClinicLocation> locations = new LinkedHashMap<>();
    private Map<Integer, Specialty> specialties = new LinkedHashMap<>();
    private Map<Integer, HealthcareProfessional> professionals = new LinkedHashMap<>();
    private Map<Integer, Schedule> schedules = new LinkedHashMap<>();

    public HealthcareProfessionalSchedule(Clock clock, AppointmentService appointments) {
        this.clock = clock;
        this.appointments = appointments;
    }

    public int loadClinicLocations(Path file) throws IOException {
        var rows = Csv.read(file, LOCATION_HEADER);
        var updated = new LinkedHashMap<>(locations);
        for (var row : rows) {
            int id = Csv.id(row.get(0));
            Csv.putRecord(updated, id, new ClinicLocation(id,
                    Csv.required(row.get(1), "Clinic name"), row.get(2)));
        }
        locations = updated;
        return rows.size();
    }

    public int importAvailability(Path file, Integer defaultLocationId) throws IOException {
        var table = Csv.read(file);
        boolean hasLocationColumn = table.header().equals(EXPORT_HEADER);
        if (!hasLocationColumn && !table.header().equals(AVAILABILITY_HEADER)) {
            throw new IllegalArgumentException("Expected the professional availability CSV header");
        }
        ClinicLocation defaultLocation = hasLocationColumn ? null : requireLocation(defaultLocationId);

        var importedSpecialties = new LinkedHashMap<>(specialties);
        var specialtiesByName = new LinkedHashMap<String, Specialty>();
        specialties.values().forEach(specialty -> specialtiesByName.put(specialty.name(), specialty));
        var importedProfessionals = new LinkedHashMap<>(professionals);
        var professionalIds = new LinkedHashMap<ProfessionalKey, Integer>();
        professionals.values().forEach(professional ->
                professionalIds.put(ProfessionalKey.from(professional), professional.professionalId()));
        var importedSchedules = new LinkedHashMap<>(schedules);

        for (var row : table.rows()) {
            String firstName = Csv.required(row.get(0), "First name");
            String lastName = Csv.required(row.get(1), "Last name");
            String title = row.get(2);
            String specialtyName = Csv.required(row.get(3), "Specialty");
            LocalDate date = LocalDate.parse(row.get(4));
            LocalTime start = LocalTime.parse(row.get(5), TIME);
            LocalTime end = LocalTime.parse(row.get(6), TIME);
            validateTimes(start, end);
            ClinicLocation location = hasLocationColumn ? requireLocation(Csv.id(row.get(7))) : defaultLocation;

            Specialty specialty = specialtiesByName.computeIfAbsent(specialtyName, name -> {
                var added = new Specialty(importedSpecialties.size() + 1, name);
                importedSpecialties.put(added.specialtyId(), added);
                return added;
            });
            var key = new ProfessionalKey(firstName, lastName, title);
            int professionalId = professionalIds.computeIfAbsent(key, ignored -> {
                int id = importedProfessionals.size() + 1;
                importedProfessionals.put(id, new HealthcareProfessional(id, firstName, lastName, title,
                        Set.of(), Set.of()));
                return id;
            });
            HealthcareProfessional professional = importedProfessionals.get(professionalId);
            importedProfessionals.put(professionalId, professional.withAssignment(specialty, location));

            var schedule = new Schedule(importedSchedules.size() + 1, professionalId, location,
                    date.atTime(start), date.atTime(end));
            Schedule overlap = findOverlap(importedSchedules.values(), schedule);
            if (overlap == null) {
                importedSchedules.put(schedule.scheduleId(), schedule);
            } else if (!sameInterval(overlap, schedule)) {
                throw overlapError(overlap);
            }
        }

        int added = importedSchedules.size() - schedules.size();
        specialties = importedSpecialties;
        professionals = importedProfessionals;
        schedules = importedSchedules;
        return added;
    }

    public int exportAvailability(Path file) throws IOException {
        var rows = new ArrayList<List<String>>();
        for (Schedule schedule : schedules.values()) {
            HealthcareProfessional professional = selectProfessional(schedule.professionalId());
            for (Specialty specialty : professional.specialties().stream()
                    .sorted(Comparator.comparingInt(Specialty::specialtyId)).toList()) {
                rows.add(List.of(professional.firstName(), professional.lastName(),
                        professional.professionalInformation(), specialty.name(),
                        schedule.startTime().toLocalDate().toString(),
                        TIME.format(schedule.startTime()), TIME.format(schedule.endTime()),
                        Integer.toString(schedule.location().locationId())));
            }
        }
        Csv.write(file, EXPORT_HEADER, rows);
        return schedules.size();
    }

    public List<ClinicLocation> clinicLocations() {
        return List.copyOf(locations.values());
    }

    public List<HealthcareProfessional> healthcareProfessionals() {
        return List.copyOf(professionals.values());
    }

    public List<Specialty> specialties() {
        return List.copyOf(specialties.values());
    }

    public HealthcareProfessional selectProfessional(int professionalId) {
        HealthcareProfessional professional = professionals.get(professionalId);
        if (professional == null) {
            throw new IllegalArgumentException("Professional ID not found. Choose an ID from the professionals listed.");
        }
        return professional;
    }

    public List<Schedule> makeAvailability(int professionalId, int locationId, LocalDate startDate,
                                           LocalDate endDate, RepetitionPattern repetitionPattern,
                                           LocalTime startTime, LocalTime endTime) {
        HealthcareProfessional professional = selectProfessional(professionalId);
        ClinicLocation location = locations.get(locationId);
        if (location == null || !professional.locations().contains(location)) {
            throw new IllegalArgumentException(
                    "This professional is not assigned to that clinic. Choose a clinic listed for the professional.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be the same as or later than the start date.");
        }
        validateTimes(startTime, endTime);

        var created = new ArrayList<Schedule>();
        for (LocalDate date = startDate; ; date = date.plusDays(1)) {
            if (repetitionPattern.matches(date)) {
                var schedule = new Schedule(schedules.size() + created.size() + 1, professionalId,
                        location, date.atTime(startTime), date.atTime(endTime));
                Schedule overlap = findOverlap(schedules.values(), schedule);
                if (overlap != null) {
                    throw overlapError(overlap);
                }
                created.add(schedule);
            }
            if (date.equals(endDate)) {
                break;
            }
        }
        if (created.isEmpty()) {
            throw new IllegalArgumentException("No dates from " + startDate + " through " + endDate
                    + " match the selected recurrence. Extend the date range or change the recurrence settings.");
        }
        created.forEach(schedule -> schedules.put(schedule.scheduleId(), schedule));
        return List.copyOf(created);
    }

    public List<Schedule> searchAvailability(int specialtyId, LocalDate startDate, LocalDate endDate) {
        Specialty specialty = specialties.get(specialtyId);
        if (specialty == null) {
            throw new IllegalArgumentException("Specialty ID not found. Choose an ID from the specialties listed.");
        }
        return findAvailable(professional -> professional.specialties().contains(specialty), startDate, endDate);
    }

    public List<Schedule> searchAvailabilityForProfessional(
            int professionalId, LocalDate startDate, LocalDate endDate) {
        if (!professionals.containsKey(professionalId)) {
            throw new IllegalArgumentException("Professional ID not found. Choose an ID from the professionals listed.");
        }
        return findAvailable(professional -> professional.professionalId() == professionalId, startDate, endDate);
    }

    Schedule scheduleForBooking(int scheduleId) {
        Schedule schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule ID not found. Search availability and choose a listed schedule.");
        }
        return schedule;
    }

    private List<Schedule> findAvailable(Predicate<HealthcareProfessional> matches,
                                         LocalDate startDate, LocalDate endDate) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be the same as or later than the start date.");
        }
        if (startDate.isBefore(now.toLocalDate())) {
            throw new IllegalArgumentException("Start date must be today or a future date. Enter a date on or after "
                    + now.toLocalDate() + ".");
        }
        return schedules.values().stream()
                .filter(schedule -> matches.test(professionals.get(schedule.professionalId())))
                .filter(schedule -> !schedule.startTime().isBefore(now)
                        && !schedule.startTime().toLocalDate().isBefore(startDate)
                        && !schedule.startTime().toLocalDate().isAfter(endDate))
                .filter(appointments::isAvailable)
                .sorted(Comparator.comparing(Schedule::startTime).thenComparingInt(Schedule::scheduleId))
                .toList();
    }

    private ClinicLocation requireLocation(Integer locationId) {
        ClinicLocation location = locations.get(locationId);
        if (location == null) {
            throw new IllegalArgumentException(
                    "Clinic ID not found. Import clinic locations and choose an ID from the list.");
        }
        return location;
    }

    private static void validateTimes(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be later than start time.");
        }
    }

    private static Schedule findOverlap(Iterable<Schedule> existing, Schedule candidate) {
        for (Schedule schedule : existing) {
            if (schedule.professionalId() == candidate.professionalId()
                    && schedule.overlaps(candidate.startTime(), candidate.endTime())) {
                return schedule;
            }
        }
        return null;
    }

    private static boolean sameInterval(Schedule existing, Schedule candidate) {
        return existing.location().equals(candidate.location())
                && existing.startTime().equals(candidate.startTime())
                && existing.endTime().equals(candidate.endTime());
    }

    private static IllegalArgumentException overlapError(Schedule schedule) {
        return new IllegalArgumentException("overlaps existing availability (schedule "
                + schedule.scheduleId() + ", " + schedule.startTime() + ", " + schedule.endTime() + ")");
    }
}
