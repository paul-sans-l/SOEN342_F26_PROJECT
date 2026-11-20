package soen342;

public final class BookingInterface {
    private final AppointmentController controller;
    private Integer identifiedPatientId;

    public BookingInterface(AppointmentController controller) {
        this.controller = controller;
    }

    public int identification(String firstName, String lastName, String insuranceNumber) {
        identifiedPatientId = null;
        identifiedPatientId = controller.identifyPatient(firstName, lastName, insuranceNumber);
        return identifiedPatientId;
    }

    public Appointment requestAppointment(int scheduleId, int serviceId) {
        if (identifiedPatientId == null) {
            throw new IllegalArgumentException("Identify the patient before requesting an appointment.");
        }
        return controller.requestAppointment(identifiedPatientId, scheduleId, serviceId);
    }
}
