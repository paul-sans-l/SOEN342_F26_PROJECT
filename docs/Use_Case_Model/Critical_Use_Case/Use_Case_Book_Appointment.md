# Fully Dressed Use Case: Book Appointment

**Scope:** Appointment Tracking System  
**Level:** User Goal  
**Primary Actor:** Patient or Administrative Staff

## Stakeholders and Interests

- **Patient:** Wants to successfully schedule an appointment with an available healthcare professional.
- **Administrative Staff:** Wants to verify and complete an appointment request on behalf of a patient.
- **Healthcare Professional:** Wants appointments to respect their availability.

## Preconditions

- The patient is registered and can be identified in the system by the Administrative Staff.
- The healthcare professional has an available schedule that the Administrative Staff can use to book an appointment.

## Trigger

The Patient or Administrative Staff requests to book an appointment.

## Success Guarantee

The appointment is created and associated with the patient and healthcare professional for a confirmed time slot.

## Minimal Guarantee

No appointment is created for an unavailable date.

## Main Success Scenario

1. The Patient or Administrative Staff provides the patient's identification information.
2. The system successfully identifies the patient.
3. The Patient or Administrative Staff requests an appointment.
4. The system checks the healthcare professional's schedule for available time slots and applicable scheduling constraints.
5. The system displays the available appointment dates.
6. The Patient or Administrative Staff selects an available date.
7. The system verifies that the selected date is still available.
8. The system creates the appointment and fills the time slot.
9. The system confirms that the appointment has been successfully booked.

## Extensions / Alternative Flows

### Step 7a. The requested date is no longer available

1. The system determines that the requested date is no longer available.
2. The appointment is not created.
3. The system informs the Patient or Administrative Staff that the requested date is unavailable.
4. The Patient or Administrative Staff may select another available date and continue from Step 6.
