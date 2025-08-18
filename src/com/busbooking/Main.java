package com.busbooking;

import com.busbooking.core.BusService;
import com.busbooking.core.FileStorage;
import com.busbooking.ui.BusBookingApp;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BusService service = new BusService(new FileStorage("bus_booking_details.txt"));
            BusBookingApp app = new BusBookingApp(service);
            app.setVisible(true);
        });
    }
}

