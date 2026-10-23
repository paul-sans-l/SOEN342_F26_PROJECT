# Fully Dressed Use Case: Register Patient Arrival

**Scope:** Appointment Tracking System  
**Level:** User Goal  
**Primary Actor:** Administrative Staff

## Stakeholders and Interests

- **Patient:** Wants their arrival to be registered for the correct scheduled appointment.
- **Administrative Staff:** Wants to find the correct patient and register their arrival for today's scheduled appointment.
- **Healthcare Professional:** Wants the appointment status to accurately indicate whether the patient has arrived.

## Preconditions

- The Administrative Staff has patient information to use as search criteria.

## Trigger

The Administrative Staff begins registering a patient's arrival for a scheduled appointment.

## Success Guarantee

The patient's arrival is registered, and the appointment status is set to `Arrived`.

## Minimal Guarantee

The appointment status is not changed when the arrival cannot be registered.

## Main Success Scenario

1. The Administrative Staff searches for the patient using the patient's insurance number. The search may also use the patient's date of birth, phone number, or first and last name.
2. The system displays a list of matching patients, including each patient's ID, first name, last name, and date of birth.
3. The Administrative Staff selects the patient using the patient ID.
4. The system displays the patient's appointment scheduled for today, including the appointment ID, start time, end time, status, and the healthcare professional's first and last name.
5. The Administrative Staff requests to register the patient's arrival using the appointment ID.
6. The system validates the appointment, registers the patient's arrival, and sets the appointment status to `Arrived`.
7. The system confirms the arrival and displays the appointment ID with the status set to `Arrived`.

## Extensions / Alternative Flows

### 2a. No patient matches the search criteria

1. The system informs the Administrative Staff that no patient was found.
2. The arrival is not registered.
3. The use case ends unsuccessfully.

### 4a. The patient has no appointment scheduled for today

1. The system informs the Administrative Staff that no appointment is scheduled today.
2. The arrival is not registered.
3. The use case ends unsuccessfully.

### 6a. The appointment does not belong to the selected patient

1. The system informs the Administrative Staff that the appointment does not match the selected patient.
2. The arrival is not registered.
3. The use case ends unsuccessfully.

### 6b. The appointment status is `Cancelled`

1. The system informs the Administrative Staff that the appointment is cancelled.
2. The arrival is not registered.
3. The use case ends unsuccessfully.

### 6c. The appointment status is already `Arrived`

1. The system informs the Administrative Staff that the arrival has already been registered.
2. The appointment status is not changed.
3. The use case ends unsuccessfully.
