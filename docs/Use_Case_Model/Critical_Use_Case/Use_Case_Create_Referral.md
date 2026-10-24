# Fully Dressed Use Case: Create Referral

**Scope:** Appointment Tracking System  
**Level:** User Goal  
**Primary Actor:** Healthcare Professional

## Stakeholders and Interests

- **Patient:** Wants the required referral to be created with accurate information.
- **Administrative Staff:** Wants valid referral information to be available when an appointment requires a referral.
- **Healthcare Professional:** Wants to create a valid referral for the patient.

## Preconditions

- The Healthcare Professional is authorized to create a referral.
- The patient exists in the system.
- The Healthcare Professional has the required referral information.

## Trigger

The Healthcare Professional requests to create a referral.

## Success Guarantee

A referral is created and associated with the patient and referring Healthcare Professional.

## Minimal Guarantee

No referral is created with invalid or incomplete information.

## Main Success Scenario

1. The Healthcare Professional requests to create a referral.
2. The system requests the required referral information.
3. The Healthcare Professional provides the patient, referring physician, clinical reason, and priority.
4. The system validates the provided referral information.
5. The system determines that the referral information is valid.
6. The system creates the referral.
7. The system confirms to the Healthcare Professional that the referral was successfully created.

## Extensions / Alternative Flows

### 5a. The referral information is invalid or incomplete

1. The system determines that some of the referral information is invalid or incomplete.
2. The referral is not created.
3. The system notifies the Healthcare Professional that the provided information is invalid.
4. The Healthcare Professional may correct the information and continue from Step 3.
