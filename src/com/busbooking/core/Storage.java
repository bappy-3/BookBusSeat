package com.busbooking.core;

import com.busbooking.model.Seat;

public interface Storage {
    void save(BusServiceState state) throws Exception;
    BusServiceState load() throws Exception;

    class BusServiceState {
        public final boolean[][][][] booked; // [route][day][time][seat]
        public final Seat[][][][] seatData; // parallel data for name/id/phone

        public BusServiceState(boolean[][][][] booked, Seat[][][][] seatData) {
            this.booked = booked;
            this.seatData = seatData;
        }
    }
}

