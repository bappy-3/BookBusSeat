package com.busbooking.core;

import com.busbooking.exception.DuplicateCredentialsException;
import com.busbooking.exception.InvalidCredentialsException;
import com.busbooking.exception.InvalidSelectionException;
import com.busbooking.exception.MissingCredentialsException;
import com.busbooking.exception.SeatAlreadyBookedException;
import com.busbooking.model.Seat;

public abstract class AbstractBusService {
    public static final int MAX_ROUTES = 8;
    public static final int MAX_DAYS = 3;
    public static final int MAX_TIMES = 10;
    public static final int MAX_SEATS = 40;

    public abstract String[] getRouteNames();
    public abstract String[] getDayNames();
    public abstract String[] getTimeSlots();

    public abstract boolean isBooked(int routeIndex, int dayIndex, int timeIndex, int seatIndex) throws InvalidSelectionException;
    public abstract Seat getSeat(int routeIndex, int dayIndex, int timeIndex, int seatIndex) throws InvalidSelectionException;

    public abstract void bookSeat(int routeIndex, int dayIndex, int timeIndex, int seatIndex, String name, String id, String phone)
            throws InvalidSelectionException, SeatAlreadyBookedException, MissingCredentialsException, DuplicateCredentialsException;

    public abstract void cancelSeat(int routeIndex, int dayIndex, int timeIndex, int seatIndex, String id, String phone) throws InvalidSelectionException, InvalidCredentialsException;

    public abstract void persist();
}

