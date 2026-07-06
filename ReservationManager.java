package org.example.service;

import org.example.data.HotelDataStore;
import org.example.model.Guest;
import org.example.model.PaymentMethod;
import org.example.model.PaymentRecord;
import org.example.model.Reservation;
import org.example.model.ReservationStatus;
import org.example.model.Room;
import org.example.model.RoomCategory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer that encapsulates all business logic for the hotel reservation system.
 * Manages room searching, booking, cancellation, payment processing, and dashboard statistics.
 */
public class ReservationManager {

    private final HotelDataStore dataStore;

    public ReservationManager() {
        this.dataStore = HotelDataStore.getInstance();
    }

    // ── Room Search ──────────────────────────────────────────────────────

    /**
     * Searches for available rooms, optionally filtered by category.
     * Checks that each room is marked available AND has no confirmed reservation
     * that overlaps the requested date range.
     *
     * @param category the room category to filter by, or {@code null} for all categories
     * @param checkIn  the desired check-in date
     * @param checkOut the desired check-out date
     * @return list of rooms that are available for the entire requested period
     */
    public List<Room> searchAvailableRooms(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        List<Room> allRooms = dataStore.getRooms();
        List<Reservation> allReservations = dataStore.getReservations();

        return allRooms.stream()
                .filter(room -> {
                    // Filter by category if specified
                    if (category != null && room.getCategory() != category) {
                        return false;
                    }
                    // Room must be marked as available
                    if (!room.isAvailable()) {
                        return false;
                    }
                    // Check for overlapping confirmed reservations
                    boolean hasOverlap = allReservations.stream()
                            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED
                                    || r.getStatus() == ReservationStatus.CHECKED_IN)
                            .filter(r -> r.getRoom().getRoomNumber() == room.getRoomNumber())
                            .anyMatch(r -> datesOverlap(checkIn, checkOut, r.getCheckIn(), r.getCheckOut()));
                    return !hasOverlap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks whether two date ranges overlap.
     * Two ranges [s1, e1) and [s2, e2) overlap if s1 < e2 AND s2 < e1.
     */
    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // ── Reservation Management ───────────────────────────────────────────

    /**
     * Creates a new reservation for the specified guest and room.
     *
     * @param guestName  the guest's full name
     * @param email      the guest's email
     * @param phone      the guest's phone number
     * @param roomNumber the room number to book
     * @param checkIn    check-in date
     * @param checkOut   check-out date
     * @return the created Reservation
     * @throws IllegalArgumentException if the room is not found or not available
     */
    public Reservation makeReservation(String guestName, String email, String phone,
                                       int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        // Find the room
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room " + roomNumber + " not found.");
        }

        // Verify room is available for the requested dates
        List<Room> availableRooms = searchAvailableRooms(room.getCategory(), checkIn, checkOut);
        boolean isAvailable = availableRooms.stream()
                .anyMatch(r -> r.getRoomNumber() == roomNumber);
        if (!isAvailable) {
            throw new IllegalArgumentException("Room " + roomNumber + " is not available for the selected dates.");
        }

        // Create guest and reservation
        Guest guest = new Guest(guestName, email, phone);
        Reservation reservation = new Reservation(guest, room, checkIn, checkOut);

        // If check-in is today or in the past, mark room as unavailable
        if (!checkIn.isAfter(LocalDate.now())) {
            room.setAvailable(false);
            dataStore.updateRoom(room);
        }

        // Persist the reservation
        dataStore.addReservation(reservation);

        return reservation;
    }

    /**
     * Cancels an existing reservation.
     *
     * @param reservationId the ID of the reservation to cancel
     * @return the cancelled Reservation
     * @throws IllegalArgumentException if the reservation is not found
     */
    public Reservation cancelReservation(String reservationId) {
        Reservation reservation = getReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }

        // Cancel the reservation
        reservation.cancel();

        // Mark the room as available again
        Room room = findRoomByNumber(reservation.getRoom().getRoomNumber());
        if (room != null) {
            room.setAvailable(true);
            dataStore.updateRoom(room);
        }

        // Persist changes
        dataStore.updateReservation(reservation);

        return reservation;
    }

    // ── Payment Processing ───────────────────────────────────────────────

    /**
     * Processes a payment for a reservation.
     * Simulates a brief processing delay.
     *
     * @param reservationId the reservation to pay for
     * @param method        the payment method
     * @return the created PaymentRecord
     * @throws IllegalArgumentException if the reservation is not found
     */
    public PaymentRecord processPayment(String reservationId, PaymentMethod method) {
        Reservation reservation = getReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }

        // Simulate payment processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Create payment record with SUCCESS status
        PaymentRecord payment = new PaymentRecord(
                reservationId,
                reservation.getTotalAmount(),
                method,
                "SUCCESS"
        );

        // Persist the payment
        dataStore.addPayment(payment);

        return payment;
    }

    // ── Query Methods ────────────────────────────────────────────────────

    /**
     * Retrieves a reservation by its ID.
     */
    public Reservation getReservation(String reservationId) {
        return dataStore.getReservations().stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns all reservations.
     */
    public List<Reservation> getAllReservations() {
        return dataStore.getReservations();
    }

    /**
     * Returns only active (non-cancelled) reservations.
     */
    public List<Reservation> getActiveReservations() {
        return dataStore.getReservations().stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    // ── Dashboard Statistics ─────────────────────────────────────────────

    /**
     * Computes dashboard statistics for the hotel.
     *
     * @return a map containing:
     *   - "totalRooms"          (Integer)
     *   - "availableRooms"      (Integer)
     *   - "activeBookings"      (Integer)
     *   - "totalRevenue"        (Double)
     *   - "recentReservations"  (List of last 5 Reservations)
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Room> allRooms = dataStore.getRooms();
        List<Reservation> allReservations = dataStore.getReservations();
        List<PaymentRecord> allPayments = dataStore.getPayments();

        // Total rooms
        stats.put("totalRooms", allRooms.size());

        // Available rooms count
        long availableCount = allRooms.stream().filter(Room::isAvailable).count();
        stats.put("availableRooms", (int) availableCount);

        // Active (non-cancelled) bookings
        List<Reservation> active = allReservations.stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .collect(Collectors.toList());
        stats.put("activeBookings", active.size());

        // Total revenue from successful payments
        double totalRevenue = allPayments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        // Recent reservations (last 5, sorted by creation time descending)
        List<Reservation> recent = allReservations.stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
        stats.put("recentReservations", recent);

        return stats;
    }

    // ── Helper Methods ───────────────────────────────────────────────────

    /**
     * Finds a room by its number from the data store.
     */
    private Room findRoomByNumber(int roomNumber) {
        return dataStore.getRooms().stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .findFirst()
                .orElse(null);
    }
}
