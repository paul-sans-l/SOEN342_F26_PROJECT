package soen342;

public final class AppointmentController {
    private final PatientRepository patients;
    private final HealthcareProfessionalSchedule availability;
    private final AppointmentService appointments;

    public AppointmentController(PatientRepository patients, HealthcareProfessionalSchedule availability,
                                 AppointmentService appointments) {
        this.patients = patients;
        this.availability = availability;
        this.appointments = appointments;
    }

    public int identifyPatient(String firstName, String lastName, String insuranceNumber) {
        return patients.identifyPatient(firstName, lastName, insuranceNumber).patientId();
    }

    public Appointment requestAppointment(int patientId, int scheduleId, int serviceId) {
        Patient patient = patients.identifyPatient(patientId);
        Schedule schedule = availability.scheduleForBooking(scheduleId);
        HealthcareProfessional professional = availability.selectProfessional(schedule.professionalId());
        return appointments.createAppointment(patient, schedule, professional, serviceId);
    }
}
