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
| [Partial UML class diagram](https://github.com/paul-sans-l/SOEN342_F26_PROJECT/pull/29) | The class diagram is tracked separately in PR #29. It presents domain associations and design dependencies as two views of the same model. |
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

1. Load the clinic, patient, and medical service data with options 1–3. Press Enter at each path prompt to use the examples in `data/`. The patient examples come from the handout. The clinic and medical service are demo records.
2. Select option 4 and press Enter to import the suggested availability CSV, or type another path. For the handout's seven-column file, accept clinic ID `1` after loading the demo clinic.
3. Use **Make availability** to create future intervals for a professional. Weekly recurrence accepts selected weekdays. Monthly recurrence uses a day of the month and skips months without that day. The handout's original availability dates are retained for import.
4. Search by professional or specialty. To book for a walk-in or phone caller, use option 7 to identify the patient and select a returned schedule ID and a service ID. The appointment reserves the entire selected interval.
5. Use option 8 to save all stored schedules to a CSV. Enter a new path to save a copy or enter your working CSV's path to replace it with the export. The export includes a `Location ID` column, so it can be imported again after loading the matching clinic CSV.

Each run starts with empty in-memory data. Choosing a file at launch sets the suggested import path. The console reads it when you choose import and writes availability when you choose export. Confirmed appointments are stored in memory for the current run. The demo service has no additional resource requirements. Services marked as requiring resources return the unavailable-resource outcome while reservation design is deferred.

## Repository tree

The Phase II additions in `src/` and `data/` run the PoC. The documents in `docs/` describe the behaviors, system responses, and domain relationships that the Java code follows.

```text
.
├── README.md
├── data/                                            # CSV inputs for the console
│   ├── clinic_locations.csv                         # Demo clinic and location ID
│   ├── medical_services.csv                         # Demo service and resource requirement flag
│   ├── patients.csv                                 # Example patients for identification
│   └── professional_availability.csv                # Availability rows from the handout
├── docs/
│   ├── System_Sequence_diagram/                     # Actor requests, responses, and alternate flows
│   ├── System_operations_contracts/
│   │   └── Contracts_System_Operations.md           # Preconditions and results of system operations
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
├── run-poc.ps1                                      # Compiles and launches the console
└── src/
    └── soen342/                                     # Java source for the Iteration II PoC
        ├── Main.java                                # Console entry point and menu
        ├── BookingInterface.java                    # Patient identification and booking boundary
        ├── AppointmentController.java               # Resolves patient, schedule, and service for booking
        ├── AppointmentService.java                  # Rechecks availability and records the appointment
        ├── PatientRepository.java                   # Loads and identifies patients
        ├── HealthcareProfessionalSchedule.java      # Creates, searches, imports, and exports schedules
        ├── RepetitionPattern.java                   # Weekly and monthly availability patterns
        ├── Csv.java                                 # Reads and writes UTF-8 CSV files
        ├── Appointment.java                         # Confirmed booking for the selected interval
        ├── AppointmentRequest.java                  # Request linked to the confirmed appointment
        ├── ClinicLocation.java                      # Clinic hosting a schedule
        ├── HealthcareProfessional.java              # Provider, specialties, and practice locations
        ├── MedicalService.java                      # Service selected when booking
        ├── Patient.java                             # Patient identity and contact details
        ├── Schedule.java                            # One bookable interval
        └── Specialty.java                           # Specialty used in availability searches
```

