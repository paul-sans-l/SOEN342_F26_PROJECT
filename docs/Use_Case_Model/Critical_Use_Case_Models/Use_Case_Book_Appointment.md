### Use Case: Book Appointment:
**Scope:** Appointment Tracking System
**Level:** User Goal
**Actor:** Patient or Administrative Staff

#### Stakeholder and Interests:
* **Patient**: Wants to successfully schedule an appointment with an available healthcare professional.
* **Administrative Staff:** Wants to verify the request for an appointment on behalf of the patients sent in the request
* **Healthcare Professional:** Wants appointments to respect their availability 

---

### First Scenario -- Happy Path

#### Preconditions
* The patient is registered and can be identified in the system by the Administrative Staff
* The healthcare professional has an available schedule that the Administrative Staff is able to slot.

#### Main Scenario:
1. The Patient or Administrative Staff provides the patient's identification information.
2. The system successfully identifies the patient.
3. The Patient or Administrative Staff requests an appointment
4. The system checks the healthcare professional's schedules for available time slots and applicable scheduling constraints
5. The system displays the available appointment dates.
6. The Patient or Administrative Staff selects an available date 
7. The system verifies that the selected date is still available 
8. The system creates the appointment and fills in the time slot. 
9. The system confirms that the appointment has been successfully booked. 

#### Postcondition:
* The appointment is created and associated with the patient and healthcare professionals for a confirmed time slot.

### Second Scenario -- Unhappy Path

#### Preconditions
* The patient is registered and can be identified in the system by the Administrative Staff
* The healthcare professional has an available schedule that the Administrative Staff can slot.

#### Main Scenario
1. The Patient or Administrative Staff provides the patient's identification information
2. The System successfully identifies the patient.
3. The Patient or Administrative Staff requests an appointment.
4. The system checks the healthcare professional's availability and applicable time constraints.
5. The system displays the available appointment dates.
6. The Patient or Administrative Staff requests a date.
7. **The system determines that the requested date is no longer available**
8. The appointment is not created.
9. The system informs the Patient or Administrative Staff that the requested date is unavailable
10. The Patient or Administrative Staff may select another available date.

#### Post Conditions
* No Appointment is created for the unavailable date
