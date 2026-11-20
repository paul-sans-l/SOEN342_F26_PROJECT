# SOEN342_F26_PROJECT

Kadio Paul-Philippe Ehui - 40269547 (Team Leader) Section H HB
Luca Nasrallah - 40316548 - Section H HB
Juan Carlos Bastidas - 40228022 - Section H HC

---
## Phase II updates

Phase II extends the appointment-tracking design with waitlists and resource requirements and adds a runnable Java console proof of concept (PoC).

| Area | What changed |
| --- | --- |
| [Use-case model](./docs/Use_Case_Model/Use_Case_Model.md#use-case-diagram) | The Mermaid diagram groups four critical and 17 non-critical cases. Phase II adds availability search and creation, waitlist management and phone offers, and conditional resource checks. The Mermaid diagram replaces the standalone use-case PNG. |
| [UML domain model](./docs/UML_Domain_Model/UML_Domain_model.md#iteration-ii-domain-model) | Iteration II adds waitlist entries, appointment offers, and the distinction between resources required by a medical service and resources allocated to an appointment. |
| [System sequence diagrams](./docs/System_Sequence_diagram/) | All ten SSDs now have editable Mermaid Markdown. The booking SSD uses `scheduleId` and `serviceId`. The consultation SSD shows a confirmed follow-up appointment, and its outdated PNG was removed. |
| [UML interaction diagrams](./docs/UML_Interactions_diagram/) | Design-level interactions cover Book Appointment, Conduct Consultation, and Create Referral. The booking interaction and [operation contract](./docs/System_operations_contracts/Contracts_System_Operations.md#1-book-appointment) use the identified patient and the full interval of the selected schedule. |
| [Partial UML class diagram](./docs/UML_Class_Diagram/UML_Class_Diagram.md) | The class diagram presents domain associations and design dependencies as two views of the same model. |
| [Java PoC](./src/soen342/) and [sample data](./data/) | The console runs the five required operations in one in-memory workflow. It imports availability, creates recurring intervals, searches for free schedules, books a selected interval, and exports availability to CSV. Separate CSVs supply clinics, patients, and services. |

The use-case model includes staff changes to waitlist priority. The corresponding SSD is still pending.

## Run the Iteration II PoC

Run [the launcher](./run-poc.ps1) in PowerShell from the repository root with JDK 17 or newer on `PATH`. It compiles the Java sources and opens the console menu.

```powershell
.\run-poc.ps1
```

To suggest a different file for **Import availability**, pass its path when launching.

```powershell
.\run-poc.ps1 -AvailabilityCsv 'C:\path\to\availability.csv'
```

### Run the automated walkthrough

```powershell
.\run-poc.ps1 -Demo
```

The demo starts with empty in-memory data, imports fictional patients and professionals from the [demo CSVs](./data/demo/), and prints the whole console session. It uses November 1, 2026 in Toronto as a fixed test date so the sample availability can be searched and booked. The menu repeats after each action. Follow each `Choice:` line to see what runs next. A blank answer accepts the default shown in brackets.

### What the demo output means

Options **1, 4, 5, 6, and 7** cover the five PoC requirements. Options **2 and 3** let staff inspect the data and check what happened after booking.

| What you see | What happened |
| --- | --- |
| **Choice 1 — Import availability** | The demo loads **1 clinic, 8 patients, and 1 medical service**, then imports **10 schedule intervals** for **7 healthcare professionals** at Demo Clinic. The availability CSV supplies the professionals and their specialties. |
| **Choice 2 — View clinic locations** | `1 \| Demo Clinic \| 1 Demo Avenue` shows the clinic's ID, name, and address. The demo uses clinic ID `1` when creating availability. |
| **Choice 3 — View patients and their appointments** | The list shows eight patients, from `Patient 1 \| Emily Walker` to `Patient 8 \| Emma Wilson`. Each shows `No appointments reserved`. |
| **Choice 4 — Make availability** | The demo selects **Grace Miller** at Demo Clinic and asks for Mondays from **09:00 to 12:00** between November 9 and 16. Both Mondays match, so the system creates **schedule `11` on November 9** and **schedule `12` on November 16**. |
| **Choice 5 — Search availability** | The demo searches **Family Medicine** from November 2 through 16. It finds four free intervals for Grace Miller, with `1` and `2` from the imported CSV and `11` and `12` from the recurring availability. The `T` in `2026-11-09T09:00` separates the date from the time. |
| **Choice 6 — Book an appointment** | The app identifies **Emily Walker** as patient `1` and books schedule `11` for service `1`, **General Consultation**. `Confirmed \| appointment 1` means the entire **09:00–12:00** interval is reserved. Schedule `11` identifies the time slot. Appointment `1` identifies the booking. |
| **Choice 3 again — Check the reservation** | Emily's confirmed General Consultation appears under her name for **November 9, 09:00 to 12:00, at Demo Clinic with Grace Miller**. The other seven patients still have no reservations. |
| **Choice 7 — Export availability** | The app writes **12 schedule rows** to a temporary CSV, including the 10 imported rows and the 2 it created. The path appears on the `Demo export` line at the end. The export includes all stored schedule intervals. Appointment status stays in memory for the current run. |
| **Choice 5 again — Check the booked slot** | The demo searches for Grace Miller on **November 9 only**. `No availability found` is expected because her only interval on that date is booked. |
| **Choice 0 — Exit** | `PASS` means all seven choices produced the expected results, the appointment appeared under the right patient, the booked slot disappeared from search, and the export contained the expected rows. |

### Use the interactive menu

An interactive run starts empty and uses today's date. On the first import, accept the sample supporting CSVs or type `custom` to select your own clinic, patient, and service files. Later imports reuse those records. Use option 4 to create future intervals when the handout's dates are in the past. Weekly recurrence accepts selected weekdays. Monthly recurrence uses a day of the month and skips months without that day.

Use option 7 with a new path to save a copy, or enter the path of your working CSV to replace it. The export includes a `Location ID` column for reimport with the matching clinic CSV. The demo service has no additional resource requirements. Services that require resources return the unavailable-resource outcome while reservation design is deferred.

## Repository tree

The Phase II additions in `src/` and `data/` run the PoC. The documents in `docs/` describe the behaviors, system responses, and domain relationships that the Java code follows.

```text
.
├── README.md
├── data/                                            # CSV inputs for the console
│   ├── clinic_locations.csv                         # Demo clinic and location ID
│   ├── medical_services.csv                         # Demo service and resource requirement flag
│   ├── patients.csv                                 # Example patients for identification
│   ├── professional_availability.csv                # Availability rows from the handout
│   └── demo/                                        # Fictional data for the automated walkthrough
│       ├── patients.csv                             # Eight fictional patients
│       └── professional_availability.csv            # Ten intervals for seven fictional professionals
├── docs/
│   ├── System_Sequence_diagram/                     # Actor requests, responses, and alternate flows
│   ├── System_operations_contracts/
│   │   └── Contracts_System_Operations.md           # Preconditions and results of system operations
│   ├── UML_Class_Diagram/
│   │   └── UML_Class_Diagram.md                      # Partial design classes and dependencies
│   ├── UML_Domain_Model/
│   │   └── UML_Domain_model.md                      # Iteration I and II entities and relationships
│   ├── UML_Interactions_diagram/                    # How objects carry out the system operations
│   │   ├── Booking_Appointment_Interaction_Diagram.md
│   │   ├── Conduct_Consultation_Interaction_Diagram.md
│   │   └── Create_Referral_Interaction_Diagram.md
│   └── Use_Case_Model/
│       ├── Use_Case_Model.md                        # Critical and non-critical cases in one diagram
│       └── Critical_Use_Case/                       # Written success and alternate flows
│           ├── Use_Case_Book_Appointment.md
│           ├── Use_Case_Create_Referral.md
│           └── Use_Case_Register_Patient_Arrival.md
├── run-poc.ps1                                      # Launches the console or runs the demo
├── src/
│   └── soen342/                                     # Java source for the Iteration II PoC
│       ├── Main.java                                # Console entry point and menu
│       ├── BookingInterface.java                    # Patient identification and booking boundary
│       ├── AppointmentController.java               # Resolves patient, schedule, and service for booking
│       ├── AppointmentService.java                  # Rechecks availability and records the appointment
│       ├── PatientRepository.java                   # Loads and identifies patients
│       ├── HealthcareProfessionalSchedule.java      # Creates, searches, imports, and exports schedules
│       ├── RepetitionPattern.java                   # Weekly and monthly availability patterns
│       ├── Csv.java                                 # Reads and writes UTF-8 CSV files
│       ├── Appointment.java                         # Confirmed booking for the selected interval
│       ├── AppointmentRequest.java                  # Request linked to the confirmed appointment
│       ├── ClinicLocation.java                      # Clinic hosting a schedule
│       ├── HealthcareProfessional.java              # Provider, specialties, and practice locations
│       ├── MedicalService.java                      # Service selected when booking
│       ├── Patient.java                             # Patient identity and contact details
│       ├── Schedule.java                            # One bookable interval
│       └── Specialty.java                           # Specialty used in availability searches
└── tests/
    └── soen342/
        └── PocDemo.java                             # Exercises all seven options and verifies the export
```

