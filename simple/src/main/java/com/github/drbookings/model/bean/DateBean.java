package com.github.drbookings.model.bean;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drbookings.LocalDateAdapter;
import com.github.drbookings.OverbookingException;
import com.github.drbookings.model.DataModel;
import com.github.drbookings.model.Rooms;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.util.Callback;

public class DateBean implements Iterable<RoomBean>, Comparable<DateBean> {

    private static final Logger logger = LoggerFactory.getLogger(DateBean.class);

    public static Callback<DateBean, Observable[]> extractor() {
	return param -> new Observable[] { param.selfProperty() };
    }

    private String id;

    private final ObjectProperty<DataModel> dataModel = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    /**
     * Internally updated.
     */
    private final FloatProperty auslastung = new SimpleFloatProperty();

    /**
     * Internally updated.
     */
    private final IntegerProperty roomCount = new SimpleIntegerProperty();

    private final ListProperty<RoomBean> rooms = new SimpleListProperty<>(
	    FXCollections.observableArrayList(RoomBean.extractor()));

    /**
     * Internally updated.
     */
    private final ObjectProperty<DateBean> self = new SimpleObjectProperty<>();

    /**
     * Internally updated.
     */
    private final ListProperty<BookingBean> bookings = new SimpleListProperty<>(
	    FXCollections.observableArrayList(BookingBean.extractor()));

    DateBean() {
	setId(UUID.randomUUID().toString());

	selfProperty().bind(Bindings.createObjectBinding(update(), bookingsProperty()));

	bindAuslastungProperty();
	bindRoomCountProperty();
	bindRoomsProperty();

	rooms.add(new RoomBean("1").setDateBean(this));
	rooms.add(new RoomBean("2").setDateBean(this));
	rooms.add(new RoomBean("3").setDateBean(this));
	rooms.add(new RoomBean("4").setDateBean(this));

    }

    public DateBean(final LocalDate date) {
	this();
	setDate(date);
    }

    public synchronized DateBean addRoom(final RoomBean room) throws OverbookingException {
	final RoomBean rb = getRoom(room.getName());
	if (rb != null) {
	    rb.merge(room);
	} else {
	    rooms.add(room);
	    room.setDateBean(this);
	}
	return this;
    }

    public FloatProperty auslastungProperty() {
	return this.auslastung;
    }

    private void bindAuslastungProperty() {
	auslastungProperty().bind(Bindings.createFloatBinding(() -> {
	    float cnt = 0;

	    // System.err.println("Rooms " + roomsProperty());
	    for (final RoomBean rb : rooms) {
		cnt += rb.bookingsProperty().filtered(b -> !b.isCheckOut()).size();
	    }
	    if (cnt == 0) {
		return cnt;
	    }
	    final float result2 = cnt / roomsProperty().size();
	    // System.err.println("Booking cnt " + cnt);
	    // System.err.println("Rooms cnt " + roomsProperty().size());
	    // System.err.println("Auslastung update to " + result2);
	    return result2;
	}, roomsProperty()));

    }

    private void bindRoomCountProperty() {
	roomCountProperty().bind(Bindings.createIntegerBinding(() -> roomsProperty().size(), roomsProperty()));

    }

    private void bindRoomsProperty() {
	roomsProperty().addListener((ListChangeListener<RoomBean>) c -> {
	    setBookings(Rooms.bookingsView(c.getList()));
	});

    }

    private ReadOnlyListProperty<BookingBean> bookingsProperty() {
	return this.bookings;
    }

    @Override
    public int compareTo(final DateBean o) {
	return getDate().compareTo(o.getDate());
    }

    public ObjectProperty<DataModel> dataModelProperty() {
	return this.dataModel;
    }

    public ObjectProperty<LocalDate> dateProperty() {
	return this.date;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final DateBean other = (DateBean) obj;
	if (getDate() == null) {
	    if (other.getDate() != null) {
		return false;
	    }
	} else if (!getDate().equals(other.getDate())) {
	    return false;
	}
	return true;
    }

    public float getAuslastung() {
	return this.auslastungProperty().get();
    }

    public List<BookingBean> getBookings() {
	return Collections.unmodifiableList(this.bookingsProperty().get());
    }

    @XmlElement(name = "data-model")
    @XmlIDREF
    public DataModel getDataModel() {
	return this.dataModelProperty().get();
    }

    @XmlElement(name = "date")
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    public LocalDate getDate() {
	return this.dateProperty().get();
    }

    @XmlID
    @XmlAttribute
    public String getId() {
	return id;
    }

    public RoomBean getRoom(final String name) {
	final List<RoomBean> rooms = roomsProperty().filtered(r -> r.getName().equals(name));
	if (rooms.size() > 1) {
	    if (logger.isErrorEnabled()) {
		logger.error("Too many rooms with id " + name + " (" + rooms + ")");
	    }
	}
	if (rooms.isEmpty()) {
	    return null;
	}
	return rooms.get(0);
    }

    public int getRoomCount() {
	return this.roomCountProperty().get();
    }

    @XmlElementWrapper(name = "rooms")
    @XmlElement(name = "room")
    public List<RoomBean> getRooms() {
	// System.err.println("GetRooms called, " + roomsProperty().get() + "
	// returned");
	return this.roomsProperty().get();
    }

    public DateBean getSelf() {
	return this.selfProperty().get();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (getDate() == null ? 0 : getDate().hashCode());
	return result;
    }

    @Override
    public Iterator<RoomBean> iterator() {
	return roomsProperty().iterator();
    }

    public void merge(final DateBean b) throws OverbookingException {
	for (final RoomBean room : b.getRooms()) {
	    final RoomBean room2 = getRoom(room.getName());
	    room2.merge(room);
	}
    }

    public IntegerProperty roomCountProperty() {
	return this.roomCount;
    }

    public ListProperty<RoomBean> roomsProperty() {
	return this.rooms;
    }

    public ObjectProperty<DateBean> selfProperty() {
	return this.self;
    }

    /**
     * Internally updated.
     */
    @SuppressWarnings("unused")
    private void setAuslastung(final float auslastung) {
	this.auslastungProperty().set(auslastung);
    }

    /**
     * Internally updated.
     */
    private void setBookings(final Collection<? extends BookingBean> bookings) {
	this.bookingsProperty().setAll(bookings);
    }

    /**
     * Set by {@link DataModel} itself.
     */
    public void setDataModel(final DataModel dataModel) {
	this.dataModelProperty().set(dataModel);
    }

    public DateBean setDate(final LocalDate date) {
	this.dateProperty().set(date);

	return this;
    }

    private void setId(final String id) {
	this.id = id;
    }

    /**
     * Internally updated.
     */
    @SuppressWarnings("unused")
    private void setRoomCount(final int roomCount) {
	this.roomCountProperty().set(roomCount);
    }

    public void setRooms(final Collection<? extends RoomBean> rooms) {
	// System.err.println("Settings rooms from " + this.rooms + " to " +
	// rooms);
	this.roomsProperty().setAll(rooms);
    }

    /**
     * Internally updated.
     */
    @SuppressWarnings("unused")
    private void setSelf(final DateBean self) {
	this.selfProperty().set(self);
    }

    @Override
    public String toString() {
	return "DateBean: Date:" + getDate() + ", Rooms:" + getRooms();
    }

    private Callable<DateBean> update() {
	return () -> DateBean.this;
    }

}
