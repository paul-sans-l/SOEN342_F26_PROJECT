> Note Preview can be done through the Github page when accessing the `docs\System_operations_contracts\System_operations.md` file OR could be read through VSCode using a UML translator and the Markdown in Preview Mode. 

```mermaid
classDiagram
    class System {
        RequestAppointment(Date)
        recordClinicalInformation(observations, diagnosis, treatmentRecommendations, followUpRequirements)
        CreatePrescription()
        Referal(Patient,Physician,Reason ,Priority)
        RegisterArrival(AppointmentId)
        }
```