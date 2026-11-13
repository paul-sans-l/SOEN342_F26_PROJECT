> Note Preview can be done through the Github page when accessing the file OR could be read through VSCode using a UML translator and the Markdown in Preview Mode. 

```mermaid

flowchart LR
    Patient["Patient"]
    Staff["Administrative Staff"]

    subgraph ATS["Appointment Tracking System"]
        UI["Booking Interface"]
        AP["Appointment Controller"]
        PS["Patient Repository"]
        AS["Appointment Service"]
        HCS["Healthcare Professional Schedule"]
        RS["Resource Service"]
        DB["Appointment Database"]
        WL["Waiting List Service"]
    end

    Patient -->|"1: Request appointment"| UI
    Staff -->|"1: Request appointment on behalf of patient"| UI

    UI -->|"2: Submit booking request"| AP

    AP -->|"2.1: Search patient"| PS
    PS -->|"2.2: Return patient information"| AP

    AP -->|"2.3: Check slot availability"| AS
    AS -->|"2.3.1: Retrieve available slots"| HCS
    HCS -->|"2.3.2: Return availability status"| AS
    AS -->|"2.3.3: Return availability result"| AP

    AP -->|"3: Check required resources [slot available]"| RS
    RS -->|"3.1: Return resource availability"| AP

    AP -->|"3.2: Allocate resources [resources available]"| RS
    AP -->|"3.3: Save appointment [resources available]"| DB
    DB -->|"3.4: Return booking confirmation"| AP

    AP -->|"4: Request waitlist registration [no slot available]"| WL
    WL -->|"4.1: Register patient and requested criteria"| DB
    DB -->|"4.2: Return registration result"| WL
    WL -->|"4.3: Return waitlist confirmation"| AP

    AP -->|"5: Return operation result (booking or waitlist confirmation)"| UI
    UI -->|"5.1: Display operation result"| Patient
    UI -->|"5.1: Display operation result"| Staff

```

2.x: Common initial interactions (patient search and availability checks).



3.x: Successful booking flow.



4.x: Waiting-list registration flow.



5.x: Communicate the final result.