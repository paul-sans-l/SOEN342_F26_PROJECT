package soen342;

import java.util.LinkedHashSet;
import java.util.Set;

public record HealthcareProfessional(
        int professionalId,
        String firstName,
        String lastName,
        String professionalInformation,
        Set<Specialty> specialties,
        Set<ClinicLocation> locations) {

    public HealthcareProfessional {
        specialties = Set.copyOf(specialties);
        locations = Set.copyOf(locations);
    }

    HealthcareProfessional withAssignment(Specialty specialty, ClinicLocation location) {
        var updatedSpecialties = new LinkedHashSet<>(specialties);
        updatedSpecialties.add(specialty);
        var updatedLocations = new LinkedHashSet<>(locations);
        updatedLocations.add(location);
        return new HealthcareProfessional(
                professionalId, firstName, lastName, professionalInformation,
                updatedSpecialties, updatedLocations);
    }
}
