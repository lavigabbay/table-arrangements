package com.lavi.tablearrangments.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GuestTableTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static GuestTable getGuestTableSample1() {
        return new GuestTable()
            .id(1L)
            .lastNameAndFirstName("lastNameAndFirstName1")
            .numberOfSeats(1)
            .phone("phone1")
            .side("side1")
            .notWithId(1L)
            .withId(1L)
            .conditions("conditions1");
    }

    public static GuestTable getGuestTableSample2() {
        return new GuestTable()
            .id(2L)
            .lastNameAndFirstName("lastNameAndFirstName2")
            .numberOfSeats(2)
            .phone("phone2")
            .side("side2")
            .notWithId(2L)
            .withId(2L)
            .conditions("conditions2");
    }

    public static GuestTable getGuestTableRandomSampleGenerator() {
        return new GuestTable()
            .id(longCount.incrementAndGet())
            .lastNameAndFirstName(UUID.randomUUID().toString())
            .numberOfSeats(intCount.incrementAndGet())
            .phone(UUID.randomUUID().toString())
            .side(UUID.randomUUID().toString())
            .notWithId(longCount.incrementAndGet())
            .withId(longCount.incrementAndGet())
            .conditions(UUID.randomUUID().toString());
    }
}
