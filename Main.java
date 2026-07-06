package org.example;

import org.example.ui.HotelReservationApp;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize the GUI and show the window
                HotelReservationApp app = new HotelReservationApp();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}


