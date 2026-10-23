### Use Case: Create Referral:
**Scope:** Appointment Tracking System
**Level:** User Goal
**Actor:** Health Care Professional

#### Stakeholder and Intrests:
* **Patient**: Wants to successfully schedule an appointment with an available healthcare professional.
* **Administrative Staff:** Wnats to verify the request of an appointment on behalf of the patients sent in request
* **Healthcare Professional:** Wants appointments to respect their availablilities 

---

### First Scenario -- Happy Path

#### Preconditions
* The Healthcare Professional is authorized to create a referral
* The patient exist in the system
* The Healthcare Professional has the required refferal information

#### Main Scenario:
1. The Healthcare Professioanal request to create a referral 
2. The system request the required referral information
3. The healthcare professional provides the patient, referring physicians, clinical reason, and priority
4. The system validates the provided referral information
5. The system determine that the referral information is valid.
6. The system creates the referral 
7. The system confirm to the Healthcare Professional that the referral was successfully created. 

#### Postcondition:
* A referral is created and assciated with the patient and referring Healthcare Professional 

### Second Scenario -- Unhappy Path

#### Preconditions
* The Healthcare Professional is authorized to create a referral
* The Patient exists in the System

#### Main Scenario
1. The Healthcare Professional requests to create a referral
2. The system requests the required referral information
3. The Healthcare Provides the patient, referring physician, clinical reason, and priority
4. The System validates the provided referrral information
5. The system determine that some of the referral information is invald or incomplete
6. THe refereral is not created.
7. The system notifies the Healthcare Professional that the provided information is invalid.
8. The Healthcare Professional may correct the information and try again. 

#### Post Conditions
* No referral is created with the invalid information. 