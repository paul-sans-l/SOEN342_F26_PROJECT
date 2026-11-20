# UML Interaction Diagram: Book Appointment

The [booking SSD](../System_Sequence_diagram/1_Book_Appointment_SSD.md) shows the actor's requests and responses. This interaction shows how the PoC identifies a patient, searches availability, and confirms an appointment for the entire selected schedule.

```mermaid
sequenceDiagram
    actor User as Patient or Administrative Staff
    participant Console as Console
    participant UI as booking:BookingInterface
    participant Controller as appointmentController:AppointmentController
    participant Patients as patients:PatientRepository
    participant Availability as availability:HealthcareProfessionalSchedule
    participant Appointments as appointments:AppointmentService
    participant Request as request:AppointmentRequest
    participant Appointment as appointment:Appointment

    User->>Console: identification(firstName, lastName, insuranceNumber)
    Console->>UI: identification(firstName, lastName, insuranceNumber)
    UI->>Controller: identifyPatient(firstName, lastName, insuranceNumber)
    Controller->>Patients: identifyPatient(firstName, lastName, insuranceNumber)

    alt Patient found
        Patients-->>Controller: Patient
        Controller-->>UI: patientId
        UI-->>Console: patientId
        Console-->>User: successful identification (patientId)
        User->>Console: searchAvailability(specialtyId, startDate, endDate)
        Console->>Availability: searchAvailability(specialtyId, startDate, endDate)

        alt No dates available
            Availability-->>Console: empty list
            Console-->>User: no date available
        else Dates available
            Availability-->>Console: available schedules and their IDs
            Console-->>User: available dates, professionals, and locations
            User->>Console: requestAppointment(scheduleId, serviceId)
            Console->>UI: requestAppointment(scheduleId, serviceId)
            UI->>Controller: requestAppointment(patientId, scheduleId, serviceId)
            Controller->>Patients: identifyPatient(patientId)
            Patients-->>Controller: Patient
            Controller->>Availability: scheduleForBooking(scheduleId)
            Availability-->>Controller: Schedule
            Controller->>Availability: selectProfessional(schedule.professionalId)
            Availability-->>Controller: HealthcareProfessional
            Controller->>Appointments: createAppointment(patient, schedule, professional, serviceId)
            Appointments->>Appointments: recheck availability and selected service

            alt Date no longer available
                Appointments-->>Controller: date not available
                Controller-->>UI: date not available
                UI-->>Console: date not available
                Console-->>User: date not available
            else Required resource unavailable
                Appointments-->>Controller: required resource unavailable
                Controller-->>UI: required resource unavailable
                UI-->>Console: required resource unavailable
                Console-->>User: required resource unavailable
            else Appointment and required resources available
                Appointments->>Request: create approved request for patient
                Request-->>Appointments: AppointmentRequest
                Appointments->>Appointment: create confirmed appointment for the full schedule interval
                Appointment-->>Appointments: Appointment
                Appointments-->>Controller: Appointment
                Controller-->>UI: Appointment
                UI-->>Console: Appointment
                Console-->>User: confirmed (appointmentId, startTime, endTime, status)
            end
        end
    else Patient could not be identified
        Patients-->>Controller: identification error
        Controller-->>UI: identification error
        UI-->>Console: identification error
        Console-->>User: identification error
    end
```

## Traceability

| System operation | Design realization | Domain-model support |
|---|---|---|
| `identification(firstName, lastName, insuranceNumber)` | `BookingInterface` keeps the ID returned by `PatientRepository` for the next booking request. | `Patient` holds the lookup fields and its unique `patientId`. |
| `searchAvailability(specialtyId, startDate, endDate)` | `HealthcareProfessionalSchedule` returns bookable schedules. Direct search can also filter by professional. | A `HealthcareProfessional` has specialties and schedules. Each schedule is hosted at a clinic location. |
| `requestAppointment(scheduleId, serviceId)` | `AppointmentController` resolves the patient, schedule, and professional. `AppointmentService` rechecks the interval and selects the medical service by ID. | `AppointmentRequest` belongs to a patient and results in an `Appointment` for the patient, professional, and medical service. |

The [booking operation contract](../System_operations_contracts/Contracts_System_Operations.md#system-operation-1-requestappointmentscheduleid-serviceid) defines the approved request and confirmed appointment. The [join-waitlist SSD](../System_Sequence_diagram/8_Join_Waitlist_SSD.md) documents the wider waitlist flow. The PoC reports unavailable slots and uses a demo service with no additional resource requirements.
