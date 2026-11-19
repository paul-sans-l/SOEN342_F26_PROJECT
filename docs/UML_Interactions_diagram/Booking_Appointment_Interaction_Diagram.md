# UML Interaction Diagram: Book Appointment

This design-level sequence diagram realizes the **RequestAppointment** system operation defined by the SSD `1_Book_Appointment_SSD` and the operation contracts. `BookingInterface` is the boundary object and `AppointmentController` coordinates the interaction. All remaining participants are concepts from the UML domain model (v2.0.0).

```mermaid
sequenceDiagram
    actor Patient as Patient
    actor Staff as Administrative Staff
    participant UI as bookingUI:BookingInterface
    participant Controller as appointmentController:AppointmentController
    participant PatientRecord as patient:Patient
    participant Professional as professional:HealthcareProfessional
    participant Schedule as schedule:Schedule
    participant AppointmentRequest as appointmentRequest:AppointmentRequest
    participant Appointment as appointment:Appointment
    participant ResourceService as resourceService:ResourceService
    participant WaitingList as waitingList:WaitingListService

    alt Patient books directly
        Patient->>UI: requestAppointment(startTime, endTime)
    else Staff books on behalf of patient
        Staff->>UI: requestAppointmentForPatient(patientId, startTime, endTime)
    end
    UI->>Controller: requestAppointment(patientId, startTime, endTime)
    Controller->>PatientRecord: identify(patientId)
    PatientRecord-->>Controller: Patient

    Controller->>Professional: identify(professionalId)
    Professional-->>Controller: HealthcareProfessional
    Controller->>Schedule: getAvailableSlots(startTime, endTime)
    Schedule-->>Controller: availabilityStatus

    alt Appointment available
        Controller->>ResourceService: checkRequiredResources(startTime, endTime)
        ResourceService-->>Controller: resourceAvailability

        opt Resources available
            Controller->>AppointmentRequest: create(Patient, professional, startTime, endTime)
            Note over AppointmentRequest: requestId is generated and status = "Approved"
            AppointmentRequest-->>Controller: requestId
            Controller->>Appointment: create(Patient, professional, AppointmentRequest, startTime, endTime)
            Note over Appointment: appointmentId is generated and status = "Confirmed"
            Appointment-->>Controller: appointmentId
            Controller-->>UI: appointment confirmed
            UI-->>Patient: appointmentId, startTime, endTime, status = "Confirmed"
            UI-->>Staff: appointmentId, patient, startTime, endTime, status = "Confirmed"
        end
    else Appointment unavailable
        Controller-->>UI: date not available
        UI-->>Patient: date not available
        UI-->>Staff: date not available
        opt Patient joins waiting list
            Controller->>WaitingList: registerOnWaitlist(Patient, criteria)
            WaitingList-->>Controller: waiting-list confirmation
            Controller-->>UI: waiting-list confirmation
            UI-->>Patient: waiting-list confirmation
        end
    end
```

## Traceability

| System operation | Design realization | Domain-model support |
|---|---|---|
| `RequestAppointment(startTime, endTime)` | The controller identifies the patient and healthcare professional, checks the professional's schedule, and creates an approved appointment request and confirmed appointment when the slot is available. | `Patient` requests `AppointmentRequest`; `HealthcareProfessional` is assigned to `Appointment`; `Schedule` provides availability; `AppointmentRequest` results in `Appointment`. |
| `requestAppointmentForPatient(patientId, startTime, endTime)` | Administrative Staff submits the booking through the interface on behalf of the patient. | `AdministrativeStaff` may submit an `AppointmentRequest` on behalf of a `Patient`. |

## Operation Contract Realized: `RequestAppointment(startTime, endTime)`

| Postcondition | Type | Realized by |
|---|---|---|
| An `AppointmentRequest` `ar` was created. | Instance creation | `create(Patient, professional, startTime, endTime)` |
| `ar.requestId` was set to a new unique value. | Attribute modification | `AppointmentRequest` constructor |
| `ar.requestedAt` became the current date and time. | Attribute modification | `AppointmentRequest` constructor |
| `ar.status` became `"Approved"`. | Attribute modification | `AppointmentRequest` constructor |
| `ar` was associated with `Patient`. | Association formed | `create(Patient, professional, startTime, endTime)` |
| An `Appointment` `a` was created. | Instance creation | `create(Patient, professional, AppointmentRequest, startTime, endTime)` |
| `a.appointmentId` was set to a new unique value. | Attribute modification | `Appointment` constructor |
| `a.startTime` and `a.endTime` became the given values. | Attribute modification | `Appointment` constructor |
| `a.status` became `"Confirmed"`. | Attribute modification | `Appointment` constructor |
| `a` was associated with `ar`. | Association formed | `AppointmentRequest` parameter of `create` |
| `a` was associated with `Patient`. | Association formed | `Patient` parameter of `create` |
| `a` was associated with `HealthcareProfessional`. | Association formed | `HealthcareProfessional` parameter of `create` |

## Scope Boundary

Resource availability and waiting-list registration are shown only as optional booking alternatives. The base appointment operation creates the appointment only when the professional, time slot, and required resources are available. Waiting-list and resource-allocation details are not additional postconditions of `RequestAppointment`.
