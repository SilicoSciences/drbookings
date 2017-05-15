package com.github.drbookings.ui.beans;

import java.time.LocalDate;

import com.github.drbookings.model.data.Room;
import com.github.drbookings.ui.DateEntry;

public class RoomEntry extends DateEntry<Room> {

    public RoomEntry(final LocalDate date, final Room element) {
	super(date, element);
    }

}