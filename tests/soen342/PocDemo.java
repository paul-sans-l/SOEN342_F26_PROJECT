package soen342;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public final class PocDemo {
    private PocDemo() {}

    public static void main(String[] args) throws IOException {
        Path export = Files.createTempFile("soen342-availability-demo-", ".csv");
        boolean passed = false;
        try {
            runDemo(export);
            passed = true;
        } finally {
            if (!passed) {
                Files.deleteIfExists(export);
            }
        }
    }

    private static void runDemo(Path export) throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2026-11-01T14:00:00Z"), ZoneId.of("America/Toronto"));
        Path sample = Path.of("data", "professional_availability.csv");
        List<String> answers = List.of(
                "1", "", "", "",                           // Import the handout and its supporting data.
                "2",                                       // View the loaded clinic.
                "3",                                       // Patients have no reservations yet.
                "4", "1", "1", "2026-11-09", "2026-11-16",  // Create two Monday intervals.
                "weekly", "MONDAY", "09:00", "12:00",
                "5", "specialty", "1", "2026-11-02", "2026-11-16",
                "6", "Émilie", "Lavoie", "LAVE99010101", "professional", "1",
                "2026-11-09", "2026-11-09", "9", "1",
                "3",                                       // View the confirmed reservation.
                "7", export.toString(),
                "5", "professional", "1", "2026-11-09", "2026-11-09", // Check the booked slot.
                "0");

        var screen = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        screen.println("Unattended PoC demo | Fixed test date: 2026-11-01 (Toronto)");
        screen.println("Sample CSVs are used as inputs. The export goes to a separate temporary file.");
        var transcript = new ByteArrayOutputStream();
        try (var output = new PrintStream(transcript, true, StandardCharsets.UTF_8);
             var input = new BufferedReader(new StringReader(String.join("\n", answers))) {
                 @Override
                 public String readLine() throws IOException {
                     String answer = super.readLine();
                     if (answer != null) {
                         output.println(answer);
                     }
                     return answer;
                 }
             }) {
            new Main(input, output, clock, sample).run();
        }
        String console = transcript.toString(StandardCharsets.UTF_8);
        screen.print(console);
        verifyConsole(console);

        Csv.Table exported = Csv.read(export);
        if (exported.rows().size() != 10 || !exported.header().contains("Location ID")
                || !exported.rows().contains(List.of("Amélie", "Tremblay", "Dr.", "Family Medicine",
                        "2026-11-09", "09:00", "12:00", "1"))
                || !exported.rows().contains(List.of("Amélie", "Tremblay", "Dr.", "Family Medicine",
                        "2026-11-16", "09:00", "12:00", "1"))) {
            throw new IllegalStateException("The export should include all ten schedules and their clinic IDs");
        }
        screen.println("\nPASS: All seven options, reservation display, booked-slot search check, and CSV verification.");
        screen.println("Demo export: " + export);
    }

    private static void verifyConsole(String console) {
        if (console.contains("Error:")) {
            throw new IllegalStateException("An option reported an error during the demo");
        }
        List<String> expectedInOrder = List.of(
                "Choice: 1", "Clinic locations: 1 | Patients: 4 | Medical services: 1",
                "Availability schedules imported: 8",
                "Choice: 2", "Clinic locations:", "1 | Demo Clinic | 1 Demo Avenue",
                "Choice: 3", "Patients and their appointments:", "Patient 1 | Émilie Lavoie",
                "No appointments reserved",
                "Choice: 4", "Availability created: 2",
                "Choice: 5", "1 | 2026-11-02T09:00 to 2026-11-02T09:30",
                "2 | 2026-11-02T09:30 to 2026-11-02T10:00",
                "9 | 2026-11-09T09:00 to 2026-11-09T12:00",
                "10 | 2026-11-16T09:00 to 2026-11-16T12:00",
                "Choice: 6", "Successful identification: patient 1",
                "Confirmed | appointment 1 | 2026-11-09T09:00 to 2026-11-09T12:00",
                "Choice: 3", "Patient 1 | Émilie Lavoie",
                "Appointment 1 | Confirmed | 2026-11-09 09:00 to 12:00 | Demo Clinic | Amélie Tremblay | General Consultation",
                "Choice: 7", "Availability schedules exported: 10",
                "Choice: 5", "No availability found",
                "Choice: 0");
        int next = 0;
        for (String expected : expectedInOrder) {
            int found = console.indexOf(expected, next);
            if (found == -1) {
                throw new IllegalStateException("Missing expected console output: " + expected);
            }
            next = found + expected.length();
        }
    }
}
