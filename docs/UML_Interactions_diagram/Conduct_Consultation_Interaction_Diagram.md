# UML Interaction Diagram: Conduct Consultation

This design-level sequence diagram realizes the **Conduct Consultation** system operations defined by the existing SSD and operation contracts. `ConsultationInterface` is the boundary object and `ConsultationController` coordinates the interaction. All remaining participants are concepts from the UML domain model.

```mermaid
sequenceDiagram
    actor HCP as Healthcare Professional
    participant UI as consultationUI:ConsultationInterface
    participant Controller as consultationController:ConsultationController
    participant Appointment as appointment:Appointment
    participant Consultation as consultation:Consultation
    participant Record as clinicalRecord:ClinicalRecord
    participant Prescription as prescription:Prescription
    participant Medication as medication:Medication
    participant Schedule as schedule:Schedule
    participant Request as followUpRequest:AppointmentRequest
    participant FollowUp as followUpAppointment:Appointment

    HCP->>UI: startConsultation(appointmentId)
    UI->>Controller: startConsultation(appointmentId, HCP)
    Controller->>Appointment: verifyAssignedProfessionalAndStatus(HCP)
    Appointment-->>Controller: Patient and status

    alt Healthcare Professional is not assigned
        Controller-->>UI: access denied
        UI-->>HCP: access denied
    else Appointment status is not Arrived
        Controller-->>UI: patient has not arrived
        UI-->>HCP: patient has not arrived
    else Appointment is valid
        Controller->>Consultation: create(Appointment, HCP, Patient, consultationDate)
        Consultation-->>Controller: consultationId
        Controller-->>UI: consultation started
        UI-->>HCP: consultationId and Patient information

        HCP->>UI: recordClinicalInformation(details)
        UI->>Controller: recordClinicalInformation(details)
        Controller->>Record: create(details)
        Controller->>Consultation: associateClinicalRecord(Record)
        Controller-->>UI: clinical record saved
        UI-->>HCP: clinicalRecordId

        opt Prescription is needed
            HCP->>UI: createPrescription()
            UI->>Controller: createPrescription()
            Controller->>Prescription: create(issueDate, HCP, Patient)
            Controller->>Consultation: addPrescription(Prescription)
            Controller-->>UI: prescription created
            UI-->>HCP: prescriptionId and issueDate

            loop For each medication
                HCP->>UI: addMedication(medicationId)
                UI->>Controller: addMedication(medicationId)
                Controller->>Medication: identify(medicationId)
                alt Medication exists
                    Medication-->>Controller: Medication
                    Controller->>Prescription: addMedication(Medication)
                    Controller-->>UI: medication added
                    UI-->>HCP: medicationId and name
                else Medication does not exist
                    Controller-->>UI: medication not found
                    UI-->>HCP: medication not found
                end
            end
        end

        opt Follow-up appointment is needed
            HCP->>UI: requestFollowUpAppointment(startTime, endTime)
            UI->>Controller: requestFollowUpAppointment(startTime, endTime)
            Controller->>Schedule: checkAvailability(startTime, endTime)
            Schedule-->>Controller: availability result
            alt Healthcare Professional is available
                Controller->>Request: create(Patient, requestedAt, Approved)
                Controller->>FollowUp: create(startTime, endTime, Confirmed, Patient, HCP)
                Controller->>Request: associateAppointment(FollowUp)
                Controller-->>UI: follow-up appointment created
                UI-->>HCP: appointmentId, time, and Confirmed status
            else Healthcare Professional is unavailable
                Controller-->>UI: professional not available
                UI-->>HCP: professional not available at requested time
            end
        end

        HCP->>UI: endConsultation()
        UI->>Controller: endConsultation()
        Controller->>Consultation: verifyClinicalRecordAndPrescriptions()
        alt A Prescription has no Medication
            Controller-->>UI: prescription requires medication
            UI-->>HCP: prescription must contain a medication
        else Consultation is complete
            Controller->>Appointment: setStatus(Completed)
            Controller-->>UI: consultation completed
            UI-->>HCP: consultationId and Completed status
        end
    end
```

## Traceability

| System operation | Design realization | Domain-model support |
|---|---|---|
| `startConsultation(appointmentId)` | The controller verifies the assigned professional and `Arrived` status before creating the consultation. | `Appointment` results in `Consultation`; `HealthcareProfessional` conducts it; `Patient` receives it. |
| `recordClinicalInformation(...)` | A clinical record is created and associated with the active consultation. | `Consultation` produces zero or one `ClinicalRecord`. |
| `createPrescription()` | A prescription is created for the active consultation, professional, and patient. | `Consultation` may produce `Prescription`; the professional issues it and the patient receives it. |
| `addMedication(medicationId)` | The medication is identified before it is associated with the prescription. | `Prescription` contains one or more `Medication` objects. |
| `requestFollowUpAppointment(startTime, endTime)` | The professional's schedule is checked before an approved request and confirmed appointment are created. | `HealthcareProfessional` has `Schedule`; `AppointmentRequest` may result in `Appointment`. |
| `endConsultation()` | The consultation record and prescriptions are validated before the appointment becomes `Completed`. | `Appointment.status`, `Consultation`, `ClinicalRecord`, `Prescription`, and `Medication` support the contract postconditions. |

## Scope Boundary

Waiting-list offers and resource allocation are not added to this interaction. The Iteration II requirements introduce those concerns for appointment availability and booking, while the existing Conduct Consultation SSD and operation contracts do not invoke them.
