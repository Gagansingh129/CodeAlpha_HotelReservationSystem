package org.example.ui;

import org.example.model.Reservation;
import org.example.model.Room;
import org.example.model.RoomCategory;
import org.example.service.ReservationManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final HotelReservationApp app;
    private final ReservationManager manager;

    private JPanel statsContainer;
    private JTable recentTable;
    private DefaultTableModel tableModel;

    // Detailed panels on the right side
    private JPanel categoryDistributionPanel;
    private JPanel categoryGrid;
    private JPanel operationalChecksPanel;

    public DashboardPanel(HotelReservationApp app) {
        this.app = app;
        this.manager = new ReservationManager();

        setLayout(new BorderLayout(0, ThemeManager.PADDING));
        setBackground(ThemeManager.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING));

        // Header Section
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Center Scrollable Container
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // 1. Stats Row
        statsContainer = new JPanel(new GridLayout(1, 4, 16, 0));
        statsContainer.setOpaque(false);
        centerPanel.add(statsContainer);
        centerPanel.add(Box.createVerticalStrut(ThemeManager.PADDING));

        // 2. Main Grid Layout: Left Column (Table), Right Column (Charts & Operations)
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Column (Recent Bookings - 65% width)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 16);
        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setOpaque(false);
        leftContainer.add(createRecentBookingsTable(), BorderLayout.CENTER);
        mainGrid.add(leftContainer, gbc);

        // Right Column (Analytics & Operations - 35% width)
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel rightContainer = new JPanel();
        rightContainer.setOpaque(false);
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));

        categoryDistributionPanel = createCategoryDistributionPanel();
        rightContainer.add(categoryDistributionPanel);
        rightContainer.add(Box.createVerticalStrut(16));

        operationalChecksPanel = createOperationalChecksPanel();
        rightContainer.add(operationalChecksPanel);

        mainGrid.add(rightContainer, gbc);
        centerPanel.add(mainGrid);
        centerPanel.add(Box.createVerticalStrut(ThemeManager.PADDING));

        // 3. Quick Actions Panel at bottom
        JPanel quickActions = createQuickActions();
        centerPanel.add(quickActions);

        // Scrollpane wrapper
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        ThemeManager.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Initial Data Load
        refresh();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Operations Dashboard");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time occupancy status, revenue metrics, and booking logs.");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);

        // Live Clock/Date info
        JLabel dateLabel = new JLabel("📅  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        dateLabel.setFont(ThemeManager.FONT_SUBHEADING);
        dateLabel.setForeground(ThemeManager.ACCENT_GOLD);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(dateLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createRecentBookingsTable() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Recent Guest Registry");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(title, BorderLayout.NORTH);

        // Table Model Setup
        String[] columns = {"ID Ref", "Guest Name", "Room", "Check-In", "Check-Out", "Amount Paid", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new JTable(tableModel);
        recentTable.setFont(ThemeManager.FONT_BODY);
        recentTable.setForeground(ThemeManager.TEXT_PRIMARY);
        recentTable.setBackground(ThemeManager.BG_CARD);
        recentTable.setRowHeight(38);
        recentTable.setGridColor(ThemeManager.BORDER);
        recentTable.setShowHorizontalLines(true);
        recentTable.setShowVerticalLines(false);
        recentTable.setSelectionBackground(new Color(0x1c, 0x22, 0x2c));
        recentTable.setSelectionForeground(ThemeManager.TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = recentTable.getTableHeader();
        header.setFont(ThemeManager.FONT_BUTTON);
        header.setBackground(ThemeManager.BG_INPUT);
        header.setForeground(ThemeManager.TEXT_PRIMARY);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));

        // Custom Cell Renderers
        recentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? ThemeManager.BG_CARD : ThemeManager.BG_DARK);
                c.setForeground(ThemeManager.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                if (column == 6) { // Status column
                    setHorizontalAlignment(SwingConstants.CENTER);
                    String status = (String) value;
                    if ("Confirmed".equalsIgnoreCase(status)) {
                        setForeground(ThemeManager.ACCENT_GREEN);
                    } else if ("Cancelled".equalsIgnoreCase(status)) {
                        setForeground(ThemeManager.ACCENT_RED);
                    } else if ("Checked In".equalsIgnoreCase(status)) {
                        setForeground(ThemeManager.ACCENT_BLUE);
                    } else {
                        setForeground(ThemeManager.TEXT_SECONDARY);
                    }
                } else if (column == 5) { // Amount Paid
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setForeground(ThemeManager.ACCENT_GOLD);
                } else if (column == 0) { // Truncated ID
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setForeground(ThemeManager.TEXT_SECONDARY);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected) {
                    c.setBackground(new Color(0x21, 0x26, 0x2d));
                }

                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(recentTable);
        ThemeManager.styleScrollPane(tableScroll);
        tableScroll.setPreferredSize(new Dimension(680, 240));
        card.add(tableScroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCategoryDistributionPanel() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Occupancy by Category");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        // Grid that lists Category progress indicators
        categoryGrid = new JPanel();
        categoryGrid.setOpaque(false);
        categoryGrid.setLayout(new BoxLayout(categoryGrid, BoxLayout.Y_AXIS));
        card.add(categoryGrid, BorderLayout.CENTER);

        return card;
    }

    private JPanel createOperationalChecksPanel() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Hotel Operations Status");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Adding operational checkpoints with visual emojis
        content.add(createStatusLine("🧹 Housekeeping", "22 Rooms Ready  |  3 Pending Clean", ThemeManager.ACCENT_GREEN));
        content.add(Box.createVerticalStrut(10));
        content.add(createStatusLine("🛎️ Front Desk", "Expected check-ins today: 4 guests", ThemeManager.ACCENT_BLUE));
        content.add(Box.createVerticalStrut(10));
        content.add(createStatusLine("🔧 Facility Management", "All HVAC & Electrical systems OK", ThemeManager.ACCENT_GOLD));
        content.add(Box.createVerticalStrut(10));
        content.add(createStatusLine("📶 Infrastructure", "Core Wi-Fi network online (100% SLA)", ThemeManager.ACCENT_PURPLE));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatusLine(String label, String detail, Color statusColor) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BUTTON);
        lbl.setForeground(ThemeManager.TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);

        JLabel det = new JLabel(detail);
        det.setFont(ThemeManager.FONT_SMALL);
        det.setForeground(ThemeManager.TEXT_SECONDARY);
        det.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(det, BorderLayout.CENTER);

        // Status indicator dot
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusColor);
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 16));
        dot.setOpaque(false);
        row.add(dot, BorderLayout.EAST);

        return row;
    }

    private JPanel createQuickActions() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quick Management Shortcuts");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel buttonGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        buttonGrid.setOpaque(false);

        JButton btnSearch = ThemeManager.createIconButton("🔍", "Reserve Room");
        btnSearch.addActionListener(e -> app.navigateTo(HotelReservationApp.PANEL_SEARCH));
        buttonGrid.add(btnSearch);

        JButton btnBook = ThemeManager.createIconButton("📝", "Register Guest Check-in");
        btnBook.addActionListener(e -> app.navigateTo(HotelReservationApp.PANEL_BOOKING));
        buttonGrid.add(btnBook);

        JButton btnView = ThemeManager.createIconButton("📋", "Audit Reservations Ledger");
        btnView.addActionListener(e -> app.navigateTo(HotelReservationApp.PANEL_RESERVATIONS));
        buttonGrid.add(btnView);

        card.add(buttonGrid, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes stats, tables, and distribution charts.
     */
    @SuppressWarnings("unchecked")
    public void refresh() {
        Map<String, Object> stats = manager.getDashboardStats();

        int totalRooms = (int) stats.getOrDefault("totalRooms", 0);
        int availableRooms = (int) stats.getOrDefault("availableRooms", 0);
        int activeBookings = (int) stats.getOrDefault("activeBookings", 0);
        double revenue = (double) stats.getOrDefault("totalRevenue", 0.0);
        List<Reservation> recent = (List<Reservation>) stats.get("recentReservations");

        int occupiedRooms = totalRooms - availableRooms;
        double occupancyRate = totalRooms > 0 ? ((double) occupiedRooms / totalRooms) * 100 : 0.0;

        // Rebuild Stats Cards
        statsContainer.removeAll();
        statsContainer.add(ThemeManager.createStatCard("OCCUPANCY RATE", String.format("%.1f%%", occupancyRate), ThemeManager.ACCENT_BLUE));
        statsContainer.add(ThemeManager.createStatCard("AVAILABLE INVENTORY", availableRooms + " / " + totalRooms, ThemeManager.ACCENT_ORANGE));
        statsContainer.add(ThemeManager.createStatCard("ACTIVE RESERVATIONS", String.valueOf(activeBookings), ThemeManager.ACCENT_GREEN));
        statsContainer.add(ThemeManager.createStatCard("TOTAL REVENUE LOG", String.format("₹%,.0f", revenue), ThemeManager.ACCENT_GOLD));
        statsContainer.revalidate();
        statsContainer.repaint();

        // Populate Recent Bookings Table
        tableModel.setRowCount(0);
        if (recent != null) {
            for (Reservation r : recent) {
                tableModel.addRow(new Object[]{
                        "#" + r.getReservationId().substring(0, 8),
                        r.getGuest().getName(),
                        "Room " + r.getRoom().getRoomNumber(),
                        r.getCheckIn().toString(),
                        r.getCheckOut().toString(),
                        String.format("₹%,.2f", r.getTotalAmount()),
                        r.getStatus().getDisplayName()
                });
            }
        }

        // Calculate occupancy distribution by category
        int standardTotal = 0, standardOccupied = 0;
        int deluxeTotal = 0, deluxeOccupied = 0;
        int suiteTotal = 0, suiteOccupied = 0;

        for (Room room : org.example.data.HotelDataStore.getInstance().getRooms()) {
            boolean isOccupied = !room.isAvailable();
            switch (room.getCategory()) {
                case STANDARD -> {
                    standardTotal++;
                    if (isOccupied) standardOccupied++;
                }
                case DELUXE -> {
                    deluxeTotal++;
                    if (isOccupied) deluxeOccupied++;
                }
                case SUITE -> {
                    suiteTotal++;
                    if (isOccupied) suiteOccupied++;
                }
            }
        }

        // Rebuild the category progress bar list
        if (categoryGrid != null) {
            categoryGrid.removeAll();

            categoryGrid.add(createCategoryProgressBar("Standard Tier Rooms", standardOccupied, standardTotal, ThemeManager.ACCENT_BLUE));
            categoryGrid.add(Box.createVerticalStrut(12));
            categoryGrid.add(createCategoryProgressBar("Deluxe Tier Rooms", deluxeOccupied, deluxeTotal, ThemeManager.ACCENT_PURPLE));
            categoryGrid.add(Box.createVerticalStrut(12));
            categoryGrid.add(createCategoryProgressBar("Luxury Suite Rooms", suiteOccupied, suiteTotal, ThemeManager.ACCENT_GOLD));

            categoryGrid.revalidate();
            categoryGrid.repaint();
        }
    }

    private JPanel createCategoryProgressBar(String label, int occupied, int total, Color barColor) {
        JPanel progressPanel = new JPanel();
        progressPanel.setOpaque(false);
        progressPanel.setLayout(new BorderLayout(0, 4));

        // Header info row
        JPanel textRow = new JPanel(new BorderLayout());
        textRow.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_SMALL);
        lbl.setForeground(ThemeManager.TEXT_PRIMARY);
        textRow.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(occupied + " / " + total + " Booked");
        val.setFont(ThemeManager.FONT_SMALL);
        val.setForeground(ThemeManager.TEXT_SECONDARY);
        textRow.add(val, BorderLayout.EAST);

        progressPanel.add(textRow, BorderLayout.NORTH);

        // Visual progress bar
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Track Background
                g2.setColor(ThemeManager.BG_INPUT);
                g2.fillRoundRect(0, 0, w, h, 6, 6);

                // Progress Fill
                if (total > 0) {
                    double pct = (double) occupied / total;
                    int fillWidth = (int) (w * pct);
                    if (fillWidth > 0) {
                        g2.setColor(barColor);
                        g2.fillRoundRect(0, 0, fillWidth, h, 6, 6);
                    }
                }

                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setOpaque(false);
        progressPanel.add(bar, BorderLayout.CENTER);

        return progressPanel;
    }
}
