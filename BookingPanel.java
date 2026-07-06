package org.example.ui;

import org.example.model.PaymentMethod;
import org.example.model.PaymentRecord;
import org.example.model.Reservation;
import org.example.model.Room;
import org.example.service.ReservationManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class BookingPanel extends JPanel {

    private final HotelReservationApp app;
    private final ReservationManager manager;

    private Room selectedRoom;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int currentStep = 1;

    // Wizard navigation cards
    private CardLayout wizardLayout;
    private JPanel wizardContent;

    // Step indicators
    private JLabel step1Label;
    private JLabel step2Label;
    private JLabel step3Label;

    // Input fields for Step 1
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JLabel roomDetailsLabel;

    // Summary labels for Step 2
    private JLabel summaryGuestName;
    private JLabel summaryGuestContact;
    private JLabel summaryRoomInfo;
    private JLabel summaryDates;
    private JLabel summaryNights;
    private JLabel summaryTotal;

    // Input fields for Step 3 (Payment)
    private JComboBox<PaymentMethod> paymentMethodCombo;
    private JTextField cardNumField;
    private JTextField cardExpiryField;
    private JTextField cardCvvField;

    // Success panels details
    private JLabel successResId;
    private JLabel successAmount;
    private JLabel successPaymentId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public BookingPanel(HotelReservationApp app) {
        this.app = app;
        this.manager = new ReservationManager();

        setLayout(new BorderLayout(0, ThemeManager.PADDING));
        setBackground(ThemeManager.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING, ThemeManager.PADDING));

        // Header Section
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Wizard layout & panel
        wizardLayout = new CardLayout();
        wizardContent = new JPanel(wizardLayout);
        wizardContent.setOpaque(false);

        // Add steps
        wizardContent.add(createStep1Panel(), "STEP_1");
        wizardContent.add(createStep2Panel(), "STEP_2");
        wizardContent.add(createStep3Panel(), "STEP_3");
        wizardContent.add(createSuccessPanel(), "SUCCESS");

        add(wizardContent, BorderLayout.CENTER);

        setStep(1);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Reservation Wizard");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Follow the steps to complete your reservation.");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);

        // Step Progress Indicators (horizontal row)
        JPanel stepsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        stepsRow.setOpaque(false);

        step1Label = new JLabel("1. Details");
        step1Label.setFont(ThemeManager.FONT_BUTTON);
        step1Label.setForeground(ThemeManager.ACCENT_GOLD);

        JLabel arrow1 = new JLabel("➔");
        arrow1.setFont(ThemeManager.FONT_BODY);
        arrow1.setForeground(ThemeManager.TEXT_MUTED);

        step2Label = new JLabel("2. Summary");
        step2Label.setFont(ThemeManager.FONT_BUTTON);
        step2Label.setForeground(ThemeManager.TEXT_MUTED);

        JLabel arrow2 = new JLabel("➔");
        arrow2.setFont(ThemeManager.FONT_BODY);
        arrow2.setForeground(ThemeManager.TEXT_MUTED);

        step3Label = new JLabel("3. Payment");
        step3Label.setFont(ThemeManager.FONT_BUTTON);
        step3Label.setForeground(ThemeManager.TEXT_MUTED);

        stepsRow.add(step1Label);
        stepsRow.add(arrow1);
        stepsRow.add(step2Label);
        stepsRow.add(arrow2);
        stepsRow.add(step3Label);

        header.add(stepsRow, BorderLayout.EAST);
        return header;
    }

    private void setStep(int step) {
        this.currentStep = step;
        step1Label.setForeground(ThemeManager.TEXT_MUTED);
        step2Label.setForeground(ThemeManager.TEXT_MUTED);
        step3Label.setForeground(ThemeManager.TEXT_MUTED);

        if (step == 1) {
            step1Label.setForeground(ThemeManager.ACCENT_GOLD);
            wizardLayout.show(wizardContent, "STEP_1");
        } else if (step == 2) {
            step2Label.setForeground(ThemeManager.ACCENT_GOLD);
            wizardLayout.show(wizardContent, "STEP_2");
        } else if (step == 3) {
            step3Label.setForeground(ThemeManager.ACCENT_GOLD);
            wizardLayout.show(wizardContent, "STEP_3");
        } else if (step == 4) {
            wizardLayout.show(wizardContent, "SUCCESS");
        }
    }

    // ==================== STEP 1: GUEST DETAILS ====================

    private JPanel createStep1Panel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);

        // Left Side: Room Summary Card
        JPanel roomSummaryCard = ThemeManager.createCard();
        roomSummaryCard.setPreferredSize(new Dimension(320, 360));
        roomSummaryCard.setLayout(new BorderLayout());

        JLabel rsTitle = new JLabel("Selected Room");
        rsTitle.setFont(ThemeManager.FONT_HEADING);
        rsTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        roomSummaryCard.add(rsTitle, BorderLayout.NORTH);

        roomDetailsLabel = new JLabel("<html>No room selected.<br>Please search and choose a room first.</html>");
        roomDetailsLabel.setFont(ThemeManager.FONT_BODY);
        roomDetailsLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        roomSummaryCard.add(roomDetailsLabel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        container.add(roomSummaryCard, gbc);

        // Right Side: Guest details input form
        JPanel formCard = ThemeManager.createCard();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(8, 16, 8, 16);
        formGbc.weightx = 1.0;

        // Title
        JLabel formTitle = new JLabel("Guest Information");
        formTitle.setFont(ThemeManager.FONT_HEADING);
        formTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.gridwidth = 2;
        formCard.add(formTitle, formGbc);

        // Name
        formGbc.gridwidth = 1;
        formGbc.gridy = 1;
        formGbc.gridx = 0;
        JLabel nameLabel = new JLabel("Full Name *");
        nameLabel.setFont(ThemeManager.FONT_SMALL);
        nameLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formCard.add(nameLabel, formGbc);

        formGbc.gridx = 1;
        nameField = ThemeManager.createStyledTextField("e.g. John Doe");
        formCard.add(nameField, formGbc);

        // Email
        formGbc.gridy = 2;
        formGbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email Address *");
        emailLabel.setFont(ThemeManager.FONT_SMALL);
        emailLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formCard.add(emailLabel, formGbc);

        formGbc.gridx = 1;
        emailField = ThemeManager.createStyledTextField("john.doe@example.com");
        formCard.add(emailField, formGbc);

        // Phone
        formGbc.gridy = 3;
        formGbc.gridx = 0;
        JLabel phoneLabel = new JLabel("Phone Number *");
        phoneLabel.setFont(ThemeManager.FONT_SMALL);
        phoneLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formCard.add(phoneLabel, formGbc);

        formGbc.gridx = 1;
        phoneField = ThemeManager.createStyledTextField("e.g. +91 9876543210");
        formCard.add(phoneField, formGbc);

        // Check-In
        formGbc.gridy = 4;
        formGbc.gridx = 0;
        JLabel checkInLabel = new JLabel("Check-In Date *");
        checkInLabel.setFont(ThemeManager.FONT_SMALL);
        checkInLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formCard.add(checkInLabel, formGbc);

        formGbc.gridx = 1;
        checkInField = ThemeManager.createStyledTextField("YYYY-MM-DD");
        formCard.add(checkInField, formGbc);

        // Check-Out
        formGbc.gridy = 5;
        formGbc.gridx = 0;
        JLabel checkOutLabel = new JLabel("Check-Out Date *");
        checkOutLabel.setFont(ThemeManager.FONT_SMALL);
        checkOutLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        formCard.add(checkOutLabel, formGbc);

        formGbc.gridx = 1;
        checkOutField = ThemeManager.createStyledTextField("YYYY-MM-DD");
        formCard.add(checkOutField, formGbc);

        // Next Button
        formGbc.gridy = 6;
        formGbc.gridx = 1;
        formGbc.fill = GridBagConstraints.NONE;
        formGbc.anchor = GridBagConstraints.EAST;
        JButton btnNext = ThemeManager.createPrimaryButton("Continue to Summary");
        btnNext.addActionListener(e -> processStep1());
        formCard.add(btnNext, formGbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(formCard, gbc);

        return container;
    }

    private void processStep1() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Please select a room from the search tab first.", "Room Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String checkInStr = checkInField.getText().trim();
        String checkOutStr = checkOutField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || checkInStr.isEmpty() || checkOutStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all the required fields.", "Incomplete Fields", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            checkInDate = LocalDate.parse(checkInStr, DATE_FORMATTER);
            checkOutDate = LocalDate.parse(checkOutStr, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Invalid Dates", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.", "Invalid Date Range", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Fill Step 2 details
        summaryGuestName.setText(name);
        summaryGuestContact.setText("Email: " + email + "  |  Phone: " + phone);
        summaryRoomInfo.setText(selectedRoom.getCategory().getDisplayName() + " — Room " + selectedRoom.getRoomNumber() + " (Floor " + selectedRoom.getFloor() + ")");
        summaryDates.setText("From " + checkInStr + " to " + checkOutStr);

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double total = selectedRoom.getPricePerNight() * nights;

        summaryNights.setText(nights + " Nights Stay");
        summaryTotal.setText(String.format("₹%,.2f", total));

        setStep(2);
    }

    // ==================== STEP 2: SUMMARY ====================

    private JPanel createStep2Panel() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Heading
        JLabel title = new JLabel("Verify Reservation Details");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        gbc.gridy = 0;
        card.add(title, gbc);

        // Inner fields container
        JPanel summaryBox = new JPanel(new GridLayout(5, 1, 8, 12));
        summaryBox.setOpaque(false);
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        summaryGuestName = new JLabel();
        summaryGuestName.setFont(ThemeManager.FONT_SUBHEADING);
        summaryGuestName.setForeground(ThemeManager.TEXT_PRIMARY);
        summaryBox.add(summaryGuestName);

        summaryGuestContact = new JLabel();
        summaryGuestContact.setFont(ThemeManager.FONT_BODY);
        summaryGuestContact.setForeground(ThemeManager.TEXT_SECONDARY);
        summaryBox.add(summaryGuestContact);

        summaryRoomInfo = new JLabel();
        summaryRoomInfo.setFont(ThemeManager.FONT_BODY);
        summaryRoomInfo.setForeground(ThemeManager.TEXT_PRIMARY);
        summaryBox.add(summaryRoomInfo);

        summaryDates = new JLabel();
        summaryDates.setFont(ThemeManager.FONT_BODY);
        summaryDates.setForeground(ThemeManager.TEXT_SECONDARY);
        summaryBox.add(summaryDates);

        JPanel pricingRow = new JPanel(new BorderLayout());
        pricingRow.setOpaque(false);
        summaryNights = new JLabel();
        summaryNights.setFont(ThemeManager.FONT_BODY);
        summaryNights.setForeground(ThemeManager.TEXT_SECONDARY);
        pricingRow.add(summaryNights, BorderLayout.WEST);

        summaryTotal = new JLabel();
        summaryTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        summaryTotal.setForeground(ThemeManager.ACCENT_GOLD);
        pricingRow.add(summaryTotal, BorderLayout.EAST);
        summaryBox.add(pricingRow);

        gbc.gridy = 1;
        card.add(summaryBox, gbc);

        // Buttons navigation row
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonsRow.setOpaque(false);

        JButton btnBack = ThemeManager.createSecondaryButton("Back");
        btnBack.addActionListener(e -> setStep(1));
        buttonsRow.add(btnBack);

        JButton btnConfirm = ThemeManager.createPrimaryButton("Confirm & Proceed to Pay");
        btnConfirm.addActionListener(e -> setStep(3));
        buttonsRow.add(btnConfirm);

        gbc.gridy = 2;
        card.add(buttonsRow, gbc);

        return card;
    }

    // ==================== STEP 3: PAYMENT ====================

    private JPanel createStep3Panel() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Heading
        JLabel title = new JLabel("Secure Payment Gateway");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        gbc.gridy = 0;
        card.add(title, gbc);

        // Method Combo
        JPanel methodPanel = new JPanel(new BorderLayout(0, 4));
        methodPanel.setOpaque(false);
        JLabel lblMethod = new JLabel("Select Payment Method");
        lblMethod.setFont(ThemeManager.FONT_SMALL);
        lblMethod.setForeground(ThemeManager.TEXT_SECONDARY);
        methodPanel.add(lblMethod, BorderLayout.NORTH);

        paymentMethodCombo = new JComboBox<>(PaymentMethod.values());
        paymentMethodCombo.setFont(ThemeManager.FONT_BODY);
        paymentMethodCombo.setForeground(ThemeManager.TEXT_PRIMARY);
        paymentMethodCombo.setBackground(ThemeManager.BG_INPUT);
        paymentMethodCombo.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        paymentMethodCombo.setFocusable(false);
        methodPanel.add(paymentMethodCombo, BorderLayout.CENTER);

        gbc.gridy = 1;
        card.add(methodPanel, gbc);

        // Credit Card Details Simulation Form
        JPanel cardDetailsPanel = new JPanel(new GridBagLayout());
        cardDetailsPanel.setOpaque(false);
        cardDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeManager.BORDER), "Card Information", 0, 0, ThemeManager.FONT_SMALL, ThemeManager.TEXT_SECONDARY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.fill = GridBagConstraints.HORIZONTAL;
        cgbc.insets = new Insets(8, 8, 8, 8);
        cgbc.weightx = 1.0;

        // Card number
        cgbc.gridx = 0;
        cgbc.gridy = 0;
        cgbc.gridwidth = 2;
        JLabel lblCardNum = new JLabel("Card Number");
        lblCardNum.setFont(ThemeManager.FONT_SMALL);
        lblCardNum.setForeground(ThemeManager.TEXT_SECONDARY);
        cardDetailsPanel.add(lblCardNum, cgbc);

        cgbc.gridy = 1;
        cardNumField = ThemeManager.createStyledTextField("4111 2222 3333 4444");
        cardDetailsPanel.add(cardNumField, cgbc);

        // Expiry
        cgbc.gridwidth = 1;
        cgbc.gridy = 2;
        cgbc.gridx = 0;
        JLabel lblExpiry = new JLabel("Expiry Date");
        lblExpiry.setFont(ThemeManager.FONT_SMALL);
        lblExpiry.setForeground(ThemeManager.TEXT_SECONDARY);
        cardDetailsPanel.add(lblExpiry, cgbc);

        cgbc.gridy = 3;
        cardExpiryField = ThemeManager.createStyledTextField("MM/YY");
        cardDetailsPanel.add(cardExpiryField, cgbc);

        // CVV
        cgbc.gridx = 1;
        cgbc.gridy = 2;
        JLabel lblCvv = new JLabel("CVV / CVC");
        lblCvv.setFont(ThemeManager.FONT_SMALL);
        lblCvv.setForeground(ThemeManager.TEXT_SECONDARY);
        cardDetailsPanel.add(lblCvv, cgbc);

        cgbc.gridy = 3;
        cardCvvField = ThemeManager.createStyledTextField("123");
        cardDetailsPanel.add(cardCvvField, cgbc);

        gbc.gridy = 2;
        card.add(cardDetailsPanel, gbc);

        // Buttons navigation row
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonsRow.setOpaque(false);

        JButton btnBack = ThemeManager.createSecondaryButton("Back");
        btnBack.addActionListener(e -> setStep(2));
        buttonsRow.add(btnBack);

        JButton btnPay = ThemeManager.createPrimaryButton("Process Payment");
        btnPay.addActionListener(e -> handlePaymentProcessing());
        buttonsRow.add(btnPay);

        gbc.gridy = 3;
        card.add(buttonsRow, gbc);

        return card;
    }

    private void handlePaymentProcessing() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        PaymentMethod method = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        if (method == null) method = PaymentMethod.CREDIT_CARD;

        // Verify credit card inputs only if method is CREDIT_CARD/DEBIT_CARD
        if (method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD) {
            String num = cardNumField.getText().trim();
            String exp = cardExpiryField.getText().trim();
            String cvv = cardCvvField.getText().trim();
            if (num.isEmpty() || exp.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your card details.", "Card Information Required", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Show Processing dialog
        JDialog processingDlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Processing Payment", true);
        processingDlg.setUndecorated(true);
        processingDlg.setSize(300, 120);
        processingDlg.setLocationRelativeTo(this);

        JPanel panel = new ThemeManager.RoundedPanel(ThemeManager.CARD_RADIUS, ThemeManager.BG_CARD, ThemeManager.BORDER);
        panel.setLayout(new GridBagLayout());
        JLabel spinnerLabel = new JLabel("💳 Processing Transaction...");
        spinnerLabel.setFont(ThemeManager.FONT_SUBHEADING);
        spinnerLabel.setForeground(ThemeManager.ACCENT_GOLD);
        panel.add(spinnerLabel);
        processingDlg.add(panel);

        // Execute payment in background thread
        PaymentMethod finalMethod = method;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Reservation reservation;
            private PaymentRecord payment;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    // Make Reservation
                    reservation = manager.makeReservation(name, email, phone, selectedRoom.getRoomNumber(), checkInDate, checkOutDate);
                    // Process simulated payment
                    payment = manager.processPayment(reservation.getReservationId(), finalMethod);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                processingDlg.dispose();
                if (error != null) {
                    JOptionPane.showMessageDialog(BookingPanel.this, "Transaction failed: " + error.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Update success details and show
                    successResId.setText("Booking ID: " + reservation.getReservationId());
                    successAmount.setText("Total Charged: " + String.format("₹%,.2f", payment.getAmount()));
                    successPaymentId.setText("Payment Ref: " + payment.getPaymentId());

                    // Reset form fields
                    nameField.setText("");
                    emailField.setText("");
                    phoneField.setText("");
                    cardNumField.setText("");
                    cardExpiryField.setText("");
                    cardCvvField.setText("");

                    setStep(4);
                }
            }
        };

        worker.execute();
        processingDlg.setVisible(true); // blocks until dispose is called in done()
    }

    // ==================== STEP 4: SUCCESS ====================

    private JPanel createSuccessPanel() {
        JPanel card = ThemeManager.createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Big Checkmark Banner
        JLabel checkmark = new JLabel("✓");
        checkmark.setFont(new Font("Segoe UI", Font.BOLD, 72));
        checkmark.setForeground(ThemeManager.ACCENT_GREEN);
        checkmark.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        card.add(checkmark, gbc);

        JLabel confirmedTitle = new JLabel("Reservation Confirmed!");
        confirmedTitle.setFont(ThemeManager.FONT_HEADING);
        confirmedTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        confirmedTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        card.add(confirmedTitle, gbc);

        JLabel successMsg = new JLabel("A confirmation email and invoice have been dispatched.");
        successMsg.setFont(ThemeManager.FONT_BODY);
        successMsg.setForeground(ThemeManager.TEXT_SECONDARY);
        successMsg.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        card.add(successMsg, gbc);

        // Success info container
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 6, 8));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        successResId = new JLabel("Booking ID:");
        successResId.setFont(ThemeManager.FONT_BODY);
        successResId.setForeground(ThemeManager.TEXT_PRIMARY);
        infoPanel.add(successResId);

        successAmount = new JLabel("Total Charged:");
        successAmount.setFont(ThemeManager.FONT_BODY);
        successAmount.setForeground(ThemeManager.ACCENT_GOLD);
        infoPanel.add(successAmount);

        successPaymentId = new JLabel("Payment Ref:");
        successPaymentId.setFont(ThemeManager.FONT_SMALL);
        successPaymentId.setForeground(ThemeManager.TEXT_SECONDARY);
        infoPanel.add(successPaymentId);

        gbc.gridy = 3;
        card.add(infoPanel, gbc);

        // Navigation back buttons
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        buttonsRow.setOpaque(false);

        JButton btnViewBookings = ThemeManager.createSecondaryButton("View My Reservations");
        btnViewBookings.addActionListener(e -> {
            setStep(1);
            app.navigateTo(HotelReservationApp.PANEL_RESERVATIONS);
        });
        buttonsRow.add(btnViewBookings);

        JButton btnDone = ThemeManager.createPrimaryButton("Back to Dashboard");
        btnDone.addActionListener(e -> {
            setStep(1);
            app.navigateTo(HotelReservationApp.PANEL_DASHBOARD);
        });
        buttonsRow.add(btnDone);

        gbc.gridy = 4;
        card.add(buttonsRow, gbc);

        return card;
    }

    public void setSelectedRoom(Room room, String checkIn, String checkOut) {
        this.selectedRoom = room;

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width: 200px;'>");
        sb.append("<h3 style='margin:0;color:#E2B04A;'>").append(room.getCategory().getDisplayName()).append("</h3>");
        sb.append("<p style='margin:4px 0 12px 0;font-size:16px;font-weight:bold;color:#E6EDF3;'>Room ").append(room.getRoomNumber()).append("</p>");
        sb.append("<p style='margin:2px 0;'><b>Floor:</b> ").append(room.getFloor()).append("</p>");
        sb.append("<p style='margin:2px 0;'><b>Max Guests:</b> ").append(room.getMaxOccupancy()).append("</p>");
        sb.append("<p style='margin:12px 0;font-size:16px;color:#E2B04A;'><b>₹").append(String.format("%,.0f", room.getPricePerNight())).append("</b> / night</p>");
        sb.append("<p style='margin:0;color:#8B949E;font-size:11px;'>").append(room.getDescription()).append("</p>");
        sb.append("</body></html>");

        roomDetailsLabel.setText(sb.toString());

        checkInField.setText(checkIn);
        checkOutField.setText(checkOut);
    }

    public void reset() {
        setStep(1);
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        cardNumField.setText("");
        cardExpiryField.setText("");
        cardCvvField.setText("");

        selectedRoom = null;
        roomDetailsLabel.setText("<html>No room selected.<br>Please search and choose a room first.</html>");

        LocalDate today = LocalDate.now();
        checkInField.setText(today.format(DATE_FORMATTER));
        checkOutField.setText(today.plusDays(1).format(DATE_FORMATTER));
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }

    public void resetIfNeeded() {
        if (currentStep == 4 || selectedRoom == null) {
            reset();
        }
    }
}
