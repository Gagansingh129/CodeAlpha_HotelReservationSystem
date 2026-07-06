package org.example.ui;

import org.example.model.Reservation;
import org.example.model.ReservationStatus;
import org.example.service.ReservationManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationsPanel extends JPanel {

    private final HotelReservationApp app;
    private final ReservationManager manager;

    private JTable resTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel totalResLabel;

    private List<Reservation> allReservations;

    public ReservationsPanel(HotelReservationApp app) {
        this.app = app;
        this.manager = new ReservationManager();

        setLayout(new BorderLayout(0, ThemeManager.PADDING));
        setBackground(ThemeManager.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING));

        // Header Section
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Toolbar and Table Card
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Toolbar: Search filter
        JPanel toolbarCard = createToolbar();
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(16));

        // Table Panel Card
        JPanel tableCard = createTableCard();
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);

        refresh();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Reservations Management");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Track guest bookings, payment records, and cancellations.");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);

        // Counter label on right
        totalResLabel = new JLabel("0 Total Reservations");
        totalResLabel.setFont(ThemeManager.FONT_SUBHEADING);
        totalResLabel.setForeground(ThemeManager.ACCENT_GOLD);
        totalResLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(totalResLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createToolbar() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        searchPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍 Filter bookings:");
        searchIcon.setFont(ThemeManager.FONT_BODY);
        searchIcon.setForeground(ThemeManager.TEXT_SECONDARY);
        searchPanel.add(searchIcon);

        searchField = ThemeManager.createStyledTextField("Search by ID or Guest Name...");
        searchField.setPreferredSize(new Dimension(300, 36));
        // Add real-time search filtering on typing
        searchField.addCaretListener(e -> filterReservations());
        searchPanel.add(searchField);

        card.add(searchPanel, BorderLayout.WEST);

        // Operations buttons on right
        JPanel operationsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        operationsPanel.setOpaque(false);

        JButton btnViewDetails = ThemeManager.createSecondaryButton("View Full Details");
        btnViewDetails.setPreferredSize(new Dimension(150, 36));
        btnViewDetails.addActionListener(e -> showSelectedDetails());
        operationsPanel.add(btnViewDetails);

        JButton btnCancel = ThemeManager.createDangerButton("Cancel Booking");
        btnCancel.setPreferredSize(new Dimension(140, 36));
        btnCancel.addActionListener(e -> cancelSelectedBooking());
        operationsPanel.add(btnCancel);

        card.add(operationsPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel createTableCard() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout());

        String[] columns = {"Booking ID", "Guest Name", "Room", "Category", "Check-In", "Check-Out", "Amount Paid", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resTable = new JTable(tableModel);
        resTable.setFont(ThemeManager.FONT_BODY);
        resTable.setForeground(ThemeManager.TEXT_PRIMARY);
        resTable.setBackground(ThemeManager.BG_CARD);
        resTable.setRowHeight(38);
        resTable.setGridColor(ThemeManager.BORDER);
        resTable.setShowHorizontalLines(true);
        resTable.setShowVerticalLines(false);
        resTable.setSelectionBackground(new Color(0x1c, 0x22, 0x2c));
        resTable.setSelectionForeground(ThemeManager.TEXT_PRIMARY);

        // Double click row opens details
        resTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelectedDetails();
                }
            }
        });

        // Header Styling
        JTableHeader header = resTable.getTableHeader();
        header.setFont(ThemeManager.FONT_BUTTON);
        header.setBackground(ThemeManager.BG_INPUT);
        header.setForeground(ThemeManager.TEXT_PRIMARY);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));

        // Row Alternating & Badge Rendering
        resTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? ThemeManager.BG_CARD : ThemeManager.BG_DARK);
                c.setForeground(ThemeManager.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                if (column == 7) { // Status column
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
                } else if (column == 6) { // Amount Paid
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setForeground(ThemeManager.ACCENT_GOLD);
                } else if (column == 0) { // Reservation ID
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

        JScrollPane scroll = new JScrollPane(resTable);
        ThemeManager.styleScrollPane(scroll);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void filterReservations() {
        String filter = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        List<Reservation> filtered;
        if (filter.isEmpty() || filter.startsWith("search by id or")) {
            filtered = allReservations;
        } else {
            filtered = allReservations.stream()
                    .filter(r -> r.getReservationId().toLowerCase().contains(filter)
                            || r.getGuest().getName().toLowerCase().contains(filter))
                    .collect(Collectors.toList());
        }

        for (Reservation r : filtered) {
            tableModel.addRow(new Object[]{
                    r.getReservationId().substring(0, 12) + "...",
                    r.getGuest().getName(),
                    "Room " + r.getRoom().getRoomNumber(),
                    r.getRoom().getCategory().getDisplayName(),
                    r.getCheckIn().toString(),
                    r.getCheckOut().toString(),
                    String.format("₹%,.2f", r.getTotalAmount()),
                    r.getStatus().getDisplayName()
            });
        }
    }

    private Reservation getSelectedReservation() {
        int selectedRow = resTable.getSelectedRow();
        if (selectedRow == -1) return null;

        // Extract ID from full list using search criteria or matching selected index
        // To be safe, we retrieve the truncated ID in column 0 and search by prefix
        String truncatedId = (String) tableModel.getValueAt(selectedRow, 0);
        String prefix = truncatedId.replace("...", "");

        return allReservations.stream()
                .filter(r -> r.getReservationId().startsWith(prefix))
                .findFirst()
                .orElse(null);
    }

    private void showSelectedDetails() {
        Reservation r = getSelectedReservation();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Display beautiful Details Dialog
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reservation Details", true);
        dlg.setSize(500, 480);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new ThemeManager.RoundedPanel(ThemeManager.CARD_RADIUS, ThemeManager.BG_CARD, ThemeManager.BORDER);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Title
        JLabel title = new JLabel("✦ THE GRAND HOTEL — Details");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.ACCENT_GOLD);
        gbc.gridy = 0;
        panel.add(title, gbc);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.BORDER);
        gbc.gridy = 1;
        panel.add(sep, gbc);

        // Content Builder
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width: 380px; font-family: Segoe UI; color: #E6EDF3;'>");
        sb.append("<table style='width: 100%; border-collapse: collapse;'>");
        sb.append("<tr><td><b>Booking Reference:</b></td><td style='color:#8B949E;'>").append(r.getReservationId()).append("</td></tr>");
        sb.append("<tr><td><b>Status:</b></td><td style='color:#3FB950;'><b>").append(r.getStatus().getDisplayName()).append("</b></td></tr>");
        sb.append("<tr><td colspan='2'><hr style='border:0;border-top:1px solid #30363d;margin:8px 0;'></td></tr>");
        sb.append("<tr><td><b>Guest Name:</b></td><td>").append(r.getGuest().getName()).append("</td></tr>");
        sb.append("<tr><td><b>Email:</b></td><td>").append(r.getGuest().getEmail()).append("</td></tr>");
        sb.append("<tr><td><b>Phone:</b></td><td>").append(r.getGuest().getPhone()).append("</td></tr>");
        sb.append("<tr><td colspan='2'><hr style='border:0;border-top:1px solid #30363d;margin:8px 0;'></td></tr>");
        sb.append("<tr><td><b>Reserved Room:</b></td><td>Room ").append(r.getRoom().getRoomNumber()).append(" (Floor ").append(r.getRoom().getFloor()).append(")</td></tr>");
        sb.append("<tr><td><b>Category:</b></td><td>").append(r.getRoom().getCategory().getDisplayName()).append("</td></tr>");
        sb.append("<tr><td><b>Stay Period:</b></td><td>").append(r.getCheckIn().toString()).append(" to ").append(r.getCheckOut().toString()).append("</td></tr>");
        sb.append("<tr><td><b>Nights:</b></td><td>").append(r.computeNights()).append(" Nights</td></tr>");
        sb.append("<tr><td colspan='2'><hr style='border:0;border-top:1px solid #30363d;margin:8px 0;'></td></tr>");
        sb.append("<tr><td><b style='font-size:16px;color:#E2B04A;'>Grand Total:</b></td><td style='font-size:18px;font-weight:bold;color:#E2B04A;'>").append(String.format("₹%,.2f", r.getTotalAmount())).append("</td></tr>");
        sb.append("</table>");
        sb.append("</body></html>");

        JLabel contentLabel = new JLabel(sb.toString());
        gbc.gridy = 2;
        panel.add(contentLabel, gbc);

        // Action Button Close
        JButton btnClose = ThemeManager.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnClose, gbc);

        dlg.add(panel);
        dlg.setUndecorated(true);
        dlg.setVisible(true);
    }

    private void cancelSelectedBooking() {
        Reservation r = getSelectedReservation();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (r.getStatus() == ReservationStatus.CANCELLED) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.", "Operation Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel the reservation for " + r.getGuest().getName() + "?\nThis will make Room " + r.getRoom().getRoomNumber() + " available for booking.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.cancelReservation(r.getReservationId());
                JOptionPane.showMessageDialog(this, "Reservation cancelled successfully.", "Booking Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refresh() {
        allReservations = manager.getAllReservations();
        totalResLabel.setText(allReservations.size() + " Total Reservations");
        filterReservations();
    }
}
