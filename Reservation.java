package org.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Represents a hotel reservation linking a guest to a room for a date range.
 * Supports full JSON serialization with guest details and room data stored inline.
 */
public class Reservation {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String reservationId;
    private Guest guest;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private ReservationStatus status;
    private double totalAmount;
    private final LocalDateTime createdAt;

    /**
     * Creates a new Reservation with an auto-generated UUID and current timestamp.
     */
    public Reservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.reservationId = UUID.randomUUID().toString();
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = ReservationStatus.CONFIRMED;
        this.totalAmount = computeTotal();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Internal constructor for deserialization.
     */
    private Reservation(String reservationId, Guest guest, Room room, LocalDate checkIn,
                        LocalDate checkOut, ReservationStatus status, double totalAmount,
                        LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    // ── Business Methods ─────────────────────────────────────────────────

    /**
     * Computes the number of nights for this reservation.
     */
    public long computeNights() {
        if (checkIn == null || checkOut == null) return 0;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return Math.max(nights, 0);
    }

    /**
     * Computes the total cost based on room price and number of nights.
     */
    public double computeTotal() {
        if (room == null) return 0.0;
        return room.getPricePerNight() * computeNights();
    }

    /**
     * Cancels this reservation by setting status to CANCELLED.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getReservationId() {
        return reservationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // ── JSON Serialization ───────────────────────────────────────────────

    /**
     * Serializes this Reservation to a JSON string.
     * Guest details and room data are stored inline as nested objects.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"reservationId\":\"").append(escapeJson(reservationId)).append("\",");
        sb.append("\"guest\":").append(guest.toJson()).append(",");
        sb.append("\"room\":").append(room.toJson()).append(",");
        sb.append("\"checkIn\":\"").append(checkIn.format(DATE_FORMATTER)).append("\",");
        sb.append("\"checkOut\":\"").append(checkOut.format(DATE_FORMATTER)).append("\",");
        sb.append("\"status\":\"").append(status.name()).append("\",");
        sb.append("\"totalAmount\":").append(totalAmount).append(",");
        sb.append("\"createdAt\":\"").append(createdAt.format(DATETIME_FORMATTER)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a Reservation from a JSON string.
     * Reconstructs nested Guest and Room objects from inline data.
     */
    public static Reservation fromJson(String json) {
        String reservationId = extractStringValue(json, "reservationId");

        // Extract nested guest object
        String guestJson = extractNestedObject(json, "guest");
        Guest guest = Guest.fromJson(guestJson);

        // Extract nested room object
        String roomJson = extractNestedObject(json, "room");
        Room room = Room.fromJson(roomJson);

        String checkInStr = extractStringValue(json, "checkIn");
        String checkOutStr = extractStringValue(json, "checkOut");
        LocalDate checkIn = LocalDate.parse(checkInStr, DATE_FORMATTER);
        LocalDate checkOut = LocalDate.parse(checkOutStr, DATE_FORMATTER);

        String statusStr = extractStringValue(json, "status");
        ReservationStatus status = ReservationStatus.valueOf(statusStr);

        double totalAmount = extractDoubleValue(json, "totalAmount");

        String createdAtStr = extractStringValue(json, "createdAt");
        LocalDateTime createdAt = LocalDateTime.parse(createdAtStr, DATETIME_FORMATTER);

        return new Reservation(reservationId, guest, room, checkIn, checkOut, status, totalAmount, createdAt);
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

    private static String extractStringValue(String json, String key) {
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

    private static double extractDoubleValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int start = json.indexOf(searchKey);
        if (start == -1) return 0.0;
        start += searchKey.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        // Make sure we're not hitting a string value or nested object
        if (start < json.length() && (json.charAt(start) == '"' || json.charAt(start) == '{')) return 0.0;
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

    /**
     * Extracts a nested JSON object value by matching braces.
     * For example, given key "guest", extracts the full {...} object.
     */
    static String extractNestedObject(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx == -1) return "{}";

        int start = keyIdx + searchKey.length();
        // Skip whitespace to find opening brace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length() || json.charAt(start) != '{') return "{}";

        int depth = 0;
        boolean inStr = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inStr) {
                if (c == '\\') {
                    i++; // skip escaped character
                } else if (c == '"') {
                    inStr = false;
                }
            } else {
                if (c == '"') {
                    inStr = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
        }
        return "{}";
    }

    @Override
    public String toString() {
        return "Reservation{id='" + reservationId
                + "', guest=" + (guest != null ? guest.getName() : "null")
                + ", room=" + (room != null ? room.getRoomNumber() : "null")
                + ", checkIn=" + checkIn + ", checkOut=" + checkOut
                + ", status=" + status.getDisplayName()
                + ", total=" + totalAmount + "}";
    }
}
