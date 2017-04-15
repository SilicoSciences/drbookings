package com.github.drbookings.ui.controller;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.drbookings.model.data.Cleaning;
import com.github.drbookings.model.data.Room;

public class CleaningEntryTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEqualsHashCode01() {
	final CleaningEntry ce1 = new CleaningEntry(LocalDate.now(), new Room("testRoom"),
		new Cleaning("testCleaning"));
	final CleaningEntry ce2 = new CleaningEntry(LocalDate.now(), new Room("testRoom"),
		new Cleaning("testCleaning"));
	assertEquals(ce1.hashCode(), ce2.hashCode());
	assertEquals(ce1, ce2);
    }

}