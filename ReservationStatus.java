package org.example.model;

/**
 * Represents the status of a hotel reservation.
 * Each status has a human-readable display name and an associated color hex code for UI rendering.
 */
public enum ReservationStatus {
    CONFIRMED("Confirmed", "#2ecc71"),
    CANCELLED("Cancelled", "#e74c3c"),
    CHECKED_IN("Checked In", "#3498db"),
    CHECKED_OUT("Checked Out", "#95a5a6");

    private final String displayName;
    private final String colorHex;

    ReservationStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }
}
