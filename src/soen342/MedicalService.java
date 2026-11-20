package soen342;

public record MedicalService(
        int serviceId, String name, String description, boolean requiresResources) {}
