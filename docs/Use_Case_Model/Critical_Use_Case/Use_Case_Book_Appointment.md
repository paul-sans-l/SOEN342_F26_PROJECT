# Fully Dressed Use Case: Book Appointment

**Scope:** Appointment Tracking System  
**Level:** User Goal  
**Primary Actor:** Patient or Administrative Staff

## Stakeholders and Interests

- **Patient:** Wants to successfully schedule an appointment with an available healthcare professional or request placement on a waiting list when no suitable slot is available.
- **Administrative Staff:** Wants to verify and complete an appointment request on behalf of a patient, including a request that follows an accepted waiting-list offer.
- **Healthcare Professional:** Wants appointments to respect their availability and the resource needs of the scheduled medical service.

## Preconditions

- The patient is registered and can be identified in the system by the Administrative Staff.
- The requested healthcare professional, specialty, or medical service can be identified in the system.

## Trigger

The Patient or Administrative Staff requests to book an appointment.

## Success Guarantee

The appointment is created and associated with the patient, healthcare professional, and medical service for a confirmed time slot. Any predefined resources required by the medical service are allocated to the appointment.

## Minimal Guarantee

No appointment or resource allocation is created for a time slot when the healthcare professional or a required resource is unavailable.

## Main Success Scenario

1. The Patient or Administrative Staff provides the patient's identification information.
2. The system successfully identifies the patient.
3. The Patient or Administrative Staff requests an appointment for a medical service and provides the applicable healthcare professional, specialty, or date-range criteria.
4. The system checks the healthcare professional's schedule for available time slots that match the request.
5. If the medical service requires additional resources, the system checks the availability of the required predefined examination room, equipment, or supporting human role.
6. The system displays the appointment time slots for which the healthcare professional and any required resources are available.
7. The Patient or Administrative Staff selects an available time slot.
8. The system verifies that the selected time slot and any required resources are still available.
9. The system creates the appointment, fills the healthcare professional's time slot, and allocates any resources required by the medical service.
10. The system confirms that the appointment has been successfully booked.

## Extensions / Alternative Flows

### Step 4a. No suitable appointment slot is available

1. The system informs the Patient or Administrative Staff that no suitable appointment slot is available.
2. The Patient may request placement on the waiting list.
3. The Patient provides waiting-list criteria for a particular healthcare professional, specialty, or date range.
4. The system creates a waiting-list entry for the Patient and records the provided criteria.
5. The system confirms that the Patient has been placed on the waiting list, and the use case ends without creating an appointment.

### Step 8a. The selected time slot or a required resource is no longer available

1. The system determines that the selected time slot or a resource required by the medical service is no longer available.
2. The appointment and resource allocations are not created.
3. The system informs the Patient or Administrative Staff that the selected time slot is unavailable.
4. The Patient or Administrative Staff may select another available time slot and continue from Step 7.

### Alternative entry: The Patient accepts a waiting-list offer

1. Administrative Staff records that the Patient accepted an available appointment offered by phone.
2. The system retrieves the Patient, offered time slot, and associated appointment request.
3. The booking process continues from Step 8.

## Iteration II Traceability

- **Waiting-list alternative:** Iteration II requires waiting-list support when a healthcare professional has no available slots and permits criteria for a particular professional, specialty, or date range. In the domain model, `AppointmentRequest` may create a `WaitlistEntry`, and that entry is associated with the `Patient` and optional preferred `HealthcareProfessional` or `Specialty`.
- **Accepted phone offer:** Iteration II states that an available appointment may be offered by phone to an eligible waitlisted patient based on priority. The use-case model connects an accepted **Offer Appointment** to **Book Appointment**, while the domain model connects `AppointmentOffer` to the resulting `Appointment`.
- **Conditional resource availability:** Iteration II states that some medical services require a predefined examination room, specialized equipment, or supporting human role. The use-case model makes **Check Resource Availability** a conditional extension of **Check Availability**. The domain model represents the same scope through `MedicalService`, `ResourceRequirement`, `Resource`, and `ResourceAllocation`.
