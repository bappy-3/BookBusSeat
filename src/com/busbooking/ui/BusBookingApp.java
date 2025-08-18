package com.busbooking.ui;

import com.busbooking.core.AbstractBusService;
import com.busbooking.core.BusService;
import com.busbooking.exception.InvalidSelectionException;
import com.busbooking.exception.SeatAlreadyBookedException;
import com.busbooking.model.Seat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

public class BusBookingApp extends JFrame {
    private final AbstractBusService service;
    private final JComboBox<String> routeBox;
    private final JComboBox<String> dayBox;
    private final JComboBox<String> timeBox;
    private final JPanel seatPanel;

    public BusBookingApp(BusService service) {
        super("Bus Seat Booking");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);

        JPanel top = new JPanel();
        routeBox = new JComboBox<>(service.getRouteNames());
        dayBox = new JComboBox<>(service.getDayNames());
        timeBox = new JComboBox<>(service.getTimeSlots());
        JButton refreshBtn = new JButton("View Seats");
        JButton bookBtn = new JButton("Book");
        JButton cancelBtn = new JButton("Cancel");
        top.add(new JLabel("Route:"));
        top.add(routeBox);
        top.add(new JLabel("Day:"));
        top.add(dayBox);
        top.add(new JLabel("Time:"));
        top.add(timeBox);
        top.add(refreshBtn);
        top.add(bookBtn);
        top.add(cancelBtn);

        seatPanel = new JPanel(new GridLayout(11, 5, 10, 10));
        seatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(seatPanel);

        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refreshSeats());
        bookBtn.addActionListener(e -> bookSeat());
        cancelBtn.addActionListener(e -> cancelSeat());

        refreshSeats();
    }

    private void refreshSeats() {
        seatPanel.removeAll();
        int r = routeBox.getSelectedIndex();
        int d = dayBox.getSelectedIndex();
        int t = timeBox.getSelectedIndex();

        // Row 0: driver row (no seats), driver at top-right corner
        seatPanel.add(createSpacer());
        seatPanel.add(createSpacer());
        seatPanel.add(createSpacer()); // aisle
        seatPanel.add(createSpacer());
        JLabel driver = new JLabel("Driver", SwingConstants.CENTER);
        driver.setOpaque(true);
        driver.setBackground(new Color(200, 220, 255));
        driver.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        seatPanel.add(driver);

        // Rows 1..10: 2 seats, aisle, 2 seats
        int seatCounter = 0; // maps to seat indices 0..39
        for (int row = 1; row <= 10; row++) {
            for (int col = 0; col < 5; col++) {
                if (col == 2) { // aisle
                    seatPanel.add(createSpacer());
                    continue;
                }
                int seatInRow = col < 2 ? col : col - 1; // 0,1, -,2,3
                int seatIndex = (row - 1) * 4 + seatInRow;
                seatCounter = seatIndex;

                JButton btn = new JButton(String.valueOf(seatIndex + 1));
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                try {
                    boolean booked = service.isBooked(r, d, t, seatIndex);
                    btn.setBackground(booked ? new Color(255, 200, 200) : new Color(200, 255, 200));
                    if (booked) {
                        Seat s = service.getSeat(r, d, t, seatIndex);
                        btn.setToolTipText("Booked by: " + s.getName());
                    } else {
                        btn.setToolTipText("Available");
                    }
                } catch (InvalidSelectionException ex) {
                    btn.setBackground(Color.LIGHT_GRAY);
                }
                seatPanel.add(btn);
            }
        }
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private JPanel createSpacer() {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        return spacer;
    }

    private void bookSeat() {
        int r = routeBox.getSelectedIndex();
        int d = dayBox.getSelectedIndex();
        int t = timeBox.getSelectedIndex();
        String seatStr = JOptionPane.showInputDialog(this, "Enter seat number (1-40):");
        if (seatStr == null || seatStr.isBlank()) return;
        try {
            int seatIndex = Integer.parseInt(seatStr) - 1;
            JTextField nameField = new JTextField();
            JTextField idField = new JTextField();
            JTextField phoneField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("ID:"));
            panel.add(idField);
            panel.add(new JLabel("Phone:"));
            panel.add(phoneField);
            int result = JOptionPane.showConfirmDialog(this, panel, "Enter passenger details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                service.bookSeat(r, d, t, seatIndex, nameField.getText(), idField.getText(), phoneField.getText());
                JOptionPane.showMessageDialog(this, "Seat booked successfully");
                refreshSeats();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SeatAlreadyBookedException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidSelectionException ex) {
            JOptionPane.showMessageDialog(this, "Invalid selection", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSeat() {
        int r = routeBox.getSelectedIndex();
        int d = dayBox.getSelectedIndex();
        int t = timeBox.getSelectedIndex();
        String seatStr = JOptionPane.showInputDialog(this, "Enter seat number (1-40) to cancel:");
        if (seatStr == null || seatStr.isBlank()) return;
        try {
            int seatIndex = Integer.parseInt(seatStr) - 1;
            JTextField idField = new JTextField();
            JTextField phoneField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("ID (used during booking):"));
            panel.add(idField);
            panel.add(new JLabel("Phone (used during booking):"));
            panel.add(phoneField);
            int result = JOptionPane.showConfirmDialog(this, panel, "Verify credentials", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;
            service.cancelSeat(r, d, t, seatIndex, idField.getText(), phoneField.getText());
            JOptionPane.showMessageDialog(this, "Booking cancelled");
            refreshSeats();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidSelectionException ex) {
            JOptionPane.showMessageDialog(this, "Invalid selection", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (com.busbooking.exception.InvalidCredentialsException ex) {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

