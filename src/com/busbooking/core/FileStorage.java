package com.busbooking.core;

import com.busbooking.model.Seat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileStorage implements Storage {
    private final String filePath;

    public FileStorage(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void save(BusServiceState state) throws Exception {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int r = 0; r < AbstractBusService.MAX_ROUTES; r++) {
                for (int d = 0; d < AbstractBusService.MAX_DAYS; d++) {
                    for (int t = 0; t < AbstractBusService.MAX_TIMES; t++) {
                        for (int s = 0; s < AbstractBusService.MAX_SEATS; s++) {
                            if (state.booked[r][d][t][s]) {
                                Seat seat = state.seatData[r][d][t][s];
                                String line = r + "," + d + "," + t + "," + s + "," +
                                        escape(seat.getName()) + "," + escape(seat.getId()) + "," + escape(seat.getPhone());
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public BusServiceState load() throws Exception {
        File file = new File(filePath);
        boolean[][][][] booked = new boolean[AbstractBusService.MAX_ROUTES][AbstractBusService.MAX_DAYS][AbstractBusService.MAX_TIMES][AbstractBusService.MAX_SEATS];
        Seat[][][][] seats = new Seat[AbstractBusService.MAX_ROUTES][AbstractBusService.MAX_DAYS][AbstractBusService.MAX_TIMES][AbstractBusService.MAX_SEATS];
        for (int r = 0; r < AbstractBusService.MAX_ROUTES; r++) {
            for (int d = 0; d < AbstractBusService.MAX_DAYS; d++) {
                for (int t = 0; t < AbstractBusService.MAX_TIMES; t++) {
                    for (int s = 0; s < AbstractBusService.MAX_SEATS; s++) {
                        seats[r][d][t][s] = new Seat();
                    }
                }
            }
        }
        if (!file.exists()) {
            return new BusServiceState(booked, seats);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsv(line);
                if (parts.length < 7) continue;
                int r = Integer.parseInt(parts[0]);
                int d = Integer.parseInt(parts[1]);
                int t = Integer.parseInt(parts[2]);
                int s = Integer.parseInt(parts[3]);
                booked[r][d][t][s] = true;
                Seat seat = seats[r][d][t][s];
                seat.setBooked(true);
                seat.setName(unescape(parts[4]));
                seat.setId(unescape(parts[5]));
                seat.setPhone(unescape(parts[6]));
            }
        }
        return new BusServiceState(booked, seats);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(",", "\\,").replace("\n", "\\n");
    }

    private static String unescape(String s) {
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                if (c == 'n') out.append('\n');
                else out.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String[] splitCsv(String line) {
        String[] parts = new String[7];
        StringBuilder current = new StringBuilder();
        int idx = 0;
        boolean esc = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (esc) {
                current.append(c == 'n' ? '\n' : c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == ',') {
                if (idx < parts.length) parts[idx++] = current.toString();
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (idx < parts.length) parts[idx] = current.toString();
        return parts;
    }
}

