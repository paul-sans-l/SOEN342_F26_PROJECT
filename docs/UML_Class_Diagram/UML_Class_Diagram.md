# Partial UML Class Diagram

Both views describe the same partial design. The first shows the domain classes and their associations. The second shows how the interfaces, controllers, and services use those classes. Showing these relationships in two views makes the connectors easier to follow.

## Domain classes and associations

```mermaid
---
config:
  layout: elk
  elk:
    mergeEdges: false
  class:
    hideEmptyMembersBox: true
---

classDiagram
direction TB

%% =========================
%% DOMAIN OBJECTS
%% =========================

class Patient {
    <<Object>>
    ID patientId
    String firstName
    String lastName
    Date dateOfBirth
    String phoneNumber
    String insuranceNumber
}

class HealthcareProfessional {
    <<Object>>
    ID professionalId
    String firstName
    String lastName
    String professionalInformation
}

class AdministrativeStaff {
    <<Object>>
    ID staffId
}

class Specialty {
    <<Object>>
    ID specialtyId
    String name
}

class Schedule {
    <<Object>>
    ID scheduleId
    DateTime startTime
    DateTime endTime
}

class ClinicLocation {
    <<Object>>
    ID locationId
    String name
    String address
}

class AppointmentRequest {
    <<Object>>
    ID requestId
    DateTime requestedAt
    String status
}

class Appointment {
    <<Object>>
    ID appointmentId
    DateTime startTime
    DateTime endTime
    String status
}

class WaitlistEntry {
    <<Object>>
    ID waitlistEntryId
    DateTime requestedAt
    Date preferredStartDate
    Date preferredEndDate
    Integer priority
    String status
    DateTime removedAt
}

class AppointmentOffer {
    <<Object>>
    ID offerId
    DateTime offeredAt
    String channel
    String outcome
    DateTime respondedAt
}

class MedicalService {
    <<Object>>
    ID serviceId
    String name
    String description
}

class Resource {
    <<Object>>
    ID resourceId
    String name
    String resourceType
    String status
}

class Consultation {
    <<Object>>
    ID consultationId
    DateTime consultationDate
}

class ClinicalRecord {
    <<Object>>
    ID clinicalRecordId
    String observations
    String diagnosis
    String treatmentRecommendations
    String followUpRequirements
}

class Referral {
    <<Object>>
    ID referralId
    String clinicalReason
    String priority
    String status
}

class Prescription {
    <<Object>>
    ID prescriptionId
    Date issueDate
}

class Medication {
    <<Object>>
    ID medicationId
    String name
}

class Invoice {
    <<Object>>
    ID invoiceId
    Decimal amount
    String status
}

class Payment {
    <<Object>>
    ID paymentId
    Decimal amount
    DateTime paymentDate
}

%% =========================
%% DOMAIN RELATIONSHIPS
%% =========================

HealthcareProfessional "0..*" -- "0..*" Specialty
HealthcareProfessional "1" -- "0..*" Schedule
HealthcareProfessional "0..*" -- "1..*" ClinicLocation
ClinicLocation "1" -- "0..*" Schedule
AdministrativeStaff "0..*" -- "1..*" ClinicLocation

Patient "1" -- "0..*" AppointmentRequest
AdministrativeStaff "0..1" -- "0..*" AppointmentRequest
AppointmentRequest "1" -- "0..1" Appointment

Patient "1" -- "0..*" Appointment
HealthcareProfessional "1" -- "0..*" Appointment
MedicalService "1" -- "0..*" Appointment

Patient "1" -- "0..*" WaitlistEntry
HealthcareProfessional "0..1" -- "0..*" WaitlistEntry
Specialty "0..1" -- "0..*" WaitlistEntry
AdministrativeStaff "0..*" -- "0..*" WaitlistEntry
WaitlistEntry "1" -- "0..*" AppointmentOffer
Schedule "1" -- "0..*" AppointmentOffer
AdministrativeStaff "1" -- "0..*" AppointmentOffer
AppointmentOffer "0..1" -- "0..1" Appointment

MedicalService "0..*" -- "0..*" Resource

Appointment "1" -- "0..1" Consultation
HealthcareProfessional "1" -- "0..*" Consultation
Patient "1" -- "0..*" Consultation
Consultation "1" -- "0..1" ClinicalRecord

Patient "1" -- "0..*" Referral
HealthcareProfessional "1" -- "0..*" Referral
Specialty "0..1" -- "0..*" Referral
Referral "0..1" -- "0..*" Appointment

Consultation "1" -- "0..*" Prescription
HealthcareProfessional "1" -- "0..*" Prescription
Patient "1" -- "0..*" Prescription
Prescription "0..*" -- "1..*" Medication

Patient "1" -- "0..*" Invoice
Invoice "1" -- "0..*" Payment
```

## Design classes and dependencies

Consultation follow-ups use an approved `AppointmentRequest` linked to a confirmed `Appointment`.
Resource reservation design is scheduled for Iteration III.

```mermaid
---
config:
  layout: elk
  elk:
    mergeEdges: false
  class:
    hideEmptyMembersBox: true
---

classDiagram
direction LR

class BookingInterface {
    <<Boundary>>
    +requestAppointment(startTime, endTime)
    +requestAppointmentForPatient(patientId, startTime, endTime)
    +displayOperationResult()
}

class AppointmentController {
    <<Controller>>
    +requestAppointment(patientId, startTime, endTime)
    +cancelAppointment()
    +registerArrival()
}

class PatientRepository {
    <<Expert>>
    +identifyPatient(patientId)
    +searchPatient(insuranceNumber)
    +getPatientAppointments()
}

class AppointmentService {
    <<Expert>>
    +createAppointment()
    +validateAppointment()
    +cancelAppointment()
}

class HealthcareProfessionalSchedule {
    <<Expert>>
    +searchAvailability()
    +retrieveAvailableSlots()
    +makeAvailability()
    +selectProfessional()
}

class ResourceService {
    <<Expert>>
    +checkRequiredResources(startTime, endTime)
}

class WaitingListService {
    <<Expert>>
    +registerOnWaitlist(patient, criteria)
    +listEligiblePatients()
    +offerAppointment()
    +acceptOffer()
    +declineOffer()
    +removeFromWaitingList()
}

class ConsultationInterface {
    <<Boundary>>
    +startConsultation(appointmentId)
    +recordClinicalInformation(details)
    +createPrescription()
    +addMedication(medicationId)
    +requestFollowUpAppointment(startTime, endTime)
    +endConsultation()
}

class ConsultationController {
    <<Controller>>
    +startConsultation(appointmentId, professional)
    +recordClinicalInformation(details)
    +createPrescription()
    +addMedication(medicationId)
    +requestFollowUpAppointment(startTime, endTime)
    +endConsultation()
}

class ConsultationService {
    <<Expert>>
    +startConsultation(appointmentId, professional)
    +recordClinicalInformation(details)
    +createPrescription()
    +addMedication(medicationId)
    +requestFollowUpAppointment(startTime, endTime)
    +endConsultation()
}

class ReferralInterface {
    <<Boundary>>
    +searchPatient(insuranceNumber)
    +selectPatient(patientId)
    +createReferral(clinicalReason, priority, specialtyId)
}

class ReferralController {
    <<Controller>>
    +searchPatient(insuranceNumber)
    +selectPatient(patientId)
    +createReferral(clinicalReason, priority, specialtyId, professional)
    -validateReferralInformation(clinicalReason, priority)
}

class ReferralService {
    <<Expert>>
    +createReferral(patient, professional, specialtyId, clinicalReason, priority)
    +validateReferralInformation(clinicalReason, priority)
}

%% =========================
%% EXPERT RESPONSIBILITIES
%% =========================

BookingInterface --> AppointmentController : submits requests

AppointmentController ..> PatientRepository : uses
AppointmentController ..> AppointmentService : uses
AppointmentController ..> HealthcareProfessionalSchedule : uses
AppointmentController ..> ResourceService : uses
AppointmentController ..> WaitingListService : uses

PatientRepository ..> Patient : manages
PatientRepository ..> Appointment : retrieves

AppointmentService ..> Appointment : manages
AppointmentService ..> AppointmentRequest : manages

HealthcareProfessionalSchedule ..> HealthcareProfessional : accesses
HealthcareProfessionalSchedule ..> Schedule : manages
HealthcareProfessionalSchedule ..> Specialty : accesses
HealthcareProfessionalSchedule ..> ClinicLocation : accesses

ResourceService ..> Resource : checks availability
ResourceService ..> MedicalService : checks requirements

WaitingListService ..> WaitlistEntry : manages
WaitingListService ..> AppointmentOffer : manages
WaitingListService ..> HealthcareProfessionalSchedule : uses
WaitingListService ..> AppointmentService : uses

ConsultationInterface --> ConsultationController : submits requests
ConsultationController ..> ConsultationService : uses
ConsultationService ..> Appointment : verifies and completes
ConsultationService ..> Consultation : manages
ConsultationService ..> ClinicalRecord : manages
ConsultationService ..> Prescription : manages
ConsultationService ..> Medication : accesses
ConsultationService ..> HealthcareProfessionalSchedule : checks follow-up availability
ConsultationService ..> AppointmentService : books follow-ups

ReferralInterface --> ReferralController : submits requests
ReferralController ..> PatientRepository : looks up patients
ReferralController ..> ReferralService : uses
ReferralService ..> Referral : manages
ReferralService ..> Patient : accesses
ReferralService ..> HealthcareProfessional : accesses
ReferralService ..> Specialty : accesses

```
