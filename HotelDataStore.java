package org.example.data;

import org.example.model.PaymentRecord;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.RoomCategory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton data store that manages all persistence for the hotel reservation system.
 * Data is stored as JSON files in the {@code data/} directory relative to the working directory.
 */
public class HotelDataStore {

    private static final String DATA_DIR = "data";
    private static final String ROOMS_FILE = "rooms.json";
    private static final String RESERVATIONS_FILE = "reservations.json";
    private static final String PAYMENTS_FILE = "payments.json";

    private static HotelDataStore instance;

    private List<Room> rooms;
    private List<Reservation> reservations;
    private List<PaymentRecord> payments;

    /**
     * Private constructor — use {@link #getInstance()}.
     */
    private HotelDataStore() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        payments = new ArrayList<>();
    }

    /**
     * Returns the singleton instance, creating it and loading data if necessary.
     */
    public static synchronized HotelDataStore getInstance() {
        if (instance == null) {
            instance = new HotelDataStore();
            instance.loadAll();
        }
        return instance;
    }

    // ── Data Loading ─────────────────────────────────────────────────────

    /**
     * Loads all data from JSON files. If files don't exist, initializes seed data.
     */
    public void loadAll() {
        Path dataDir = Paths.get(DATA_DIR);
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (IOException e) {
                System.err.println("Failed to create data directory: " + e.getMessage());
            }
        }

        Path roomsPath = dataDir.resolve(ROOMS_FILE);
        Path reservationsPath = dataDir.resolve(RESERVATIONS_FILE);
        Path paymentsPath = dataDir.resolve(PAYMENTS_FILE);

        if (!Files.exists(roomsPath)) {
            initializeSeedData();
            return;
        }

        // Load rooms
        try {
            String roomsJson = Files.readString(roomsPath);
            List<String> roomObjects = parseJsonArray(roomsJson);
            rooms = new ArrayList<>();
            for (String obj : roomObjects) {
                rooms.add(Room.fromJson(obj));
            }
        } catch (IOException e) {
            System.err.println("Failed to load rooms: " + e.getMessage());
            rooms = new ArrayList<>();
        }

        // Load reservations
        try {
            if (Files.exists(reservationsPath)) {
                String reservationsJson = Files.readString(reservationsPath);
                List<String> resObjects = parseJsonArray(reservationsJson);
                reservations = new ArrayList<>();
                for (String obj : resObjects) {
                    reservations.add(Reservation.fromJson(obj));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load reservations: " + e.getMessage());
            reservations = new ArrayList<>();
        }

        // Load payments
        try {
            if (Files.exists(paymentsPath)) {
                String paymentsJson = Files.readString(paymentsPath);
                List<String> payObjects = parseJsonArray(paymentsJson);
                payments = new ArrayList<>();
                for (String obj : payObjects) {
                    payments.add(PaymentRecord.fromJson(obj));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load payments: " + e.getMessage());
            payments = new ArrayList<>();
        }
    }

    // ── Seed Data ────────────────────────────────────────────────────────

    /**
     * Initializes 25 rooms across three categories and saves them.
     */
    public void initializeSeedData() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        payments = new ArrayList<>();

        // Floor 1: Rooms 101-110 — STANDARD
        List<String> standardAmenities = Arrays.asList("Wi-Fi", "TV", "AC", "Room Service");
        for (int i = 101; i <= 110; i++) {
            rooms.add(new Room(
                    i,
                    RoomCategory.STANDARD,
                    2500.0,
                    true,
                    RoomCategory.STANDARD.getDescription(),
                    standardAmenities,
                    1,
                    2
            ));
        }

        // Floor 2: Rooms 201-210 — DELUXE
        List<String> deluxeAmenities = Arrays.asList("Wi-Fi", "TV", "AC", "Mini Bar", "Room Service", "City View");
        for (int i = 201; i <= 210; i++) {
            rooms.add(new Room(
                    i,
                    RoomCategory.DELUXE,
                    4500.0,
                    true,
                    RoomCategory.DELUXE.getDescription(),
                    deluxeAmenities,
                    2,
                    3
            ));
        }

        // Floor 3: Rooms 301-305 — SUITE
        List<String> suiteAmenities = Arrays.asList(
                "Wi-Fi", "Smart TV", "AC", "Mini Bar", "Room Service",
                "Panoramic View", "Jacuzzi", "Living Area"
        );
        for (int i = 301; i <= 305; i++) {
            rooms.add(new Room(
                    i,
                    RoomCategory.SUITE,
                    8500.0,
                    true,
                    RoomCategory.SUITE.getDescription(),
                    suiteAmenities,
                    3,
                    4
            ));
        }

        saveRooms();
        saveReservations();
        savePayments();
    }

    // ── Save Methods ─────────────────────────────────────────────────────

    /**
     * Saves the rooms list to rooms.json.
     */
    public void saveRooms() {
        String json = writeJsonArray(rooms.stream().map(Room::toJson).collect(Collectors.toList()));
        writeFile(ROOMS_FILE, json);
    }

    /**
     * Saves the reservations list to reservations.json.
     */
    public void saveReservations() {
        String json = writeJsonArray(reservations.stream().map(Reservation::toJson).collect(Collectors.toList()));
        writeFile(RESERVATIONS_FILE, json);
    }

    /**
     * Saves the payments list to payments.json.
     */
    public void savePayments() {
        String json = writeJsonArray(payments.stream().map(PaymentRecord::toJson).collect(Collectors.toList()));
        writeFile(PAYMENTS_FILE, json);
    }

    // ── Query Methods ────────────────────────────────────────────────────

    /**
     * Returns all rooms that are currently marked as available.
     */
    public List<Room> getAvailableRooms() {
        return rooms.stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Returns the full list of rooms.
     */
    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    /**
     * Returns the full list of reservations.
     */
    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }

    /**
     * Returns the full list of payment records.
     */
    public List<PaymentRecord> getPayments() {
        return new ArrayList<>(payments);
    }

    // ── Mutation Methods ─────────────────────────────────────────────────

    /**
     * Adds a reservation and persists it immediately.
     */
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        saveReservations();
    }

    /**
     * Adds a payment record and persists it immediately.
     */
    public void addPayment(PaymentRecord payment) {
        payments.add(payment);
        savePayments();
    }

    /**
     * Updates an existing room (matched by room number) and persists.
     */
    public void updateRoom(Room updatedRoom) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomNumber() == updatedRoom.getRoomNumber()) {
                rooms.set(i, updatedRoom);
                break;
            }
        }
        saveRooms();
    }

    /**
     * Updates an existing reservation (matched by reservation ID) and persists.
     */
    public void updateReservation(Reservation updatedReservation) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getReservationId().equals(updatedReservation.getReservationId())) {
                reservations.set(i, updatedReservation);
                break;
            }
        }
        saveReservations();
    }

    // ── JSON Array Serialization ─────────────────────────────────────────

    /**
     * Builds a JSON array string from a list of individual JSON object strings.
     */
    private String writeJsonArray(List<String> jsonItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < jsonItems.size(); i++) {
            sb.append("  ").append(jsonItems.get(i));
            if (i < jsonItems.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parses a JSON array string into a list of individual JSON object strings.
     * Uses brace-matching to correctly handle nested objects and quoted strings.
     */
    public static List<String> parseJsonArray(String json) {
        List<String> objects = new ArrayList<>();
        if (json == null) return objects;
        json = json.trim();
        if (json.isEmpty() || json.equals("[]")) return objects;
        if (json.charAt(0) != '[' || json.charAt(json.length() - 1) != ']') return objects;

        int depth = 0;
        int objStart = -1;
        boolean inString = false;

        for (int i = 1; i < json.length() - 1; i++) {
            char c = json.charAt(i);

            if (inString) {
                if (c == '\\') {
                    i++; // skip the escaped character
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) {
                    objStart = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart != -1) {
                    objects.add(json.substring(objStart, i + 1));
                    objStart = -1;
                }
            }
        }

        return objects;
    }

    // ── File I/O ─────────────────────────────────────────────────────────

    /**
     * Writes content to a file within the data directory.
     */
    private void writeFile(String filename, String content) {
        try {
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.writeString(dir.resolve(filename), content);
        } catch (IOException e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
        }
    }
}
