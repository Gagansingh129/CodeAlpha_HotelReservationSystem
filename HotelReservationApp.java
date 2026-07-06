package org.example.ui;

import org.example.model.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class HotelReservationApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DashboardPanel dashboardPanel;
    private SearchPanel searchPanel;
    private BookingPanel bookingPanel;
    private ReservationsPanel reservationsPanel;
    private List<NavItem> navItems = new ArrayList<>();
    private String activeNav = "Dashboard";

    public static final String PANEL_DASHBOARD = "Dashboard";
    public static final String PANEL_SEARCH = "Search";
    public static final String PANEL_BOOKING = "Booking";
    public static final String PANEL_RESERVATIONS = "Reservations";

    public HotelReservationApp() {
        setTitle("The Grand Hotel — Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeManager.BG_DARK);

        // Main layout: sidebar on left, content on right
        setLayout(new BorderLayout());

        // Create sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Create content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeManager.BG_DARK);

        dashboardPanel = new DashboardPanel(this);
        searchPanel = new SearchPanel(this);
        bookingPanel = new BookingPanel(this);
        reservationsPanel = new ReservationsPanel(this);

        contentPanel.add(dashboardPanel, PANEL_DASHBOARD);
        contentPanel.add(searchPanel, PANEL_SEARCH);
        contentPanel.add(bookingPanel, PANEL_BOOKING);
        contentPanel.add(reservationsPanel, PANEL_RESERVATIONS);

        add(contentPanel, BorderLayout.CENTER);

        // Start on dashboard
        navigateTo(PANEL_DASHBOARD);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border line
                g2.setColor(ThemeManager.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setOpaque(false);

        // Top section: Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel logoLabel = new JLabel("THE GRAND HOTEL");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoLabel.setForeground(ThemeManager.ACCENT_GOLD);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Hotel & Resorts");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitleLabel);

        // Separator
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.BORDER);
                g.drawLine(24, 0, getWidth() - 24, 0);
            }
        };
        sep.setPreferredSize(new Dimension(0, 1));
        sep.setOpaque(false);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Navigation items
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addNavItem(navPanel, "\uD83D\uDCCA", "Dashboard", PANEL_DASHBOARD);
        addNavItem(navPanel, "\uD83D\uDD0D", "Search Rooms", PANEL_SEARCH);
        addNavItem(navPanel, "\uD83D\uDCDD", "New Booking", PANEL_BOOKING);
        addNavItem(navPanel, "\uD83D\uDCCB", "My Reservations", PANEL_RESERVATIONS);

        // Combine top section
        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(logoPanel);
        topSection.add(sep);
        topSection.add(navPanel);

        sidebar.add(topSection, BorderLayout.NORTH);

        // Bottom section: version info
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setFont(ThemeManager.FONT_SMALL);
        versionLabel.setForeground(ThemeManager.TEXT_MUTED);
        bottomPanel.add(versionLabel);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel parent, String emoji, String text, String panelName) {
        NavItem item = new NavItem(emoji, text, panelName);
        navItems.add(item);
        parent.add(item);
        parent.add(Box.createVerticalStrut(2));
    }

    /**
     * Navigate to a specific panel by name.
     */
    public void navigateTo(String panelName) {
        activeNav = panelName;
        cardLayout.show(contentPanel, panelName);

        // Refresh panel data
        switch (panelName) {
            case PANEL_DASHBOARD:
                dashboardPanel.refresh();
                break;
            case PANEL_SEARCH:
                searchPanel.refresh();
                break;
            case PANEL_BOOKING:
                bookingPanel.resetIfNeeded();
                break;
            case PANEL_RESERVATIONS:
                reservationsPanel.refresh();
                break;
        }

        // Update nav item highlighting
        for (NavItem item : navItems) {
            item.setActive(item.panelName.equals(panelName));
        }
    }

    /**
     * Set the selected room for the booking panel and navigate to it.
     */
    public void setBookingRoom(Room room, String checkIn, String checkOut) {
        bookingPanel.setSelectedRoom(room, checkIn, checkOut);
        navigateTo(PANEL_BOOKING);
    }

    // ==================== NAV ITEM ====================

    private class NavItem extends JPanel {
        private final String panelName;
        private boolean isActive = false;
        private boolean isHovered = false;
        private final Color hoverBg = new Color(0x1c, 0x22, 0x2c);
        private final Color activeBg = new Color(0x1f, 0x26, 0x31);

        NavItem(String emoji, String text, String panelName) {
            this.panelName = panelName;
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 48));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout());

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            inner.setOpaque(false);
            inner.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

            JLabel emojiLabel = new JLabel(emoji);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            emojiLabel.setForeground(ThemeManager.TEXT_SECONDARY);

            JLabel textLabel = new JLabel(text);
            textLabel.setFont(ThemeManager.FONT_SIDEBAR);
            textLabel.setForeground(ThemeManager.TEXT_SECONDARY);

            inner.add(emojiLabel);
            inner.add(textLabel);
            add(inner, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    HotelReservationApp.this.navigateTo(panelName);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.isActive = active;
            // Update child label colors
            updateLabels(this);
            repaint();
        }

        private void updateLabels(Container c) {
            for (Component comp : c.getComponents()) {
                if (comp instanceof JLabel lbl) {
                    lbl.setForeground(isActive ? ThemeManager.ACCENT_GOLD : ThemeManager.TEXT_SECONDARY);
                }
                if (comp instanceof Container container) {
                    updateLabels(container);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(activeBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Gold left border
                g2.setColor(ThemeManager.ACCENT_GOLD);
                g2.fillRoundRect(0, 6, 3, getHeight() - 12, 3, 3);
            } else if (isHovered) {
                g2.setColor(hoverBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
