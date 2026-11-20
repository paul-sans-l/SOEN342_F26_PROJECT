package soen342;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@FunctionalInterface
public interface RepetitionPattern {
    boolean matches(LocalDate date);

    static RepetitionPattern weekly(Set<DayOfWeek> days) {
        Set<DayOfWeek> selectedDays = Set.copyOf(days);
        if (selectedDays.isEmpty()) {
            throw new IllegalArgumentException("Select at least one weekday for the weekly recurrence.");
        }
        return date -> selectedDays.contains(date.getDayOfWeek());
    }

    static RepetitionPattern monthly(int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("Day of month must be a number from 1 through 31.");
        }
        return date -> date.getDayOfMonth() == dayOfMonth;
    }
}
