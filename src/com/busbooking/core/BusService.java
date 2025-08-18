package com.busbooking.core;

import com.busbooking.exception.InvalidCredentialsException;
import com.busbooking.exception.InvalidSelectionException;
import com.busbooking.exception.SeatAlreadyBookedException;
import com.busbooking.model.Seat;

public class BusService extends AbstractBusService {
    private final Storage storage;

    private final String[] routeNames = new String[] {
            "Uttara to NSU", "NSU to Uttara", "Dhanmondi to NSU", "NSU to Dhanmondi",
            "Mirpur to NSU", "NSU to Mirpur", "Banani to NSU", "NSU to Banani"
    };
    private final String[] dayNames = new String[] {"ST", "MW", "RA"};
    private final String[] timeSlots = new String[] {
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
    };

    private final boolean[][][][] booked = new boolean[MAX_ROUTES][MAX_DAYS][MAX_TIMES][MAX_SEATS];
    private final Seat[][][][] seats = new Seat[MAX_ROUTES][MAX_DAYS][MAX_TIMES][MAX_SEATS];

    public BusService(Storage storage) {
        this.storage = storage;
        for (int r = 0; r < MAX_ROUTES; r++) {
            for (int d = 0; d < MAX_DAYS; d++) {
                for (int t = 0; t < MAX_TIMES; t++) {
                    for (int s = 0; s < MAX_SEATS; s++) {
                        seats[r][d][t][s] = new Seat();
                    }
                }
            }
        }
        tryLoad();
    }

    private void tryLoad() {
        try {
            Storage.BusServiceState state = storage.load();
            if (state != null) {
                for (int r = 0; r < MAX_ROUTES; r++) {
                    for (int d = 0; d < MAX_DAYS; d++) {
                        for (int t = 0; t < MAX_TIMES; t++) {
                            for (int s = 0; s < MAX_SEATS; s++) {
                                booked[r][d][t][s] = state.booked[r][d][t][s];
                                Seat src = state.seatData[r][d][t][s];
                                Seat dst = seats[r][d][t][s];
                                dst.setBooked(src.isBooked());
                                dst.setName(src.getName());
                                dst.setId(src.getId());
                                dst.setPhone(src.getPhone());
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String[] getRouteNames() {
        return routeNames;
    }

    @Override
    public String[] getDayNames() {
        return dayNames;
    }

    @Override
    public String[] getTimeSlots() {
        return timeSlots;
    }

    @Override
    public boolean isBooked(int routeIndex, int dayIndex, int timeIndex, int seatIndex) throws InvalidSelectionException {
        validateIndices(routeIndex, dayIndex, timeIndex, seatIndex);
        return booked[routeIndex][dayIndex][timeIndex][seatIndex];
    }

    @Override
    public Seat getSeat(int routeIndex, int dayIndex, int timeIndex, int seatIndex) throws InvalidSelectionException {
        validateIndices(routeIndex, dayIndex, timeIndex, seatIndex);
        return seats[routeIndex][dayIndex][timeIndex][seatIndex];
    }

    @Override
    public void bookSeat(int routeIndex, int dayIndex, int timeIndex, int seatIndex, String name, String id, String phone)
            throws InvalidSelectionException, SeatAlreadyBookedException {
        validateIndices(routeIndex, dayIndex, timeIndex, seatIndex);
        if (booked[routeIndex][dayIndex][timeIndex][seatIndex]) {
            throw new SeatAlreadyBookedException("Seat already booked");
        }
        Seat seat = seats[routeIndex][dayIndex][timeIndex][seatIndex];
        seat.setName(name);
        seat.setId(id);
        seat.setPhone(phone);
        seat.setBooked(true);
        booked[routeIndex][dayIndex][timeIndex][seatIndex] = true;
        persist();
    }

    @Override
    public void cancelSeat(int routeIndex, int dayIndex, int timeIndex, int seatIndex, String id, String phone) throws InvalidSelectionException, InvalidCredentialsException {
        validateIndices(routeIndex, dayIndex, timeIndex, seatIndex);
        Seat seat = seats[routeIndex][dayIndex][timeIndex][seatIndex];
        if (seat.isBooked()) {
            boolean idMatches = seat.getId() != null && seat.getId().equals(id);
            boolean phoneMatches = seat.getPhone() != null && seat.getPhone().equals(phone);
            if (!(idMatches && phoneMatches)) {
                throw new InvalidCredentialsException("Invalid credentials");
            }
        }
        seat.setBooked(false);
        seat.setName("");
        seat.setId("");
        seat.setPhone("");
        booked[routeIndex][dayIndex][timeIndex][seatIndex] = false;
        persist();
    }

    @Override
    public void persist() {
        try {
            storage.save(new Storage.BusServiceState(booked, seats));
        } catch (Exception ignored) {
        }
    }

    private void validateIndices(int routeIndex, int dayIndex, int timeIndex, int seatIndex) throws InvalidSelectionException {
        if (routeIndex < 0 || routeIndex >= MAX_ROUTES ||
                dayIndex < 0 || dayIndex >= MAX_DAYS ||
                timeIndex < 0 || timeIndex >= MAX_TIMES ||
                seatIndex < 0 || seatIndex >= MAX_SEATS) {
            throw new InvalidSelectionException("Invalid selection indices");
        }
    }
}

