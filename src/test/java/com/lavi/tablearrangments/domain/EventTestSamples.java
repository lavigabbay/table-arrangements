package com.lavi.tablearrangments.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EventTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Event getEventSample1() {
        return new Event()
            .id(1L)
            .eventName("eventName1")
            .eventOwners("eventOwners1")
            .groomParents("groomParents1")
            .brideParents("brideParents1");
    }

    public static Event getEventSample2() {
        return new Event()
            .id(2L)
            .eventName("eventName2")
            .eventOwners("eventOwners2")
            .groomParents("groomParents2")
            .brideParents("brideParents2");
    }

    public static Event getEventRandomSampleGenerator() {
        return new Event()
            .id(longCount.incrementAndGet())
            .eventName(UUID.randomUUID().toString())
            .eventOwners(UUID.randomUUID().toString())
            .groomParents(UUID.randomUUID().toString())
            .brideParents(UUID.randomUUID().toString());
    }
}
