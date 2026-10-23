### Use Case: Create Referral:
**Scope:** Appointment Tracking System
**Level:** User Goal
**Actor:** Health Care Professional

#### Stakeholder and Interests:
* **Patient**: Wants to successfully schedule an appointment with an available healthcare professional.
* **Administrative Staff:** Wants to verify the request for an appointment on behalf of the patients sent in the request
* **Healthcare Professional:** Wants appointments to respect their availabilities 

---

### First Scenario -- Happy Path

#### Preconditions
* The Healthcare Professional is authorized to create a referral
* The patient exists in the system
* The Healthcare Professional has the required referral information

#### Main Scenario:
1. The Healthcare Professional requests to create a referral 
2. The system requests the required referral information
3. The healthcare professional provides the patient, referring physicians, clinical reason, and priority
4. The system validates the provided referral information
5. The system determines that the referral information is valid.
6. The system creates the referral 
7. The system confirms to the Healthcare Professional that the referral was successfully created. 

#### Postcondition:
* A referral is created and associated with the patient and referring Healthcare Professional 

### Second Scenario -- Unhappy Path

#### Preconditions
* The Healthcare Professional is authorized to create a referral
* The Patient exists in the System

#### Main Scenario
1. The Healthcare Professional requests to create a referral
2. The system requests the required referral information
3. The Healthcare Professional provides the patient, referring physician, clinical reason, and priority
4. The System validates the provided referral information
5. The system determines that some of the referral information is invalid or incomplete
6. The referral is not created.
7. The system notifies the Healthcare Professional that the provided information is invalid.
8. The Healthcare Professional may correct the information and try again. 

#### Post Conditions
* No referral is created with the invalid information. 
