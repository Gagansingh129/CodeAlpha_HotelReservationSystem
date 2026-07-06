package org.example.model;

import java.util.UUID;

/**
 * Represents a hotel guest with personal contact information.
 * The guestId is auto-generated as a UUID on creation.
 */
public class Guest {

    private final String guestId;
    private String name;
    private String email;
    private String phone;

    /**
     * Creates a new Guest with an auto-generated UUID.
     */
    public Guest(String name, String email, String phone) {
        this.guestId = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Internal constructor used by fromJson to reconstruct a Guest with an existing ID.
     */
    private Guest(String guestId, String name, String email, String phone) {
        this.guestId = guestId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    // ── JSON Serialization ───────────────────────────────────────────────

    /**
     * Serializes this Guest to a JSON string.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"guestId\":\"").append(escapeJson(guestId)).append("\",");
        sb.append("\"name\":\"").append(escapeJson(name)).append("\",");
        sb.append("\"email\":\"").append(escapeJson(email)).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(phone)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a Guest from a JSON string.
     */
    public static Guest fromJson(String json) {
        String guestId = extractStringValue(json, "guestId");
        String name = extractStringValue(json, "name");
        String email = extractStringValue(json, "email");
        String phone = extractStringValue(json, "phone");
        return new Guest(guestId, name, email, phone);
    }

    // ── JSON Helpers ─────────────────────────────────────────────────────

    static String escapeJson(String s) {
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

    @Override
    public String toString() {
        return "Guest{guestId='" + guestId + "', name='" + name
                + "', email='" + email + "', phone='" + phone + "'}";
    }
}
