/*
 * DrBookings
 * Copyright (C) 2016 - 2018 Alexander Kerner
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
package com.github.drbookings.model.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drbookings.model.BookingEntry;
import com.github.drbookings.model.BookingEntryPair;
import com.github.drbookings.model.RoomEntry;
import com.github.drbookings.model.exception.AlreadyBusyException;
import com.github.drbookings.model.exception.OverbookingException;
import com.github.drbookings.ui.CleaningEntry;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Alexander Kerner
 * @deprecated
 */
@Deprecated
public class DrBookingsDataImpl extends DrBookingsDataCore implements DrBookingsData {

    private static final Logger logger = LoggerFactory.getLogger(DrBookingsDataImpl.class);

    /**
     * A dummy property to listen on for booking changes. The actual value does not
     * mean anything. Maybe refactor to UI.
     */
    private final BooleanProperty bookingsChanged;
    /**
     * A dummy property to listen on for cleaning changes. The actual value does not
     * mean anything. Maybe refactor to UI.
     */
    private final BooleanProperty cleaningsChanged;

    private final DrBookingsCleaningData cleaningData;

    public DrBookingsDataImpl() {

	cleaningsChanged = new SimpleBooleanProperty(false);
	bookingsChanged = new SimpleBooleanProperty(false);
	this.cleaningData = new DrBookingsCleaningData();
    }

    @Override
    public synchronized List<BookingEntry> addBooking(final BookingBean bb) throws OverbookingException {
	final List<BookingEntry> result = super.addBooking(bb);
	notifyBookingsChanged();
	return result;
    }

    @Override
    public synchronized CleaningEntry createAndAddCleaning(final String id, final String cleaningName,
	    final LocalDate date, final String roomName) throws AlreadyBusyException {

	final CleaningEntry result = cleaningData.createAndAddCleaning(id, cleaningName, date,
		getOrCreateRoom(roomName, date));
	notifyCleaningsChanged();
	return result;
    }

    public BooleanProperty bookingsChangedProperty() {

	return bookingsChanged;
    }

    @Override
    public boolean cleaningNeededFor(final String roomName, final LocalDate date) {

	// check, if there is a booking at all
	final Optional<BookingEntryPair> bookingEntryPairOptional = getBookingEntryPair(roomName, date);
	if (bookingEntryPairOptional.isPresent()) {
	    final BookingEntryPair bookingEntryPair = bookingEntryPairOptional.get();
	    if (!bookingEntryPair.hasCheckOut()) {
		// no checkout, no cleaning
		return false;
	    }
	}
	// check for cleaning entry that room that day
	final Optional<CleaningEntry> cleaningEntryOptional = getCleaningEntry(roomName, date);
	if (cleaningEntryOptional.isPresent()) {
	    return false;
	}
	// if there is no such cleaning, check time period until next booking
	return true;
    }

    @Override
    public BooleanProperty cleaningsChangedProperty() {

	return cleaningsChanged;
    }

    public void clear() {

	bookingEntries.clear();
	cleaningData.cleaningEntries.clear();
	roomEntries.clear();
    }

    @Override
    public Optional<BookingEntryPair> getAfter(final BookingEntry e, final int numDays) {

	final LocalDate plusX = e.getDate().plusDays(numDays);
	return getBookingEntryPair(e.getRoom().getName(), plusX);
    }

    @Override
    public Optional<BookingEntryPair> getBefore(final BookingEntry e, final int numDays) {

	final LocalDate plusOneDay = e.getDate().plusDays(1);
	return getBookingEntryPair(e.getRoom().getName(), plusOneDay);
    }

    @Override
    public synchronized List<BookingEntry> getBookingEntries() {

	return Collections.unmodifiableList(
		getBookingEntryPairs().stream().flatMap(e -> e.toList().stream()).collect(Collectors.toList()));
    }

    @Override
    public synchronized Optional<BookingEntryPair> getBookingEntryPair(final String roomName, final LocalDate date) {

	return Optional.ofNullable(bookingEntries.get(getBookingEntryMultiKey(roomName, date)));
    }

    @Override
    public synchronized List<BookingEntryPair> getBookingEntryPairs() {

	return Collections.unmodifiableList(new ArrayList<>(bookingEntries.values()));
    }

    public List<BookingEntryPair> getBookingEntryPairs(final LocalDate date) {

	final List<BookingEntryPair> result = new ArrayList<>();
	final Set<String> roomNames = getRoomNames();
	for (final String roomName : roomNames) {
	    final BookingEntryPair ce = bookingEntries.get(getBookingEntryMultiKey(roomName, date));
	    if (ce != null) {
		result.add(ce);
	    }
	}
	return result;
    }

    @Override
    public synchronized List<BookingBean> getBookings() {

	return Collections.unmodifiableList(new ArrayList<>(getBookingEntryPairs().stream()
		.flatMap(e -> e.toList().stream()).map(be -> be.getElement()).collect(Collectors.toSet())));
    }

    @Override
    public synchronized List<CleaningEntry> getCleaningEntries() {

	return Collections.unmodifiableList(new ArrayList<>(cleaningData.cleaningEntries.values()));
    }

    public List<CleaningEntry> getCleaningEntries(final LocalDate date) {

	final List<CleaningEntry> result = new ArrayList<>();
	final Set<String> roomNames = getRoomNames();
	for (final String roomName : roomNames) {
	    final CleaningEntry ce = cleaningData.cleaningEntries
		    .get(cleaningData.getCleaningEntryMultiKey(roomName, date));
	    if (ce != null) {
		result.add(ce);
	    }
	}
	return result;
    }

    @Override
    public Optional<CleaningEntry> getCleaningEntry(final String roomName, final LocalDate date) {

	return Optional
		.ofNullable(cleaningData.cleaningEntries.get(cleaningData.getCleaningEntryMultiKey(roomName, date)));
    }

    public Optional<LocalDate> getFirstBookingDate() {

	final List<BookingEntry> bes = new ArrayList<>(getBookingEntries());
	if (bes.isEmpty()) {
	    return Optional.empty();
	}
	Collections.sort(bes);
	return Optional.of(bes.get(0).getDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BookingEntry> getFirstBookingEntry(final Optional<BookingEntryPair> bookingsThatDay) {

	if (bookingsThatDay.isPresent()) {
	    return Optional.of(bookingsThatDay.get().getFirst());
	}
	return Optional.empty();
    }

    @Override
    public Optional<BookingEntry> getFirstBookingEntry(final String roomName, final LocalDate date) {

	return getFirstBookingEntry(getBookingEntryPair(roomName, date));
    }

    public Optional<LocalDate> getLastBookingDate() {

	final List<BookingEntry> bes = new ArrayList<>(getBookingEntries());
	if (bes.isEmpty()) {
	    return Optional.empty();
	}
	Collections.sort(bes, Comparator.reverseOrder());
	return Optional.of(bes.get(0).getDate());
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Optional<BookingEntry> getLastBookingEntry(final Optional<BookingEntryPair> bookingsThatDay) {

	if (bookingsThatDay.isPresent()) {
	    return Optional.of(bookingsThatDay.get().getLast());
	}
	return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BookingEntry> getLastBookingEntry(final String roomName, final LocalDate date) {

	return getLastBookingEntry(getBookingEntryPair(roomName, date));
    }

    /**
     * Returns an {@link Optional} holding a {@link BookingEntry} that has the same
     * {@link Room} and is any days later than the given {@link BookingEntry}. If
     * there is more than one booking (e.g. check-in and check-out), the later one
     * is returned (check-out).
     *
     * @param e
     *            BookingEntry
     * @return
     */
    public Optional<BookingEntry> getNext(final BookingEntry e) {

	if (isEmtpy()) {
	    return Optional.empty();
	}
	LocalDate date = e.getDate();
	final LocalDate last = getLastBookingDate().get();
	Optional<BookingEntryPair> bookingsThatDay = null;
	while ((date.isBefore(last) || date.equals(last))
		&& (bookingsThatDay == null || !bookingsThatDay.isPresent())) {
	    date = date.plusDays(1);
	    bookingsThatDay = getBookingEntryPair(e.getRoom().getName(), date);
	}
	if (bookingsThatDay.isPresent()) {
	    return Optional.of(bookingsThatDay.get().getFirst());
	}
	return Optional.empty();
    }

    /**
     * Returns an {@link Optional} holding a {@link BookingEntry} that has the same
     * {@link Room} and is one day plus to the given {@link BookingEntry}.
     *
     * @param e
     *            BookingEntry
     * @return
     */
    @Override
    public Optional<BookingEntryPair> getOneDayAfter(final BookingEntry e) {

	return getAfter(e, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BookingEntryPair> getOneDayBefore(final BookingEntry e) {

	final LocalDate minusOneDay = e.getDate().minusDays(1);
	return getBookingEntryPair(e.getRoom().getName(), minusOneDay);
    }

    public List<RoomEntry> getRoomEntries(final LocalDate date) {

	final List<RoomEntry> result = new ArrayList<>();
	final Set<String> roomNames = getRoomNames();
	for (final String roomName : roomNames) {
	    final RoomEntry re = roomEntries.get(getRoomEntryMultiKey(roomName, date));
	    if (re != null) {
		result.add(re);
	    }
	}
	return result;
    }

    public synchronized Optional<RoomEntry> getRoomEntry(final String roomName, final LocalDate date) {

	return Optional.ofNullable(roomEntries.get(getRoomEntryMultiKey(roomName, date)));
    }

    protected Set<String> getRoomNames() {

	return roomEntries.keySet().stream().map(k -> (String) k.getKey(0)).collect(Collectors.toSet());
    }

    public boolean isEmtpy() {

	return bookingEntries.isEmpty() && cleaningData.cleaningEntries.isEmpty();
    }

    private void notifyBookingsChanged() {

	// simply toggle the value
	bookingsChanged.set(!bookingsChanged.get());
    }

    private void notifyCleaningsChanged() {

	// simply toggle the value
	cleaningsChanged.set(!cleaningsChanged.get());
    }

    public void removeBooking(final BookingBean booking) {

	final List<BookingEntry> bes = Bookings.toEntries(booking);
	for (final BookingEntry be : bes) {
	    bookingEntries.remove(getBookingEntryMultiKey(be.getRoom().getName(), be.getDate()));
	}
    }

    public void removeBookings(final Collection<? extends BookingBean> bookings) {

	bookings.forEach(e -> removeBooking(e));
    }

    public void removeCleaning(final CleaningEntry cleaningEntry) {

	cleaningData.cleaningEntries.remove(
		cleaningData.getCleaningEntryMultiKey(cleaningEntry.getRoom().getName(), cleaningEntry.getDate()));
    }

    @Override
    public void removeCleaning(final LocalDate date, final String roomName) {
	cleaningData.cleaningEntries.remove(cleaningData.getCleaningEntryMultiKey(roomName, date));

    }

    @Override
    public void setCleaning(final String name, final LocalDate date, final String roomName) {
	final CleaningEntry newCe = cleaningData.createNewCleaningEntry(null, cleaningData.getOrCreateCleaning(name),
		date, getOrCreateRoom(roomName, date));
	final CleaningEntry ce = cleaningData.cleaningEntries.put(cleaningData.getCleaningEntryMultiKey(roomName, date),
		newCe);
	if (ce != null && logger.isInfoEnabled()) {
	    logger.info(ce + " replaced by " + newCe);
	}
    }
}
