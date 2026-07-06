package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a payment transaction associated with a reservation.
 */
public class PaymentRecord {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String paymentId;
    private final String reservationId;
    private final double amount;
    private final PaymentMethod method;
    private final String status; // "SUCCESS", "PENDING", "FAILED"
    private final LocalDateTime timestamp;

    /**
     * Creates a new PaymentRecord with an auto-generated UUID and current timestamp.
     */
    public PaymentRecord(String reservationId, double amount, PaymentMethod method, String status) {
        this.paymentId = UUID.randomUUID().toString();
        this.reservationId = reservationId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Internal constructor for deserialization.
     */
    private PaymentRecord(String paymentId, String reservationId, double amount,
                          PaymentMethod method, String status, LocalDateTime timestamp) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.timestamp = timestamp;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getPaymentId() {
        return paymentId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // ── JSON Serialization ───────────────────────────────────────────────

    /**
     * Serializes this PaymentRecord to a JSON string.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"paymentId\":\"").append(escapeJson(paymentId)).append("\",");
        sb.append("\"reservationId\":\"").append(escapeJson(reservationId)).append("\",");
        sb.append("\"amount\":").append(amount).append(",");
        sb.append("\"method\":\"").append(method.name()).append("\",");
        sb.append("\"status\":\"").append(escapeJson(status)).append("\",");
        sb.append("\"timestamp\":\"").append(timestamp.format(FORMATTER)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a PaymentRecord from a JSON string.
     */
    public static PaymentRecord fromJson(String json) {
        String paymentId = extractStringValue(json, "paymentId");
        String reservationId = extractStringValue(json, "reservationId");
        double amount = extractDoubleValue(json, "amount");
        String methodStr = extractStringValue(json, "method");
        PaymentMethod method = PaymentMethod.valueOf(methodStr);
        String status = extractStringValue(json, "status");
        String timestampStr = extractStringValue(json, "timestamp");
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, FORMATTER);

        return new PaymentRecord(paymentId, reservationId, amount, method, status, timestamp);
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

    @Override
    public String toString() {
        return "PaymentRecord{paymentId='" + paymentId + "', reservationId='" + reservationId
                + "', amount=" + amount + ", method=" + method.getDisplayName()
                + ", status='" + status + "', timestamp=" + timestamp + "}";
    }
}
