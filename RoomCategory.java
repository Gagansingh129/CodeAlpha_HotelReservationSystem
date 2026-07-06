package org.example.model;

/**
 * Represents the category of a hotel room.
 * Each category has a display name, base price per night, and a description.
 */
public enum RoomCategory {
    STANDARD("Standard", 2500.0, "Clean, comfortable rooms with essential amenities"),
    DELUXE("Deluxe", 4500.0, "Spacious rooms with premium furnishings and city view"),
    SUITE("Suite", 8500.0, "Luxurious suites with separate living area and panoramic views");

    private final String displayName;
    private final double basePrice;
    private final String description;

    RoomCategory(String displayName, double basePrice, String description) {
        this.displayName = displayName;
        this.basePrice = basePrice;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }
}
