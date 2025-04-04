package com.lavi.tablearrangments.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GuestTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Guest getGuestSample1() {
        return new Guest().id(1L).lastNameAndFirstName("lastNameAndFirstName1").numberOfSeats(1).phone("phone1");
    }

    public static Guest getGuestSample2() {
        return new Guest().id(2L).lastNameAndFirstName("lastNameAndFirstName2").numberOfSeats(2).phone("phone2");
    }

    public static Guest getGuestRandomSampleGenerator() {
        return new Guest()
            .id(longCount.incrementAndGet())
            .lastNameAndFirstName(UUID.randomUUID().toString())
            .numberOfSeats(intCount.incrementAndGet())
            .phone(UUID.randomUUID().toString());
    }
}
