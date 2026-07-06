package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hotel room with its category, pricing, availability, and amenities.
 */
public class Room {

    private int roomNumber;
    private RoomCategory category;
    private double pricePerNight;
    private boolean available;
    private String description;
    private List<String> amenities;
    private int floor;
    private int maxOccupancy;

    /**
     * Full constructor.
     */
    public Room(int roomNumber, RoomCategory category, double pricePerNight, boolean available,
                String description, List<String> amenities, int floor, int maxOccupancy) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.available = available;
        this.description = description;
        this.amenities = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>();
        this.floor = floor;
        this.maxOccupancy = maxOccupancy;
    }

    /**
     * Default constructor.
     */
    public Room() {
        this.amenities = new ArrayList<>();
        this.available = true;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAmenities() {
        return new ArrayList<>(amenities);
    }

    public int getFloor() {
        return floor;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>();
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    // ── JSON Serialization ───────────────────────────────────────────────

    /**
     * Serializes this Room to a JSON string.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"roomNumber\":").append(roomNumber).append(",");
        sb.append("\"category\":\"").append(category.name()).append("\",");
        sb.append("\"pricePerNight\":").append(pricePerNight).append(",");
        sb.append("\"available\":").append(available).append(",");
        sb.append("\"description\":\"").append(escapeJson(description)).append("\",");
        sb.append("\"amenities\":[");
        for (int i = 0; i < amenities.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(amenities.get(i))).append("\"");
        }
        sb.append("],");
        sb.append("\"floor\":").append(floor).append(",");
        sb.append("\"maxOccupancy\":").append(maxOccupancy);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a Room from a JSON string.
     */
    public static Room fromJson(String json) {
        int roomNumber = extractIntValue(json, "roomNumber");
        String categoryStr = extractStringValue(json, "category");
        RoomCategory category = RoomCategory.valueOf(categoryStr);
        double pricePerNight = extractDoubleValue(json, "pricePerNight");
        boolean available = extractBooleanValue(json, "available");
        String description = extractStringValue(json, "description");
        List<String> amenities = extractStringArray(json, "amenities");
        int floor = extractIntValue(json, "floor");
        int maxOccupancy = extractIntValue(json, "maxOccupancy");

        return new Room(roomNumber, category, pricePerNight, available, description, amenities, floor, maxOccupancy);
    }

    // ── JSON Helpers ─────────────────────────────────────────────────────

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    static String extractStringValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return "";
        start += searchKey.length();
        StringBuilder value = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"':  value.append('"');  i++; break;
                    case '\\': value.append('\\'); i++; break;
                    case 'n':  value.append('\n'); i++; break;
                    case 'r':  value.append('\r'); i++; break;
                    case 't':  value.append('\t'); i++; break;
                    default:   value.append(c);         break;
                }
            } else if (c == '"') {
                break;
            } else {
                value.append(c);
            }
        }
        return value.toString();
    }

    static int extractIntValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int start = json.indexOf(searchKey);
        if (start == -1) return 0;
        start += searchKey.length();
        // Skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        StringBuilder num = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ',' || c == '}' || c == ']') break;
            if (!Character.isWhitespace(c)) num.append(c);
        }
        String trimmed = num.toString().trim();
        if (trimmed.isEmpty()) return 0;
        return Integer.parseInt(trimmed);
    }

    static double extractDoubleValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int start = json.indexOf(searchKey);
        if (start == -1) return 0.0;
        start += searchKey.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        StringBuilder num = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ',' || c == '}' || c == ']') break;
            if (!Character.isWhitespace(c)) num.append(c);
        }
        String trimmed = num.toString().trim();
        if (trimmed.isEmpty()) return 0.0;
        return Double.parseDouble(trimmed);
    }

    static boolean extractBooleanValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int start = json.indexOf(searchKey);
        if (start == -1) return false;
        start += searchKey.length();
        String rest = json.substring(start).trim();
        return rest.startsWith("true");
    }

    /**
     * Extracts a JSON array of strings, e.g. ["Wi-Fi","TV","AC"].
     */
    static List<String> extractStringArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String searchKey = "\"" + key + "\":[";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            // Try with space before bracket
            searchKey = "\"" + key + "\": [";
            start = json.indexOf(searchKey);
            if (start == -1) return result;
        }
        start += searchKey.length();

        // Find the matching closing bracket
        int depth = 1;
        int end = start;
        boolean inStr = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inStr) {
                if (c == '\\') { i++; continue; }
                if (c == '"') inStr = false;
            } else {
                if (c == '"') inStr = true;
                else if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) { end = i; break; }
                }
            }
        }

        String arrContent = json.substring(start, end).trim();
        if (arrContent.isEmpty()) return result;

        // Parse individual quoted strings
        boolean inString = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < arrContent.length(); i++) {
            char c = arrContent.charAt(i);
            if (inString) {
                if (c == '\\' && i + 1 < arrContent.length()) {
                    char next = arrContent.charAt(i + 1);
                    switch (next) {
                        case '"':  current.append('"');  i++; break;
                        case '\\': current.append('\\'); i++; break;
                        case 'n':  current.append('\n'); i++; break;
                        default:   current.append(c);         break;
                    }
                } else if (c == '"') {
                    result.add(current.toString());
                    current = new StringBuilder();
                    inString = false;
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inString = true;
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Room{number=" + roomNumber
                + ", category=" + category.getDisplayName()
                + ", price=" + pricePerNight
                + ", available=" + available
                + ", floor=" + floor
                + ", maxOccupancy=" + maxOccupancy
                + "}";
    }
}
