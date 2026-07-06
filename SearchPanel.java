package org.example.ui;

import org.example.model.Room;
import org.example.model.RoomCategory;
import org.example.service.ReservationManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SearchPanel extends JPanel {

    private final HotelReservationApp app;
    private final ReservationManager manager;

    private JComboBox<String> categoryCombo;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JPanel resultsGrid;
    private JLabel statusLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public SearchPanel(HotelReservationApp app) {
        this.app = app;
        this.manager = new ReservationManager();

        setLayout(new BorderLayout(0, ThemeManager.PADDING));
        setBackground(ThemeManager.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING));

        // Header Section
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Filter & Search bar panel
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel filterBar = createFilterBar();
        topPanel.add(filterBar);
        topPanel.add(Box.createVerticalStrut(16));

        // Status Label for Messages/Errors
        statusLabel = new JLabel("Enter dates and category to check availability.");
        statusLabel.setFont(ThemeManager.FONT_BODY);
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        // Results Grid Panel (scrollable)
        resultsGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        resultsGrid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(resultsGrid);
        ThemeManager.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Prepopulate dates: check-in is today, check-out is tomorrow
        LocalDate today = LocalDate.now();
        checkInField.setText(today.format(DATE_FORMATTER));
        checkOutField.setText(today.plusDays(1).format(DATE_FORMATTER));

        // Run initial search
        performSearch();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Search Available Rooms");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Filter rooms by category and stay duration.");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel createFilterBar() {
        JPanel bar = ThemeManager.createCard();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));
        bar.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Category Filter
        JPanel categoryPanel = new JPanel(new BorderLayout(0, 4));
        categoryPanel.setOpaque(false);
        JLabel lblCat = new JLabel("Room Category");
        lblCat.setFont(ThemeManager.FONT_SMALL);
        lblCat.setForeground(ThemeManager.TEXT_SECONDARY);
        categoryPanel.add(lblCat, BorderLayout.NORTH);

        String[] categories = {"All Categories", "Standard", "Deluxe", "Suite"};
        categoryCombo = ThemeManager.createStyledComboBox(categories);
        categoryCombo.setPreferredSize(new Dimension(140, 36));
        categoryPanel.add(categoryCombo, BorderLayout.CENTER);
        bar.add(categoryPanel);

        // Check-In Date Filter
        JPanel checkInPanel = new JPanel(new BorderLayout(0, 4));
        checkInPanel.setOpaque(false);
        JLabel lblCheckIn = new JLabel("Check-In Date");
        lblCheckIn.setFont(ThemeManager.FONT_SMALL);
        lblCheckIn.setForeground(ThemeManager.TEXT_SECONDARY);
        checkInPanel.add(lblCheckIn, BorderLayout.NORTH);

        checkInField = ThemeManager.createStyledTextField("YYYY-MM-DD");
        checkInField.setPreferredSize(new Dimension(130, 36));
        checkInPanel.add(checkInField, BorderLayout.CENTER);
        bar.add(checkInPanel);

        // Check-Out Date Filter
        JPanel checkOutPanel = new JPanel(new BorderLayout(0, 4));
        checkOutPanel.setOpaque(false);
        JLabel lblCheckOut = new JLabel("Check-Out Date");
        lblCheckOut.setFont(ThemeManager.FONT_SMALL);
        lblCheckOut.setForeground(ThemeManager.TEXT_SECONDARY);
        checkOutPanel.add(lblCheckOut, BorderLayout.NORTH);

        checkOutField = ThemeManager.createStyledTextField("YYYY-MM-DD");
        checkOutField.setPreferredSize(new Dimension(130, 36));
        checkOutPanel.add(checkOutField, BorderLayout.CENTER);
        bar.add(checkOutPanel);

        // Search Button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        // Spacer at top to align with inputs
        JLabel spacer = new JLabel(" ");
        spacer.setFont(ThemeManager.FONT_SMALL);
        buttonPanel.add(spacer, BorderLayout.NORTH);

        JButton btnSearch = ThemeManager.createPrimaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(120, 36));
        btnSearch.addActionListener(e -> performSearch());
        buttonPanel.add(btnSearch, BorderLayout.CENTER);
        bar.add(buttonPanel);

        return bar;
    }

    private void performSearch() {
        String checkInStr = checkInField.getText().trim();
        String checkOutStr = checkOutField.getText().trim();

        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInStr, DATE_FORMATTER);
            checkOut = LocalDate.parse(checkOutStr, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            statusLabel.setForeground(ThemeManager.ACCENT_RED);
            statusLabel.setText("⚠️ Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            statusLabel.setForeground(ThemeManager.ACCENT_RED);
            statusLabel.setText("⚠️ Check-out date must be after check-in date.");
            return;
        }

        if (checkIn.isBefore(LocalDate.now())) {
            statusLabel.setForeground(ThemeManager.ACCENT_RED);
            statusLabel.setText("⚠️ Check-in date cannot be in the past.");
            return;
        }

        // Map selection to enum
        int categoryIndex = categoryCombo.getSelectedIndex();
        RoomCategory selectedCategory = null;
        if (categoryIndex == 1) selectedCategory = RoomCategory.STANDARD;
        else if (categoryIndex == 2) selectedCategory = RoomCategory.DELUXE;
        else if (categoryIndex == 3) selectedCategory = RoomCategory.SUITE;

        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setText("🔍 Searching available rooms...");

        // Run search in background
        RoomCategory finalCat = selectedCategory;
        LocalDate finalIn = checkIn;
        LocalDate finalOut = checkOut;

        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() {
                return manager.searchAvailableRooms(finalCat, finalIn, finalOut);
            }

            @Override
            protected void done() {
                try {
                    List<Room> availableRooms = get();
                    displayResults(availableRooms, checkInStr, checkOutStr);
                } catch (Exception e) {
                    statusLabel.setForeground(ThemeManager.ACCENT_RED);
                    statusLabel.setText("⚠️ Error searching: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayResults(List<Room> rooms, String checkIn, String checkOut) {
        resultsGrid.removeAll();
        if (rooms.isEmpty()) {
            statusLabel.setForeground(ThemeManager.ACCENT_ORANGE);
            statusLabel.setText("⚠️ No available rooms found matching your criteria.");

            // Show nice empty panel
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            JLabel emptyLabel = new JLabel("No Rooms Available");
            emptyLabel.setFont(ThemeManager.FONT_HEADING);
            emptyLabel.setForeground(ThemeManager.TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(emptyLabel);
            resultsGrid.add(empty);
        } else {
            statusLabel.setForeground(ThemeManager.ACCENT_GREEN);
            statusLabel.setText("✦ Found " + rooms.size() + " rooms available for booking.");

            for (Room room : rooms) {
                JPanel card = createRoomCard(room, checkIn, checkOut);
                resultsGrid.add(card);
            }
        }
        resultsGrid.revalidate();
        resultsGrid.repaint();
    }

    private JPanel createRoomCard(Room room, String checkIn, String checkOut) {
        // ThemeManager.createCard creates a panel with border and BG_CARD background
        JPanel card = new ThemeManager.RoundedPanel(ThemeManager.CARD_RADIUS, ThemeManager.BG_CARD, ThemeManager.BORDER);
        card.setPreferredSize(new Dimension(280, 240));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header Panel: Room Number and Category Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel numLabel = new JLabel("Room " + room.getRoomNumber());
        numLabel.setFont(ThemeManager.FONT_HEADING);
        numLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        topRow.add(numLabel, BorderLayout.WEST);

        // Category Badge
        Color badgeColor;
        switch (room.getCategory()) {
            case SUITE:
                badgeColor = ThemeManager.ACCENT_GOLD;
                break;
            case DELUXE:
                badgeColor = ThemeManager.ACCENT_PURPLE;
                break;
            default:
                badgeColor = ThemeManager.ACCENT_BLUE;
                break;
        }

        JLabel catBadge = new JLabel(" " + room.getCategory().getDisplayName() + " ");
        catBadge.setFont(ThemeManager.FONT_SMALL);
        catBadge.setOpaque(true);
        catBadge.setBackground(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 40));
        catBadge.setForeground(badgeColor);
        catBadge.setBorder(BorderFactory.createLineBorder(badgeColor, 1));
        topRow.add(catBadge, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Middle Section: Info & Amenities
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        // Price
        JLabel priceLabel = new JLabel(String.format("₹%,.0f / night", room.getPricePerNight()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(ThemeManager.ACCENT_GOLD);
        center.add(priceLabel);
        center.add(Box.createVerticalStrut(6));

        // Details (floor, occupancy)
        JLabel detailLabel = new JLabel("Floor " + room.getFloor() + "  •  Max Guests: " + room.getMaxOccupancy());
        detailLabel.setFont(ThemeManager.FONT_SMALL);
        detailLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        center.add(detailLabel);
        center.add(Box.createVerticalStrut(10));

        // Amenities chips (First 3 only, for space)
        JPanel amenitiesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        amenitiesPanel.setOpaque(false);
        List<String> amenities = room.getAmenities();
        for (int i = 0; i < Math.min(3, amenities.size()); i++) {
            JLabel chip = new JLabel(" " + amenities.get(i) + " ");
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            chip.setOpaque(true);
            chip.setBackground(ThemeManager.BG_INPUT);
            chip.setForeground(ThemeManager.TEXT_SECONDARY);
            chip.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1));
            amenitiesPanel.add(chip);
        }
        center.add(amenitiesPanel);

        card.add(center, BorderLayout.CENTER);

        // Bottom Action Button
        JButton bookBtn = ThemeManager.createPrimaryButton("Book Now");
        bookBtn.setPreferredSize(new Dimension(0, 36));
        bookBtn.addActionListener(e -> app.setBookingRoom(room, checkIn, checkOut));
        card.add(bookBtn, BorderLayout.SOUTH);

        return card;
    }

    public void refresh() {
        performSearch();
    }

    /**
     * Custom Layout manager for automatic wrapping of elements in FlowLayout.
     * Needed because standard FlowLayout does not calculate height properly for scrollpanes.
     */
    private static class WrapLayout extends FlowLayout {
        public WrapLayout() {
            super();
        }

        public WrapLayout(int align) {
            super(align);
        }

        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                int insetLeft = target.getInsets().left;
                int insetRight = target.getInsets().right;
                int maxwidth = targetWidth - (insetLeft + insetRight + hgap * 2);

                int nmembers = target.getComponentCount();
                int x = 0;
                int y = target.getInsets().top + vgap;
                int rowHeight = 0;

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (x == 0) {
                            x = d.width;
                            rowHeight = d.height;
                        } else {
                            if (x + d.width + hgap > maxwidth) {
                                y += rowHeight + vgap;
                                x = d.width;
                                rowHeight = d.height;
                            } else {
                                x += d.width + hgap;
                                rowHeight = Math.max(rowHeight, d.height);
                            }
                        }
                    }
                }
                return new Dimension(targetWidth, y + rowHeight + vgap + target.getInsets().bottom);
            }
        }
    }
}
